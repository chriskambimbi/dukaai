package com.example.dukaai.data.repository

import com.example.dukaai.data.local.dao.CreditLedgerDao
import com.example.dukaai.data.local.dao.PaymentDao
import com.example.dukaai.data.local.entity.CreditLedgerEntity
import com.example.dukaai.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing credit ledger
 * Handles credit transactions and payment tracking
 */
@Singleton
class CreditRepository @Inject constructor(
    private val creditLedgerDao: CreditLedgerDao,
    private val paymentDao: PaymentDao
) {

    /**
     * Get all credit entries
     */
    fun getAllCredits(): Flow<List<CreditLedgerEntity>> = creditLedgerDao.getAllCreditRecords()

    /**
     * Get credit by ID
     */
    fun getCreditById(creditId: String): Flow<CreditLedgerEntity?> =
        creditLedgerDao.getCreditById(creditId)

    /**
     * Get credits by customer
     */
    fun getCreditsByCustomer(customerId: String): Flow<List<CreditLedgerEntity>> =
        creditLedgerDao.getCreditsByCustomer(customerId)

    /**
     * Get unpaid credits
     */
    fun getUnpaidCredits(): Flow<List<CreditLedgerEntity>> =
        creditLedgerDao.getCreditsByStatus("UNPAID")

    /**
     * Get overdue credits
     */
    fun getOverdueCredits(): Flow<List<CreditLedgerEntity>> {
        val now = System.currentTimeMillis()
        return creditLedgerDao.getOverdueCredits(now)
    }

    /**
     * Get total outstanding credit
     */
    fun getTotalOutstandingCredit(): Flow<Double?> =
        creditLedgerDao.getTotalOutstandingDebt()

    /**
     * Get customer's total credit
     */
    fun getCustomerTotalCredit(customerId: String): Flow<Double?> =
        creditLedgerDao.getTotalDebtByCustomer(customerId)

    /**
     * Record new credit
     */
    suspend fun recordCredit(credit: CreditLedgerEntity): Result<Long> {
        return try {
            val creditId = creditLedgerDao.insertCredit(credit)
            Result.success(creditId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update credit entry
     */
    suspend fun updateCredit(credit: CreditLedgerEntity): Result<Unit> {
        return try {
            creditLedgerDao.updateCredit(credit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Record payment for credit
     */
    suspend fun recordPayment(
        creditId: String,
        payment: PaymentEntity
    ): Result<Unit> {
        return try {
            // Get the credit
            val credit = creditLedgerDao.getCreditById(creditId).first()
                ?: return Result.failure(Exception("Credit not found"))

            // Calculate new remaining amount
            val newRemainingAmount = credit.amountRemaining - payment.amount

            // Update credit
            val updatedCredit = credit.copy(
                amountPaid = credit.amountPaid + payment.amount,
                amountRemaining = newRemainingAmount,
                status = if (newRemainingAmount <= 0.0) "PAID" else "PARTIAL"
            )
            creditLedgerDao.updateCredit(updatedCredit)

            // Insert payment
            paymentDao.insertPayment(payment)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Mark credit as fully paid
     */
    suspend fun markAsPaid(creditId: String): Result<Unit> {
        return try {
            val credit = creditLedgerDao.getCreditById(creditId).first()
                ?: return Result.failure(Exception("Credit not found"))

            val updatedCredit = credit.copy(
                amountPaid = credit.amount,
                amountRemaining = 0.0,
                status = "PAID"
            )
            creditLedgerDao.updateCredit(updatedCredit)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete credit entry
     */
    suspend fun deleteCredit(credit: CreditLedgerEntity): Result<Unit> {
        return try {
            creditLedgerDao.deleteCredit(credit)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get credit statistics for a customer
     */
    suspend fun getCustomerCreditStats(customerId: String): CreditStats {
        val credits = creditLedgerDao.getCreditsByCustomer(customerId).first()
        val totalCredit = creditLedgerDao.getTotalDebtByCustomer(customerId).first() ?: 0.0
        val unpaidCredits = credits.filter { it.status == "UNPAID" || it.status == "PENDING" }
        val overdueCredits = credits.filter {
            it.status != "PAID" && it.dueDate != null && it.dueDate < System.currentTimeMillis()
        }

        return CreditStats(
            totalCredits = credits.size,
            totalAmount = credits.sumOf { it.amount },
            totalPaid = credits.sumOf { it.amountPaid },
            totalRemaining = totalCredit,
            unpaidCount = unpaidCredits.size,
            overdueCount = overdueCredits.size
        )
    }

    /**
     * Get overall credit statistics
     */
    suspend fun getOverallCreditStats(): CreditStats {
        val allCredits = creditLedgerDao.getAllCreditRecords().first()
        val totalOutstanding = creditLedgerDao.getTotalOutstandingDebt().first() ?: 0.0
        val unpaidCredits = allCredits.filter { it.status == "UNPAID" || it.status == "PENDING" }
        val overdueCredits = allCredits.filter {
            it.status != "PAID" && it.dueDate != null && it.dueDate < System.currentTimeMillis()
        }

        return CreditStats(
            totalCredits = allCredits.size,
            totalAmount = allCredits.sumOf { it.amount },
            totalPaid = allCredits.sumOf { it.amountPaid },
            totalRemaining = totalOutstanding,
            unpaidCount = unpaidCredits.size,
            overdueCount = overdueCredits.size
        )
    }

    /**
     * Get credits due within specified days
     */
    suspend fun getCreditsDueSoon(daysAhead: Int = 7): List<CreditLedgerEntity> {
        val now = System.currentTimeMillis()
        val futureDate = now + (daysAhead * 24 * 60 * 60 * 1000L)
        val allCredits = creditLedgerDao.getUnpaidCredits().first()
        return allCredits.filter { credit ->
            credit.dueDate != null && credit.dueDate in now..futureDate
        }
    }
}

/**
 * Credit statistics data class
 */
data class CreditStats(
    val totalCredits: Int,
    val totalAmount: Double,
    val totalPaid: Double,
    val totalRemaining: Double,
    val unpaidCount: Int,
    val overdueCount: Int
)
