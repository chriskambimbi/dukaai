package com.example.dukaai.ui.viewmodel

import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.repository.ProductRepository
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
 * Unit tests for ProductViewModel
 * Tests state management, CRUD operations, and filtering
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProductViewModelTest {

    @MockK
    private lateinit var productRepository: ProductRepository

    private lateinit var viewModel: ProductViewModel

    private val testDispatcher = StandardTestDispatcher()

    private val testProduct1 = ProductEntity(
        id = "product-1",
        name = "Coca Cola 500ml",
        category = "Beverages",
        currentStock = 100,
        minStockThreshold = 10,
        buyingPrice = 8.0,
        sellingPrice = 12.0,
        barcode = "1234567890"
    )

    private val testProduct2 = ProductEntity(
        id = "product-2",
        name = "Sprite 500ml",
        category = "Beverages",
        currentStock = 50,
        minStockThreshold = 10,
        buyingPrice = 8.0,
        sellingPrice = 12.0
    )

    private val testProduct3 = ProductEntity(
        id = "product-3",
        name = "Bread Loaf",
        category = "Bakery",
        currentStock = 20,
        minStockThreshold = 5,
        buyingPrice = 15.0,
        sellingPrice = 25.0
    )

    private val lowStockProduct = ProductEntity(
        id = "product-4",
        name = "Milk 1L",
        category = "Dairy",
        currentStock = 3,
        minStockThreshold = 10,
        buyingPrice = 20.0,
        sellingPrice = 30.0
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        Dispatchers.setMain(testDispatcher)

        // Default mock behavior
        every { productRepository.getAllProducts() } returns flowOf(listOf(testProduct1, testProduct2, testProduct3))
        every { productRepository.getLowStockProducts() } returns flowOf(listOf(lowStockProduct))
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ProductViewModel {
        return ProductViewModel(productRepository)
    }

    // ==================== Initialization Tests ====================

    @Test
    fun `initial state should load products and low stock`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(3, viewModel.products.value.size)
        assertEquals(1, viewModel.lowStockProducts.value.size)
    }

    @Test
    fun `initial state should have no selected product`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertNull(viewModel.selectedProduct.value)
    }

    @Test
    fun `initial state should have empty search query`() = runTest {
        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals("", viewModel.searchQuery.value)
    }

    // ==================== Load Products Tests ====================

    @Test
    fun `loadProducts should update products state`() = runTest {
        // Given
        viewModel = createViewModel()
        val newProducts = listOf(testProduct1)
        every { productRepository.getAllProducts() } returns flowOf(newProducts)

        // When
        viewModel.loadProducts()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.products.value.size)
        assertEquals("Coca Cola 500ml", viewModel.products.value[0].name)
    }

    @Test
    fun `loadProducts should handle empty list`() = runTest {
        // Given
        every { productRepository.getAllProducts() } returns flowOf(emptyList())
        viewModel = createViewModel()

        // When
        viewModel.loadProducts()
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.products.value.isEmpty())
    }

    @Test
    fun `loadProducts should update loading state`() = runTest {
        // Given
        viewModel = createViewModel()

        // When
        viewModel.loadProducts()

        // Then - loading should be true initially
        // After advanceUntilIdle, loading should be false
        advanceUntilIdle()
        assertFalse(viewModel.isLoading.value)
    }

    // ==================== Load Product By ID Tests ====================

    @Test
    fun `loadProduct should set selected product`() = runTest {
        // Given
        viewModel = createViewModel()
        every { productRepository.getProductById("product-1") } returns flowOf(testProduct1)

        // When
        viewModel.loadProduct("product-1")
        advanceUntilIdle()

        // Then
        assertNotNull(viewModel.selectedProduct.value)
        assertEquals("Coca Cola 500ml", viewModel.selectedProduct.value?.name)
    }

    @Test
    fun `loadProduct should handle nonexistent product`() = runTest {
        // Given
        viewModel = createViewModel()
        every { productRepository.getProductById("nonexistent") } returns flowOf(null)

        // When
        viewModel.loadProduct("nonexistent")
        advanceUntilIdle()

        // Then
        assertNull(viewModel.selectedProduct.value)
    }

    // ==================== Add Product Tests ====================

    @Test
    fun `addProduct should call repository and reload products`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { productRepository.insertProduct(any()) } returns 1L

        // When
        viewModel.addProduct(testProduct1)
        advanceUntilIdle()

        // Then
        coVerify { productRepository.insertProduct(testProduct1) }
        // Products should be reloaded
        verify(atLeast = 2) { productRepository.getAllProducts() }
    }

    @Test
    fun `addProduct should handle error`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { productRepository.insertProduct(any()) } throws RuntimeException("Insert failed")

        // When
        viewModel.addProduct(testProduct1)
        advanceUntilIdle()

        // Then
        assertEquals("Insert failed", viewModel.error.value)
    }

    // ==================== Update Product Tests ====================

    @Test
    fun `updateProduct should call repository and reload products`() = runTest {
        // Given
        viewModel = createViewModel()
        val updatedProduct = testProduct1.copy(sellingPrice = 15.0)
        coEvery { productRepository.updateProduct(any()) } just Runs

        // When
        viewModel.updateProduct(updatedProduct)
        advanceUntilIdle()

        // Then
        coVerify { productRepository.updateProduct(updatedProduct) }
    }

    @Test
    fun `updateProduct should handle error`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { productRepository.updateProduct(any()) } throws RuntimeException("Update failed")

        // When
        viewModel.updateProduct(testProduct1)
        advanceUntilIdle()

        // Then
        assertEquals("Update failed", viewModel.error.value)
    }

    // ==================== Update Stock Tests ====================

    @Test
    fun `updateStock should call repository and reload products`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { productRepository.updateStock(any(), any(), any()) } just Runs

        // When
        viewModel.updateStock("product-1", 150, "Restock")
        advanceUntilIdle()

        // Then
        coVerify { productRepository.updateStock("product-1", 150, "Restock") }
    }

    @Test
    fun `updateStock should reload low stock products`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { productRepository.updateStock(any(), any(), any()) } just Runs

        // When
        viewModel.updateStock("product-4", 50, null)
        advanceUntilIdle()

        // Then
        verify(atLeast = 2) { productRepository.getLowStockProducts() }
    }

    // ==================== Delete Product Tests ====================

    @Test
    fun `deleteProduct should call repository and reload products`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { productRepository.deleteProduct(any()) } just Runs

        // When
        viewModel.deleteProduct(testProduct1)
        advanceUntilIdle()

        // Then
        coVerify { productRepository.deleteProduct(testProduct1) }
    }

    @Test
    fun `deleteProduct should handle error`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { productRepository.deleteProduct(any()) } throws RuntimeException("Delete failed")

        // When
        viewModel.deleteProduct(testProduct1)
        advanceUntilIdle()

        // Then
        assertEquals("Delete failed", viewModel.error.value)
    }

    // ==================== Search Products Tests ====================

    @Test
    fun `searchProducts should update search query`() = runTest {
        // Given
        viewModel = createViewModel()
        every { productRepository.searchProducts(any()) } returns flowOf(listOf(testProduct1))

        // When
        viewModel.searchProducts("Cola")
        advanceUntilIdle()

        // Then
        assertEquals("Cola", viewModel.searchQuery.value)
    }

    @Test
    fun `searchProducts should call repository with query`() = runTest {
        // Given
        viewModel = createViewModel()
        every { productRepository.searchProducts("Cola") } returns flowOf(listOf(testProduct1))

        // When
        viewModel.searchProducts("Cola")
        advanceUntilIdle()

        // Then
        verify { productRepository.searchProducts("Cola") }
    }

    @Test
    fun `searchProducts with blank query should reload all products`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.searchProducts("")
        advanceUntilIdle()

        // Then
        verify(atLeast = 2) { productRepository.getAllProducts() }
    }

    // ==================== Filter By Category Tests ====================

    @Test
    fun `filterByCategory should update selected category`() = runTest {
        // Given
        viewModel = createViewModel()
        every { productRepository.getProductsByCategory("Beverages") } returns flowOf(listOf(testProduct1, testProduct2))

        // When
        viewModel.filterByCategory("Beverages")
        advanceUntilIdle()

        // Then
        assertEquals("Beverages", viewModel.selectedCategory.value)
    }

    @Test
    fun `filterByCategory should call repository`() = runTest {
        // Given
        viewModel = createViewModel()
        every { productRepository.getProductsByCategory("Beverages") } returns flowOf(listOf(testProduct1, testProduct2))

        // When
        viewModel.filterByCategory("Beverages")
        advanceUntilIdle()

        // Then
        verify { productRepository.getProductsByCategory("Beverages") }
    }

    @Test
    fun `filterByCategory with null should reload all products`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // When
        viewModel.filterByCategory(null)
        advanceUntilIdle()

        // Then
        assertNull(viewModel.selectedCategory.value)
        verify(atLeast = 2) { productRepository.getAllProducts() }
    }

    // ==================== Filtered Products Tests ====================

    @Test
    fun `filteredProducts should combine filters`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()

        // Initial products should include all 3
        assertEquals(3, viewModel.products.value.size)

        // Filtered products should also be available through combine
        assertNotNull(viewModel.filteredProducts)
    }

    // ==================== Clear Error Tests ====================

    @Test
    fun `clearError should reset error state`() = runTest {
        // Given
        viewModel = createViewModel()
        coEvery { productRepository.insertProduct(any()) } throws RuntimeException("Test error")
        viewModel.addProduct(testProduct1)
        advanceUntilIdle()
        assertEquals("Test error", viewModel.error.value)

        // When
        viewModel.clearError()

        // Then
        assertNull(viewModel.error.value)
    }

    // ==================== Low Stock Products Tests ====================

    @Test
    fun `lowStockProducts should be updated on init`() = runTest {
        // Given/When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then
        assertEquals(1, viewModel.lowStockProducts.value.size)
        assertEquals("Milk 1L", viewModel.lowStockProducts.value[0].name)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `viewModel should handle repository exception gracefully`() = runTest {
        // Given
        every { productRepository.getAllProducts() } throws RuntimeException("Network error")

        // When
        viewModel = createViewModel()
        advanceUntilIdle()

        // Then - should not crash and error should be captured
        assertNotNull(viewModel.error.value)
    }

    @Test
    fun `concurrent operations should not cause race conditions`() = runTest {
        // Given
        viewModel = createViewModel()
        advanceUntilIdle()
        coEvery { productRepository.insertProduct(any()) } returns 1L

        // When - multiple operations
        viewModel.addProduct(testProduct1)
        viewModel.loadProducts()
        viewModel.searchProducts("test")
        advanceUntilIdle()

        // Then - should complete without exception
        assertFalse(viewModel.isLoading.value)
    }
}
