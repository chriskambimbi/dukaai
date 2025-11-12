package com.example.dukaai.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukaai.data.local.entity.CreditLedgerEntity
import com.example.dukaai.data.local.entity.CustomerEntity
import com.example.dukaai.data.local.entity.PaymentEntity
import com.example.dukaai.data.repository.CreditRepository
import com.example.dukaai.data.repository.CreditStats
import com.example.dukaai.data.repository.CustomerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Credit and Customer screens
 */
@HiltViewModel
class CreditViewModel @Inject constructor(
    private val creditRepository: CreditRepository,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    // All credits
    private val _credits = MutableStateFlow<List<CreditLedgerEntity>>(emptyList())
    val credits: StateFlow<List<CreditLedgerEntity>> = _credits.asStateFlow()

    // All customers
    private val _customers = MutableStateFlow<List<CustomerEntity>>(emptyList())
    val customers: StateFlow<List<CustomerEntity>> = _customers.asStateFlow()

    // Selected customer
    private val _selectedCustomer = MutableStateFlow<CustomerEntity?>(null)
    val selectedCustomer: StateFlow<CustomerEntity?> = _selectedCustomer.asStateFlow()

    // Customer credits
    private val _customerCredits = MutableStateFlow<List<CreditLedgerEntity>>(emptyList())
    val customerCredits: StateFlow<List<CreditLedgerEntity>> = _customerCredits.asStateFlow()

    // Credit statistics
    private val _creditStats = MutableStateFlow<CreditStats?>(null)
    val creditStats: StateFlow<CreditStats?> = _creditStats.asStateFlow()

    // Total outstanding credit
    private val _totalOutstanding = MutableStateFlow(0.0)
    val totalOutstanding: StateFlow<Double> = _totalOutstanding.asStateFlow()

    // Filter state
    private val _creditFilter = MutableStateFlow("ALL")
    val creditFilter: StateFlow<String> = _creditFilter.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadCredits()
        loadCustomers()
        loadCreditStats()
    }

    /**
     * Load all credits
     */
    fun loadCredits() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                creditRepository.getAllCredits()
                    .catch { e -> _error.value = e.message }
                    .collect { creditList ->
                        _credits.value = creditList
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Load all customers
     */
    fun loadCustomers() {
        viewModelScope.launch {
            try {
                customerRepository.getAllCustomers()
                    .catch { e -> _error.value = e.message }
                    .collect { customerList ->
                        _customers.value = customerList
                    }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Load customer by ID
     */
    fun loadCustomer(customerId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                customerRepository.getCustomerById(customerId)
                    .catch { e -> _error.value = e.message }
                    .collect { customer ->
                        _selectedCustomer.value = customer
                        _isLoading.value = false
                    }

                // Load customer's credits
                creditRepository.getCreditsByCustomer(customerId)
                    .catch { e -> _error.value = e.message }
                    .collect { credits ->
                        _customerCredits.value = credits
                    }
            } catch (e: Exception) {
                _error.value = e.message
                _isLoading.value = false
            }
        }
    }

    /**
     * Load credit statistics
     */
    private fun loadCreditStats() {
        viewModelScope.launch {
            try {
                val stats = creditRepository.getOverallCreditStats()
                _creditStats.value = stats

                creditRepository.getTotalOutstandingCredit()
                    .catch { e -> _error.value = e.message }
                    .collect { total ->
                        _totalOutstanding.value = total ?: 0.0
                    }
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    /**
     * Add new customer
     */
    fun addCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = customerRepository.addCustomer(customer)
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    loadCustomers()
                },
                onFailure = { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * Record new credit
     */
    fun recordCredit(credit: CreditLedgerEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = creditRepository.recordCredit(credit)
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    loadCredits()
                    loadCreditStats()
                },
                onFailure = { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * Record payment
     */
    fun recordPayment(creditId: String, payment: PaymentEntity) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = creditRepository.recordPayment(creditId, payment)
            result.fold(
                onSuccess = {
                    _isLoading.value = false
                    loadCredits()
                    loadCreditStats()
                    _selectedCustomer.value?.let { customer ->
                        loadCustomer(customer.id)
                    }
                },
                onFailure = { e ->
                    _error.value = e.message
                    _isLoading.value = false
                }
            )
        }
    }

    /**
     * Set credit filter
     */
    fun setFilter(filter: String) {
        _creditFilter.value = filter
    }

    /**
     * Get filtered credits
     */
    val filteredCredits: StateFlow<List<CreditLedgerEntity>> = combine(
        credits,
        creditFilter
    ) { creditList, filter ->
        when (filter) {
            "UNPAID" -> creditList.filter { it.status == "UNPAID" }
            "OVERDUE" -> creditList.filter {
                it.status != "PAID" && it.dueDate != null && it.dueDate!! < System.currentTimeMillis()
            }
            "DUE_SOON" -> {
                val now = System.currentTimeMillis()
                val weekAhead = now + (7 * 24 * 60 * 60 * 1000)
                creditList.filter {
                    it.status != "PAID" && it.dueDate != null &&
                    it.dueDate!! > now && it.dueDate!! <= weekAhead
                }
            }
            "PAID" -> creditList.filter { it.status == "PAID" }
            else -> creditList
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }
}
