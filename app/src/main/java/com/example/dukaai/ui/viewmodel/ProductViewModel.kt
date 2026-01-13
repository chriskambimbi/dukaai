package com.example.dukaai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Product-related screens
 */
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    // State for product list
    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    val products: StateFlow<List<ProductEntity>> = _products.asStateFlow()

    // State for low stock products
    private val _lowStockProducts = MutableStateFlow<List<ProductEntity>>(emptyList())
    val lowStockProducts: StateFlow<List<ProductEntity>> = _lowStockProducts.asStateFlow()

    // State for selected product
    private val _selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedProduct: StateFlow<ProductEntity?> = _selectedProduct.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter state
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        loadProducts()
        loadLowStockProducts()
    }

    /**
     * Load all products
     */
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.getAllProducts()
                    .catch { e ->
                        _error.value = e.message
                    }
                    .collect { productList ->
                        _products.value = productList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Load low stock products
     */
    fun loadLowStockProducts() {
        viewModelScope.launch {
            try {
                productRepository.getLowStockProducts()
                    .catch { e ->
                        _error.value = e.message
                    }
                    .collect { lowStockList ->
                        _lowStockProducts.value = lowStockList
                    }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Load product by ID
     */
    fun loadProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.getProductById(productId)
                    .catch { e ->
                        _error.value = e.message
                    }
                    .collect { product ->
                        _selectedProduct.value = product
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Add new product
     */
    fun addProduct(product: ProductEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.insertProduct(product)
                _isLoading.value = false
                loadProducts()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Update product
     */
    fun updateProduct(product: ProductEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.updateProduct(product)
                _isLoading.value = false
                loadProducts()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Update product stock
     */
    fun updateStock(productId: String, newStock: Int, reason: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.updateStock(productId, newStock, reason)
                _isLoading.value = false
                loadProducts()
                loadLowStockProducts()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete product
     */
    fun deleteProduct(product: ProductEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                productRepository.deleteProduct(product)
                _isLoading.value = false
                loadProducts()
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Search products
     */
    fun searchProducts(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadProducts()
            } else {
                productRepository.searchProducts(query)
                    .catch { e ->
                        _error.value = e.message
                    }
                    .collect { searchResults ->
                        _products.value = searchResults
                    }
            }
        }
    }

    /**
     * Filter by category
     */
    fun filterByCategory(category: String?) {
        _selectedCategory.value = category
        viewModelScope.launch {
            if (category == null) {
                loadProducts()
            } else {
                productRepository.getProductsByCategory(category)
                    .catch { e ->
                        _error.value = e.message
                    }
                    .collect { filteredProducts ->
                        _products.value = filteredProducts
                    }
            }
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Get filtered products (combining search and category filter)
     * Uses Dispatchers.Default to avoid blocking UI thread during filtering
     */
    val filteredProducts: StateFlow<List<ProductEntity>> = combine(
        products,
        searchQuery,
        selectedCategory
    ) { productList, query, category ->
        var filtered = productList

        // Apply category filter
        if (category != null) {
            filtered = filtered.filter { it.category == category }
        }

        // Apply search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.barcode?.contains(query, ignoreCase = true) == true ||
                it.category.contains(query, ignoreCase = true)
            }
        }

        filtered
    }
        .flowOn(Dispatchers.Default) // Perform filtering on background thread
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
