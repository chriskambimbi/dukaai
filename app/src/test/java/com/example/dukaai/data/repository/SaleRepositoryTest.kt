package com.example.dukaai.data.repository

import androidx.room.withTransaction
import com.example.dukaai.data.local.DukaDatabase
import com.example.dukaai.data.local.dao.InventoryLogDao
import com.example.dukaai.data.local.dao.ProductDao
import com.example.dukaai.data.local.dao.SaleDao
import com.example.dukaai.data.local.entity.InventoryLogEntity
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.local.entity.SaleEntity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SaleRepository
 * Tests business logic, transaction atomicity, and error handling
 */
class SaleRepositoryTest {

    @MockK
    private lateinit var database: DukaDatabase

    @MockK
    private lateinit var saleDao: SaleDao

    @MockK
    private lateinit var productDao: ProductDao

    @MockK
    private lateinit var inventoryLogDao: InventoryLogDao

    private lateinit var saleRepository: SaleRepository

    private val testProduct = ProductEntity(
        id = "product-1",
        name = "Test Product",
        category = "Test Category",
        currentStock = 100,
        minStockThreshold = 10,
        buyingPrice = 10.0,
        sellingPrice = 15.0
    )

    private val testSale = SaleEntity(
        id = "sale-1",
        productId = "product-1",
        quantity = 5,
        unitPrice = 15.0,
        totalAmount = 75.0,
        saleType = "CASH"
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        saleRepository = SaleRepository(database, saleDao, productDao, inventoryLogDao)

        // Mock the Room withTransaction extension function
        // This executes the transaction block directly for testing
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { database.withTransaction(captureLambda<suspend () -> Any>()) } coAnswers {
            lambda<suspend () -> Any>().captured.invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== recordSale Tests ====================

    @Test
    fun `recordSale should succeed when product has sufficient stock`() = runTest {
        // Given
        coEvery { productDao.getProductById("product-1") } returns flowOf(testProduct)
        coEvery { saleDao.insertSale(any()) } returns 1L
        coEvery { productDao.updateStock(any(), any(), any()) } just Runs
        coEvery { inventoryLogDao.insertLog(any()) } returns 1L

        // When
        val result = saleRepository.recordSale(testSale)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())

        // Verify all operations were called
        coVerify(exactly = 1) { saleDao.insertSale(testSale) }
        coVerify(exactly = 1) { productDao.updateStock("product-1", 95, any()) }
        coVerify(exactly = 1) { inventoryLogDao.insertLog(match {
            it.productId == "product-1" &&
            it.actionType == "SALE" &&
            it.quantityChange == -5 &&
            it.previousStock == 100 &&
            it.newStock == 95
        }) }
    }

    @Test
    fun `recordSale should fail when product not found`() = runTest {
        // Given
        coEvery { productDao.getProductById("product-1") } returns flowOf(null)

        // When
        val result = saleRepository.recordSale(testSale)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Product not found", result.exceptionOrNull()?.message)

        // Verify no database writes occurred
        coVerify(exactly = 0) { saleDao.insertSale(any()) }
        coVerify(exactly = 0) { productDao.updateStock(any(), any(), any()) }
        coVerify(exactly = 0) { inventoryLogDao.insertLog(any()) }
    }

    @Test
    fun `recordSale should fail when insufficient stock`() = runTest {
        // Given
        val lowStockProduct = testProduct.copy(currentStock = 3)
        coEvery { productDao.getProductById("product-1") } returns flowOf(lowStockProduct)

        // When
        val result = saleRepository.recordSale(testSale)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Insufficient stock") == true)

        // Verify no database writes occurred
        coVerify(exactly = 0) { saleDao.insertSale(any()) }
    }

    @Test
    fun `recordSale should handle database exception`() = runTest {
        // Given
        coEvery { productDao.getProductById("product-1") } returns flowOf(testProduct)
        coEvery { database.withTransaction(captureLambda<suspend () -> Any>()) } throws RuntimeException("Database error")

        // When
        val result = saleRepository.recordSale(testSale)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Database error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `recordSale should calculate correct new stock`() = runTest {
        // Given
        val productWith50Stock = testProduct.copy(currentStock = 50)
        val saleOf20Items = testSale.copy(quantity = 20)

        coEvery { productDao.getProductById("product-1") } returns flowOf(productWith50Stock)
        coEvery { saleDao.insertSale(any()) } returns 1L
        coEvery { productDao.updateStock(any(), any(), any()) } just Runs
        coEvery { inventoryLogDao.insertLog(any()) } returns 1L

        // When
        saleRepository.recordSale(saleOf20Items)

        // Then - verify stock is updated to 30 (50 - 20)
        coVerify { productDao.updateStock("product-1", 30, any()) }
    }

    // ==================== recordBulkSales Tests ====================

    @Test
    fun `recordBulkSales should succeed for multiple valid sales`() = runTest {
        // Given
        val sales = listOf(
            testSale.copy(id = "sale-1", quantity = 5),
            testSale.copy(id = "sale-2", quantity = 10),
            testSale.copy(id = "sale-3", quantity = 15)
        )

        coEvery { productDao.getProductById("product-1") } returns flowOf(testProduct)
        coEvery { saleDao.insertSale(any()) } returnsMany listOf(1L, 2L, 3L)
        coEvery { productDao.updateStock(any(), any(), any()) } just Runs
        coEvery { inventoryLogDao.insertLog(any()) } returns 1L

        // When
        val result = saleRepository.recordBulkSales(sales)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(listOf(1L, 2L, 3L), result.getOrNull())
    }

    @Test
    fun `recordBulkSales should return empty list for empty input`() = runTest {
        // When
        val result = saleRepository.recordBulkSales(emptyList())

        // Then
        assertTrue(result.isSuccess)
        assertEquals(emptyList<Long>(), result.getOrNull())
    }

    @Test
    fun `recordBulkSales should fail if any product has insufficient stock`() = runTest {
        // Given - product only has 10 items but second sale needs 50
        val productWith10Stock = testProduct.copy(currentStock = 10)
        val sales = listOf(
            testSale.copy(id = "sale-1", quantity = 5),
            testSale.copy(id = "sale-2", quantity = 50) // This will fail
        )

        coEvery { productDao.getProductById("product-1") } returns flowOf(productWith10Stock)

        // When
        val result = saleRepository.recordBulkSales(sales)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Insufficient stock") == true)
    }

    // ==================== deleteSale Tests ====================

    @Test
    fun `deleteSale should restore stock and remove sale`() = runTest {
        // Given
        coEvery { saleDao.getSaleById("sale-1") } returns flowOf(testSale)
        coEvery { productDao.getProductById("product-1") } returns flowOf(testProduct)
        coEvery { productDao.updateStock(any(), any(), any()) } just Runs
        coEvery { inventoryLogDao.insertLog(any()) } returns 1L
        coEvery { saleDao.deleteSale(any()) } just Runs

        // When
        val result = saleRepository.deleteSale("sale-1")

        // Then
        assertTrue(result.isSuccess)

        // Verify stock was restored (100 + 5 = 105)
        coVerify { productDao.updateStock("product-1", 105, any()) }

        // Verify inventory log was created
        coVerify { inventoryLogDao.insertLog(match {
            it.actionType == "SALE_REVERSAL" &&
            it.quantityChange == 5 &&
            it.newStock == 105
        }) }

        // Verify sale was deleted
        coVerify { saleDao.deleteSale(testSale) }
    }

    @Test
    fun `deleteSale should fail when sale not found`() = runTest {
        // Given
        coEvery { saleDao.getSaleById("nonexistent") } returns flowOf(null)

        // When
        val result = saleRepository.deleteSale("nonexistent")

        // Then
        assertTrue(result.isFailure)
        assertEquals("Sale not found", result.exceptionOrNull()?.message)
    }

    // ==================== Query Tests ====================

    @Test
    fun `getTodaySales should return sales within today's date range`() = runTest {
        // Given
        val todaySales = listOf(testSale, testSale.copy(id = "sale-2"))
        coEvery { saleDao.getSalesByDateRange(any(), any()) } returns flowOf(todaySales)

        // When
        val result = saleRepository.getTodaySales()

        // Then
        result.collect { sales ->
            assertEquals(2, sales.size)
        }
    }

    @Test
    fun `getSalesStats should calculate correct statistics`() = runTest {
        // Given
        val sales = listOf(
            testSale.copy(quantity = 5, totalAmount = 75.0),
            testSale.copy(id = "sale-2", quantity = 10, totalAmount = 150.0)
        )
        coEvery { saleDao.getSalesByDateRange(any(), any()) } returns flowOf(sales)
        coEvery { saleDao.getTotalRevenue(any(), any()) } returns flowOf(225.0)
        coEvery { saleDao.getSalesCount(any(), any()) } returns flowOf(2)

        // When
        val stats = saleRepository.getSalesStats(0L, Long.MAX_VALUE)

        // Then
        assertEquals(2, stats.totalSales)
        assertEquals(225.0, stats.totalRevenue, 0.01)
        assertEquals(112.5, stats.averageSaleValue, 0.01)
        assertEquals(15, stats.totalItemsSold)
    }
}
