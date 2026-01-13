package com.example.dukaai.data.repository

import com.example.dukaai.data.local.dao.CustomerDao
import com.example.dukaai.data.local.dao.CustomerWithDebt
import com.example.dukaai.data.local.dao.CreditLedgerDao
import com.example.dukaai.data.local.entity.CustomerEntity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CustomerRepository
 * Tests optimized queries, CRUD operations, and error handling
 */
class CustomerRepositoryTest {

    @MockK
    private lateinit var customerDao: CustomerDao

    @MockK
    private lateinit var creditLedgerDao: CreditLedgerDao

    private lateinit var customerRepository: CustomerRepository

    private val testCustomer = CustomerEntity(
        id = "customer-1",
        name = "John Doe",
        phoneNumber = "+260971234567",
        address = "123 Main St",
        notes = "Regular customer"
    )

    private val testCustomer2 = CustomerEntity(
        id = "customer-2",
        name = "Jane Smith",
        phoneNumber = "+260972345678",
        address = "456 Oak Ave"
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        customerRepository = CustomerRepository(customerDao, creditLedgerDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== Basic CRUD Tests ====================

    @Test
    fun `getAllCustomers should return all customers`() = runTest {
        // Given
        val customers = listOf(testCustomer, testCustomer2)
        every { customerDao.getAllCustomers() } returns flowOf(customers)

        // When
        val result = customerRepository.getAllCustomers()

        // Then
        result.collect { list ->
            assertEquals(2, list.size)
            assertEquals("John Doe", list[0].name)
            assertEquals("Jane Smith", list[1].name)
        }
    }

    @Test
    fun `getCustomerById should return customer when exists`() = runTest {
        // Given
        every { customerDao.getCustomerById("customer-1") } returns flowOf(testCustomer)

        // When
        val result = customerRepository.getCustomerById("customer-1")

        // Then
        result.collect { customer ->
            assertNotNull(customer)
            assertEquals("John Doe", customer?.name)
        }
    }

    @Test
    fun `getCustomerById should return null when not exists`() = runTest {
        // Given
        every { customerDao.getCustomerById("nonexistent") } returns flowOf(null)

        // When
        val result = customerRepository.getCustomerById("nonexistent")

        // Then
        result.collect { customer ->
            assertNull(customer)
        }
    }

    @Test
    fun `searchCustomers should return matching customers`() = runTest {
        // Given
        val matchingCustomers = listOf(testCustomer)
        every { customerDao.searchCustomers(any()) } returns flowOf(matchingCustomers)

        // When
        val result = customerRepository.searchCustomers("John")

        // Then
        result.collect { list ->
            assertEquals(1, list.size)
            assertEquals("John Doe", list[0].name)
        }
    }

    // ==================== Add Customer Tests ====================

    @Test
    fun `addCustomer should succeed when phone is unique`() = runTest {
        // Given
        coEvery { customerDao.getCustomerByPhone("+260971234567") } returns null
        coEvery { customerDao.insertCustomer(any()) } returns 1L

        // When
        val result = customerRepository.addCustomer(testCustomer)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { customerDao.insertCustomer(testCustomer) }
    }

    @Test
    fun `addCustomer should fail when phone already exists`() = runTest {
        // Given
        coEvery { customerDao.getCustomerByPhone("+260971234567") } returns testCustomer

        // When
        val result = customerRepository.addCustomer(testCustomer)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already exists") == true)
        coVerify(exactly = 0) { customerDao.insertCustomer(any()) }
    }

    @Test
    fun `addCustomer should succeed when phone is null`() = runTest {
        // Given
        val customerWithoutPhone = testCustomer.copy(phoneNumber = null)
        coEvery { customerDao.insertCustomer(any()) } returns 1L

        // When
        val result = customerRepository.addCustomer(customerWithoutPhone)

        // Then
        assertTrue(result.isSuccess)
        // Phone check should be skipped
        coVerify(exactly = 0) { customerDao.getCustomerByPhone(any()) }
    }

    @Test
    fun `addCustomer should handle database exception`() = runTest {
        // Given
        coEvery { customerDao.getCustomerByPhone(any()) } returns null
        coEvery { customerDao.insertCustomer(any()) } throws RuntimeException("DB error")

        // When
        val result = customerRepository.addCustomer(testCustomer)

        // Then
        assertTrue(result.isFailure)
        assertEquals("DB error", result.exceptionOrNull()?.message)
    }

    // ==================== Update Customer Tests ====================

    @Test
    fun `updateCustomer should succeed`() = runTest {
        // Given
        coEvery { customerDao.updateCustomer(any()) } just Runs

        // When
        val updatedCustomer = testCustomer.copy(name = "John Updated")
        val result = customerRepository.updateCustomer(updatedCustomer)

        // Then
        assertTrue(result.isSuccess)
        coVerify { customerDao.updateCustomer(updatedCustomer) }
    }

    @Test
    fun `updateCustomer should handle exception`() = runTest {
        // Given
        coEvery { customerDao.updateCustomer(any()) } throws RuntimeException("Update failed")

        // When
        val result = customerRepository.updateCustomer(testCustomer)

        // Then
        assertTrue(result.isFailure)
    }

    // ==================== Delete Customer Tests ====================

    @Test
    fun `deleteCustomer should succeed`() = runTest {
        // Given
        coEvery { customerDao.deleteCustomer(any()) } just Runs

        // When
        val result = customerRepository.deleteCustomer(testCustomer)

        // Then
        assertTrue(result.isSuccess)
        coVerify { customerDao.deleteCustomer(testCustomer) }
    }

    // ==================== Optimized Query Tests (N+1 Fix Verification) ====================

    @Test
    fun `getCustomersWithCredit should use single JOIN query`() = runTest {
        // Given
        val customersWithDebt = listOf(
            CustomerWithDebt(testCustomer, 500.0),
            CustomerWithDebt(testCustomer2, 300.0)
        )
        coEvery { customerDao.getCustomersWithOutstandingDebt() } returns customersWithDebt

        // When
        val result = customerRepository.getCustomersWithCredit()

        // Then
        assertEquals(2, result.size)
        assertEquals("John Doe", result[0].name)
        assertEquals("Jane Smith", result[1].name)

        // Verify only ONE database call was made (not N+1)
        coVerify(exactly = 1) { customerDao.getCustomersWithOutstandingDebt() }
        // Verify N+1 pattern is NOT used
        coVerify(exactly = 0) { creditLedgerDao.getTotalDebtByCustomer(any()) }
    }

    @Test
    fun `getCustomersWithCreditDetails should return customers with debt amounts`() = runTest {
        // Given
        val customersWithDebt = listOf(
            CustomerWithDebt(testCustomer, 500.0),
            CustomerWithDebt(testCustomer2, 300.0)
        )
        coEvery { customerDao.getCustomersWithOutstandingDebt() } returns customersWithDebt

        // When
        val result = customerRepository.getCustomersWithCreditDetails()

        // Then
        assertEquals(2, result.size)
        assertEquals(500.0, result[0].totalDebt, 0.01)
        assertEquals(300.0, result[1].totalDebt, 0.01)
    }

    @Test
    fun `getCustomersByDebt should return customers sorted by debt descending`() = runTest {
        // Given - customer2 has higher debt
        val sortedByDebt = listOf(
            CustomerWithDebt(testCustomer2, 800.0),
            CustomerWithDebt(testCustomer, 200.0)
        )
        coEvery { customerDao.getCustomersSortedByDebt() } returns sortedByDebt

        // When
        val result = customerRepository.getCustomersByDebt()

        // Then
        assertEquals(2, result.size)
        assertEquals("Jane Smith", result[0].name) // Higher debt first
        assertEquals("John Doe", result[1].name)

        // Verify single query was used
        coVerify(exactly = 1) { customerDao.getCustomersSortedByDebt() }
    }

    @Test
    fun `getCustomersByDebtWithAmounts should include debt values`() = runTest {
        // Given
        val sortedByDebt = listOf(
            CustomerWithDebt(testCustomer2, 800.0),
            CustomerWithDebt(testCustomer, 200.0)
        )
        coEvery { customerDao.getCustomersSortedByDebt() } returns sortedByDebt

        // When
        val result = customerRepository.getCustomersByDebtWithAmounts()

        // Then
        assertEquals(800.0, result[0].totalDebt, 0.01)
        assertEquals(200.0, result[1].totalDebt, 0.01)
    }

    @Test
    fun `getCustomersWithCredit should return empty list when no debt`() = runTest {
        // Given
        coEvery { customerDao.getCustomersWithOutstandingDebt() } returns emptyList()

        // When
        val result = customerRepository.getCustomersWithCredit()

        // Then
        assertTrue(result.isEmpty())
    }

    // ==================== Customer Credit Info Tests ====================

    @Test
    fun `getCustomerWithCreditInfo should return credit details`() = runTest {
        // Given
        every { customerDao.getCustomerById("customer-1") } returns flowOf(testCustomer)
        every { creditLedgerDao.getTotalDebtByCustomer("customer-1") } returns flowOf(500.0)

        // When
        val result = customerRepository.getCustomerWithCreditInfo("customer-1")

        // Then
        assertNotNull(result)
        assertEquals("John Doe", result?.customer?.name)
        assertEquals(500.0, result?.totalCredit ?: 0.0, 0.01)
        assertTrue(result?.hasOutstandingCredit == true)
    }

    @Test
    fun `getCustomerWithCreditInfo should return null for nonexistent customer`() = runTest {
        // Given
        every { customerDao.getCustomerById("nonexistent") } returns flowOf(null)

        // When
        val result = customerRepository.getCustomerWithCreditInfo("nonexistent")

        // Then
        assertNull(result)
    }

    @Test
    fun `getCustomerWithCreditInfo should handle zero credit`() = runTest {
        // Given
        every { customerDao.getCustomerById("customer-1") } returns flowOf(testCustomer)
        every { creditLedgerDao.getTotalDebtByCustomer("customer-1") } returns flowOf(null)

        // When
        val result = customerRepository.getCustomerWithCreditInfo("customer-1")

        // Then
        assertNotNull(result)
        assertEquals(0.0, result?.totalCredit ?: -1.0, 0.01)
        assertFalse(result?.hasOutstandingCredit == true)
    }

    // ==================== Customer Count Tests ====================

    @Test
    fun `getCustomerCount should return correct count`() = runTest {
        // Given
        val customers = listOf(testCustomer, testCustomer2)
        every { customerDao.getAllCustomers() } returns flowOf(customers)

        // When
        val count = customerRepository.getCustomerCount()

        // Then
        assertEquals(2, count)
    }

    @Test
    fun `getCustomerCount should return zero for empty database`() = runTest {
        // Given
        every { customerDao.getAllCustomers() } returns flowOf(emptyList())

        // When
        val count = customerRepository.getCustomerCount()

        // Then
        assertEquals(0, count)
    }
}
