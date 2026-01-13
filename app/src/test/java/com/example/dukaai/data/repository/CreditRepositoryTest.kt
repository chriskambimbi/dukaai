package com.example.dukaai.data.repository

import com.example.dukaai.data.local.dao.CreditLedgerDao
import com.example.dukaai.data.local.dao.PaymentDao
import com.example.dukaai.data.local.entity.CreditLedgerEntity
import com.example.dukaai.data.local.entity.PaymentEntity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for CreditRepository
 * Tests credit recording, payment processing, and statistics calculation
 */
class CreditRepositoryTest {

    @MockK
    private lateinit var creditLedgerDao: CreditLedgerDao

    @MockK
    private lateinit var paymentDao: PaymentDao

    private lateinit var creditRepository: CreditRepository

    private val testCredit = CreditLedgerEntity(
        id = "credit-1",
        customerId = "customer-1",
        saleId = "sale-1",
        amount = 500.0,
        amountPaid = 0.0,
        amountRemaining = 500.0,
        status = "UNPAID",
        dueDate = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days from now
    )

    private val partiallyPaidCredit = CreditLedgerEntity(
        id = "credit-2",
        customerId = "customer-1",
        saleId = "sale-2",
        amount = 1000.0,
        amountPaid = 400.0,
        amountRemaining = 600.0,
        status = "PARTIAL"
    )

    private val paidCredit = CreditLedgerEntity(
        id = "credit-3",
        customerId = "customer-2",
        saleId = "sale-3",
        amount = 200.0,
        amountPaid = 200.0,
        amountRemaining = 0.0,
        status = "PAID"
    )

    private val overdueCredit = CreditLedgerEntity(
        id = "credit-4",
        customerId = "customer-1",
        saleId = "sale-4",
        amount = 300.0,
        amountPaid = 0.0,
        amountRemaining = 300.0,
        status = "UNPAID",
        dueDate = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // 7 days ago
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        creditRepository = CreditRepository(creditLedgerDao, paymentDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== Get All Credits Tests ====================

    @Test
    fun `getAllCredits should return all credit records`() = runTest {
        // Given
        val credits = listOf(testCredit, partiallyPaidCredit, paidCredit)
        every { creditLedgerDao.getAllCreditRecords() } returns flowOf(credits)

        // When
        val result = creditRepository.getAllCredits()

        // Then
        result.collect { list ->
            assertEquals(3, list.size)
        }
    }

    // ==================== Get Credit By ID Tests ====================

    @Test
    fun `getCreditById should return credit when exists`() = runTest {
        // Given
        every { creditLedgerDao.getCreditById("credit-1") } returns flowOf(testCredit)

        // When
        val result = creditRepository.getCreditById("credit-1")

        // Then
        result.collect { credit ->
            assertNotNull(credit)
            assertEquals(500.0, credit!!.amount, 0.01)
            assertEquals("UNPAID", credit.status)
        }
    }

    @Test
    fun `getCreditById should return null when not exists`() = runTest {
        // Given
        every { creditLedgerDao.getCreditById("nonexistent") } returns flowOf(null)

        // When
        val result = creditRepository.getCreditById("nonexistent")

        // Then
        result.collect { credit ->
            assertNull(credit)
        }
    }

    // ==================== Get Credits By Customer Tests ====================

    @Test
    fun `getCreditsByCustomer should return customer credits`() = runTest {
        // Given
        val customerCredits = listOf(testCredit, partiallyPaidCredit, overdueCredit)
        every { creditLedgerDao.getCreditsByCustomer("customer-1") } returns flowOf(customerCredits)

        // When
        val result = creditRepository.getCreditsByCustomer("customer-1")

        // Then
        result.collect { list ->
            assertEquals(3, list.size)
            assertTrue(list.all { it.customerId == "customer-1" })
        }
    }

    // ==================== Get Unpaid Credits Tests ====================

    @Test
    fun `getUnpaidCredits should return only unpaid credits`() = runTest {
        // Given
        val unpaidCredits = listOf(testCredit, overdueCredit)
        every { creditLedgerDao.getCreditsByStatus("UNPAID") } returns flowOf(unpaidCredits)

        // When
        val result = creditRepository.getUnpaidCredits()

        // Then
        result.collect { list ->
            assertEquals(2, list.size)
            assertTrue(list.all { it.status == "UNPAID" })
        }
    }

    // ==================== Get Overdue Credits Tests ====================

    @Test
    fun `getOverdueCredits should return overdue credits`() = runTest {
        // Given
        every { creditLedgerDao.getOverdueCredits(any()) } returns flowOf(listOf(overdueCredit))

        // When
        val result = creditRepository.getOverdueCredits()

        // Then
        result.collect { list ->
            assertEquals(1, list.size)
            assertTrue(list[0].dueDate!! < System.currentTimeMillis())
        }
    }

    // ==================== Get Total Outstanding Credit Tests ====================

    @Test
    fun `getTotalOutstandingCredit should return sum of remaining amounts`() = runTest {
        // Given
        every { creditLedgerDao.getTotalOutstandingDebt() } returns flowOf(1400.0)

        // When
        val result = creditRepository.getTotalOutstandingCredit()

        // Then
        result.collect { total ->
            assertEquals(1400.0, total ?: 0.0, 0.01)
        }
    }

    @Test
    fun `getTotalOutstandingCredit should return null when no credits`() = runTest {
        // Given
        every { creditLedgerDao.getTotalOutstandingDebt() } returns flowOf(null)

        // When
        val result = creditRepository.getTotalOutstandingCredit()

        // Then
        result.collect { total ->
            assertNull(total)
        }
    }

    // ==================== Record Credit Tests ====================

    @Test
    fun `recordCredit should succeed and return credit id`() = runTest {
        // Given
        coEvery { creditLedgerDao.insertCredit(any()) } returns 1L

        // When
        val result = creditRepository.recordCredit(testCredit)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { creditLedgerDao.insertCredit(testCredit) }
    }

    @Test
    fun `recordCredit should handle database exception`() = runTest {
        // Given
        coEvery { creditLedgerDao.insertCredit(any()) } throws RuntimeException("DB error")

        // When
        val result = creditRepository.recordCredit(testCredit)

        // Then
        assertTrue(result.isFailure)
        assertEquals("DB error", result.exceptionOrNull()?.message)
    }

    // ==================== Update Credit Tests ====================

    @Test
    fun `updateCredit should succeed`() = runTest {
        // Given
        coEvery { creditLedgerDao.updateCredit(any()) } just Runs
        val updatedCredit = testCredit.copy(status = "PARTIAL", amountPaid = 100.0)

        // When
        val result = creditRepository.updateCredit(updatedCredit)

        // Then
        assertTrue(result.isSuccess)
        coVerify { creditLedgerDao.updateCredit(updatedCredit) }
    }

    @Test
    fun `updateCredit should handle exception`() = runTest {
        // Given
        coEvery { creditLedgerDao.updateCredit(any()) } throws RuntimeException("Update failed")

        // When
        val result = creditRepository.updateCredit(testCredit)

        // Then
        assertTrue(result.isFailure)
    }

    // ==================== Record Payment Tests ====================

    @Test
    fun `recordPayment should update credit and insert payment`() = runTest {
        // Given
        val payment = PaymentEntity(
            id = "payment-1",
            creditId = "credit-1",
            amount = 200.0,
            paymentMethod = "CASH"
        )
        every { creditLedgerDao.getCreditById("credit-1") } returns flowOf(testCredit)
        coEvery { creditLedgerDao.updateCredit(any()) } just Runs
        coEvery { paymentDao.insertPayment(any()) } returns 1L

        // When
        val result = creditRepository.recordPayment("credit-1", payment)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            creditLedgerDao.updateCredit(match {
                it.amountPaid == 200.0 && it.amountRemaining == 300.0 && it.status == "PARTIAL"
            })
        }
        coVerify { paymentDao.insertPayment(payment) }
    }

    @Test
    fun `recordPayment should mark as PAID when fully paid`() = runTest {
        // Given
        val fullPayment = PaymentEntity(
            id = "payment-1",
            creditId = "credit-1",
            amount = 500.0,
            paymentMethod = "CASH"
        )
        every { creditLedgerDao.getCreditById("credit-1") } returns flowOf(testCredit)
        coEvery { creditLedgerDao.updateCredit(any()) } just Runs
        coEvery { paymentDao.insertPayment(any()) } returns 1L

        // When
        val result = creditRepository.recordPayment("credit-1", fullPayment)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            creditLedgerDao.updateCredit(match {
                it.amountRemaining <= 0.0 && it.status == "PAID"
            })
        }
    }

    @Test
    fun `recordPayment should fail for nonexistent credit`() = runTest {
        // Given
        val payment = PaymentEntity(
            id = "payment-1",
            creditId = "nonexistent",
            amount = 100.0,
            paymentMethod = "CASH"
        )
        every { creditLedgerDao.getCreditById("nonexistent") } returns flowOf(null)

        // When
        val result = creditRepository.recordPayment("nonexistent", payment)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Credit not found", result.exceptionOrNull()?.message)
    }

    // ==================== Mark As Paid Tests ====================

    @Test
    fun `markAsPaid should update credit to PAID status`() = runTest {
        // Given
        every { creditLedgerDao.getCreditById("credit-1") } returns flowOf(testCredit)
        coEvery { creditLedgerDao.updateCredit(any()) } just Runs

        // When
        val result = creditRepository.markAsPaid("credit-1")

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            creditLedgerDao.updateCredit(match {
                it.status == "PAID" && it.amountPaid == 500.0 && it.amountRemaining == 0.0
            })
        }
    }

    @Test
    fun `markAsPaid should fail for nonexistent credit`() = runTest {
        // Given
        every { creditLedgerDao.getCreditById("nonexistent") } returns flowOf(null)

        // When
        val result = creditRepository.markAsPaid("nonexistent")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Credit not found", result.exceptionOrNull()?.message)
    }

    // ==================== Delete Credit Tests ====================

    @Test
    fun `deleteCredit should delete successfully`() = runTest {
        // Given
        coEvery { creditLedgerDao.deleteCredit(any()) } just Runs

        // When
        val result = creditRepository.deleteCredit(testCredit)

        // Then
        assertTrue(result.isSuccess)
        coVerify { creditLedgerDao.deleteCredit(testCredit) }
    }

    @Test
    fun `deleteCredit should handle exception`() = runTest {
        // Given
        coEvery { creditLedgerDao.deleteCredit(any()) } throws RuntimeException("Delete failed")

        // When
        val result = creditRepository.deleteCredit(testCredit)

        // Then
        assertTrue(result.isFailure)
    }

    // ==================== Credit Statistics Tests ====================

    @Test
    fun `getCustomerCreditStats should calculate correct statistics`() = runTest {
        // Given
        val customerCredits = listOf(testCredit, partiallyPaidCredit, overdueCredit)
        every { creditLedgerDao.getCreditsByCustomer("customer-1") } returns flowOf(customerCredits)
        every { creditLedgerDao.getTotalDebtByCustomer("customer-1") } returns flowOf(1400.0)

        // When
        val stats = creditRepository.getCustomerCreditStats("customer-1")

        // Then
        assertEquals(3, stats.totalCredits)
        assertEquals(1800.0, stats.totalAmount, 0.01) // 500 + 1000 + 300
        assertEquals(400.0, stats.totalPaid, 0.01)    // only partiallyPaidCredit has payments
        assertEquals(1400.0, stats.totalRemaining, 0.01)
        assertEquals(2, stats.unpaidCount)           // testCredit and overdueCredit
        assertEquals(1, stats.overdueCount)          // only overdueCredit
    }

    @Test
    fun `getOverallCreditStats should calculate total statistics`() = runTest {
        // Given
        val allCredits = listOf(testCredit, partiallyPaidCredit, paidCredit, overdueCredit)
        every { creditLedgerDao.getAllCreditRecords() } returns flowOf(allCredits)
        every { creditLedgerDao.getTotalOutstandingDebt() } returns flowOf(1400.0)

        // When
        val stats = creditRepository.getOverallCreditStats()

        // Then
        assertEquals(4, stats.totalCredits)
        assertEquals(2000.0, stats.totalAmount, 0.01)
        assertEquals(600.0, stats.totalPaid, 0.01)    // 400 + 200
        assertEquals(1400.0, stats.totalRemaining, 0.01)
    }

    // ==================== Credits Due Soon Tests ====================

    @Test
    fun `getCreditsDueSoon should return credits due within specified days`() = runTest {
        // Given
        val now = System.currentTimeMillis()
        val creditDueIn3Days = testCredit.copy(
            id = "credit-due-soon",
            dueDate = now + (3 * 24 * 60 * 60 * 1000)
        )
        val creditDueIn10Days = testCredit.copy(
            id = "credit-due-later",
            dueDate = now + (10 * 24 * 60 * 60 * 1000)
        )
        every { creditLedgerDao.getUnpaidCredits() } returns flowOf(listOf(creditDueIn3Days, creditDueIn10Days, overdueCredit))

        // When
        val result = creditRepository.getCreditsDueSoon(7)

        // Then
        assertEquals(1, result.size) // Only creditDueIn3Days should be returned
        assertEquals("credit-due-soon", result[0].id)
    }

    @Test
    fun `getCreditsDueSoon should exclude already overdue credits`() = runTest {
        // Given
        every { creditLedgerDao.getUnpaidCredits() } returns flowOf(listOf(overdueCredit))

        // When
        val result = creditRepository.getCreditsDueSoon(7)

        // Then
        assertTrue(result.isEmpty())
    }

    // ==================== Edge Cases ====================

    @Test
    fun `credit with null dueDate should be handled`() = runTest {
        // Given
        val noDueDateCredit = testCredit.copy(dueDate = null)
        every { creditLedgerDao.getUnpaidCredits() } returns flowOf(listOf(noDueDateCredit))

        // When
        val result = creditRepository.getCreditsDueSoon(7)

        // Then
        assertTrue(result.isEmpty()) // Credits without due date are not "due soon"
    }

    @Test
    fun `payment exceeding remaining amount should mark as paid`() = runTest {
        // Given
        val overpayment = PaymentEntity(
            id = "payment-1",
            creditId = "credit-1",
            amount = 600.0, // More than 500 remaining
            paymentMethod = "CASH"
        )
        every { creditLedgerDao.getCreditById("credit-1") } returns flowOf(testCredit)
        coEvery { creditLedgerDao.updateCredit(any()) } just Runs
        coEvery { paymentDao.insertPayment(any()) } returns 1L

        // When
        val result = creditRepository.recordPayment("credit-1", overpayment)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            creditLedgerDao.updateCredit(match {
                it.status == "PAID" // amountRemaining becomes negative but status is PAID
            })
        }
    }
}
