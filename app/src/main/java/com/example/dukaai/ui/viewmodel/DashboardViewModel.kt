package com.example.dukaai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.local.entity.SaleEntity
import com.example.dukaai.data.repository.CreditRepository
import com.example.dukaai.data.repository.ProductRepository
import com.example.dukaai.data.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Dashboard screen
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val creditRepository: CreditRepository
) : ViewModel() {

    // Today's revenue
    private val _todayRevenue = MutableStateFlow(0.0)
    val todayRevenue: StateFlow<Double> = _todayRevenue.asStateFlow()

    // Total stock count
    private val _totalStockCount = MutableStateFlow(0)
    val totalStockCount: StateFlow<Int> = _totalStockCount.asStateFlow()

    // Low stock count
    private val _lowStockCount = MutableStateFlow(0)
    val lowStockCount: StateFlow<Int> = _lowStockCount.asStateFlow()

    // Pending credit
    private val _pendingCredit = MutableStateFlow(0.0)
    val pendingCredit: StateFlow<Double> = _pendingCredit.asStateFlow()

    // Recent sales
    private val _recentSales = MutableStateFlow<List<SaleEntity>>(emptyList())
    val recentSales: StateFlow<List<SaleEntity>> = _recentSales.asStateFlow()

    // Low stock products
    private val _lowStockProducts = MutableStateFlow<List<ProductEntity>>(emptyList())
    val lowStockProducts: StateFlow<List<ProductEntity>> = _lowStockProducts.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadDashboardData()
    }

    /**
     * Load all dashboard data
     */
    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Load today's revenue
                saleRepository.getTodayRevenue()
                    .catch { e -> _error.value = e.message }
                    .collect { revenue ->
                        _todayRevenue.value = revenue ?: 0.0
                    }

                // Load total stock count
                productRepository.getAllProducts()
                    .catch { e -> _error.value = e.message }
                    .collect { products ->
                        _totalStockCount.value = products.sumOf { it.currentStock }
                    }

                // Load low stock products
                productRepository.getLowStockProducts()
                    .catch { e -> _error.value = e.message }
                    .collect { lowStock ->
                        _lowStockProducts.value = lowStock
                        _lowStockCount.value = lowStock.size
                    }

                // Load pending credit
                creditRepository.getTotalOutstandingCredit()
                    .catch { e -> _error.value = e.message }
                    .collect { credit ->
                        _pendingCredit.value = credit ?: 0.0
                    }

                // Load recent sales
                saleRepository.getTodaySales()
                    .catch { e -> _error.value = e.message }
                    .collect { sales ->
                        _recentSales.value = sales.take(10) // Last 10 sales
                    }

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh dashboard
     */
    fun refresh() {
        loadDashboardData()
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}
