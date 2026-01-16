package com.example.dukaai.ml.functiongemma

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * High-level service for FunctionGemma-based natural language command processing.
 *
 * This service provides a simple interface for:
 * 1. Converting natural language to function calls
 * 2. Executing those function calls against DukaAI repositories
 * 3. Generating user-friendly responses
 *
 * Architecture:
 * User Input → FunctionGemmaService → FunctionGemmaInference → FunctionGemmaParser → DukaFunctionExecutor → Repositories
 */
@Singleton
class FunctionGemmaService @Inject constructor(
    private val inference: FunctionGemmaInference,
    private val parser: FunctionGemmaParser,
    private val executor: DukaFunctionExecutor
) {

    /**
     * Process a natural language command and execute the appropriate function(s).
     *
     * @param userInput The user's natural language input (voice or text)
     * @return Result containing the execution results or error
     */
    suspend fun processCommand(userInput: String): ProcessingResult = withContext(Dispatchers.Default) {
        try {
            // Step 1: Initialize the model if needed
            if (!inference.isReady()) {
                inference.initialize().getOrElse {
                    // If TFLite model not available, fall back to pattern-based parsing
                    return@withContext processWithFallback(userInput)
                }
            }

            // Step 2: Generate function call(s) from the model
            val modelOutput = inference.generateFunctionCall(userInput).getOrElse {
                // Fall back if inference fails
                return@withContext processWithFallback(userInput)
            }

            // Step 3: Parse the function call(s)
            val functionCalls = parser.parse(modelOutput)

            if (functionCalls.isEmpty()) {
                return@withContext ProcessingResult.NoFunctionDetected(
                    userInput = userInput,
                    message = "I couldn't understand that command. Try saying something like 'sell 3 Coca-Cola' or 'how much does John owe?'"
                )
            }

            // Step 4: Validate function calls
            val validationErrors = mutableListOf<String>()
            functionCalls.forEach { call ->
                val tool = parser.findMatchingTool(call.functionName)
                if (tool != null) {
                    val missingParams = parser.validateFunctionCall(call, tool)
                    if (missingParams.isNotEmpty()) {
                        validationErrors.add("${call.functionName} missing: ${missingParams.joinToString()}")
                    }
                }
            }

            if (validationErrors.isNotEmpty()) {
                return@withContext ProcessingResult.ValidationError(
                    userInput = userInput,
                    errors = validationErrors
                )
            }

            // Step 5: Execute function call(s)
            val results = executor.executeAll(functionCalls)

            // Step 6: Build response
            ProcessingResult.Success(
                userInput = userInput,
                functionCalls = functionCalls,
                results = results,
                explanation = parser.extractExplanation(modelOutput)
            )

        } catch (e: Exception) {
            ProcessingResult.Error(
                userInput = userInput,
                message = "An error occurred: ${e.message}",
                exception = e
            )
        }
    }

    /**
     * Fallback processing using pattern-based parsing when the ML model is unavailable.
     * This allows the app to work offline without the TFLite model.
     */
    private suspend fun processWithFallback(userInput: String): ProcessingResult {
        val input = userInput.lowercase().trim()

        // Pattern-based command detection
        val functionCall = when {
            // Sale patterns
            input.matches(Regex("(sell|sold|record sale).*")) -> {
                parseSaleCommand(input)
            }

            // Stock check patterns
            input.matches(Regex("(how many|check stock|stock of|inventory).*")) -> {
                parseStockCheckCommand(input)
            }

            // Payment patterns
            input.matches(Regex("(paid|payment|received|customer paid).*")) -> {
                parsePaymentCommand(input)
            }

            // Balance check patterns
            input.matches(Regex("(how much|balance|owes|debt).*")) -> {
                parseBalanceCheckCommand(input)
            }

            // Add product patterns
            input.matches(Regex("(add product|new product|create product).*")) -> {
                parseAddProductCommand(input)
            }

            // Add customer patterns
            input.matches(Regex("(add customer|new customer|register customer).*")) -> {
                parseAddCustomerCommand(input)
            }

            // Analytics patterns
            input.matches(Regex("(sales today|today's sales|revenue|analytics|how much did).*")) -> {
                parseAnalyticsCommand(input)
            }

            // Low stock patterns
            input.matches(Regex("(low stock|running low|stock alert).*")) -> {
                ParsedFunctionCall("get_low_stock_alerts", emptyMap())
            }

            else -> null
        }

        return if (functionCall != null) {
            val results = executor.executeAll(listOf(functionCall))
            ProcessingResult.Success(
                userInput = userInput,
                functionCalls = listOf(functionCall),
                results = results,
                explanation = null
            )
        } else {
            ProcessingResult.NoFunctionDetected(
                userInput = userInput,
                message = "I couldn't understand that command. Try:\n• 'Sell 3 Coca-Cola'\n• 'How many bread in stock?'\n• 'John paid 500'\n• 'How much does Mary owe?'"
            )
        }
    }

    // ============ Pattern-based parsers for fallback ============

    private fun parseSaleCommand(input: String): ParsedFunctionCall? {
        // Pattern: "sell/sold [quantity] [product]" or "[quantity] [product] sold"
        val patterns = listOf(
            Regex("(sell|sold)\\s+(\\d+)\\s+(.+)"),
            Regex("(\\d+)\\s+(.+?)\\s+(sold|sell)"),
            Regex("record\\s+sale\\s+(\\d+)\\s+(.+)")
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                val (qty, product) = when {
                    match.groupValues[1].toIntOrNull() != null -> {
                        match.groupValues[1].toInt() to match.groupValues[2]
                    }
                    match.groupValues[2].toIntOrNull() != null -> {
                        match.groupValues[2].toInt() to match.groupValues[3]
                    }
                    else -> {
                        1 to match.groupValues[3]
                    }
                }

                return ParsedFunctionCall(
                    functionName = "record_sale",
                    arguments = mapOf(
                        "product_name" to product.trim(),
                        "quantity" to qty
                    )
                )
            }
        }

        // Simple pattern: "sell [product]" (quantity = 1)
        val simplePattern = Regex("(sell|sold)\\s+(.+)")
        simplePattern.find(input)?.let {
            return ParsedFunctionCall(
                functionName = "record_sale",
                arguments = mapOf(
                    "product_name" to it.groupValues[2].trim(),
                    "quantity" to 1
                )
            )
        }

        return null
    }

    private fun parseStockCheckCommand(input: String): ParsedFunctionCall? {
        // Pattern: "how many [product]" or "check stock [product]" or "stock of [product]"
        val patterns = listOf(
            Regex("how\\s+many\\s+(.+?)\\s*(in stock|remaining|left)?"),
            Regex("check\\s+stock\\s+(?:of\\s+)?(.+)"),
            Regex("stock\\s+of\\s+(.+)"),
            Regex("inventory\\s+(?:of\\s+)?(.+)")
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                return ParsedFunctionCall(
                    functionName = "check_stock",
                    arguments = mapOf("product_name" to match.groupValues[1].trim())
                )
            }
        }

        return null
    }

    private fun parsePaymentCommand(input: String): ParsedFunctionCall? {
        // Patterns: "John paid 500" or "received 500 from John" or "payment 500 from John"
        val patterns = listOf(
            Regex("(.+?)\\s+paid\\s+(\\d+(?:\\.\\d+)?)"),
            Regex("received\\s+(\\d+(?:\\.\\d+)?)\\s+from\\s+(.+)"),
            Regex("payment\\s+(\\d+(?:\\.\\d+)?)\\s+from\\s+(.+)"),
            Regex("customer\\s+paid\\s+(\\d+(?:\\.\\d+)?).*?from\\s+(.+)")
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                val (customer, amount) = when {
                    match.groupValues[1].toDoubleOrNull() != null -> {
                        match.groupValues[2] to match.groupValues[1].toDouble()
                    }
                    else -> {
                        match.groupValues[1] to (match.groupValues[2].toDoubleOrNull() ?: 0.0)
                    }
                }

                return ParsedFunctionCall(
                    functionName = "record_payment",
                    arguments = mapOf(
                        "customer_name" to customer.trim(),
                        "amount" to amount
                    )
                )
            }
        }

        return null
    }

    private fun parseBalanceCheckCommand(input: String): ParsedFunctionCall? {
        // Patterns: "how much does John owe" or "John's balance" or "balance for John"
        val patterns = listOf(
            Regex("how\\s+much\\s+does\\s+(.+?)\\s+owe"),
            Regex("(.+?)'s\\s+balance"),
            Regex("balance\\s+(?:for|of)\\s+(.+)"),
            Regex("debt\\s+(?:for|of)\\s+(.+)"),
            Regex("what\\s+does\\s+(.+?)\\s+owe")
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                return ParsedFunctionCall(
                    functionName = "get_customer_balance",
                    arguments = mapOf("customer_name" to match.groupValues[1].trim())
                )
            }
        }

        return null
    }

    private fun parseAddProductCommand(input: String): ParsedFunctionCall? {
        // Pattern: "add product [name] at [price]" or "new product [name] [price]"
        val patterns = listOf(
            Regex("(?:add|new|create)\\s+product\\s+(.+?)\\s+(?:at|for|price)?\\s*(\\d+(?:\\.\\d+)?)"),
            Regex("(?:add|new|create)\\s+product\\s+(.+)")
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                val name = match.groupValues[1].trim()
                val price = match.groupValues.getOrNull(2)?.toDoubleOrNull() ?: 0.0

                return ParsedFunctionCall(
                    functionName = "add_product",
                    arguments = mapOf(
                        "name" to name,
                        "selling_price" to price
                    )
                )
            }
        }

        return null
    }

    private fun parseAddCustomerCommand(input: String): ParsedFunctionCall? {
        // Pattern: "add customer [name]" or "add customer [name] [phone]"
        val patterns = listOf(
            Regex("(?:add|new|register)\\s+customer\\s+(.+?)\\s+(\\d{10,})"),
            Regex("(?:add|new|register)\\s+customer\\s+(.+)")
        )

        for (pattern in patterns) {
            val match = pattern.find(input)
            if (match != null) {
                val name = match.groupValues[1].trim()
                val phone = match.groupValues.getOrNull(2)

                val args = mutableMapOf<String, Any?>("name" to name)
                phone?.let { args["phone"] = it }

                return ParsedFunctionCall(
                    functionName = "add_customer",
                    arguments = args
                )
            }
        }

        return null
    }

    private fun parseAnalyticsCommand(input: String): ParsedFunctionCall {
        val period = when {
            input.contains("yesterday") -> "yesterday"
            input.contains("week") -> "this_week"
            input.contains("month") -> "this_month"
            else -> "today"
        }

        return ParsedFunctionCall(
            functionName = "get_sales_analytics",
            arguments = mapOf("period" to period)
        )
    }

    /**
     * Check if the FunctionGemma model is ready
     */
    fun isModelReady(): Boolean = inference.isReady()

    /**
     * Initialize the FunctionGemma model
     */
    suspend fun initializeModel(): Result<Unit> = inference.initialize()

    /**
     * Release resources
     */
    fun close() {
        inference.close()
    }
}

/**
 * Result of processing a command
 */
sealed class ProcessingResult {
    abstract val userInput: String

    /**
     * Command was successfully processed and executed
     */
    data class Success(
        override val userInput: String,
        val functionCalls: List<ParsedFunctionCall>,
        val results: List<FunctionExecutionResult>,
        val explanation: String?
    ) : ProcessingResult() {
        /**
         * Get a user-friendly summary message
         */
        fun getSummaryMessage(): String {
            return results.joinToString("\n") { result ->
                when (result) {
                    is FunctionExecutionResult.Success -> result.message
                    is FunctionExecutionResult.Error -> "Error: ${result.errorMessage}"
                    is FunctionExecutionResult.NeedsConfirmation -> result.message
                    is FunctionExecutionResult.ProductNotFound -> {
                        val suggestions = if (result.suggestions.isNotEmpty()) {
                            " Did you mean: ${result.suggestions.joinToString(", ")}?"
                        } else ""
                        "Product '${result.productName}' not found.$suggestions"
                    }
                    is FunctionExecutionResult.CustomerNotFound -> {
                        val suggestions = if (result.suggestions.isNotEmpty()) {
                            " Did you mean: ${result.suggestions.joinToString(", ")}?"
                        } else ""
                        "Customer '${result.customerName}' not found.$suggestions"
                    }
                    is FunctionExecutionResult.InsufficientStock -> {
                        "Not enough ${result.productName} in stock. Available: ${result.availableStock}, Requested: ${result.requestedQuantity}"
                    }
                }
            }
        }

        /**
         * Check if all results were successful
         */
        fun allSuccessful(): Boolean {
            return results.all { it is FunctionExecutionResult.Success }
        }
    }

    /**
     * No function could be detected from the input
     */
    data class NoFunctionDetected(
        override val userInput: String,
        val message: String
    ) : ProcessingResult()

    /**
     * Function calls were detected but validation failed
     */
    data class ValidationError(
        override val userInput: String,
        val errors: List<String>
    ) : ProcessingResult()

    /**
     * An error occurred during processing
     */
    data class Error(
        override val userInput: String,
        val message: String,
        val exception: Exception? = null
    ) : ProcessingResult()
}
