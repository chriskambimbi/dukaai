package com.example.dukaai.data.local.dao

import androidx.room.*
import com.example.dukaai.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Result class for customer with their total outstanding debt
 * Used by JOIN query to avoid N+1 queries
 */
data class CustomerWithDebt(
    @Embedded val customer: CustomerEntity,
    @ColumnInfo(name = "totalDebt") val totalDebt: Double
)

/**
 * Data Access Object for Customer operations
 */
@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    /**
     * Efficient single-query method to get all customers with outstanding credit.
     * Uses LEFT JOIN with GROUP BY to calculate total debt in one database call.
     * Avoids N+1 query problem.
     */
    @Query("""
        SELECT c.*, COALESCE(SUM(cl.amountRemaining), 0.0) as totalDebt
        FROM customers c
        LEFT JOIN credit_ledger cl ON c.id = cl.customerId AND cl.status != 'PAID'
        GROUP BY c.id
        HAVING totalDebt > 0
        ORDER BY c.name ASC
    """)
    suspend fun getCustomersWithOutstandingDebt(): List<CustomerWithDebt>

    /**
     * Get all customers with their debt amounts, sorted by debt (highest first).
     * Single query using JOIN - no N+1 problem.
     */
    @Query("""
        SELECT c.*, COALESCE(SUM(cl.amountRemaining), 0.0) as totalDebt
        FROM customers c
        LEFT JOIN credit_ledger cl ON c.id = cl.customerId AND cl.status != 'PAID'
        GROUP BY c.id
        HAVING totalDebt > 0
        ORDER BY totalDebt DESC
    """)
    suspend fun getCustomersSortedByDebt(): List<CustomerWithDebt>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    fun getCustomerById(customerId: String): Flow<CustomerEntity?>

    @Query("SELECT * FROM customers WHERE phoneNumber = :phoneNumber LIMIT 1")
    suspend fun getCustomerByPhone(phoneNumber: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: String)

    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
