package com.example.dukaai.data.repository

import com.example.dukaai.data.local.dao.CustomerDao
import com.example.dukaai.data.local.dao.CreditLedgerDao
import com.example.dukaai.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing customers
 * Handles customer data and credit management queries
 */
@Singleton
class CustomerRepository @Inject constructor(
    private val customerDao: CustomerDao,
    private val creditLedgerDao: CreditLedgerDao
) {

    /**
     * Get all customers
     */
    fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    /**
     * Get customer by ID
     */
    fun getCustomerById(customerId: String): Flow<CustomerEntity?> =
        customerDao.getCustomerById(customerId)

    /**
     * Search customers by name or phone
     */
    fun searchCustomers(query: String): Flow<List<CustomerEntity>> =
        customerDao.searchCustomers("%$query%")

    /**
     * Get customers with outstanding credit
     */
    suspend fun getCustomersWithCredit(): List<CustomerEntity> {
        // Get all customers and filter those with credit
        val allCustomers = customerDao.getAllCustomers().first()
        val customersWithCredit = mutableListOf<CustomerEntity>()

        allCustomers.forEach { customer ->
            val totalDebt = creditLedgerDao.getTotalDebtByCustomer(customer.id).first()
            if (totalDebt != null && totalDebt > 0.0) {
                customersWithCredit.add(customer)
            }
        }

        return customersWithCredit
    }

    /**
     * Get customer by phone number
     */
    suspend fun getCustomerByPhone(phoneNumber: String): CustomerEntity? =
        customerDao.getCustomerByPhone(phoneNumber)

    /**
     * Add new customer
     */
    suspend fun addCustomer(customer: CustomerEntity): Result<Long> {
        return try {
            // Check if customer with same phone already exists
            customer.phoneNumber?.let { phone ->
                val existingCustomer = customerDao.getCustomerByPhone(phone)
                if (existingCustomer != null) {
                    return Result.failure(Exception("Customer with phone $phone already exists"))
                }
            }

            val customerId = customerDao.insertCustomer(customer)
            Result.success(customerId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update customer
     */
    suspend fun updateCustomer(customer: CustomerEntity): Result<Unit> {
        return try {
            customerDao.updateCustomer(customer)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete customer
     */
    suspend fun deleteCustomer(customer: CustomerEntity): Result<Unit> {
        return try {
            customerDao.deleteCustomer(customer)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get total number of customers
     */
    suspend fun getCustomerCount(): Int {
        val customers = customerDao.getAllCustomers().first()
        return customers.size
    }

    /**
     * Get customers sorted by total credit (highest first)
     */
    suspend fun getCustomersByDebt(): List<CustomerEntity> {
        val allCustomers = customerDao.getAllCustomers().first()
        val customersWithDebt = mutableListOf<Pair<CustomerEntity, Double>>()

        allCustomers.forEach { customer ->
            val totalDebt = creditLedgerDao.getTotalDebtByCustomer(customer.id).first() ?: 0.0
            if (totalDebt > 0.0) {
                customersWithDebt.add(Pair(customer, totalDebt))
            }
        }

        return customersWithDebt.sortedByDescending { it.second }.map { it.first }
    }

    /**
     * Get customer with credit details
     */
    suspend fun getCustomerWithCreditInfo(customerId: String): CustomerCreditInfo? {
        val customer = customerDao.getCustomerById(customerId).first() ?: return null
        val totalCredit = creditLedgerDao.getTotalDebtByCustomer(customerId).first() ?: 0.0

        return CustomerCreditInfo(
            customer = customer,
            totalCredit = totalCredit,
            hasOutstandingCredit = totalCredit > 0
        )
    }
}

/**
 * Customer with credit information
 */
data class CustomerCreditInfo(
    val customer: CustomerEntity,
    val totalCredit: Double,
    val hasOutstandingCredit: Boolean
)
