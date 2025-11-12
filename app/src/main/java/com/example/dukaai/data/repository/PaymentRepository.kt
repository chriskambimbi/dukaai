package com.example.dukaai.data.repository

import com.example.dukaai.data.local.dao.PaymentDao
import com.example.dukaai.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing payments
 * Handles payment records and history
 */
@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao
) {

    /**
     * Get all payments
     */
    fun getAllPayments(): Flow<List<PaymentEntity>> = paymentDao.getAllPayments()

    /**
     * Get payment by ID
     */
    fun getPaymentById(paymentId: String): Flow<PaymentEntity?> =
        paymentDao.getPaymentById(paymentId)

    /**
     * Get payments by customer (through credits)
     * Uses JOIN query to get payments for all credits belonging to a customer
     */
    suspend fun getPaymentsByCustomer(customerId: String): List<PaymentEntity> {
        return paymentDao.getPaymentsByCustomer(customerId)
    }

    /**
     * Get payments for a specific credit
     */
    fun getPaymentsForCredit(creditId: String): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByCredit(creditId)

    /**
     * Get payments by date range
     */
    fun getPaymentsByDateRange(startDate: Long, endDate: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByDateRange(startDate, endDate)

    /**
     * Get today's payments
     */
    fun getTodayPayments(): Flow<List<PaymentEntity>> {
        val startOfDay = getStartOfDay()
        val endOfDay = getEndOfDay()
        return paymentDao.getPaymentsByDateRange(startOfDay, endOfDay)
    }

    /**
     * Get total payments received
     */
    suspend fun getTotalPayments(): Double {
        val allPayments = paymentDao.getAllPayments().first()
        return allPayments.sumOf { it.amount }
    }

    /**
     * Get customer's total payments
     * Uses JOIN query to sum all payments for credits belonging to a customer
     */
    suspend fun getCustomerTotalPayments(customerId: String): Double {
        return paymentDao.getTotalPaymentsByCustomer(customerId)
    }

    /**
     * Record a new payment
     */
    suspend fun recordPayment(payment: PaymentEntity): Result<Long> {
        return try {
            val paymentId = paymentDao.insertPayment(payment)
            Result.success(paymentId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update payment
     */
    suspend fun updatePayment(payment: PaymentEntity): Result<Unit> {
        return try {
            paymentDao.updatePayment(payment)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete payment
     */
    suspend fun deletePayment(payment: PaymentEntity): Result<Unit> {
        return try {
            paymentDao.deletePayment(payment)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get payment statistics for a customer
     */
    suspend fun getCustomerPaymentStats(customerId: String): PaymentStats {
        val payments = getPaymentsByCustomer(customerId)
        val totalAmount = getCustomerTotalPayments(customerId)

        return PaymentStats(
            totalPayments = payments.size,
            totalAmount = totalAmount,
            averagePayment = if (payments.isNotEmpty()) totalAmount / payments.size else 0.0,
            lastPaymentDate = payments.maxByOrNull { it.timestamp }?.timestamp
        )
    }

    /**
     * Get payment statistics for date range
     */
    suspend fun getPaymentStats(startDate: Long, endDate: Long): PaymentStats {
        val payments = paymentDao.getPaymentsByDateRange(startDate, endDate).first()
        val totalAmount = payments.sumOf { it.amount }

        return PaymentStats(
            totalPayments = payments.size,
            totalAmount = totalAmount,
            averagePayment = if (payments.isNotEmpty()) totalAmount / payments.size else 0.0,
            lastPaymentDate = payments.maxByOrNull { it.timestamp }?.timestamp
        )
    }

    /**
     * Get payments by payment method
     */
    suspend fun getPaymentsByMethod(method: String): List<PaymentEntity> {
        val allPayments = paymentDao.getAllPayments().first()
        return allPayments.filter { it.paymentMethod == method }
    }

    /**
     * Get payment method statistics
     */
    suspend fun getPaymentMethodStats(): Map<String, PaymentMethodStats> {
        val allPayments = paymentDao.getAllPayments().first()

        return allPayments.groupBy { it.paymentMethod }
            .mapValues { (_, payments) ->
                PaymentMethodStats(
                    count = payments.size,
                    totalAmount = payments.sumOf { it.amount },
                    percentage = (payments.size.toDouble() / allPayments.size) * 100
                )
            }
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
 * Payment statistics data class
 */
data class PaymentStats(
    val totalPayments: Int,
    val totalAmount: Double,
    val averagePayment: Double,
    val lastPaymentDate: Long?
)

/**
 * Payment method statistics
 */
data class PaymentMethodStats(
    val count: Int,
    val totalAmount: Double,
    val percentage: Double
)
