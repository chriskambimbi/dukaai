package com.example.dukaai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.local.entity.SaleEntity
import com.example.dukaai.data.repository.ProductRepository
import com.example.dukaai.data.repository.SaleRepository
import com.example.dukaai.data.repository.SalesStats
import com.example.dukaai.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Cart item for sale screen
 */
data class CartItem(
    val product: ProductEntity,
    val quantity: Int,
    val unitPrice: Double
) {
    val totalPrice: Double get() = quantity * unitPrice
}

/**
 * ViewModel for Sale-related screens
 */
@HiltViewModel
class SaleViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    // Cart items
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Sales list
    private val _sales = MutableStateFlow<List<SaleEntity>>(emptyList())
    val sales: StateFlow<List<SaleEntity>> = _sales.asStateFlow()

    // Today's sales
    private val _todaySales = MutableStateFlow<List<SaleEntity>>(emptyList())
    val todaySales: StateFlow<List<SaleEntity>> = _todaySales.asStateFlow()

    // Sales statistics
    private val _salesStats = MutableStateFlow<SalesStats?>(null)
    val salesStats: StateFlow<SalesStats?> = _salesStats.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Success state (for showing confirmation)
    private val _saleCompleted = MutableStateFlow(false)
    val saleCompleted: StateFlow<Boolean> = _saleCompleted.asStateFlow()

    // Cart total
    val cartTotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Cart items count
    val cartItemCount: StateFlow<Int> = _cartItems.map { items ->
        items.sumOf { it.quantity }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    init {
        loadTodaySales()
        loadSalesStats()
    }

    /**
     * Add product to cart
     */
    fun addToCart(product: ProductEntity, quantity: Int = 1) {
        val currentCart = _cartItems.value.toMutableList()

        // Check if product already in cart
        val existingIndex = currentCart.indexOfFirst { it.product.id == product.id }

        if (existingIndex >= 0) {
            // Update quantity
            val existingItem = currentCart[existingIndex]
            currentCart[existingIndex] = existingItem.copy(
                quantity = existingItem.quantity + quantity
            )
        } else {
            // Add new item
            currentCart.add(
                CartItem(
                    product = product,
                    quantity = quantity,
                    unitPrice = product.sellingPrice
                )
            )
        }

        _cartItems.value = currentCart
    }

    /**
     * Remove product from cart
     */
    fun removeFromCart(productId: String) {
        _cartItems.value = _cartItems.value.filter { it.product.id != productId }
    }

    /**
     * Update cart item quantity
     */
    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }

        _cartItems.value = _cartItems.value.map { item ->
            if (item.product.id == productId) {
                item.copy(quantity = newQuantity)
            } else {
                item
            }
        }
    }

    /**
     * Clear cart
     */
    fun clearCart() {
        _cartItems.value = emptyList()
        _saleCompleted.value = false
    }

    /**
     * Complete sale (cash)
     */
    fun completeCashSale(customerId: String? = null) {
        viewModelScope.launch {
            if (_cartItems.value.isEmpty()) {
                _error.value = "Cart is empty"
                return@launch
            }

            _isLoading.value = true

            try {
                val sales = _cartItems.value.map { cartItem ->
                    SaleEntity(
                        id = UUID.randomUUID().toString(),
                        productId = cartItem.product.id,
                        customerId = customerId,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        totalAmount = cartItem.totalPrice,
                        saleType = "CASH",
                        timestamp = System.currentTimeMillis()
                    )
                }

                val result = saleRepository.recordBulkSales(sales)
                result.fold(
                    onSuccess = {
                        _isLoading.value = false
                        _saleCompleted.value = true
                        clearCart()
                        loadTodaySales()
                        loadSalesStats()
                    },
                    onFailure = { e ->
                        _error.value = e.message
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Complete sale (credit)
     */
    fun completeCreditSale(customerId: String) {
        viewModelScope.launch {
            if (_cartItems.value.isEmpty()) {
                _error.value = "Cart is empty"
                return@launch
            }

            _isLoading.value = true

            try {
                val sales = _cartItems.value.map { cartItem ->
                    SaleEntity(
                        id = UUID.randomUUID().toString(),
                        productId = cartItem.product.id,
                        customerId = customerId,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice,
                        totalAmount = cartItem.totalPrice,
                        saleType = "CREDIT",
                        timestamp = System.currentTimeMillis()
                    )
                }

                val result = saleRepository.recordBulkSales(sales)
                result.fold(
                    onSuccess = {
                        _isLoading.value = false
                        _saleCompleted.value = true
                        clearCart()
                        loadTodaySales()
                        loadSalesStats()
                    },
                    onFailure = { e ->
                        _error.value = e.message
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Load today's sales
     */
    fun loadTodaySales() {
        viewModelScope.launch {
            try {
                saleRepository.getTodaySales()
                    .catch { e ->
                        _error.value = e.message
                    }
                    .collect { salesList ->
                        _todaySales.value = salesList
                    }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Load sales statistics
     */
    private fun loadSalesStats() {
        viewModelScope.launch {
            try {
                val startOfDay = DateUtils.getStartOfDay()
                val endOfDay = DateUtils.getEndOfDay()
                val stats = saleRepository.getSalesStats(startOfDay, endOfDay)
                _salesStats.value = stats
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Load all sales
     */
    fun loadAllSales() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                saleRepository.getAllSales()
                    .catch { e ->
                        _error.value = e.message
                    }
                    .collect { salesList ->
                        _sales.value = salesList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}
