package com.example.dukaai.data.repository

import com.example.dukaai.data.local.dao.InventoryLogDao
import com.example.dukaai.data.local.dao.ProductDao
import com.example.dukaai.data.local.entity.InventoryLogEntity
import com.example.dukaai.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Product data operations
 * Provides a clean API for data access to the rest of the app
 */
@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao,
    private val inventoryLogDao: InventoryLogDao
) {

    fun getAllProducts(): Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getProductById(productId: String): Flow<ProductEntity?> = productDao.getProductById(productId)

    fun getProductsByCategory(category: String): Flow<List<ProductEntity>> =
        productDao.getProductsByCategory(category)

    fun getLowStockProducts(): Flow<List<ProductEntity>> = productDao.getLowStockProducts()

    fun getOutOfStockProducts(): Flow<List<ProductEntity>> = productDao.getOutOfStockProducts()

    fun searchProducts(query: String): Flow<List<ProductEntity>> = productDao.searchProducts(query)

    fun getAllCategories(): Flow<List<String>> = productDao.getAllCategories()

    suspend fun getProductByBarcode(barcode: String): ProductEntity? =
        productDao.getProductByBarcode(barcode)

    suspend fun insertProduct(product: ProductEntity): Long {
        return productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ProductEntity) {
        productDao.updateProduct(product)
    }

    suspend fun updateStock(productId: String, newStock: Int, reason: String? = null) {
        val product = productDao.getProductById(productId)
        product.collect { p ->
            p?.let {
                // Log the inventory change
                val log = InventoryLogEntity(
                    productId = productId,
                    actionType = "ADJUST",
                    quantityChange = newStock - it.currentStock,
                    previousStock = it.currentStock,
                    newStock = newStock,
                    reason = reason
                )
                inventoryLogDao.insertLog(log)

                // Update the stock
                productDao.updateStock(productId, newStock)
            }
        }
    }

    suspend fun deleteProduct(product: ProductEntity) {
        productDao.deleteProduct(product)
    }

    suspend fun deleteProductById(productId: String) {
        productDao.deleteProductById(productId)
    }
}
