package com.example.dukaai.data.local.dao

import androidx.room.*
import com.example.dukaai.data.local.entity.CreditLedgerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Credit Ledger operations
 */
@Dao
interface CreditLedgerDao {

    @Query("SELECT * FROM credit_ledger ORDER BY createdAt DESC")
    fun getAllCreditRecords(): Flow<List<CreditLedgerEntity>>

    @Query("SELECT * FROM credit_ledger WHERE id = :creditId")
    fun getCreditById(creditId: String): Flow<CreditLedgerEntity?>

    @Query("SELECT * FROM credit_ledger WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getCreditsByCustomer(customerId: String): Flow<List<CreditLedgerEntity>>

    @Query("SELECT * FROM credit_ledger WHERE status = :status ORDER BY createdAt DESC")
    fun getCreditsByStatus(status: String): Flow<List<CreditLedgerEntity>>

    @Query("SELECT * FROM credit_ledger WHERE status != 'PAID' ORDER BY createdAt DESC")
    fun getUnpaidCredits(): Flow<List<CreditLedgerEntity>>

    @Query("SELECT * FROM credit_ledger WHERE dueDate < :currentTime AND status != 'PAID' ORDER BY dueDate ASC")
    fun getOverdueCredits(currentTime: Long = System.currentTimeMillis()): Flow<List<CreditLedgerEntity>>

    @Query("SELECT SUM(amountRemaining) FROM credit_ledger WHERE customerId = :customerId AND status != 'PAID'")
    fun getTotalDebtByCustomer(customerId: String): Flow<Double?>

    @Query("SELECT SUM(amountRemaining) FROM credit_ledger WHERE status != 'PAID'")
    fun getTotalOutstandingDebt(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredit(credit: CreditLedgerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredits(credits: List<CreditLedgerEntity>)

    @Update
    suspend fun updateCredit(credit: CreditLedgerEntity)

    @Query("UPDATE credit_ledger SET amountPaid = :amountPaid, amountRemaining = :amountRemaining, status = :status, updatedAt = :timestamp WHERE id = :creditId")
    suspend fun updateCreditPayment(
        creditId: String,
        amountPaid: Double,
        amountRemaining: Double,
        status: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Delete
    suspend fun deleteCredit(credit: CreditLedgerEntity)

    @Query("DELETE FROM credit_ledger WHERE id = :creditId")
    suspend fun deleteCreditById(creditId: String)
}
