package com.example.dukaai.ml.functiongemma

/**
 * Tool schema definitions for DukaAI function calling with FunctionGemma.
 *
 * FunctionGemma uses control tokens to define tools and parse function calls:
 * - <start_function_declaration> ... <end_function_declaration>
 * - <start_function_call> ... <end_function_call>
 * - <escape> delimiter for string values
 *
 * Categories:
 * 1. Product Management (14 tools)
 * 2. Sales Operations (10 tools)
 * 3. Credit/Debt Management (11 tools)
 * 4. Customer Management (10 tools)
 * 5. Payment Operations (11 tools)
 * 6. Inventory Operations (4 tools)
 * 7. Analytics & Reporting (10 tools)
 * 8. Settings & Configuration (10 tools)
 * 9. Backup/Export/Import (10 tools)
 * 10. Barcode/Scanner (5 tools)
 * 11. Navigation/UI (12 tools)
 * 12. Machine Learning (5 tools)
 * 13. Voice Framework (6 tools)
 * 14. Validation & Confirmation (3 tools)
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
    val parameters: List<ToolParameter>,
    val category: ToolCategory = ToolCategory.GENERAL
)

/**
 * Tool categories for organization
 */
enum class ToolCategory {
    PRODUCT_MANAGEMENT,
    SALES_OPERATIONS,
    CREDIT_MANAGEMENT,
    CUSTOMER_MANAGEMENT,
    PAYMENT_OPERATIONS,
    INVENTORY_OPERATIONS,
    ANALYTICS_REPORTING,
    SETTINGS_CONFIGURATION,
    BACKUP_EXPORT_IMPORT,
    BARCODE_SCANNER,
    NAVIGATION_UI,
    MACHINE_LEARNING,
    VOICE_FRAMEWORK,
    VALIDATION,
    GENERAL
}

/**
 * All DukaAI tool declarations for FunctionGemma
 */
object DukaToolSchema {

    // ============================================================================
    // PRODUCT MANAGEMENT (14 tools)
    // ============================================================================

    /**
     * Add a new product to inventory
     */
    val addProduct = ToolDeclaration(
        name = "add_product",
        description = "Add a new product to the inventory. Use this when the user wants to create a new product listing.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
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
            ),
            ToolParameter(
                name = "barcode",
                type = "string",
                description = "Product barcode (EAN, UPC)",
                required = false
            ),
            ToolParameter(
                name = "low_stock_threshold",
                type = "integer",
                description = "Minimum stock level before alert",
                required = false
            ),
            ToolParameter(
                name = "unit",
                type = "string",
                description = "Unit of measurement (pieces, kg, liters)",
                required = false,
                enumValues = listOf("pieces", "kg", "liters", "packets", "boxes", "bags")
            )
        )
    )

    /**
     * Edit an existing product
     */
    val editProduct = ToolDeclaration(
        name = "edit_product",
        description = "Edit an existing product's details. Use when user wants to update product information like price, name, or category.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product to edit"
            ),
            ToolParameter(
                name = "new_name",
                type = "string",
                description = "New name for the product",
                required = false
            ),
            ToolParameter(
                name = "new_selling_price",
                type = "number",
                description = "New selling price in Kwacha",
                required = false
            ),
            ToolParameter(
                name = "new_buying_price",
                type = "number",
                description = "New buying price in Kwacha",
                required = false
            ),
            ToolParameter(
                name = "new_category",
                type = "string",
                description = "New category for the product",
                required = false
            ),
            ToolParameter(
                name = "new_low_stock_threshold",
                type = "integer",
                description = "New low stock alert threshold",
                required = false
            )
        )
    )

    /**
     * Delete a product from inventory
     */
    val deleteProduct = ToolDeclaration(
        name = "delete_product",
        description = "Delete a product from the inventory. Requires confirmation. Use when user wants to remove a product completely.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product to delete"
            ),
            ToolParameter(
                name = "confirm",
                type = "boolean",
                description = "Confirmation flag - must be true to delete",
                required = false
            )
        )
    )

    /**
     * Get product details
     */
    val getProductDetails = ToolDeclaration(
        name = "get_product_details",
        description = "Get detailed information about a specific product including price, stock, and sales history.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product"
            )
        )
    )

    /**
     * Check stock level for a product
     */
    val checkStock = ToolDeclaration(
        name = "check_stock",
        description = "Check the current stock level for a product. Use this when the user asks about remaining inventory.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product to check"
            )
        )
    )

    /**
     * Update stock for an existing product
     */
    val updateStock = ToolDeclaration(
        name = "update_stock",
        description = "Update the stock quantity for an existing product. Use this to add new stock or adjust inventory.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
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
                description = "Reason for stock adjustment",
                required = false,
                enumValues = listOf("restock", "damaged", "expired", "theft", "correction", "return")
            )
        )
    )

    /**
     * Search for products
     */
    val searchProducts = ToolDeclaration(
        name = "search_products",
        description = "Search for products by name, category, or barcode. Use this when the user wants to find products.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "query",
                type = "string",
                description = "Search query (product name, category, or barcode)"
            ),
            ToolParameter(
                name = "search_type",
                type = "string",
                description = "Type of search to perform",
                required = false,
                enumValues = listOf("name", "category", "barcode", "all")
            )
        )
    )

    /**
     * Get low stock alerts
     */
    val getLowStockAlerts = ToolDeclaration(
        name = "get_low_stock_alerts",
        description = "Get a list of products that are running low on stock. Use this to check inventory alerts.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "threshold",
                type = "integer",
                description = "Custom threshold to check (overrides product-specific thresholds)",
                required = false
            )
        )
    )

    /**
     * List all products
     */
    val listProducts = ToolDeclaration(
        name = "list_products",
        description = "List all products in inventory, optionally filtered by category or sorted by a field.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "category",
                type = "string",
                description = "Filter by category",
                required = false
            ),
            ToolParameter(
                name = "sort_by",
                type = "string",
                description = "Field to sort by",
                required = false,
                enumValues = listOf("name", "price", "stock", "sales", "created_date")
            ),
            ToolParameter(
                name = "sort_order",
                type = "string",
                description = "Sort order",
                required = false,
                enumValues = listOf("asc", "desc")
            ),
            ToolParameter(
                name = "limit",
                type = "integer",
                description = "Maximum number of products to return",
                required = false
            )
        )
    )

    /**
     * Get out of stock products
     */
    val getOutOfStock = ToolDeclaration(
        name = "get_out_of_stock",
        description = "Get all products that are completely out of stock (zero quantity).",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf()
    )

    /**
     * Get top selling products
     */
    val getTopSellingProducts = ToolDeclaration(
        name = "get_top_selling_products",
        description = "Get the top selling products by quantity or revenue.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period to analyze",
                enumValues = listOf("today", "yesterday", "this_week", "this_month", "all_time")
            ),
            ToolParameter(
                name = "metric",
                type = "string",
                description = "Metric to rank by",
                required = false,
                enumValues = listOf("quantity", "revenue", "profit")
            ),
            ToolParameter(
                name = "limit",
                type = "integer",
                description = "Number of top products to return (default 10)",
                required = false
            )
        )
    )

    /**
     * Get product categories
     */
    val getProductCategories = ToolDeclaration(
        name = "get_product_categories",
        description = "Get all product categories with counts.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf()
    )

    /**
     * Update product price
     */
    val updateProductPrice = ToolDeclaration(
        name = "update_product_price",
        description = "Update the selling or buying price of a product.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product"
            ),
            ToolParameter(
                name = "new_selling_price",
                type = "number",
                description = "New selling price in Kwacha",
                required = false
            ),
            ToolParameter(
                name = "new_buying_price",
                type = "number",
                description = "New buying price in Kwacha",
                required = false
            )
        )
    )

    /**
     * Get product profit margin
     */
    val getProductProfitMargin = ToolDeclaration(
        name = "get_product_profit_margin",
        description = "Calculate and get the profit margin for a product.",
        category = ToolCategory.PRODUCT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "The name of the product"
            )
        )
    )

    // ============================================================================
    // SALES OPERATIONS (10 tools)
    // ============================================================================

    /**
     * Record a sale transaction
     */
    val recordSale = ToolDeclaration(
        name = "record_sale",
        description = "Record a sale transaction for a product. Use this when the user wants to sell an item or record that something was sold.",
        category = ToolCategory.SALES_OPERATIONS,
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
            ),
            ToolParameter(
                name = "unit_price",
                type = "number",
                description = "Override unit price (if different from default)",
                required = false
            ),
            ToolParameter(
                name = "discount",
                type = "number",
                description = "Discount amount in Kwacha",
                required = false
            )
        )
    )

    /**
     * Record multiple sales at once (batch sale)
     */
    val recordBatchSale = ToolDeclaration(
        name = "record_batch_sale",
        description = "Record multiple product sales in a single transaction. Use when selling multiple items together.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "items",
                type = "string",
                description = "Comma-separated list of items in format 'product:quantity' (e.g., 'Coca-Cola:2,Bread:1,Sugar:3')"
            ),
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer name for credit sales",
                required = false
            ),
            ToolParameter(
                name = "sale_type",
                type = "string",
                description = "Type of sale",
                required = false,
                enumValues = listOf("cash", "credit")
            )
        )
    )

    /**
     * Edit a sale
     */
    val editSale = ToolDeclaration(
        name = "edit_sale",
        description = "Edit an existing sale transaction. Use to correct mistakes in recorded sales.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "sale_id",
                type = "string",
                description = "The ID of the sale to edit"
            ),
            ToolParameter(
                name = "new_quantity",
                type = "integer",
                description = "New quantity",
                required = false
            ),
            ToolParameter(
                name = "new_unit_price",
                type = "number",
                description = "New unit price",
                required = false
            )
        )
    )

    /**
     * Delete/void a sale
     */
    val deleteSale = ToolDeclaration(
        name = "delete_sale",
        description = "Delete or void a sale transaction. Restores stock. Requires confirmation.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "sale_id",
                type = "string",
                description = "The ID of the sale to delete"
            ),
            ToolParameter(
                name = "reason",
                type = "string",
                description = "Reason for deletion",
                required = false,
                enumValues = listOf("mistake", "refund", "cancelled", "other")
            ),
            ToolParameter(
                name = "confirm",
                type = "boolean",
                description = "Confirmation flag",
                required = false
            )
        )
    )

    /**
     * Get sale history
     */
    val getSaleHistory = ToolDeclaration(
        name = "get_sale_history",
        description = "Get sales history for a period or product.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                required = false,
                enumValues = listOf("today", "yesterday", "this_week", "this_month", "custom")
            ),
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Filter by product",
                required = false
            ),
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Filter by customer",
                required = false
            ),
            ToolParameter(
                name = "sale_type",
                type = "string",
                description = "Filter by sale type",
                required = false,
                enumValues = listOf("cash", "credit", "all")
            )
        )
    )

    /**
     * Get today's sales
     */
    val getTodaySales = ToolDeclaration(
        name = "get_today_sales",
        description = "Get all sales made today with summary.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf()
    )

    /**
     * Get recent sales
     */
    val getRecentSales = ToolDeclaration(
        name = "get_recent_sales",
        description = "Get the most recent sales transactions.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "limit",
                type = "integer",
                description = "Number of recent sales to return (default 10)",
                required = false
            )
        )
    )

    /**
     * Calculate sale total
     */
    val calculateSaleTotal = ToolDeclaration(
        name = "calculate_sale_total",
        description = "Calculate the total for a potential sale before recording.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "items",
                type = "string",
                description = "Comma-separated list of items in format 'product:quantity'"
            ),
            ToolParameter(
                name = "discount",
                type = "number",
                description = "Discount to apply",
                required = false
            )
        )
    )

    /**
     * Get sales by payment type
     */
    val getSalesByPaymentType = ToolDeclaration(
        name = "get_sales_by_payment_type",
        description = "Get sales breakdown by cash vs credit.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                enumValues = listOf("today", "yesterday", "this_week", "this_month")
            )
        )
    )

    /**
     * Process refund
     */
    val processRefund = ToolDeclaration(
        name = "process_refund",
        description = "Process a refund for a sale. Restores stock and records refund.",
        category = ToolCategory.SALES_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "sale_id",
                type = "string",
                description = "The sale to refund"
            ),
            ToolParameter(
                name = "refund_amount",
                type = "number",
                description = "Amount to refund (full if not specified)",
                required = false
            ),
            ToolParameter(
                name = "reason",
                type = "string",
                description = "Reason for refund",
                required = false
            )
        )
    )

    // ============================================================================
    // CREDIT/DEBT MANAGEMENT (11 tools)
    // ============================================================================

    /**
     * Record credit sale
     */
    val recordCreditSale = ToolDeclaration(
        name = "record_credit_sale",
        description = "Record a credit sale (pa ng'ong'ole). Customer takes goods now and pays later.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer taking credit"
            ),
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Product being purchased on credit"
            ),
            ToolParameter(
                name = "quantity",
                type = "integer",
                description = "Quantity"
            ),
            ToolParameter(
                name = "due_date",
                type = "string",
                description = "When payment is expected (e.g., 'tomorrow', 'next_week', '2024-01-15')",
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
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "The name of the customer"
            )
        )
    )

    /**
     * Get all credit balances
     */
    val getAllCreditBalances = ToolDeclaration(
        name = "get_all_credit_balances",
        description = "Get outstanding credit balances for all customers.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "sort_by",
                type = "string",
                description = "Sort by amount or name",
                required = false,
                enumValues = listOf("amount_desc", "amount_asc", "name", "oldest")
            )
        )
    )

    /**
     * Get overdue credits
     */
    val getOverdueCredits = ToolDeclaration(
        name = "get_overdue_credits",
        description = "Get all overdue credit accounts that are past their due date.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "days_overdue",
                type = "integer",
                description = "Minimum days overdue to filter",
                required = false
            )
        )
    )

    /**
     * Mark credit as paid
     */
    val markCreditAsPaid = ToolDeclaration(
        name = "mark_credit_as_paid",
        description = "Mark a specific credit entry as fully paid.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "credit_id",
                type = "string",
                description = "The credit entry ID"
            )
        )
    )

    /**
     * Get credit history for customer
     */
    val getCreditHistory = ToolDeclaration(
        name = "get_credit_history",
        description = "Get complete credit history for a customer including all credits and payments.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer name"
            ),
            ToolParameter(
                name = "status",
                type = "string",
                description = "Filter by status",
                required = false,
                enumValues = listOf("pending", "paid", "partial", "all")
            )
        )
    )

    /**
     * Send credit reminder
     */
    val sendCreditReminder = ToolDeclaration(
        name = "send_credit_reminder",
        description = "Send a payment reminder to a customer via SMS or WhatsApp.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer to remind"
            ),
            ToolParameter(
                name = "channel",
                type = "string",
                description = "Reminder channel",
                required = false,
                enumValues = listOf("sms", "whatsapp")
            )
        )
    )

    /**
     * Get total credits owed
     */
    val getTotalCreditsOwed = ToolDeclaration(
        name = "get_total_credits_owed",
        description = "Get the total amount of all outstanding credits.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf()
    )

    /**
     * Update credit due date
     */
    val updateCreditDueDate = ToolDeclaration(
        name = "update_credit_due_date",
        description = "Update the due date for a credit entry.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "credit_id",
                type = "string",
                description = "Credit entry ID"
            ),
            ToolParameter(
                name = "new_due_date",
                type = "string",
                description = "New due date"
            )
        )
    )

    /**
     * Set customer credit limit
     */
    val setCustomerCreditLimit = ToolDeclaration(
        name = "set_customer_credit_limit",
        description = "Set or update the maximum credit limit for a customer.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer name"
            ),
            ToolParameter(
                name = "credit_limit",
                type = "number",
                description = "Maximum credit limit in Kwacha"
            )
        )
    )

    /**
     * Get customers near credit limit
     */
    val getCustomersNearCreditLimit = ToolDeclaration(
        name = "get_customers_near_credit_limit",
        description = "Get customers who are close to or have exceeded their credit limit.",
        category = ToolCategory.CREDIT_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "threshold_percent",
                type = "integer",
                description = "Percentage of limit to trigger (default 80%)",
                required = false
            )
        )
    )

    // ============================================================================
    // CUSTOMER MANAGEMENT (10 tools)
    // ============================================================================

    /**
     * Add a new customer
     */
    val addCustomer = ToolDeclaration(
        name = "add_customer",
        description = "Add a new customer to the system. Use this when registering a new customer for credit sales.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "name",
                type = "string",
                description = "The customer's full name"
            ),
            ToolParameter(
                name = "phone",
                type = "string",
                description = "The customer's phone number (Zambian format)",
                required = false
            ),
            ToolParameter(
                name = "address",
                type = "string",
                description = "The customer's address",
                required = false
            ),
            ToolParameter(
                name = "notes",
                type = "string",
                description = "Additional notes about the customer",
                required = false
            )
        )
    )

    /**
     * Update customer information
     */
    val updateCustomer = ToolDeclaration(
        name = "update_customer",
        description = "Update customer details like phone, address, or notes.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer to update"
            ),
            ToolParameter(
                name = "new_name",
                type = "string",
                description = "New name",
                required = false
            ),
            ToolParameter(
                name = "new_phone",
                type = "string",
                description = "New phone number",
                required = false
            ),
            ToolParameter(
                name = "new_address",
                type = "string",
                description = "New address",
                required = false
            ),
            ToolParameter(
                name = "new_notes",
                type = "string",
                description = "Updated notes",
                required = false
            )
        )
    )

    /**
     * Delete customer
     */
    val deleteCustomer = ToolDeclaration(
        name = "delete_customer",
        description = "Delete a customer from the system. Only allowed if no outstanding credits.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer to delete"
            ),
            ToolParameter(
                name = "confirm",
                type = "boolean",
                description = "Confirmation required",
                required = false
            )
        )
    )

    /**
     * Search customers
     */
    val searchCustomers = ToolDeclaration(
        name = "search_customers",
        description = "Search for customers by name or phone number.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "query",
                type = "string",
                description = "Search query (name or phone)"
            )
        )
    )

    /**
     * Get customer details
     */
    val getCustomerDetails = ToolDeclaration(
        name = "get_customer_details",
        description = "Get full details for a customer including contact info, credit history, and purchase history.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer name"
            )
        )
    )

    /**
     * List all customers
     */
    val listCustomers = ToolDeclaration(
        name = "list_customers",
        description = "List all customers with optional filtering and sorting.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "sort_by",
                type = "string",
                description = "Sort field",
                required = false,
                enumValues = listOf("name", "balance", "recent_activity", "total_purchases")
            ),
            ToolParameter(
                name = "has_balance",
                type = "boolean",
                description = "Filter to only customers with outstanding balance",
                required = false
            )
        )
    )

    /**
     * Get customer purchase history
     */
    val getCustomerPurchaseHistory = ToolDeclaration(
        name = "get_customer_purchase_history",
        description = "Get the purchase history for a specific customer.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer name"
            ),
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                required = false,
                enumValues = listOf("this_week", "this_month", "last_3_months", "all_time")
            )
        )
    )

    /**
     * Get top customers
     */
    val getTopCustomers = ToolDeclaration(
        name = "get_top_customers",
        description = "Get the top customers by purchase amount or frequency.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "metric",
                type = "string",
                description = "Metric to rank by",
                enumValues = listOf("total_purchases", "frequency", "average_purchase")
            ),
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                required = false,
                enumValues = listOf("this_month", "last_3_months", "this_year", "all_time")
            ),
            ToolParameter(
                name = "limit",
                type = "integer",
                description = "Number of customers to return",
                required = false
            )
        )
    )

    /**
     * Get customers count
     */
    val getCustomersCount = ToolDeclaration(
        name = "get_customers_count",
        description = "Get the total number of customers.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf()
    )

    /**
     * Get new customers
     */
    val getNewCustomers = ToolDeclaration(
        name = "get_new_customers",
        description = "Get customers added in a recent period.",
        category = ToolCategory.CUSTOMER_MANAGEMENT,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                enumValues = listOf("today", "this_week", "this_month")
            )
        )
    )

    // ============================================================================
    // PAYMENT OPERATIONS (11 tools)
    // ============================================================================

    /**
     * Record a customer payment
     */
    val recordPayment = ToolDeclaration(
        name = "record_payment",
        description = "Record a payment from a customer towards their credit balance. Use this when a customer pays what they owe.",
        category = ToolCategory.PAYMENT_OPERATIONS,
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
                description = "Payment method",
                required = false,
                enumValues = listOf("cash", "mobile_money", "bank", "airtel_money", "mtn_money")
            ),
            ToolParameter(
                name = "reference",
                type = "string",
                description = "Payment reference or transaction ID",
                required = false
            )
        )
    )

    /**
     * Get payment history
     */
    val getPaymentHistory = ToolDeclaration(
        name = "get_payment_history",
        description = "Get payment history for a customer or time period.",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Filter by customer",
                required = false
            ),
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                required = false,
                enumValues = listOf("today", "this_week", "this_month", "all")
            ),
            ToolParameter(
                name = "payment_method",
                type = "string",
                description = "Filter by payment method",
                required = false
            )
        )
    )

    /**
     * Get today's payments
     */
    val getTodayPayments = ToolDeclaration(
        name = "get_today_payments",
        description = "Get all payments received today.",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf()
    )

    /**
     * Get total payments
     */
    val getTotalPayments = ToolDeclaration(
        name = "get_total_payments",
        description = "Get total payment amount for a period.",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                enumValues = listOf("today", "this_week", "this_month")
            )
        )
    )

    /**
     * Edit payment
     */
    val editPayment = ToolDeclaration(
        name = "edit_payment",
        description = "Edit an existing payment record.",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "payment_id",
                type = "string",
                description = "Payment ID to edit"
            ),
            ToolParameter(
                name = "new_amount",
                type = "number",
                description = "New amount",
                required = false
            ),
            ToolParameter(
                name = "new_method",
                type = "string",
                description = "New payment method",
                required = false
            )
        )
    )

    /**
     * Delete payment
     */
    val deletePayment = ToolDeclaration(
        name = "delete_payment",
        description = "Delete a payment record. Updates customer balance accordingly.",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "payment_id",
                type = "string",
                description = "Payment ID to delete"
            ),
            ToolParameter(
                name = "reason",
                type = "string",
                description = "Reason for deletion",
                required = false
            ),
            ToolParameter(
                name = "confirm",
                type = "boolean",
                description = "Confirmation required",
                required = false
            )
        )
    )

    /**
     * Get payments by method
     */
    val getPaymentsByMethod = ToolDeclaration(
        name = "get_payments_by_method",
        description = "Get payment breakdown by method (cash, mobile money, etc).",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                enumValues = listOf("today", "this_week", "this_month")
            )
        )
    )

    /**
     * Get recent payments
     */
    val getRecentPayments = ToolDeclaration(
        name = "get_recent_payments",
        description = "Get the most recent payment transactions.",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "limit",
                type = "integer",
                description = "Number of payments to return",
                required = false
            )
        )
    )

    /**
     * Allocate payment to specific credits
     */
    val allocatePayment = ToolDeclaration(
        name = "allocate_payment",
        description = "Allocate a payment to specific credit entries for a customer.",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer name"
            ),
            ToolParameter(
                name = "amount",
                type = "number",
                description = "Payment amount"
            ),
            ToolParameter(
                name = "credit_ids",
                type = "string",
                description = "Comma-separated credit IDs to apply payment to",
                required = false
            )
        )
    )

    /**
     * Get expected payments
     */
    val getExpectedPayments = ToolDeclaration(
        name = "get_expected_payments",
        description = "Get credits with upcoming due dates (expected payments).",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "days_ahead",
                type = "integer",
                description = "Number of days to look ahead",
                required = false
            )
        )
    )

    /**
     * Generate payment receipt
     */
    val generatePaymentReceipt = ToolDeclaration(
        name = "generate_payment_receipt",
        description = "Generate a receipt for a payment transaction.",
        category = ToolCategory.PAYMENT_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "payment_id",
                type = "string",
                description = "Payment ID"
            ),
            ToolParameter(
                name = "format",
                type = "string",
                description = "Receipt format",
                required = false,
                enumValues = listOf("text", "pdf", "thermal")
            )
        )
    )

    // ============================================================================
    // INVENTORY OPERATIONS (4 tools)
    // ============================================================================

    /**
     * Get inventory history
     */
    val getInventoryHistory = ToolDeclaration(
        name = "get_inventory_history",
        description = "Get stock movement history for a product or all products.",
        category = ToolCategory.INVENTORY_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Filter by product",
                required = false
            ),
            ToolParameter(
                name = "movement_type",
                type = "string",
                description = "Filter by type",
                required = false,
                enumValues = listOf("sale", "restock", "adjustment", "damaged", "all")
            ),
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                required = false,
                enumValues = listOf("today", "this_week", "this_month", "all")
            )
        )
    )

    /**
     * Get inventory value
     */
    val getInventoryValue = ToolDeclaration(
        name = "get_inventory_value",
        description = "Calculate total inventory value based on cost price and selling price.",
        category = ToolCategory.INVENTORY_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "value_type",
                type = "string",
                description = "Value calculation type",
                required = false,
                enumValues = listOf("cost", "retail", "both")
            ),
            ToolParameter(
                name = "category",
                type = "string",
                description = "Filter by category",
                required = false
            )
        )
    )

    /**
     * Stock take / inventory count
     */
    val recordStockTake = ToolDeclaration(
        name = "record_stock_take",
        description = "Record a physical stock count for a product. Adjusts inventory if different.",
        category = ToolCategory.INVENTORY_OPERATIONS,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Product to count"
            ),
            ToolParameter(
                name = "actual_count",
                type = "integer",
                description = "Physical count result"
            ),
            ToolParameter(
                name = "notes",
                type = "string",
                description = "Notes about discrepancy",
                required = false
            )
        )
    )

    /**
     * Get inventory summary
     */
    val getInventorySummary = ToolDeclaration(
        name = "get_inventory_summary",
        description = "Get a summary of inventory status including total items, value, and alerts.",
        category = ToolCategory.INVENTORY_OPERATIONS,
        parameters = listOf()
    )

    // ============================================================================
    // ANALYTICS & REPORTING (10 tools)
    // ============================================================================

    /**
     * Get sales analytics/summary
     */
    val getSalesAnalytics = ToolDeclaration(
        name = "get_sales_analytics",
        description = "Get sales analytics and summaries. Use this when the user asks about sales performance, revenue, or statistics.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                enumValues = listOf("today", "yesterday", "this_week", "this_month", "custom")
            ),
            ToolParameter(
                name = "metric",
                type = "string",
                description = "The metric to retrieve",
                required = false,
                enumValues = listOf("revenue", "profit", "transactions", "top_products", "all")
            )
        )
    )

    /**
     * Get daily sales data
     */
    val getDailySalesData = ToolDeclaration(
        name = "get_daily_sales_data",
        description = "Get sales data broken down by day for charts and trends.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Period to analyze",
                enumValues = listOf("this_week", "this_month", "last_30_days", "last_90_days")
            )
        )
    )

    /**
     * Get profit analytics
     */
    val getProfitAnalytics = ToolDeclaration(
        name = "get_profit_analytics",
        description = "Get profit analysis including margins, costs, and net profit.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                enumValues = listOf("today", "this_week", "this_month")
            ),
            ToolParameter(
                name = "breakdown",
                type = "string",
                description = "How to break down data",
                required = false,
                enumValues = listOf("by_product", "by_category", "by_day", "summary")
            )
        )
    )

    /**
     * Get revenue trends
     */
    val getRevenueTrends = ToolDeclaration(
        name = "get_revenue_trends",
        description = "Get revenue trends and comparisons to previous periods.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Current period",
                enumValues = listOf("this_week", "this_month")
            ),
            ToolParameter(
                name = "compare_to",
                type = "string",
                description = "Period to compare against",
                required = false,
                enumValues = listOf("last_week", "last_month", "same_period_last_year")
            )
        )
    )

    /**
     * Get category performance
     */
    val getCategoryPerformance = ToolDeclaration(
        name = "get_category_performance",
        description = "Get sales performance by product category.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period",
                enumValues = listOf("today", "this_week", "this_month")
            )
        )
    )

    /**
     * Get hourly sales pattern
     */
    val getHourlySalesPattern = ToolDeclaration(
        name = "get_hourly_sales_pattern",
        description = "Get sales patterns by hour of day to identify peak times.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Period to analyze",
                enumValues = listOf("today", "this_week", "this_month")
            )
        )
    )

    /**
     * Generate report
     */
    val generateReport = ToolDeclaration(
        name = "generate_report",
        description = "Generate a comprehensive business report.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "report_type",
                type = "string",
                description = "Type of report",
                enumValues = listOf("daily_summary", "weekly_summary", "monthly_summary", "inventory_report", "credit_report", "full_business_report")
            ),
            ToolParameter(
                name = "period",
                type = "string",
                description = "Report period",
                required = false
            ),
            ToolParameter(
                name = "format",
                type = "string",
                description = "Output format",
                required = false,
                enumValues = listOf("text", "pdf", "csv")
            )
        )
    )

    /**
     * Compare periods
     */
    val comparePeriods = ToolDeclaration(
        name = "compare_periods",
        description = "Compare business metrics between two periods.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "period1",
                type = "string",
                description = "First period"
            ),
            ToolParameter(
                name = "period2",
                type = "string",
                description = "Second period"
            ),
            ToolParameter(
                name = "metrics",
                type = "string",
                description = "Metrics to compare (comma-separated)",
                required = false
            )
        )
    )

    /**
     * Get dashboard summary
     */
    val getDashboardSummary = ToolDeclaration(
        name = "get_dashboard_summary",
        description = "Get all key metrics for the dashboard overview.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf()
    )

    /**
     * Get slow moving products
     */
    val getSlowMovingProducts = ToolDeclaration(
        name = "get_slow_moving_products",
        description = "Identify products with slow sales that may need attention.",
        category = ToolCategory.ANALYTICS_REPORTING,
        parameters = listOf(
            ToolParameter(
                name = "days_threshold",
                type = "integer",
                description = "Days without sale to be considered slow (default 30)",
                required = false
            ),
            ToolParameter(
                name = "limit",
                type = "integer",
                description = "Number of products to return",
                required = false
            )
        )
    )

    // ============================================================================
    // SETTINGS & CONFIGURATION (10 tools)
    // ============================================================================

    /**
     * Get settings
     */
    val getSettings = ToolDeclaration(
        name = "get_settings",
        description = "Get current app settings and configuration.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "category",
                type = "string",
                description = "Settings category",
                required = false,
                enumValues = listOf("general", "voice", "sync", "notifications", "security", "all")
            )
        )
    )

    /**
     * Update language setting
     */
    val updateLanguage = ToolDeclaration(
        name = "update_language",
        description = "Change the app language for voice and UI.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "language",
                type = "string",
                description = "Language to set",
                enumValues = listOf("english", "nyanja", "bemba")
            )
        )
    )

    /**
     * Enable/disable PIN protection
     */
    val setPinProtection = ToolDeclaration(
        name = "set_pin_protection",
        description = "Enable or disable PIN protection for the app.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "enabled",
                type = "boolean",
                description = "Enable PIN protection"
            ),
            ToolParameter(
                name = "pin",
                type = "string",
                description = "4-digit PIN (required if enabling)",
                required = false
            )
        )
    )

    /**
     * Update sync settings
     */
    val updateSyncSettings = ToolDeclaration(
        name = "update_sync_settings",
        description = "Update cloud sync configuration.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "auto_sync",
                type = "boolean",
                description = "Enable automatic sync",
                required = false
            ),
            ToolParameter(
                name = "wifi_only",
                type = "boolean",
                description = "Only sync on WiFi",
                required = false
            ),
            ToolParameter(
                name = "sync_frequency",
                type = "string",
                description = "How often to sync",
                required = false,
                enumValues = listOf("every_15_min", "every_hour", "every_6_hours", "daily")
            )
        )
    )

    /**
     * Set low stock threshold
     */
    val setLowStockThreshold = ToolDeclaration(
        name = "set_low_stock_threshold",
        description = "Set the default low stock alert threshold.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "threshold",
                type = "integer",
                description = "Default threshold quantity"
            )
        )
    )

    /**
     * Update notification settings
     */
    val updateNotificationSettings = ToolDeclaration(
        name = "update_notification_settings",
        description = "Configure notification preferences.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "low_stock_alerts",
                type = "boolean",
                description = "Enable low stock notifications",
                required = false
            ),
            ToolParameter(
                name = "payment_due_alerts",
                type = "boolean",
                description = "Enable payment due reminders",
                required = false
            ),
            ToolParameter(
                name = "daily_summary",
                type = "boolean",
                description = "Enable daily summary notification",
                required = false
            )
        )
    )

    /**
     * Set currency format
     */
    val setCurrencyFormat = ToolDeclaration(
        name = "set_currency_format",
        description = "Configure currency display format.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "symbol",
                type = "string",
                description = "Currency symbol (default K)",
                required = false
            ),
            ToolParameter(
                name = "position",
                type = "string",
                description = "Symbol position",
                required = false,
                enumValues = listOf("before", "after")
            ),
            ToolParameter(
                name = "decimal_places",
                type = "integer",
                description = "Number of decimal places",
                required = false
            )
        )
    )

    /**
     * Set shop details
     */
    val setShopDetails = ToolDeclaration(
        name = "set_shop_details",
        description = "Set or update shop/business information.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "shop_name",
                type = "string",
                description = "Name of the shop",
                required = false
            ),
            ToolParameter(
                name = "owner_name",
                type = "string",
                description = "Owner's name",
                required = false
            ),
            ToolParameter(
                name = "phone",
                type = "string",
                description = "Shop phone number",
                required = false
            ),
            ToolParameter(
                name = "address",
                type = "string",
                description = "Shop address",
                required = false
            )
        )
    )

    /**
     * Reset settings to default
     */
    val resetSettings = ToolDeclaration(
        name = "reset_settings",
        description = "Reset all settings to default values.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf(
            ToolParameter(
                name = "confirm",
                type = "boolean",
                description = "Confirmation required"
            )
        )
    )

    /**
     * Get app info
     */
    val getAppInfo = ToolDeclaration(
        name = "get_app_info",
        description = "Get app version, storage usage, and other info.",
        category = ToolCategory.SETTINGS_CONFIGURATION,
        parameters = listOf()
    )

    // ============================================================================
    // BACKUP/EXPORT/IMPORT (10 tools)
    // ============================================================================

    /**
     * Backup data
     */
    val backupData = ToolDeclaration(
        name = "backup_data",
        description = "Create a backup of all shop data.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf(
            ToolParameter(
                name = "backup_type",
                type = "string",
                description = "Type of backup",
                required = false,
                enumValues = listOf("full", "products_only", "sales_only", "customers_only")
            ),
            ToolParameter(
                name = "destination",
                type = "string",
                description = "Backup destination",
                required = false,
                enumValues = listOf("local", "cloud", "both")
            )
        )
    )

    /**
     * Restore data
     */
    val restoreData = ToolDeclaration(
        name = "restore_data",
        description = "Restore data from a backup. Requires confirmation.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf(
            ToolParameter(
                name = "backup_id",
                type = "string",
                description = "ID of backup to restore",
                required = false
            ),
            ToolParameter(
                name = "confirm",
                type = "boolean",
                description = "Confirmation required"
            )
        )
    )

    /**
     * Sync to cloud
     */
    val syncToCloud = ToolDeclaration(
        name = "sync_to_cloud",
        description = "Manually trigger sync to cloud storage.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf()
    )

    /**
     * Sync from cloud
     */
    val syncFromCloud = ToolDeclaration(
        name = "sync_from_cloud",
        description = "Pull latest data from cloud storage.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf()
    )

    /**
     * Get sync status
     */
    val getSyncStatus = ToolDeclaration(
        name = "get_sync_status",
        description = "Check the current sync status and last sync time.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf()
    )

    /**
     * Export to CSV
     */
    val exportToCsv = ToolDeclaration(
        name = "export_to_csv",
        description = "Export data to CSV file for spreadsheet use.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf(
            ToolParameter(
                name = "data_type",
                type = "string",
                description = "What data to export",
                enumValues = listOf("products", "sales", "customers", "credits", "payments", "inventory_logs")
            ),
            ToolParameter(
                name = "period",
                type = "string",
                description = "Time period for sales/payments",
                required = false
            )
        )
    )

    /**
     * Export to PDF
     */
    val exportToPdf = ToolDeclaration(
        name = "export_to_pdf",
        description = "Generate a PDF report.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf(
            ToolParameter(
                name = "report_type",
                type = "string",
                description = "Type of report",
                enumValues = listOf("inventory_list", "sales_report", "credit_report", "customer_statement", "daily_summary")
            ),
            ToolParameter(
                name = "period",
                type = "string",
                description = "Report period",
                required = false
            ),
            ToolParameter(
                name = "customer_name",
                type = "string",
                description = "Customer for statement",
                required = false
            )
        )
    )

    /**
     * Import products
     */
    val importProducts = ToolDeclaration(
        name = "import_products",
        description = "Import products from a CSV file.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf(
            ToolParameter(
                name = "file_path",
                type = "string",
                description = "Path to CSV file"
            ),
            ToolParameter(
                name = "update_existing",
                type = "boolean",
                description = "Update existing products if found",
                required = false
            )
        )
    )

    /**
     * List backups
     */
    val listBackups = ToolDeclaration(
        name = "list_backups",
        description = "List all available backups.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf()
    )

    /**
     * Delete backup
     */
    val deleteBackup = ToolDeclaration(
        name = "delete_backup",
        description = "Delete a backup file.",
        category = ToolCategory.BACKUP_EXPORT_IMPORT,
        parameters = listOf(
            ToolParameter(
                name = "backup_id",
                type = "string",
                description = "Backup ID to delete"
            ),
            ToolParameter(
                name = "confirm",
                type = "boolean",
                description = "Confirmation required"
            )
        )
    )

    // ============================================================================
    // BARCODE/SCANNER (5 tools)
    // ============================================================================

    /**
     * Scan product barcode
     */
    val scanProductBarcode = ToolDeclaration(
        name = "scan_product_barcode",
        description = "Initiate barcode scanning to find or add a product.",
        category = ToolCategory.BARCODE_SCANNER,
        parameters = listOf(
            ToolParameter(
                name = "action",
                type = "string",
                description = "What to do after scanning",
                required = false,
                enumValues = listOf("find_product", "add_product", "record_sale")
            )
        )
    )

    /**
     * Find product by barcode
     */
    val findByBarcode = ToolDeclaration(
        name = "find_by_barcode",
        description = "Find a product using its barcode.",
        category = ToolCategory.BARCODE_SCANNER,
        parameters = listOf(
            ToolParameter(
                name = "barcode",
                type = "string",
                description = "Barcode to search for"
            )
        )
    )

    /**
     * Update product barcode
     */
    val updateProductBarcode = ToolDeclaration(
        name = "update_product_barcode",
        description = "Set or update the barcode for a product.",
        category = ToolCategory.BARCODE_SCANNER,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Product to update"
            ),
            ToolParameter(
                name = "barcode",
                type = "string",
                description = "New barcode value"
            )
        )
    )

    /**
     * Generate barcode
     */
    val generateBarcode = ToolDeclaration(
        name = "generate_barcode",
        description = "Generate a barcode image for a product.",
        category = ToolCategory.BARCODE_SCANNER,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Product to generate barcode for"
            ),
            ToolParameter(
                name = "format",
                type = "string",
                description = "Barcode format",
                required = false,
                enumValues = listOf("EAN_13", "CODE_128", "QR_CODE")
            )
        )
    )

    /**
     * Get products without barcode
     */
    val getProductsWithoutBarcode = ToolDeclaration(
        name = "get_products_without_barcode",
        description = "List all products that don't have a barcode assigned.",
        category = ToolCategory.BARCODE_SCANNER,
        parameters = listOf()
    )

    // ============================================================================
    // NAVIGATION/UI (12 tools)
    // ============================================================================

    /**
     * Go to dashboard
     */
    val goToDashboard = ToolDeclaration(
        name = "go_to_dashboard",
        description = "Navigate to the main dashboard screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go to products
     */
    val goToProducts = ToolDeclaration(
        name = "go_to_products",
        description = "Navigate to the products list screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go to sales
     */
    val goToSales = ToolDeclaration(
        name = "go_to_sales",
        description = "Navigate to the sales screen or quick sale.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go to customers
     */
    val goToCustomers = ToolDeclaration(
        name = "go_to_customers",
        description = "Navigate to the customers list screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go to analytics
     */
    val goToAnalytics = ToolDeclaration(
        name = "go_to_analytics",
        description = "Navigate to the analytics/reports screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go to credits
     */
    val goToCredits = ToolDeclaration(
        name = "go_to_credits",
        description = "Navigate to the credit ledger screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go to settings
     */
    val goToSettings = ToolDeclaration(
        name = "go_to_settings",
        description = "Navigate to the settings screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go to add product
     */
    val goToAddProduct = ToolDeclaration(
        name = "go_to_add_product",
        description = "Navigate to the add product screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go to add customer
     */
    val goToAddCustomer = ToolDeclaration(
        name = "go_to_add_customer",
        description = "Navigate to the add customer screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Go back
     */
    val goBack = ToolDeclaration(
        name = "go_back",
        description = "Navigate back to the previous screen.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Open scanner
     */
    val openScanner = ToolDeclaration(
        name = "open_scanner",
        description = "Open the barcode scanner.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf()
    )

    /**
     * Open product detail
     */
    val openProductDetail = ToolDeclaration(
        name = "open_product_detail",
        description = "Open the detail page for a specific product.",
        category = ToolCategory.NAVIGATION_UI,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Product to view"
            )
        )
    )

    // ============================================================================
    // MACHINE LEARNING (5 tools)
    // ============================================================================

    /**
     * Classify product image
     */
    val classifyProductImage = ToolDeclaration(
        name = "classify_product_image",
        description = "Use AI to identify a product from an image.",
        category = ToolCategory.MACHINE_LEARNING,
        parameters = listOf(
            ToolParameter(
                name = "action_on_match",
                type = "string",
                description = "What to do if product is identified",
                required = false,
                enumValues = listOf("add_to_cart", "view_details", "update_stock", "just_identify")
            )
        )
    )

    /**
     * Get ML model status
     */
    val getMlModelStatus = ToolDeclaration(
        name = "get_ml_model_status",
        description = "Check the status of ML models (product classifier, voice recognition).",
        category = ToolCategory.MACHINE_LEARNING,
        parameters = listOf()
    )

    /**
     * Suggest product price
     */
    val suggestProductPrice = ToolDeclaration(
        name = "suggest_product_price",
        description = "Get AI-suggested pricing based on similar products and market data.",
        category = ToolCategory.MACHINE_LEARNING,
        parameters = listOf(
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Product to price"
            ),
            ToolParameter(
                name = "buying_price",
                type = "number",
                description = "Cost price if known",
                required = false
            )
        )
    )

    /**
     * Predict low stock
     */
    val predictLowStock = ToolDeclaration(
        name = "predict_low_stock",
        description = "Predict which products will run low based on sales patterns.",
        category = ToolCategory.MACHINE_LEARNING,
        parameters = listOf(
            ToolParameter(
                name = "days_ahead",
                type = "integer",
                description = "How many days to predict",
                required = false
            )
        )
    )

    /**
     * Get sales forecast
     */
    val getSalesForecast = ToolDeclaration(
        name = "get_sales_forecast",
        description = "Get AI-powered sales forecast based on historical data.",
        category = ToolCategory.MACHINE_LEARNING,
        parameters = listOf(
            ToolParameter(
                name = "period",
                type = "string",
                description = "Forecast period",
                enumValues = listOf("next_week", "next_month")
            ),
            ToolParameter(
                name = "product_name",
                type = "string",
                description = "Forecast for specific product",
                required = false
            )
        )
    )

    // ============================================================================
    // VOICE FRAMEWORK (6 tools)
    // ============================================================================

    /**
     * Set voice language
     */
    val setVoiceLanguage = ToolDeclaration(
        name = "set_voice_language",
        description = "Change the voice recognition and response language.",
        category = ToolCategory.VOICE_FRAMEWORK,
        parameters = listOf(
            ToolParameter(
                name = "language",
                type = "string",
                description = "Language for voice",
                enumValues = listOf("english", "nyanja", "bemba")
            )
        )
    )

    /**
     * Help with voice commands
     */
    val helpWithVoiceCommands = ToolDeclaration(
        name = "help_with_voice_commands",
        description = "Get help and examples of available voice commands.",
        category = ToolCategory.VOICE_FRAMEWORK,
        parameters = listOf(
            ToolParameter(
                name = "category",
                type = "string",
                description = "Command category to get help for",
                required = false,
                enumValues = listOf("sales", "products", "customers", "credits", "analytics", "navigation", "all")
            )
        )
    )

    /**
     * Repeat last response
     */
    val repeatLastResponse = ToolDeclaration(
        name = "repeat_last_response",
        description = "Repeat the last voice response from the assistant.",
        category = ToolCategory.VOICE_FRAMEWORK,
        parameters = listOf()
    )

    /**
     * Get voice command history
     */
    val getVoiceCommandHistory = ToolDeclaration(
        name = "get_voice_command_history",
        description = "Get the history of recent voice commands.",
        category = ToolCategory.VOICE_FRAMEWORK,
        parameters = listOf(
            ToolParameter(
                name = "limit",
                type = "integer",
                description = "Number of commands to return",
                required = false
            )
        )
    )

    /**
     * Toggle voice feedback
     */
    val toggleVoiceFeedback = ToolDeclaration(
        name = "toggle_voice_feedback",
        description = "Enable or disable voice/speech feedback.",
        category = ToolCategory.VOICE_FRAMEWORK,
        parameters = listOf(
            ToolParameter(
                name = "enabled",
                type = "boolean",
                description = "Enable voice feedback"
            )
        )
    )

    /**
     * Cancel current operation
     */
    val cancelOperation = ToolDeclaration(
        name = "cancel_operation",
        description = "Cancel the current voice operation or pending command.",
        category = ToolCategory.VOICE_FRAMEWORK,
        parameters = listOf()
    )

    // ============================================================================
    // VALIDATION & CONFIRMATION (3 tools)
    // ============================================================================

    /**
     * Verify PIN
     */
    val verifyPin = ToolDeclaration(
        name = "verify_pin",
        description = "Verify the user's PIN for sensitive operations.",
        category = ToolCategory.VALIDATION,
        parameters = listOf(
            ToolParameter(
                name = "pin",
                type = "string",
                description = "4-digit PIN to verify"
            )
        )
    )

    /**
     * Confirm operation
     */
    val confirmOperation = ToolDeclaration(
        name = "confirm_operation",
        description = "Confirm a pending operation that requires user approval.",
        category = ToolCategory.VALIDATION,
        parameters = listOf(
            ToolParameter(
                name = "operation_id",
                type = "string",
                description = "ID of the pending operation"
            ),
            ToolParameter(
                name = "confirmed",
                type = "boolean",
                description = "User confirmation (yes/no)"
            )
        )
    )

    /**
     * Cancel confirmation
     */
    val cancelConfirmation = ToolDeclaration(
        name = "cancel_confirmation",
        description = "Cancel a pending confirmation request.",
        category = ToolCategory.VALIDATION,
        parameters = listOf()
    )

    // ============================================================================
    // ALL TOOLS COLLECTION
    // ============================================================================

    /**
     * All available tools for DukaAI organized by category
     */
    val allTools: List<ToolDeclaration> by lazy {
        listOf(
            // Product Management (14 tools)
            addProduct, editProduct, deleteProduct, getProductDetails, checkStock,
            updateStock, searchProducts, getLowStockAlerts, listProducts, getOutOfStock,
            getTopSellingProducts, getProductCategories, updateProductPrice, getProductProfitMargin,

            // Sales Operations (10 tools)
            recordSale, recordBatchSale, editSale, deleteSale, getSaleHistory,
            getTodaySales, getRecentSales, calculateSaleTotal, getSalesByPaymentType, processRefund,

            // Credit Management (11 tools)
            recordCreditSale, getCustomerBalance, getAllCreditBalances, getOverdueCredits,
            markCreditAsPaid, getCreditHistory, sendCreditReminder, getTotalCreditsOwed,
            updateCreditDueDate, setCustomerCreditLimit, getCustomersNearCreditLimit,

            // Customer Management (10 tools)
            addCustomer, updateCustomer, deleteCustomer, searchCustomers, getCustomerDetails,
            listCustomers, getCustomerPurchaseHistory, getTopCustomers, getCustomersCount, getNewCustomers,

            // Payment Operations (11 tools)
            recordPayment, getPaymentHistory, getTodayPayments, getTotalPayments, editPayment,
            deletePayment, getPaymentsByMethod, getRecentPayments, allocatePayment,
            getExpectedPayments, generatePaymentReceipt,

            // Inventory Operations (4 tools)
            getInventoryHistory, getInventoryValue, recordStockTake, getInventorySummary,

            // Analytics & Reporting (10 tools)
            getSalesAnalytics, getDailySalesData, getProfitAnalytics, getRevenueTrends,
            getCategoryPerformance, getHourlySalesPattern, generateReport, comparePeriods,
            getDashboardSummary, getSlowMovingProducts,

            // Settings & Configuration (10 tools)
            getSettings, updateLanguage, setPinProtection, updateSyncSettings, setLowStockThreshold,
            updateNotificationSettings, setCurrencyFormat, setShopDetails, resetSettings, getAppInfo,

            // Backup/Export/Import (10 tools)
            backupData, restoreData, syncToCloud, syncFromCloud, getSyncStatus,
            exportToCsv, exportToPdf, importProducts, listBackups, deleteBackup,

            // Barcode/Scanner (5 tools)
            scanProductBarcode, findByBarcode, updateProductBarcode, generateBarcode, getProductsWithoutBarcode,

            // Navigation/UI (12 tools)
            goToDashboard, goToProducts, goToSales, goToCustomers, goToAnalytics,
            goToCredits, goToSettings, goToAddProduct, goToAddCustomer, goBack,
            openScanner, openProductDetail,

            // Machine Learning (5 tools)
            classifyProductImage, getMlModelStatus, suggestProductPrice, predictLowStock, getSalesForecast,

            // Voice Framework (6 tools)
            setVoiceLanguage, helpWithVoiceCommands, repeatLastResponse, getVoiceCommandHistory,
            toggleVoiceFeedback, cancelOperation,

            // Validation & Confirmation (3 tools)
            verifyPin, confirmOperation, cancelConfirmation
        )
    }

    /**
     * Get tools by category
     */
    fun getToolsByCategory(category: ToolCategory): List<ToolDeclaration> {
        return allTools.filter { it.category == category }
    }

    /**
     * Get tool by name
     */
    fun getToolByName(name: String): ToolDeclaration? {
        return allTools.find { it.name == name }
    }

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
     * Convert tool schema to FunctionGemma format for specific categories
     */
    fun toFunctionGemmaFormat(categories: List<ToolCategory>): String {
        return buildString {
            allTools.filter { it.category in categories }.forEach { tool ->
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

    /**
     * Get tool count summary by category
     */
    fun getToolCountSummary(): Map<ToolCategory, Int> {
        return allTools.groupBy { it.category }.mapValues { it.value.size }
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

    data class NavigationAction(
        val destination: String,
        val params: Map<String, Any?> = emptyMap()
    ) : FunctionExecutionResult()

    data class RequiresPin(
        val operation: String,
        val message: String
    ) : FunctionExecutionResult()
}
