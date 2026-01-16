package com.example.dukaai.ml.functiongemma

/**
 * Tool schema definitions for DukaAI function calling with FunctionGemma.
 *
 * FunctionGemma uses control tokens to define tools and parse function calls:
 * - <start_function_declaration> ... <end_function_declaration>
 * - <start_function_call> ... <end_function_call>
 * - <escape> delimiter for string values
 */

/**
 * Represents a parameter in a function declaration
 */
data class ToolParameter(
    val name: String,
    val type: String,  // "string", "integer", "number", "boolean"
    val description: String,
    val required: Boolean = true,
    val enumValues: List<String>? = null
)

/**
 * Represents a tool/function declaration
 */
data class ToolDeclaration(
    val name: String,
    val description: String,
    val parameters: List<ToolParameter>
)

/**
 * All DukaAI tool declarations for FunctionGemma
 */
object DukaToolSchema {

    /**
     * Record a sale transaction
     */
    val recordSale = ToolDeclaration(
        name = "record_sale",
        description = "Record a sale transaction for a product. Use this when the user wants to sell an item or record that something was sold.",
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product being sold (e.g., 'Coca-Cola', 'bread', 'sugar')"
            ),
            ToolParameter(
                name = "quantity",
                type = "integer",
                description = "The number of units sold (e.g., 1, 5, 10)"
            ),
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Optional name of the customer for credit sales",
                required = false
            ),
            ToolParameter(
                name = "sale_type",
                type = "string",
                description = "Type of sale: 'cash' for immediate payment, 'credit' for customer credit",
                required = false,
                enumValues = listOf("cash", "credit")
            )
        )
    )

    /**
     * Add a new product to inventory
     */
    val addProduct = ToolDeclaration(
        name = "add_product",
        description = "Add a new product to the inventory. Use this when the user wants to create a new product listing.",
        parameters = listOf(
            ToolParameter(
                name = "name",
                type = "string",
                description = "The product name"
            ),
            ToolParameter(
                name = "selling_price",
                type = "number",
                description = "The selling price in Kwacha"
            ),
            ToolParameter(
                name = "category",
                type = "string",
                description = "Product category (e.g., 'beverages', 'groceries', 'snacks', 'household')",
                required = false
            ),
            ToolParameter(
                name = "initial_stock",
                type = "integer",
                description = "Initial stock quantity",
                required = false
            ),
            ToolParameter(
                name = "buying_price",
                type = "number",
                description = "The buying/cost price in Kwacha",
                required = false
            )
        )
    )

    /**
     * Update stock for an existing product
     */
    val updateStock = ToolDeclaration(
        name = "update_stock",
        description = "Update the stock quantity for an existing product. Use this to add new stock or adjust inventory.",
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product to update"
            ),
            ToolParameter(
                name = "quantity",
                type = "integer",
                description = "The quantity to add (positive) or remove (negative)"
            ),
            ToolParameter(
                name = "reason",
                type = "string",
                description = "Reason for stock adjustment (e.g., 'restock', 'damaged', 'expired')",
                required = false
            )
        )
    )

    /**
     * Check stock level for a product
     */
    val checkStock = ToolDeclaration(
        name = "check_stock",
        description = "Check the current stock level for a product. Use this when the user asks about remaining inventory.",
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product to check"
            )
        )
    )

    /**
     * Search for products
     */
    val searchProducts = ToolDeclaration(
        name = "search_products",
        description = "Search for products by name or category. Use this when the user wants to find products.",
        parameters = listOf(
            ToolParameter(
                name = "query",
                type = "string",
                description = "Search query (product name or category)"
            )
        )
    )

    /**
     * Record a customer payment
     */
    val recordPayment = ToolDeclaration(
        name = "record_payment",
        description = "Record a payment from a customer towards their credit balance. Use this when a customer pays what they owe.",
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "The name of the customer making the payment"
            ),
            ToolParameter(
                name = "amount",
                type = "number",
                description = "The payment amount in Kwacha"
            ),
            ToolParameter(
                name = "payment_method",
                type = "string",
                description = "Payment method: 'cash', 'mobile_money', 'bank'",
                required = false,
                enumValues = listOf("cash", "mobile_money", "bank")
            )
        )
    )

    /**
     * Add a new customer
     */
    val addCustomer = ToolDeclaration(
        name = "add_customer",
        description = "Add a new customer to the system. Use this when registering a new customer for credit sales.",
        parameters = listOf(
            ToolParameter(
                name = "name",
                type = "string",
                description = "The customer's full name"
            ),
            ToolParameter(
                name = "phone",
                type = "string",
                description = "The customer's phone number",
                required = false
            ),
            ToolParameter(
                name = "address",
                type = "string",
                description = "The customer's address",
                required = false
            )
        )
    )

    /**
     * Get customer credit balance
     */
    val getCustomerBalance = ToolDeclaration(
        name = "get_customer_balance",
        description = "Get the outstanding credit balance for a customer. Use this to check how much a customer owes.",
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "The name of the customer"
            )
        )
    )

    /**
     * Get sales analytics/summary
     */
    val getSalesAnalytics = ToolDeclaration(
        name = "get_sales_analytics",
        description = "Get sales analytics and summaries. Use this when the user asks about sales performance, revenue, or statistics.",
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period: 'today', 'yesterday', 'this_week', 'this_month', 'custom'",
                enumValues = listOf("today", "yesterday", "this_week", "this_month", "custom")
            ),
            ToolParameter(
                name = "metric",
                type = "string",
                description = "The metric to retrieve: 'revenue', 'transactions', 'top_products', 'all'",
                required = false,
                enumValues = listOf("revenue", "transactions", "top_products", "all")
            )
        )
    )

    /**
     * Get low stock alerts
     */
    val getLowStockAlerts = ToolDeclaration(
        name = "get_low_stock_alerts",
        description = "Get a list of products that are running low on stock. Use this to check inventory alerts.",
        parameters = listOf()
    )

    /**
     * All available tools for DukaAI
     */
    val allTools = listOf(
        recordSale,
        addProduct,
        updateStock,
        checkStock,
        searchProducts,
        recordPayment,
        addCustomer,
        getCustomerBalance,
        getSalesAnalytics,
        getLowStockAlerts
    )

    /**
     * Convert tool schema to FunctionGemma format
     */
    fun toFunctionGemmaFormat(): String {
        return buildString {
            allTools.forEach { tool ->
                append(formatToolDeclaration(tool))
                append("\n")
            }
        }
    }

    /**
     * Format a single tool declaration for FunctionGemma
     */
    private fun formatToolDeclaration(tool: ToolDeclaration): String {
        return buildString {
            append("<start_function_declaration>")
            append(tool.name)
            append("(")

            tool.parameters.forEachIndexed { index, param ->
                if (index > 0) append(", ")
                append(param.name)
                append(": ")
                append(param.type)
                if (!param.required) append("?")
            }

            append(")")
            append(" - ")
            append(tool.description)

            // Add parameter descriptions
            if (tool.parameters.isNotEmpty()) {
                append("\nParameters:")
                tool.parameters.forEach { param ->
                    append("\n  - ${param.name}: ${param.description}")
                    if (param.enumValues != null) {
                        append(" [${param.enumValues.joinToString(", ")}]")
                    }
                }
            }

            append("<end_function_declaration>")
        }
    }
}

/**
 * Represents a parsed function call from FunctionGemma output
 */
data class ParsedFunctionCall(
    val functionName: String,
    val arguments: Map<String, Any?>
)

/**
 * Result of executing a function
 */
sealed class FunctionExecutionResult {
    data class Success(
        val functionName: String,
        val message: String,
        val data: Any? = null
    ) : FunctionExecutionResult()

    data class Error(
        val functionName: String,
        val errorMessage: String,
        val errorCode: String? = null
    ) : FunctionExecutionResult()

    data class NeedsConfirmation(
        val functionName: String,
        val message: String,
        val details: Map<String, Any?> = emptyMap()
    ) : FunctionExecutionResult()

    data class ProductNotFound(
        val productName: String,
        val suggestions: List<String> = emptyList()
    ) : FunctionExecutionResult()

    data class CustomerNotFound(
        val customerName: String,
        val suggestions: List<String> = emptyList()
    ) : FunctionExecutionResult()

    data class InsufficientStock(
        val productName: String,
        val requestedQuantity: Int,
        val availableStock: Int
    ) : FunctionExecutionResult()
}
