package com.example.dukaai.data.repository

import androidx.room.withTransaction
import com.example.dukaai.data.local.DukaDatabase
import com.example.dukaai.data.local.dao.InventoryLogDao
import com.example.dukaai.data.local.dao.ProductDao
import com.example.dukaai.data.local.dao.SaleDao
import com.example.dukaai.data.local.entity.InventoryLogEntity
import com.example.dukaai.data.local.entity.SaleEntity
import com.example.dukaai.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing sales transactions
 * Handles business logic for recording sales and updating inventory
 *
 * Uses Room transactions to ensure atomicity of multi-table operations.
 */
@Singleton
class SaleRepository @Inject constructor(
    private val database: DukaDatabase,
    private val saleDao: SaleDao,
    private val productDao: ProductDao,
    private val inventoryLogDao: InventoryLogDao
) {

    /**
     * Get all sales
     */
    fun getAllSales(): Flow<List<SaleEntity>> = saleDao.getAllSales()

    /**
     * Get sales for a specific date
     */
    fun getSalesByDate(startDate: Long, endDate: Long): Flow<List<SaleEntity>> =
        saleDao.getSalesByDateRange(startDate, endDate)

    /**
     * Get today's sales
     */
    fun getTodaySales(): Flow<List<SaleEntity>> {
        val startOfDay = DateUtils.getStartOfDay()
        val endOfDay = DateUtils.getEndOfDay()
        return saleDao.getSalesByDateRange(startOfDay, endOfDay)
    }

    /**
     * Get sales by customer
     */
    fun getSalesByCustomer(customerId: String): Flow<List<SaleEntity>> =
        saleDao.getSalesByCustomer(customerId)

    /**
     * Get total revenue (all time)
     */
    suspend fun getTotalRevenue(): Double {
        val allSales = saleDao.getAllSales().first()
        return allSales.sumOf { it.totalAmount }
    }

    /**
     * Get today's revenue
     */
    fun getTodayRevenue(): Flow<Double?> {
        val startOfDay = DateUtils.getStartOfDay()
        val endOfDay = DateUtils.getEndOfDay()
        return saleDao.getTotalRevenue(startOfDay, endOfDay)
    }

    /**
     * Record a new sale
     * Updates product stock and creates inventory log
     *
     * Uses Room transaction to ensure atomicity - if any operation fails,
     * all changes are rolled back to prevent data inconsistency.
     */
    suspend fun recordSale(sale: SaleEntity): Result<Long> {
        return try {
            // Get current product (outside transaction for validation)
            val product = productDao.getProductById(sale.productId).first()
                ?: return Result.failure(Exception("Product not found"))

            // Check if enough stock available
            if (product.currentStock < sale.quantity) {
                return Result.failure(Exception("Insufficient stock. Available: ${product.currentStock}, Required: ${sale.quantity}"))
            }

            // Calculate new stock
            val newStock = product.currentStock - sale.quantity

            // Execute all database writes in a single atomic transaction
            val saleId = database.withTransaction {
                // Insert sale
                val id = saleDao.insertSale(sale)

                // Update product stock
                productDao.updateStock(sale.productId, newStock)

                // Create inventory log
                val log = InventoryLogEntity(
                    productId = sale.productId,
                    actionType = "SALE",
                    quantityChange = -sale.quantity,
                    previousStock = product.currentStock,
                    newStock = newStock,
                    reason = "Sale #${sale.id}"
                )
                inventoryLogDao.insertLog(log)

                id // Return the sale ID
            }

            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Record multiple sales in a single atomic transaction
     *
     * If any sale fails validation or insertion, the entire batch is rolled back.
     */
    suspend fun recordBulkSales(sales: List<SaleEntity>): Result<List<Long>> {
        if (sales.isEmpty()) return Result.success(emptyList())

        return try {
            // Pre-validate all sales before starting transaction
            val validatedSales = sales.map { sale ->
                val product = productDao.getProductById(sale.productId).first()
                    ?: throw IllegalArgumentException("Product not found: ${sale.productId}")

                if (product.currentStock < sale.quantity) {
                    throw IllegalArgumentException(
                        "Insufficient stock for ${product.name}. Available: ${product.currentStock}, Required: ${sale.quantity}"
                    )
                }

                Triple(sale, product, product.currentStock - sale.quantity)
            }

            // Execute all operations in a single atomic transaction
            val saleIds = database.withTransaction {
                validatedSales.map { (sale, product, newStock) ->
                    val saleId = saleDao.insertSale(sale)
                    productDao.updateStock(sale.productId, newStock)

                    val log = InventoryLogEntity(
                        productId = sale.productId,
                        actionType = "SALE",
                        quantityChange = -sale.quantity,
                        previousStock = product.currentStock,
                        newStock = newStock,
                        reason = "Sale #${sale.id}"
                    )
                    inventoryLogDao.insertLog(log)

                    saleId
                }
            }

            Result.success(saleIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get sale by ID
     */
    fun getSaleById(saleId: String): Flow<SaleEntity?> = saleDao.getSaleById(saleId)

    /**
     * Delete a sale and restore inventory
     *
     * Uses atomic transaction to ensure stock is restored and sale is deleted together.
     * If any operation fails, all changes are rolled back.
     */
    suspend fun deleteSale(saleId: String): Result<Unit> {
        return try {
            val sale = saleDao.getSaleById(saleId).first()
                ?: return Result.failure(Exception("Sale not found"))

            val product = productDao.getProductById(sale.productId).first()
                ?: return Result.failure(Exception("Product not found"))

            // Calculate restored stock
            val newStock = product.currentStock + sale.quantity

            // Execute all operations in a single atomic transaction
            database.withTransaction {
                // Restore stock
                productDao.updateStock(sale.productId, newStock)

                // Create inventory log
                val log = InventoryLogEntity(
                    productId = sale.productId,
                    actionType = "SALE_REVERSAL",
                    quantityChange = sale.quantity,
                    previousStock = product.currentStock,
                    newStock = newStock,
                    reason = "Sale #${sale.id} reversed"
                )
                inventoryLogDao.insertLog(log)

                // Delete sale
                saleDao.deleteSale(sale)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get sales statistics for date range
     */
    suspend fun getSalesStats(startDate: Long, endDate: Long): SalesStats {
        val sales = saleDao.getSalesByDateRange(startDate, endDate).first()
        val revenue = saleDao.getTotalRevenue(startDate, endDate).first() ?: 0.0
        val count = saleDao.getSalesCount(startDate, endDate).first()

        return SalesStats(
            totalSales = count,
            totalRevenue = revenue,
            averageSaleValue = if (count > 0) revenue / count else 0.0,
            totalItemsSold = sales.sumOf { it.quantity }
        )
    }
}

/**
 * Sales statistics data class
 */
data class SalesStats(
    val totalSales: Int,
    val totalRevenue: Double,
    val averageSaleValue: Double,
    val totalItemsSold: Int
)
