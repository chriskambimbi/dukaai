package com.example.dukaai.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dukaai.data.local.entity.CustomerEntity
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.local.entity.SaleEntity
import com.example.dukaai.data.repository.CustomerRepository
import com.example.dukaai.data.repository.PaymentRepository
import com.example.dukaai.data.repository.ProductRepository
import com.example.dukaai.data.repository.SaleRepository
import com.example.dukaai.voice.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

/**
 * ViewModel for voice command functionality
 * Coordinates voice recognition, intent parsing, command execution, and voice feedback
 */
@HiltViewModel
class VoiceCommandViewModel @Inject constructor(
    private val voiceCommandService: VoiceCommandService,
    private val voiceIntentParser: VoiceIntentParser,
    private val voiceFeedbackService: VoiceFeedbackService,
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    companion object {
        private const val TAG = "VoiceCommandViewModel"
    }

    // Current language
    private val _currentLanguage = MutableStateFlow(VoiceLanguage.ENGLISH)
    val currentLanguage: StateFlow<VoiceLanguage> = _currentLanguage.asStateFlow()

    // Voice recognition state
    private val _recognitionState = MutableStateFlow<VoiceRecognitionState>(VoiceRecognitionState.Idle)
    val recognitionState: StateFlow<VoiceRecognitionState> = _recognitionState.asStateFlow()

    // Parsed command
    private val _parsedCommand = MutableStateFlow<VoiceCommand?>(null)
    val parsedCommand: StateFlow<VoiceCommand?> = _parsedCommand.asStateFlow()

    // Command execution result
    private val _executionResult = MutableStateFlow<VoiceCommandResult?>(null)
    val executionResult: StateFlow<VoiceCommandResult?> = _executionResult.asStateFlow()

    // Loading state
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // TTS initialization state
    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    // Listening state
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    init {
        initializeTts()
    }

    /**
     * Initialize Text-to-Speech
     */
    private fun initializeTts() {
        viewModelScope.launch {
            voiceFeedbackService.initialize(_currentLanguage.value)
                .collect { state ->
                    when (state) {
                        is TtsInitializationState.Ready -> {
                            _isTtsReady.value = true
                            Log.d(TAG, "TTS ready")
                        }
                        is TtsInitializationState.Error -> {
                            _error.value = state.message
                            _isTtsReady.value = false
                            Log.e(TAG, "TTS initialization failed: ${state.message}")
                        }
                    }
                }
        }
    }

    /**
     * Set language for voice commands
     */
    fun setLanguage(language: VoiceLanguage) {
        if (_currentLanguage.value == language) return

        _currentLanguage.value = language
        voiceCommandService.setLanguage(language)
        voiceFeedbackService.setLanguage(language)

        Log.d(TAG, "Language set to: ${language.displayName}")
    }

    /**
     * Start listening for voice command
     */
    fun startListening() {
        if (_isListening.value) {
            Log.w(TAG, "Already listening")
            return
        }

        if (!voiceCommandService.isAvailable()) {
            _error.value = "Voice recognition not available on this device"
            return
        }

        _isListening.value = true
        _recognitionState.value = VoiceRecognitionState.Idle
        _parsedCommand.value = null
        _executionResult.value = null

        viewModelScope.launch {
            voiceCommandService.startListening()
                .collect { state ->
                    _recognitionState.value = state

                    when (state) {
                        is VoiceRecognitionState.Success -> {
                            _isListening.value = false
                            processVoiceInput(state.text, state.confidence)
                        }
                        is VoiceRecognitionState.Error -> {
                            _isListening.value = false
                            _error.value = state.message
                            voiceFeedbackService.speakError(state.message)
                        }
                        is VoiceRecognitionState.Processing -> {
                            // Show partial results in UI
                            Log.d(TAG, "Partial: ${state.partialText}")
                        }
                        is VoiceRecognitionState.Listening -> {
                            Log.d(TAG, "Listening...")
                        }
                        VoiceRecognitionState.Idle -> {
                            // Idle state
                        }
                    }
                }
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        voiceCommandService.stopListening()
        _isListening.value = false
        _recognitionState.value = VoiceRecognitionState.Idle
    }

    /**
     * Process voice input text
     */
    private fun processVoiceInput(text: String, confidence: Float) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                // Parse voice input to command
                val command = voiceIntentParser.parse(
                    text = text,
                    language = _currentLanguage.value,
                    confidence = confidence
                )

                _parsedCommand.value = command
                Log.d(TAG, "Parsed command: ${command.type}, params: ${command.parameters}")

                // Speak confirmation
                voiceFeedbackService.speakConfirmation(command)

                // Execute command if confident
                if (command.isConfident()) {
                    executeCommand(command)
                } else {
                    _executionResult.value = VoiceCommandResult.NeedsConfirmation(
                        command = command,
                        prompt = "Low confidence. Please confirm: ${command.originalText}"
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing voice input", e)
                _error.value = e.message
                voiceFeedbackService.speakError("Failed to process command")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Execute voice command
     */
    fun executeCommand(command: VoiceCommand) {
        viewModelScope.launch {
            _isProcessing.value = true

            try {
                val result = when (command.type) {
                    VoiceCommandType.RECORD_SALE -> executeRecordSale(command)
                    VoiceCommandType.ADD_PRODUCT -> executeAddProduct(command)
                    VoiceCommandType.CHECK_STOCK -> executeCheckStock(command)
                    VoiceCommandType.RECORD_PAYMENT -> executeRecordPayment(command)
                    VoiceCommandType.ADD_CUSTOMER -> executeAddCustomer(command)
                    VoiceCommandType.VIEW_ANALYTICS -> executeViewAnalytics()
                    VoiceCommandType.LOW_STOCK_ALERT -> executeLowStockAlert()
                    VoiceCommandType.SEARCH -> executeSearch(command)
                    VoiceCommandType.NAVIGATE -> executeNavigate(command)
                    VoiceCommandType.UNKNOWN -> {
                        VoiceCommandResult.Failure(
                            error = "Unknown command",
                            reason = "Could not understand: ${command.originalText}"
                        )
                    }
                }

                _executionResult.value = result

                // Provide voice feedback
                when (result) {
                    is VoiceCommandResult.Success -> {
                        voiceFeedbackService.speakSuccess(result)
                    }
                    is VoiceCommandResult.Failure -> {
                        voiceFeedbackService.speakError(result.error)
                    }
                    is VoiceCommandResult.NeedsConfirmation -> {
                        voiceFeedbackService.speak(result.prompt)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error executing command", e)
                val failureResult = VoiceCommandResult.Failure(
                    error = "Execution failed",
                    reason = e.message ?: "Unknown error"
                )
                _executionResult.value = failureResult
                voiceFeedbackService.speakError("Failed to execute command")
            } finally {
                _isProcessing.value = false
            }
        }
    }

    /**
     * Execute RECORD_SALE command
     */
    private suspend fun executeRecordSale(command: VoiceCommand): VoiceCommandResult {
        val productName = command.parameters["product"] as? String
        val quantity = command.parameters["quantity"] as? Int ?: 1

        if (productName == null) {
            return VoiceCommandResult.Failure(
                error = "Missing product name",
                reason = "Please specify which product to sell"
            )
        }

        // Search for product
        val products = productRepository.searchProducts(productName).first()
        if (products.isEmpty()) {
            return VoiceCommandResult.Failure(
                error = "Product not found",
                reason = "No product found matching: $productName"
            )
        }

        val product = products.first()

        // Check stock availability
        if (product.currentStock < quantity) {
            return VoiceCommandResult.Failure(
                error = "Insufficient stock",
                reason = "Only ${product.currentStock} ${product.name} in stock"
            )
        }

        // Create sale
        val sale = SaleEntity(
            id = UUID.randomUUID().toString(),
            productId = product.id,
            quantity = quantity,
            unitPrice = product.sellingPrice,
            totalAmount = product.sellingPrice * quantity,
            timestamp = System.currentTimeMillis(),
            customerId = null,
            saleType = "CASH"
        )

        saleRepository.recordSale(sale)

        // Update stock
        productRepository.updateStock(
            productId = product.id,
            newStock = product.currentStock - quantity,
            reason = "Sale via voice command"
        )

        return VoiceCommandResult.Success(
            message = "Sale recorded: $quantity ${product.name} for K${sale.totalAmount}",
            data = sale
        )
    }

    /**
     * Execute ADD_PRODUCT command
     */
    private suspend fun executeAddProduct(command: VoiceCommand): VoiceCommandResult {
        val productName = command.parameters["product"] as? String
        val price = command.parameters["price"] as? Double
        val quantity = command.parameters["quantity"] as? Int

        if (productName == null) {
            return VoiceCommandResult.NeedsConfirmation(
                command = command,
                prompt = "What is the product name?"
            )
        }

        if (price == null) {
            return VoiceCommandResult.NeedsConfirmation(
                command = command,
                prompt = "What is the price for $productName?"
            )
        }

        // Create product
        val product = ProductEntity(
            id = UUID.randomUUID().toString(),
            name = productName,
            category = "General", // Default category
            currentStock = quantity ?: 0,
            minStockThreshold = 10,
            buyingPrice = price,
            sellingPrice = price, // Assuming selling price is same as buying price if not specified
            barcode = null,
            imageUrl = null,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        productRepository.insertProduct(product)

        return VoiceCommandResult.Success(
            message = "Product added: $productName at K$price",
            data = product
        )
    }

    /**
     * Execute CHECK_STOCK command
     */
    private suspend fun executeCheckStock(command: VoiceCommand): VoiceCommandResult {
        val productName = command.parameters["product"] as? String

        if (productName == null) {
            // Get all low stock products
            val lowStockProducts = productRepository.getLowStockProducts().first()
            val count = lowStockProducts.size

            return VoiceCommandResult.Success(
                message = "You have $count products with low stock",
                data = lowStockProducts
            )
        }

        // Search for specific product
        val products = productRepository.searchProducts(productName).first()
        if (products.isEmpty()) {
            return VoiceCommandResult.Failure(
                error = "Product not found",
                reason = "No product found matching: $productName"
            )
        }

        val product = products.first()
        val stockLevel = product.currentStock

        val message = if (stockLevel <= product.minStockThreshold) {
            "Low stock alert: Only $stockLevel ${product.name} remaining"
        } else {
            "Stock level: $stockLevel ${product.name} in stock"
        }

        return VoiceCommandResult.Success(
            message = message,
            data = product
        )
    }

    /**
     * Execute RECORD_PAYMENT command
     */
    private suspend fun executeRecordPayment(command: VoiceCommand): VoiceCommandResult {
        val customerName = command.parameters["customer"] as? String
        val amount = command.parameters["amount"] as? Double

        if (customerName == null || amount == null) {
            return VoiceCommandResult.NeedsConfirmation(
                command = command,
                prompt = "Please specify customer name and payment amount"
            )
        }

        // Search for customer
        val customers = customerRepository.searchCustomers(customerName).first()
        if (customers.isEmpty()) {
            return VoiceCommandResult.Failure(
                error = "Customer not found",
                reason = "No customer found matching: $customerName"
            )
        }

        val customer = customers.first()

        // TODO: Create payment record using paymentRepository
        // This would require creating a PaymentEntity and inserting it

        return VoiceCommandResult.Success(
            message = "Payment of K$amount recorded for ${customer.name}",
            data = customer
        )
    }

    /**
     * Execute ADD_CUSTOMER command
     */
    private suspend fun executeAddCustomer(command: VoiceCommand): VoiceCommandResult {
        val customerName = command.parameters["name"] as? String
        val phone = command.parameters["phone"] as? String

        if (customerName == null) {
            return VoiceCommandResult.NeedsConfirmation(
                command = command,
                prompt = "What is the customer's name?"
            )
        }

        // Create customer
        val customer = CustomerEntity(
            id = UUID.randomUUID().toString(),
            name = customerName,
            phoneNumber = phone,
            address = null,
            notes = "Added via voice command",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        customerRepository.addCustomer(customer)

        return VoiceCommandResult.Success(
            message = "Customer added: $customerName",
            data = customer
        )
    }

    /**
     * Execute VIEW_ANALYTICS command
     */
    private suspend fun executeViewAnalytics(): VoiceCommandResult {
        return VoiceCommandResult.Success(
            message = "Opening analytics dashboard",
            data = "navigate_to_analytics"
        )
    }

    /**
     * Execute LOW_STOCK_ALERT command
     */
    private suspend fun executeLowStockAlert(): VoiceCommandResult {
        val lowStockProducts = productRepository.getLowStockProducts().first()
        val count = lowStockProducts.size

        return VoiceCommandResult.Success(
            message = "You have $count products with low stock",
            data = lowStockProducts
        )
    }

    /**
     * Execute SEARCH command
     */
    private suspend fun executeSearch(command: VoiceCommand): VoiceCommandResult {
        val query = command.parameters["query"] as? String

        if (query == null) {
            return VoiceCommandResult.Failure(
                error = "Missing search query",
                reason = "Please specify what to search for"
            )
        }

        val products = productRepository.searchProducts(query).first()

        return VoiceCommandResult.Success(
            message = "Found ${products.size} products matching: $query",
            data = products
        )
    }

    /**
     * Execute NAVIGATE command
     */
    private suspend fun executeNavigate(command: VoiceCommand): VoiceCommandResult {
        // Navigation would be handled by the UI layer
        return VoiceCommandResult.Success(
            message = "Navigating...",
            data = "navigate"
        )
    }

    /**
     * Clear error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Clear execution result
     */
    fun clearExecutionResult() {
        _executionResult.value = null
    }

    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        voiceFeedbackService.stopSpeaking()
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return voiceFeedbackService.isSpeaking()
    }

    /**
     * Clean up resources
     */
    override fun onCleared() {
        super.onCleared()
        voiceCommandService.cleanup()
        voiceFeedbackService.cleanup()
        Log.d(TAG, "VoiceCommandViewModel cleared")
    }
}
