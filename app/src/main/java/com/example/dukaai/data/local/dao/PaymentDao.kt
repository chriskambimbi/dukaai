package com.example.dukaai.data.local.dao

import androidx.room.*
import com.example.dukaai.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Payment operations
 */
@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments ORDER BY timestamp DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE id = :paymentId")
    fun getPaymentById(paymentId: String): Flow<PaymentEntity?>

    @Query("SELECT * FROM payments WHERE creditId = :creditId ORDER BY timestamp DESC")
    fun getPaymentsByCredit(creditId: String): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getPaymentsByDateRange(startTime: Long, endTime: Long): Flow<List<PaymentEntity>>

    @Query("SELECT SUM(amount) FROM payments WHERE creditId = :creditId")
    fun getTotalPaidForCredit(creditId: String): Flow<Double?>

    @Query("""
        SELECT p.* FROM payments p
        INNER JOIN credit_ledger c ON p.creditId = c.id
        WHERE c.customerId = :customerId
        ORDER BY p.timestamp DESC
    """)
    suspend fun getPaymentsByCustomer(customerId: String): List<PaymentEntity>

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0.0) FROM payments p
        INNER JOIN credit_ledger c ON p.creditId = c.id
        WHERE c.customerId = :customerId
    """)
    suspend fun getTotalPaymentsByCustomer(customerId: String): Double

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<PaymentEntity>)

    @Update
    suspend fun updatePayment(payment: PaymentEntity)

    @Delete
    suspend fun deletePayment(payment: PaymentEntity)

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun deletePaymentById(paymentId: String)
}
