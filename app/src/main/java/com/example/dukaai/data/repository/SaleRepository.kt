package com.example.dukaai.data.repository

import com.example.dukaai.data.local.dao.InventoryLogDao
import com.example.dukaai.data.local.dao.ProductDao
import com.example.dukaai.data.local.dao.SaleDao
import com.example.dukaai.data.local.entity.InventoryLogEntity
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing sales transactions
 * Handles business logic for recording sales and updating inventory
 */
@Singleton
class SaleRepository @Inject constructor(
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
        val startOfDay = getStartOfDay()
        val endOfDay = getEndOfDay()
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
        val startOfDay = getStartOfDay()
        val endOfDay = getEndOfDay()
        return saleDao.getTotalRevenue(startOfDay, endOfDay)
    }

    /**
     * Record a new sale
     * Updates product stock and creates inventory log
     */
    suspend fun recordSale(sale: SaleEntity): Result<Long> {
        return try {
            // Get current product
            val product = productDao.getProductById(sale.productId).first()
                ?: return Result.failure(Exception("Product not found"))

            // Check if enough stock available
            if (product.currentStock < sale.quantity) {
                return Result.failure(Exception("Insufficient stock. Available: ${product.currentStock}, Required: ${sale.quantity}"))
            }

            // Calculate new stock
            val newStock = product.currentStock - sale.quantity

            // Insert sale
            val saleId = saleDao.insertSale(sale)

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

            Result.success(saleId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Record multiple sales in a transaction
     */
    suspend fun recordBulkSales(sales: List<SaleEntity>): Result<List<Long>> {
        return try {
            val saleIds = mutableListOf<Long>()

            for (sale in sales) {
                val result = recordSale(sale)
                if (result.isFailure) {
                    return Result.failure(result.exceptionOrNull() ?: Exception("Failed to record sale"))
                }
                saleIds.add(result.getOrThrow())
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
     * Delete a sale
     * Note: This should restore stock and create a log entry
     */
    suspend fun deleteSale(saleId: String): Result<Unit> {
        return try {
            val sale = saleDao.getSaleById(saleId).first()
                ?: return Result.failure(Exception("Sale not found"))

            val product = productDao.getProductById(sale.productId).first()
                ?: return Result.failure(Exception("Product not found"))

            // Restore stock
            val newStock = product.currentStock + sale.quantity
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

    private fun getStartOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfDay(): Long {
        val calendar = java.util.Calendar.getInstance()
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
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
