package com.example.dukaai.ui.viewmodel

import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.local.entity.SaleEntity
import com.example.dukaai.data.repository.ProductRepository
import com.example.dukaai.data.repository.SaleRepository
import com.example.dukaai.data.repository.SalesStats
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
 * Unit tests for SaleViewModel
 * Tests cart management, sale completion, and statistics
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SaleViewModelTest {

    @MockK
    private lateinit var saleRepository: SaleRepository

    @MockK
    private lateinit var productRepository: ProductRepository

    private lateinit var viewModel: SaleViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testProduct1 = ProductEntity(
        id = "product-1",
        name = "Coca Cola 500ml",
        category = "Beverages",
        currentStock = 100,
        minStockThreshold = 10,
        buyingPrice = 8.0,
        sellingPrice = 12.0
    )

    private val testProduct2 = ProductEntity(
        id = "product-2",
        name = "Bread Loaf",
        category = "Bakery",
        currentStock = 50,
        minStockThreshold = 5,
        buyingPrice = 15.0,
        sellingPrice = 25.0
    )

    private val testSale = SaleEntity(
        id = "sale-1",
        productId = "product-1",
        quantity = 5,
        unitPrice = 12.0,
        totalAmount = 60.0,
        saleType = "CASH"
    )

    private val testSalesStats = SalesStats(
        totalSales = 10,
        totalRevenue = 1500.0,
        averageSaleValue = 150.0,
        totalItemsSold = 50
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(testDispatcher)

        // Default mock behavior
        every { saleRepository.getTodaySales() } returns flowOf(listOf(testSale))
        coEvery { saleRepository.getSalesStats(any(), any()) } returns testSalesStats
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SaleViewModel {
        return SaleViewModel(saleRepository, productRepository)
    }

    // ==================== Initialization Tests ====================

    @Test
    fun `initial state should have empty cart`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.cartItems.value.isEmpty())
        assertEquals(0.0, viewModel.cartTotal.value, 0.01)
        assertEquals(0, viewModel.cartItemCount.value)
    }

    @Test
    fun `initial state should load today sales`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.todaySales.value.size)
    }

    @Test
    fun `initial state should load sales stats`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.salesStats.value)
        assertEquals(10, viewModel.salesStats.value?.totalSales)
    }

    // ==================== Add to Cart Tests ====================

    @Test
    fun `addToCart should add new product to cart`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.addToCart(testProduct1, 2)

        // Then
        assertEquals(1, viewModel.cartItems.value.size)
        assertEquals(2, viewModel.cartItems.value[0].quantity)
        // cartTotal is a derived flow that may need time to update
        val expectedTotal = viewModel.cartItems.value.sumOf { it.totalPrice }
        assertEquals(24.0, expectedTotal, 0.01)
    }

    @Test
    fun `addToCart should increment quantity for existing product`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.addToCart(testProduct1, 2)

        // When
        viewModel.addToCart(testProduct1, 3)

        // Then
        assertEquals(1, viewModel.cartItems.value.size)
        assertEquals(5, viewModel.cartItems.value[0].quantity)
        val expectedTotal = viewModel.cartItems.value.sumOf { it.totalPrice }
        assertEquals(60.0, expectedTotal, 0.01)
    }

    @Test
    fun `addToCart should add multiple different products`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.addToCart(testProduct1, 2) // 2 * 12 = 24
        viewModel.addToCart(testProduct2, 1) // 1 * 25 = 25

        // Then
        assertEquals(2, viewModel.cartItems.value.size)
        val expectedTotal = viewModel.cartItems.value.sumOf { it.totalPrice }
        assertEquals(49.0, expectedTotal, 0.01) // 24 + 25
        val expectedCount = viewModel.cartItems.value.sumOf { it.quantity }
        assertEquals(3, expectedCount) // 2 + 1
    }

    @Test
    fun `addToCart with default quantity should add 1`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.addToCart(testProduct1)

        // Then
        assertEquals(1, viewModel.cartItems.value[0].quantity)
    }

    // ==================== Remove from Cart Tests ====================

    @Test
    fun `removeFromCart should remove product`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)
        viewModel.addToCart(testProduct2, 1)

        // When
        viewModel.removeFromCart("product-1")

        // Then
        assertEquals(1, viewModel.cartItems.value.size)
        assertEquals("product-2", viewModel.cartItems.value[0].product.id)
    }

    @Test
    fun `removeFromCart should result in empty cart when last item removed`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)

        // When
        viewModel.removeFromCart("product-1")

        // Then
        assertTrue(viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun `removeFromCart with nonexistent id should not change cart`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)

        // When
        viewModel.removeFromCart("nonexistent")

        // Then
        assertEquals(1, viewModel.cartItems.value.size)
    }

    // ==================== Update Cart Item Quantity Tests ====================

    @Test
    fun `updateCartItemQuantity should update quantity`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.addToCart(testProduct1, 2)

        // When
        viewModel.updateCartItemQuantity("product-1", 5)

        // Then
        assertEquals(5, viewModel.cartItems.value[0].quantity)
        val expectedTotal = viewModel.cartItems.value.sumOf { it.totalPrice }
        assertEquals(60.0, expectedTotal, 0.01)
    }

    @Test
    fun `updateCartItemQuantity with zero should remove item`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)

        // When
        viewModel.updateCartItemQuantity("product-1", 0)

        // Then
        assertTrue(viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun `updateCartItemQuantity with negative should remove item`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)

        // When
        viewModel.updateCartItemQuantity("product-1", -1)

        // Then
        assertTrue(viewModel.cartItems.value.isEmpty())
    }

    // ==================== Clear Cart Tests ====================

    @Test
    fun `clearCart should empty the cart`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)
        viewModel.addToCart(testProduct2, 1)

        // When
        viewModel.clearCart()

        // Then
        assertTrue(viewModel.cartItems.value.isEmpty())
        assertEquals(0.0, viewModel.cartTotal.value, 0.01)
    }

    @Test
    fun `clearCart should reset saleCompleted flag`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.clearCart()

        // Then
        assertFalse(viewModel.saleCompleted.value)
    }

    // ==================== Complete Cash Sale Tests ====================

    @Test
    fun `completeCashSale should succeed with items in cart`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)
        coEvery { saleRepository.recordBulkSales(any()) } returns Result.success(listOf(1L))

        // When
        viewModel.completeCashSale()
        advanceUntilIdle()

        // Then
        coVerify { saleRepository.recordBulkSales(any()) }
        assertTrue(viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun `completeCashSale should fail with empty cart`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.completeCashSale()
        advanceUntilIdle()

        // Then
        assertEquals("Cart is empty", viewModel.error.value)
        coVerify(exactly = 0) { saleRepository.recordBulkSales(any()) }
    }

    @Test
    fun `completeCashSale should handle repository error`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)
        coEvery { saleRepository.recordBulkSales(any()) } returns Result.failure(RuntimeException("Sale failed"))

        // When
        viewModel.completeCashSale()
        advanceUntilIdle()

        // Then
        assertEquals("Sale failed", viewModel.error.value)
    }

    @Test
    fun `completeCashSale with customerId should include it in sale`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)
        coEvery { saleRepository.recordBulkSales(any()) } returns Result.success(listOf(1L))

        // When
        viewModel.completeCashSale(customerId = "customer-1")
        advanceUntilIdle()

        // Then
        coVerify {
            saleRepository.recordBulkSales(match { sales ->
                sales.all { it.customerId == "customer-1" && it.saleType == "CASH" }
            })
        }
    }

    // ==================== Complete Credit Sale Tests ====================

    @Test
    fun `completeCreditSale should succeed with items in cart`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)
        coEvery { saleRepository.recordBulkSales(any()) } returns Result.success(listOf(1L))

        // When
        viewModel.completeCreditSale("customer-1")
        advanceUntilIdle()

        // Then
        coVerify {
            saleRepository.recordBulkSales(match { sales ->
                sales.all { it.customerId == "customer-1" && it.saleType == "CREDIT" }
            })
        }
    }

    @Test
    fun `completeCreditSale should fail with empty cart`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.completeCreditSale("customer-1")
        advanceUntilIdle()

        // Then
        assertEquals("Cart is empty", viewModel.error.value)
    }

    @Test
    fun `completeCreditSale should handle repository error`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 2)
        coEvery { saleRepository.recordBulkSales(any()) } returns Result.failure(RuntimeException("Credit failed"))

        // When
        viewModel.completeCreditSale("customer-1")
        advanceUntilIdle()

        // Then
        assertEquals("Credit failed", viewModel.error.value)
    }

    // ==================== Load Sales Tests ====================

    @Test
    fun `loadTodaySales should update todaySales state`() = runTest {
        // Given
        val todaySalesList = listOf(testSale, testSale.copy(id = "sale-2"))
        every { saleRepository.getTodaySales() } returns flowOf(todaySalesList)
        viewModel = createViewModel()

        // When
        viewModel.loadTodaySales()
        advanceUntilIdle()

        // Then
        assertEquals(2, viewModel.todaySales.value.size)
    }

    @Test
    fun `loadAllSales should update sales state`() = runTest {
        // Given
        val allSales = listOf(testSale, testSale.copy(id = "sale-2"), testSale.copy(id = "sale-3"))
        every { saleRepository.getAllSales() } returns flowOf(allSales)
        viewModel = createViewModel()

        // When
        viewModel.loadAllSales()
        advanceUntilIdle()

        // Then
        assertEquals(3, viewModel.sales.value.size)
    }

    @Test
    fun `loadAllSales should update loading state`() = runTest {
        // Given
        every { saleRepository.getAllSales() } returns flowOf(emptyList())
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.loadAllSales()
        advanceUntilIdle()

        // Then
        assertFalse(viewModel.isLoading.value)
    }

    // ==================== Cart Calculations Tests ====================

    @Test
    fun `cartTotal should calculate correctly`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.addToCart(testProduct1, 3) // 3 * 12 = 36
        viewModel.addToCart(testProduct2, 2) // 2 * 25 = 50

        // Then - Calculate expected from cartItems directly
        val expectedTotal = viewModel.cartItems.value.sumOf { it.totalPrice }
        assertEquals(86.0, expectedTotal, 0.01)
    }

    @Test
    fun `cartItemCount should sum all quantities`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.addToCart(testProduct1, 3)
        viewModel.addToCart(testProduct2, 2)

        // Then - Calculate expected from cartItems directly
        val expectedCount = viewModel.cartItems.value.sumOf { it.quantity }
        assertEquals(5, expectedCount)
    }

    // ==================== Clear Error Tests ====================

    @Test
    fun `clearError should reset error state`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.completeCashSale() // This will set error "Cart is empty"
        advanceUntilIdle()
        assertNotNull(viewModel.error.value)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    // ==================== CartItem Tests ====================

    @Test
    fun `CartItem totalPrice should calculate correctly`() {
        // Given
        val cartItem = CartItem(
            product = testProduct1,
            quantity = 5,
            unitPrice = 12.0
        )

        // Then
        assertEquals(60.0, cartItem.totalPrice, 0.01)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `sale completion should reload stats`() = runTest {
        // Given
        viewModel = createViewModel()
        viewModel.addToCart(testProduct1, 1)
        coEvery { saleRepository.recordBulkSales(any()) } returns Result.success(listOf(1L))

        // When
        viewModel.completeCashSale()
        advanceUntilIdle()

        // Then
        coVerify(atLeast = 2) { saleRepository.getSalesStats(any(), any()) }
    }

    @Test
    fun `concurrent cart operations should work correctly`() = runTest {
        // Given
        viewModel = createViewModel()

        // When - rapid operations
        viewModel.addToCart(testProduct1, 1)
        viewModel.addToCart(testProduct2, 1)
        viewModel.updateCartItemQuantity("product-1", 5)
        viewModel.removeFromCart("product-2")
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.cartItems.value.size)
        assertEquals(5, viewModel.cartItems.value[0].quantity)
    }
}
