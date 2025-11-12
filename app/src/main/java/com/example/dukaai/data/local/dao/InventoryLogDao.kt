package com.example.dukaai.data.local.dao

import androidx.room.*
import com.example.dukaai.data.local.entity.InventoryLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Inventory Log operations
 */
@Dao
interface InventoryLogDao {

    @Query("SELECT * FROM inventory_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<InventoryLogEntity>>

    @Query("SELECT * FROM inventory_logs WHERE productId = :productId ORDER BY timestamp DESC")
    fun getLogsByProduct(productId: String): Flow<List<InventoryLogEntity>>

    @Query("SELECT * FROM inventory_logs WHERE actionType = :actionType ORDER BY timestamp DESC")
    fun getLogsByActionType(actionType: String): Flow<List<InventoryLogEntity>>

    @Query("SELECT * FROM inventory_logs WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getLogsByDateRange(startTime: Long, endTime: Long): Flow<List<InventoryLogEntity>>

    @Query("SELECT * FROM inventory_logs WHERE productId = :productId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsByProduct(productId: String, limit: Int = 10): Flow<List<InventoryLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: InventoryLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<InventoryLogEntity>)

    @Delete
    suspend fun deleteLog(log: InventoryLogEntity)

    @Query("DELETE FROM inventory_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: String)

    @Query("DELETE FROM inventory_logs WHERE timestamp < :cutoffTime")
    suspend fun deleteOldLogs(cutoffTime: Long)
}
