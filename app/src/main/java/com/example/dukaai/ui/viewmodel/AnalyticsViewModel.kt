package com.example.dukaai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukaai.data.local.entity.SaleEntity
import com.example.dukaai.data.repository.SaleRepository
import com.example.dukaai.data.repository.SalesStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Time period for analytics
 */
enum class TimePeriod {
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    CUSTOM
}

/**
 * ViewModel for Analytics screen
 */
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

    // Selected time period
    private val _selectedPeriod = MutableStateFlow(TimePeriod.THIS_WEEK)
    val selectedPeriod: StateFlow<TimePeriod> = _selectedPeriod.asStateFlow()

    // Sales for selected period
    private val _sales = MutableStateFlow<List<SaleEntity>>(emptyList())
    val sales: StateFlow<List<SaleEntity>> = _sales.asStateFlow()

    // Sales statistics
    private val _salesStats = MutableStateFlow<SalesStats?>(null)
    val salesStats: StateFlow<SalesStats?> = _salesStats.asStateFlow()

    // Total revenue
    private val _totalRevenue = MutableStateFlow(0.0)
    val totalRevenue: StateFlow<Double> = _totalRevenue.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadAnalytics()
    }

    /**
     * Load analytics data
     */
    fun loadAnalytics() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val (startDate, endDate) = getDateRangeForPeriod(_selectedPeriod.value)

                // Load sales
                saleRepository.getSalesByDate(startDate, endDate)
                    .catch { e -> _error.value = e.message }
                    .collect { salesList ->
                        _sales.value = salesList
                    }

                // Load sales stats
                val stats = saleRepository.getSalesStats(startDate, endDate)
                _salesStats.value = stats

                // Calculate revenue from stats
                _totalRevenue.value = stats.totalRevenue

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Set time period
     */
    fun setPeriod(period: TimePeriod) {
        _selectedPeriod.value = period
        loadAnalytics()
    }

    /**
     * Load analytics for custom date range
     */
    fun loadCustomPeriod(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _selectedPeriod.value = TimePeriod.CUSTOM
            try {
                saleRepository.getSalesByDate(startDate, endDate)
                    .catch { e -> _error.value = e.message }
                    .collect { salesList ->
                        _sales.value = salesList
                    }

                val stats = saleRepository.getSalesStats(startDate, endDate)
                _salesStats.value = stats

                // Calculate revenue from stats
                _totalRevenue.value = stats.totalRevenue

                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Get top selling products
     */
    fun getTopProducts(limit: Int = 10): List<ProductSalesInfo> {
        val productSales = _sales.value.groupBy { it.productId }
            .mapValues { (_, sales) ->
                ProductSalesInfo(
                    productId = sales.first().productId,
                    totalQuantity = sales.sumOf { it.quantity },
                    totalRevenue = sales.sumOf { it.totalAmount },
                    salesCount = sales.size
                )
            }
            .values
            .sortedByDescending { it.totalRevenue }
            .take(limit)

        return productSales
    }

    /**
     * Get daily sales data for chart
     */
    fun getDailySalesData(): List<DailySales> {
        val calendar = Calendar.getInstance()
        val dailySalesMap = _sales.value.groupBy { sale ->
            calendar.timeInMillis = sale.timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.map { (date, sales) ->
            DailySales(
                date = date,
                totalSales = sales.size,
                totalRevenue = sales.sumOf { sale -> sale.totalAmount }
            )
        }.sortedBy { it.date }

        return dailySalesMap
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    private fun getDateRangeForPeriod(period: TimePeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis

        return when (period) {
            TimePeriod.THIS_WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                Pair(calendar.timeInMillis, endDate)
            }
            TimePeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                Pair(calendar.timeInMillis, endDate)
            }
            TimePeriod.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                Pair(calendar.timeInMillis, endDate)
            }
            TimePeriod.CUSTOM -> {
                // Return default to this month for custom (will be overridden)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                Pair(calendar.timeInMillis, endDate)
            }
        }
    }
}

/**
 * Product sales information
 */
data class ProductSalesInfo(
    val productId: String,
    val totalQuantity: Int,
    val totalRevenue: Double,
    val salesCount: Int
)

/**
 * Daily sales data
 */
data class DailySales(
    val date: Long,
    val totalSales: Int,
    val totalRevenue: Double
)
