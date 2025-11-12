package com.example.dukaai.data.local.dao

import androidx.room.*
import com.example.dukaai.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Sale operations
 */
@Dao
interface SaleDao {

    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE id = :saleId")
    fun getSaleById(saleId: String): Flow<SaleEntity?>

    @Query("SELECT * FROM sales WHERE productId = :productId ORDER BY timestamp DESC")
    fun getSalesByProduct(productId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getSalesByCustomer(customerId: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE saleType = :saleType ORDER BY timestamp DESC")
    fun getSalesByType(saleType: String): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getSalesByDateRange(startTime: Long, endTime: Long): Flow<List<SaleEntity>>

    @Query("SELECT SUM(totalAmount) FROM sales WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getTotalRevenue(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(quantity) FROM sales WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getTotalItemsSold(startTime: Long, endTime: Long): Flow<Int?>

    @Query("SELECT COUNT(*) FROM sales WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getSalesCount(startTime: Long, endTime: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: SaleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<SaleEntity>)

    @Update
    suspend fun updateSale(sale: SaleEntity)

    @Delete
    suspend fun deleteSale(sale: SaleEntity)

    @Query("DELETE FROM sales WHERE id = :saleId")
    suspend fun deleteSaleById(saleId: String)
}
