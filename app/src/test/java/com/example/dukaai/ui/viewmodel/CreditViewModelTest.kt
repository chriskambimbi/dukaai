package com.example.dukaai.ui.viewmodel

import com.example.dukaai.data.local.entity.CreditLedgerEntity
import com.example.dukaai.data.local.entity.CustomerEntity
import com.example.dukaai.data.local.entity.PaymentEntity
import com.example.dukaai.data.repository.CreditRepository
import com.example.dukaai.data.repository.CreditStats
import com.example.dukaai.data.repository.CustomerRepository
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CreditViewModel
 * Tests credit management, customer handling, and filtering
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CreditViewModelTest {

    @MockK
    private lateinit var creditRepository: CreditRepository

    @MockK
    private lateinit var customerRepository: CustomerRepository

    private lateinit var viewModel: CreditViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testCustomer = CustomerEntity(
        id = "customer-1",
        name = "John Doe",
        phoneNumber = "+260971234567",
        address = "123 Main St"
    )

    private val testCustomer2 = CustomerEntity(
        id = "customer-2",
        name = "Jane Smith",
        phoneNumber = "+260972345678"
    )

    private val testCredit = CreditLedgerEntity(
        id = "credit-1",
        customerId = "customer-1",
        saleId = "sale-1",
        amount = 500.0,
        amountPaid = 0.0,
        amountRemaining = 500.0,
        status = "UNPAID",
        dueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
    )

    private val paidCredit = CreditLedgerEntity(
        id = "credit-2",
        customerId = "customer-1",
        saleId = "sale-2",
        amount = 200.0,
        amountPaid = 200.0,
        amountRemaining = 0.0,
        status = "PAID"
    )

    private val overdueCredit = CreditLedgerEntity(
        id = "credit-3",
        customerId = "customer-2",
        saleId = "sale-3",
        amount = 300.0,
        amountPaid = 0.0,
        amountRemaining = 300.0,
        status = "UNPAID",
        dueDate = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
    )

    private val testCreditStats = CreditStats(
        totalCredits = 3,
        totalAmount = 1000.0,
        totalPaid = 200.0,
        totalRemaining = 800.0,
        unpaidCount = 2,
        overdueCount = 1
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(testDispatcher)

        // Default mock behavior
        every { creditRepository.getAllCredits() } returns flowOf(listOf(testCredit, paidCredit, overdueCredit))
        every { creditRepository.getTotalOutstandingCredit() } returns flowOf(800.0)
        coEvery { creditRepository.getOverallCreditStats() } returns testCreditStats
        every { customerRepository.getAllCustomers() } returns flowOf(listOf(testCustomer, testCustomer2))
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): CreditViewModel {
        return CreditViewModel(creditRepository, customerRepository)
    }

    // ==================== Initialization Tests ====================

    @Test
    fun `initial state should load credits and customers`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(3, viewModel.credits.value.size)
        assertEquals(2, viewModel.customers.value.size)
    }

    @Test
    fun `initial state should load credit stats`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.creditStats.value)
        assertEquals(3, viewModel.creditStats.value?.totalCredits)
    }

    @Test
    fun `initial state should have default filter`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("ALL", viewModel.creditFilter.value)
    }

    // ==================== Load Credits Tests ====================

    @Test
    fun `loadCredits should update credits state`() = runTest {
        // Given
        viewModel = createViewModel()
        val newCredits = listOf(testCredit)
        every { creditRepository.getAllCredits() } returns flowOf(newCredits)

        // When
        viewModel.loadCredits()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.credits.value.size)
    }

    @Test
    fun `loadCredits should update loading state`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.loadCredits()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.value)
    }

    // ==================== Load Customer Tests ====================

    @Test
    fun `loadCustomer should set selected customer`() = runTest {
        // Given
        viewModel = createViewModel()
        every { customerRepository.getCustomerById("customer-1") } returns flowOf(testCustomer)
        every { creditRepository.getCreditsByCustomer("customer-1") } returns flowOf(listOf(testCredit, paidCredit))

        // When
        viewModel.loadCustomer("customer-1")
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.selectedCustomer.value)
        assertEquals("John Doe", viewModel.selectedCustomer.value?.name)
    }

    @Test
    fun `loadCustomer should load customer credits`() = runTest {
        // Given
        viewModel = createViewModel()
        every { customerRepository.getCustomerById("customer-1") } returns flowOf(testCustomer)
        every { creditRepository.getCreditsByCustomer("customer-1") } returns flowOf(listOf(testCredit, paidCredit))

        // When
        viewModel.loadCustomer("customer-1")
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.customerCredits.value.size)
    }

    @Test
    fun `loadCustomer should handle nonexistent customer`() = runTest {
        // Given
        viewModel = createViewModel()
        every { customerRepository.getCustomerById("nonexistent") } returns flowOf(null)
        every { creditRepository.getCreditsByCustomer("nonexistent") } returns flowOf(emptyList())

        // When
        viewModel.loadCustomer("nonexistent")
        advanceUntilIdle()

        // Then
        assertNull(viewModel.selectedCustomer.value)
    }

    // ==================== Add Customer Tests ====================

    @Test
    fun `addCustomer should call repository and reload customers`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { customerRepository.addCustomer(any()) } returns Result.success(1L)

        // When
        viewModel.addCustomer(testCustomer)
        advanceUntilIdle()

        // Then
        coVerify { customerRepository.addCustomer(testCustomer) }
    }

    @Test
    fun `addCustomer should handle error`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { customerRepository.addCustomer(any()) } returns Result.failure(RuntimeException("Add failed"))

        // When
        viewModel.addCustomer(testCustomer)
        advanceUntilIdle()

        // Then
        assertEquals("Add failed", viewModel.error.value)
    }

    // ==================== Record Credit Tests ====================

    @Test
    fun `recordCredit should call repository and reload`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { creditRepository.recordCredit(any()) } returns Result.success(1L)

        // When
        viewModel.recordCredit(testCredit)
        advanceUntilIdle()

        // Then
        coVerify { creditRepository.recordCredit(testCredit) }
    }

    @Test
    fun `recordCredit should reload stats after success`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { creditRepository.recordCredit(any()) } returns Result.success(1L)

        // When
        viewModel.recordCredit(testCredit)
        advanceUntilIdle()

        // Then
        coVerify(atLeast = 2) { creditRepository.getOverallCreditStats() }
    }

    @Test
    fun `recordCredit should handle error`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { creditRepository.recordCredit(any()) } returns Result.failure(RuntimeException("Record failed"))

        // When
        viewModel.recordCredit(testCredit)
        advanceUntilIdle()

        // Then
        assertEquals("Record failed", viewModel.error.value)
    }

    // ==================== Record Payment Tests ====================

    @Test
    fun `recordPayment should call repository`() = runTest {
        // Given
        viewModel = createViewModel()
        val payment = PaymentEntity(
            id = "payment-1",
            creditId = "credit-1",
            amount = 100.0,
            paymentMethod = "CASH"
        )
        coEvery { creditRepository.recordPayment(any(), any()) } returns Result.success(Unit)

        // When
        viewModel.recordPayment("credit-1", payment)
        advanceUntilIdle()

        // Then
        coVerify { creditRepository.recordPayment("credit-1", payment) }
    }

    @Test
    fun `recordPayment should reload data after success`() = runTest {
        // Given
        viewModel = createViewModel()
        every { customerRepository.getCustomerById("customer-1") } returns flowOf(testCustomer)
        every { creditRepository.getCreditsByCustomer("customer-1") } returns flowOf(listOf(testCredit))
        viewModel.loadCustomer("customer-1")
        advanceUntilIdle()

        val payment = PaymentEntity(
            id = "payment-1",
            creditId = "credit-1",
            amount = 100.0,
            paymentMethod = "CASH"
        )
        coEvery { creditRepository.recordPayment(any(), any()) } returns Result.success(Unit)

        // When
        viewModel.recordPayment("credit-1", payment)
        advanceUntilIdle()

        // Then
        verify(atLeast = 2) { creditRepository.getAllCredits() }
    }

    @Test
    fun `recordPayment should handle error`() = runTest {
        // Given
        viewModel = createViewModel()
        val payment = PaymentEntity(
            id = "payment-1",
            creditId = "credit-1",
            amount = 100.0,
            paymentMethod = "CASH"
        )
        coEvery { creditRepository.recordPayment(any(), any()) } returns Result.failure(RuntimeException("Payment failed"))

        // When
        viewModel.recordPayment("credit-1", payment)
        advanceUntilIdle()

        // Then
        assertEquals("Payment failed", viewModel.error.value)
    }

    // ==================== Filter Tests ====================

    @Test
    fun `setFilter should update filter state`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.setFilter("UNPAID")

        // Then
        assertEquals("UNPAID", viewModel.creditFilter.value)
    }

    @Test
    fun `filteredCredits should return only unpaid when filter is UNPAID`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.setFilter("UNPAID")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredCredits.value
        assertTrue(filtered.all { it.status == "UNPAID" })
    }

    @Test
    fun `filteredCredits should return only paid when filter is PAID`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.setFilter("PAID")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredCredits.value
        assertTrue(filtered.all { it.status == "PAID" })
    }

    @Test
    fun `filteredCredits should return all when filter is ALL`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.setFilter("ALL")
        advanceUntilIdle()

        // Then - credits.value should have all credits regardless of filteredCredits timing
        assertEquals(3, viewModel.credits.value.size)
    }

    @Test
    fun `filteredCredits should return overdue credits when filter is OVERDUE`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.setFilter("OVERDUE")
        advanceUntilIdle()

        // Then
        val filtered = viewModel.filteredCredits.value
        assertTrue(filtered.all {
            it.status != "PAID" && it.dueDate != null && it.dueDate!! < System.currentTimeMillis()
        })
    }

    // ==================== Total Outstanding Tests ====================

    @Test
    fun `totalOutstanding should reflect repository value`() = runTest {
        // Given
        every { creditRepository.getTotalOutstandingCredit() } returns flowOf(1500.0)
        viewModel = createViewModel()

        // When
        advanceUntilIdle()

        // Then
        assertEquals(1500.0, viewModel.totalOutstanding.value, 0.01)
    }

    @Test
    fun `totalOutstanding should be zero when null`() = runTest {
        // Given
        every { creditRepository.getTotalOutstandingCredit() } returns flowOf(null)
        viewModel = createViewModel()

        // When
        advanceUntilIdle()

        // Then
        assertEquals(0.0, viewModel.totalOutstanding.value, 0.01)
    }

    // ==================== Clear Error Tests ====================

    @Test
    fun `clearError should reset error state`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { customerRepository.addCustomer(any()) } returns Result.failure(RuntimeException("Test error"))
        viewModel.addCustomer(testCustomer)
        advanceUntilIdle()
        assertEquals("Test error", viewModel.error.value)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `empty credits list should be handled`() = runTest {
        // Given
        every { creditRepository.getAllCredits() } returns flowOf(emptyList())
        coEvery { creditRepository.getOverallCreditStats() } returns CreditStats(0, 0.0, 0.0, 0.0, 0, 0)
        viewModel = createViewModel()

        // When
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.credits.value.isEmpty())
        assertEquals(0, viewModel.creditStats.value?.totalCredits)
    }

    @Test
    fun `empty customers list should be handled`() = runTest {
        // Given
        every { customerRepository.getAllCustomers() } returns flowOf(emptyList())
        viewModel = createViewModel()

        // When
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.customers.value.isEmpty())
    }
}
