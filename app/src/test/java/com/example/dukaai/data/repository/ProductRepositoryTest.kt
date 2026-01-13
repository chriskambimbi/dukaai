package com.example.dukaai.data.repository

import com.example.dukaai.data.local.dao.InventoryLogDao
import com.example.dukaai.data.local.dao.ProductDao
import com.example.dukaai.data.local.entity.ProductEntity
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ProductRepository
 * Tests CRUD operations, barcode lookup, and stock management
 */
class ProductRepositoryTest {

    @MockK
    private lateinit var productDao: ProductDao

    @MockK
    private lateinit var inventoryLogDao: InventoryLogDao

    private lateinit var productRepository: ProductRepository

    private val testProduct = ProductEntity(
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
        name = "Bread Loaf",
        category = "Bakery",
        currentStock = 20,
        minStockThreshold = 5,
        buyingPrice = 15.0,
        sellingPrice = 25.0,
        barcode = "0987654321"
    )

    private val lowStockProduct = ProductEntity(
        id = "product-3",
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
        productRepository = ProductRepository(productDao, inventoryLogDao)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ==================== Get All Products Tests ====================

    @Test
    fun `getAllProducts should return all products`() = runTest {
        // Given
        val products = listOf(testProduct, testProduct2)
        every { productDao.getAllProducts() } returns flowOf(products)

        // When
        val result = productRepository.getAllProducts()

        // Then
        result.collect { list ->
            assertEquals(2, list.size)
            assertEquals("Coca Cola 500ml", list[0].name)
            assertEquals("Bread Loaf", list[1].name)
        }
    }

    @Test
    fun `getAllProducts should return empty list when no products`() = runTest {
        // Given
        every { productDao.getAllProducts() } returns flowOf(emptyList())

        // When
        val result = productRepository.getAllProducts()

        // Then
        result.collect { list ->
            assertTrue(list.isEmpty())
        }
    }

    // ==================== Get Product By ID Tests ====================

    @Test
    fun `getProductById should return product when exists`() = runTest {
        // Given
        every { productDao.getProductById("product-1") } returns flowOf(testProduct)

        // When
        val result = productRepository.getProductById("product-1")

        // Then
        result.collect { product ->
            assertNotNull(product)
            assertEquals("Coca Cola 500ml", product?.name)
            assertEquals("Beverages", product?.category)
        }
    }

    @Test
    fun `getProductById should return null when not exists`() = runTest {
        // Given
        every { productDao.getProductById("nonexistent") } returns flowOf(null)

        // When
        val result = productRepository.getProductById("nonexistent")

        // Then
        result.collect { product ->
            assertNull(product)
        }
    }

    // ==================== Get Products By Category Tests ====================

    @Test
    fun `getProductsByCategory should return filtered products`() = runTest {
        // Given
        val beverages = listOf(testProduct)
        every { productDao.getProductsByCategory("Beverages") } returns flowOf(beverages)

        // When
        val result = productRepository.getProductsByCategory("Beverages")

        // Then
        result.collect { list ->
            assertEquals(1, list.size)
            assertEquals("Beverages", list[0].category)
        }
    }

    @Test
    fun `getProductsByCategory should return empty list for unknown category`() = runTest {
        // Given
        every { productDao.getProductsByCategory("Unknown") } returns flowOf(emptyList())

        // When
        val result = productRepository.getProductsByCategory("Unknown")

        // Then
        result.collect { list ->
            assertTrue(list.isEmpty())
        }
    }

    // ==================== Low Stock Products Tests ====================

    @Test
    fun `getLowStockProducts should return products below threshold`() = runTest {
        // Given
        val lowStockList = listOf(lowStockProduct)
        every { productDao.getLowStockProducts() } returns flowOf(lowStockList)

        // When
        val result = productRepository.getLowStockProducts()

        // Then
        result.collect { list ->
            assertEquals(1, list.size)
            assertTrue(list[0].currentStock < list[0].minStockThreshold)
        }
    }

    // ==================== Out of Stock Products Tests ====================

    @Test
    fun `getOutOfStockProducts should return products with zero stock`() = runTest {
        // Given
        val outOfStockProduct = testProduct.copy(currentStock = 0)
        every { productDao.getOutOfStockProducts() } returns flowOf(listOf(outOfStockProduct))

        // When
        val result = productRepository.getOutOfStockProducts()

        // Then
        result.collect { list ->
            assertEquals(1, list.size)
            assertEquals(0, list[0].currentStock)
        }
    }

    // ==================== Search Products Tests ====================

    @Test
    fun `searchProducts should return matching products by name`() = runTest {
        // Given
        every { productDao.searchProducts(any()) } returns flowOf(listOf(testProduct))

        // When
        val result = productRepository.searchProducts("Cola")

        // Then
        result.collect { list ->
            assertEquals(1, list.size)
            assertTrue(list[0].name.contains("Cola", ignoreCase = true))
        }
    }

    @Test
    fun `searchProducts should return empty list for no match`() = runTest {
        // Given
        every { productDao.searchProducts(any()) } returns flowOf(emptyList())

        // When
        val result = productRepository.searchProducts("XYZ123")

        // Then
        result.collect { list ->
            assertTrue(list.isEmpty())
        }
    }

    // ==================== Get All Categories Tests ====================

    @Test
    fun `getAllCategories should return distinct categories`() = runTest {
        // Given
        val categories = listOf("Beverages", "Bakery", "Dairy")
        every { productDao.getAllCategories() } returns flowOf(categories)

        // When
        val result = productRepository.getAllCategories()

        // Then
        result.collect { list ->
            assertEquals(3, list.size)
            assertTrue(list.contains("Beverages"))
            assertTrue(list.contains("Bakery"))
            assertTrue(list.contains("Dairy"))
        }
    }

    // ==================== Get Product By Barcode Tests ====================

    @Test
    fun `getProductByBarcode should return product when barcode exists`() = runTest {
        // Given
        coEvery { productDao.getProductByBarcode("1234567890") } returns testProduct

        // When
        val result = productRepository.getProductByBarcode("1234567890")

        // Then
        assertNotNull(result)
        assertEquals("Coca Cola 500ml", result?.name)
        assertEquals("1234567890", result?.barcode)
    }

    @Test
    fun `getProductByBarcode should return null for unknown barcode`() = runTest {
        // Given
        coEvery { productDao.getProductByBarcode("unknown") } returns null

        // When
        val result = productRepository.getProductByBarcode("unknown")

        // Then
        assertNull(result)
    }

    // ==================== Insert Product Tests ====================

    @Test
    fun `insertProduct should insert and return row id`() = runTest {
        // Given
        coEvery { productDao.insertProduct(any()) } returns 1L

        // When
        val result = productRepository.insertProduct(testProduct)

        // Then
        assertEquals(1L, result)
        coVerify { productDao.insertProduct(testProduct) }
    }

    @Test
    fun `insertProduct should handle duplicate product`() = runTest {
        // Given
        coEvery { productDao.insertProduct(any()) } returns -1L // SQLite returns -1 on conflict with IGNORE

        // When
        val result = productRepository.insertProduct(testProduct)

        // Then
        assertEquals(-1L, result)
    }

    // ==================== Update Product Tests ====================

    @Test
    fun `updateProduct should update product successfully`() = runTest {
        // Given
        coEvery { productDao.updateProduct(any()) } just Runs
        val updatedProduct = testProduct.copy(sellingPrice = 15.0)

        // When
        productRepository.updateProduct(updatedProduct)

        // Then
        coVerify { productDao.updateProduct(updatedProduct) }
    }

    // ==================== Delete Product Tests ====================

    @Test
    fun `deleteProduct should delete product successfully`() = runTest {
        // Given
        coEvery { productDao.deleteProduct(any()) } just Runs

        // When
        productRepository.deleteProduct(testProduct)

        // Then
        coVerify { productDao.deleteProduct(testProduct) }
    }

    @Test
    fun `deleteProductById should delete by id`() = runTest {
        // Given
        coEvery { productDao.deleteProductById(any()) } just Runs

        // When
        productRepository.deleteProductById("product-1")

        // Then
        coVerify { productDao.deleteProductById("product-1") }
    }

    // ==================== Edge Cases ====================

    @Test
    fun `product with null barcode should be handled correctly`() = runTest {
        // Given
        val productWithoutBarcode = testProduct.copy(barcode = null)
        coEvery { productDao.insertProduct(any()) } returns 1L

        // When
        val result = productRepository.insertProduct(productWithoutBarcode)

        // Then
        assertEquals(1L, result)
        coVerify { productDao.insertProduct(productWithoutBarcode) }
    }

    @Test
    fun `product with zero stock should not be low stock if threshold is zero`() = runTest {
        // Given - product with 0 threshold means it's not tracked for low stock
        val noThresholdProduct = testProduct.copy(currentStock = 0, minStockThreshold = 0)
        every { productDao.getLowStockProducts() } returns flowOf(emptyList())
        every { productDao.getOutOfStockProducts() } returns flowOf(listOf(noThresholdProduct))

        // When
        val lowStock = productRepository.getLowStockProducts()
        val outOfStock = productRepository.getOutOfStockProducts()

        // Then
        lowStock.collect { list -> assertTrue(list.isEmpty()) }
        outOfStock.collect { list -> assertEquals(1, list.size) }
    }
}
