# DukaAI FunctionGemma NLU System Documentation

## Overview

DukaAI uses a FunctionGemma-based Natural Language Understanding (NLU) system that enables voice and text command processing for Zambian retail store management. The system translates natural language commands into structured function calls that execute against the app's data repositories.

## Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            User Input (Voice/Text)                          │
│                     "Sell 3 Coca-Cola to John on credit"                    │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          FunctionGemmaService                                │
│                    (High-level orchestration layer)                          │
│  • Coordinates the entire processing pipeline                                │
│  • Handles fallback pattern-based parsing when TFLite unavailable           │
│  • Returns ProcessingResult with execution outcomes                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                    ┌──────────────────┼──────────────────┐
                    ▼                  ▼                  ▼
     ┌──────────────────────┐ ┌───────────────────┐ ┌─────────────────────┐
     │ FunctionGemmaInference│ │ FunctionGemmaParser│ │ DukaFunctionExecutor│
     │  (TFLite Model)       │ │ (Output Parser)    │ │ (Business Logic)    │
     │                       │ │                    │ │                     │
     │ • Loads TFLite model  │ │ • Extracts calls   │ │ • Maps to repos     │
     │ • Runs inference      │ │ • Validates params │ │ • Executes 121 tools│
     │ • Generates func call │ │ • Formats response │ │ • Returns results   │
     └──────────────────────┘ └───────────────────┘ └─────────────────────┘
                                                              │
                                       ┌──────────────────────┼────────────────┐
                                       ▼                      ▼                ▼
                              ┌────────────────┐    ┌──────────────┐   ┌─────────────┐
                              │ProductRepository│    │SaleRepository│   │CustomerRepo │
                              │InventoryLogDao │    │CreditRepo    │   │PaymentRepo  │
                              └────────────────┘    └──────────────┘   └─────────────┘
```

## Core Components

### 1. FunctionGemmaService

**File:** `app/src/main/java/com/example/dukaai/ml/functiongemma/FunctionGemmaService.kt`

The main entry point for processing natural language commands.

```kotlin
@Singleton
class FunctionGemmaService @Inject constructor(
    private val inference: FunctionGemmaInference,
    private val parser: FunctionGemmaParser,
    private val executor: DukaFunctionExecutor
)
```

**Key Methods:**
- `processCommand(userInput: String): ProcessingResult` - Main processing pipeline
- `processWithFallback(userInput: String)` - Pattern-based fallback when TFLite unavailable
- `isModelReady(): Boolean` - Check if TFLite model is loaded
- `initializeModel(): Result<Unit>` - Initialize the TFLite model

**Processing Flow:**
1. Initialize TFLite model if needed
2. Generate function call(s) from model
3. Parse function call(s) using control tokens
4. Validate parameters against tool schema
5. Execute function call(s) via DukaFunctionExecutor
6. Build and return ProcessingResult

### 2. FunctionGemmaParser

**File:** `app/src/main/java/com/example/dukaai/ml/functiongemma/FunctionGemmaParser.kt`

Parses model output using FunctionGemma control tokens.

**Control Tokens:**
- `<start_function_call>` / `<end_function_call>` - Delimit function calls
- `<escape>` - Delimiter for string parameter values
- `<start_function_response>` / `<end_function_response>` - For multi-turn responses

**Example Model Output:**
```
<start_function_call>record_sale(product_name=<escape>Coca-Cola<escape>, quantity=3, sale_type=<escape>credit<escape>)<end_function_call>
```

**Key Methods:**
- `parse(modelOutput: String): List<ParsedFunctionCall>` - Extract function calls
- `validateFunctionCall(call, tool): List<String>` - Validate parameters
- `findMatchingTool(functionName): ToolDeclaration?` - Find tool schema
- `formatFunctionResponse(functionName, response): String` - Format for multi-turn

**Extension Functions:**
```kotlin
fun ParsedFunctionCall.getStringArg(name: String, default: String = ""): String
fun ParsedFunctionCall.getIntArg(name: String, default: Int = 0): Int
fun ParsedFunctionCall.getDoubleArg(name: String, default: Double = 0.0): Double
fun ParsedFunctionCall.getBooleanArg(name: String, default: Boolean = false): Boolean
fun ParsedFunctionCall.hasArg(name: String): Boolean
fun ParsedFunctionCall.getStringArgOrNull(name: String): String?
fun ParsedFunctionCall.getIntArgOrNull(name: String): Int?
fun ParsedFunctionCall.getDoubleArgOrNull(name: String): Double?
fun ParsedFunctionCall.getBooleanArgOrNull(name: String): Boolean?
```

### 3. DukaFunctionExecutor

**File:** `app/src/main/java/com/example/dukaai/ml/functiongemma/DukaFunctionExecutor.kt`

Executes parsed function calls against the app's data repositories.

```kotlin
@Singleton
class DukaFunctionExecutor @Inject constructor(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val creditRepository: CreditRepository,
    private val paymentRepository: PaymentRepository,
    private val inventoryLogDao: InventoryLogDao
)
```

**Key Methods:**
- `execute(call: ParsedFunctionCall): FunctionExecutionResult` - Execute single call
- `executeAll(calls: List<ParsedFunctionCall>): List<FunctionExecutionResult>` - Execute multiple calls

### 4. DukaToolSchema

**File:** `app/src/main/java/com/example/dukaai/ml/functiongemma/DukaToolSchema.kt`

Defines all 121 tool declarations with parameters and descriptions.

## Tool Categories and Implementations

### 1. Product Management (14 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `add_product` | Add new product to inventory | name, selling_price, category?, initial_stock?, buying_price?, barcode?, low_stock_threshold?, unit? |
| `edit_product` | Edit existing product details | product_name, new_name?, new_selling_price?, new_buying_price?, new_category?, new_low_stock_threshold? |
| `delete_product` | Delete product from inventory | product_name, confirm? |
| `get_product_details` | Get detailed product information | product_name |
| `check_stock` | Check current stock level | product_name |
| `update_stock` | Update stock quantity | product_name, quantity, reason? |
| `search_products` | Search products by name/category/barcode | query, search_type? |
| `get_low_stock_alerts` | Get products below threshold | threshold? |
| `list_products` | List all products with filters | category?, sort_by?, sort_order?, limit? |
| `get_out_of_stock` | Get zero-stock products | (none) |
| `get_top_selling_products` | Get top sellers by metric | period, metric?, limit? |
| `get_product_categories` | Get all categories with counts | (none) |
| `update_product_price` | Update selling/buying price | product_name, new_selling_price?, new_buying_price? |
| `get_product_profit_margin` | Calculate profit margin | product_name |

### 2. Sales Operations (10 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `record_sale` | Record a sale transaction | product_name, quantity, customer_name?, sale_type?, unit_price?, discount? |
| `record_batch_sale` | Record multiple items in one transaction | items, customer_name?, sale_type? |
| `edit_sale` | Edit existing sale | sale_id, new_quantity?, new_unit_price? |
| `delete_sale` | Delete/void a sale | sale_id, reason?, confirm? |
| `get_sale_history` | Get sales history | period?, product_name?, customer_name?, sale_type? |
| `get_today_sales` | Get today's sales summary | (none) |
| `get_recent_sales` | Get most recent sales | limit? |
| `calculate_sale_total` | Calculate total before recording | items, discount? |
| `get_sales_by_payment_type` | Get cash vs credit breakdown | period |
| `process_refund` | Process a sale refund | sale_id, refund_amount?, reason? |

### 3. Credit/Debt Management (11 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `record_credit_sale` | Record credit sale (pa ng'ong'ole) | customer_name, product_name, quantity, due_date? |
| `get_customer_balance` | Get customer's outstanding balance | customer_name |
| `get_all_credit_balances` | Get all outstanding balances | sort_by? |
| `get_overdue_credits` | Get past-due accounts | days_overdue? |
| `mark_credit_as_paid` | Mark credit entry as paid | credit_id |
| `get_credit_history` | Get customer's credit history | customer_name, status? |
| `send_credit_reminder` | Send payment reminder | customer_name, channel? |
| `get_total_credits_owed` | Get total outstanding amount | (none) |
| `update_credit_due_date` | Update credit due date | credit_id, new_due_date |
| `set_customer_credit_limit` | Set max credit limit | customer_name, credit_limit |
| `get_customers_near_credit_limit` | Get customers near limit | threshold_percent? |

### 4. Customer Management (10 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `add_customer` | Add new customer | name, phone?, address?, notes? |
| `update_customer` | Update customer details | customer_name, new_name?, new_phone?, new_address?, new_notes? |
| `delete_customer` | Delete customer | customer_name, confirm? |
| `get_customer_details` | Get customer information | customer_name |
| `search_customers` | Search customers by name/phone | query |
| `list_customers` | List all customers | sort_by?, limit? |
| `get_customer_purchase_history` | Get customer's purchases | customer_name, period? |
| `get_top_customers` | Get top customers by spend | period?, limit? |
| `get_customer_count` | Get total customer count | (none) |
| `get_customers_with_balance` | Get customers with outstanding balance | (none) |

### 5. Payment Operations (11 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `record_payment` | Record customer payment | customer_name, amount, payment_method?, notes? |
| `record_partial_payment` | Record partial payment | customer_name, amount, credit_id? |
| `get_payment_history` | Get customer's payment history | customer_name, period? |
| `get_today_payments` | Get today's payments | (none) |
| `void_payment` | Void a payment | payment_id, reason? |
| `get_payment_summary` | Get payment summary by method | period |
| `get_expected_payments` | Get payments due today/soon | days_ahead? |
| `send_payment_reminder` | Send payment reminder | customer_name, channel? |
| `record_mobile_money_payment` | Record mobile money payment | customer_name, amount, provider, transaction_id? |
| `get_mobile_money_summary` | Get mobile money summary | period, provider? |
| `reconcile_payments` | Reconcile payments for date | date |

### 6. Inventory Operations (4 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `get_inventory_log` | Get inventory change history | product_name?, period?, reason? |
| `get_inventory_alerts` | Get all inventory alerts | alert_type? |
| `adjust_inventory` | Adjust inventory with reason | product_name, adjustment, reason |
| `get_inventory_value` | Calculate total inventory value | category? |

### 7. Analytics & Reporting (10 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `get_sales_analytics` | Get sales analytics | period, group_by? |
| `get_revenue_summary` | Get revenue summary | period |
| `get_product_performance` | Get product performance metrics | product_name, period? |
| `get_category_performance` | Get category sales performance | period? |
| `compare_periods` | Compare two time periods | period1, period2, metric? |
| `get_daily_sales_data` | Get daily sales breakdown | period? |
| `get_profit_analytics` | Get profit analytics | period, breakdown? |
| `get_customer_analytics` | Get customer analytics | period? |
| `get_hourly_sales_pattern` | Get sales by hour | period? |
| `generate_report` | Generate a report | report_type, format? |

### 8. Settings & Configuration (10 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `get_settings` | Get current app settings | (none) |
| `update_setting` | Update a setting | setting_name, value |
| `set_currency` | Set currency display | currency_code |
| `set_low_stock_threshold` | Set default low stock threshold | threshold |
| `set_language` | Set app language | language |
| `get_business_profile` | Get business profile | (none) |
| `update_business_profile` | Update business profile | name?, phone?, address?, logo? |
| `set_receipt_footer` | Set receipt footer text | footer_text |
| `toggle_feature` | Enable/disable feature | feature_name, enabled |
| `reset_settings` | Reset to default settings | confirm? |

### 9. Backup/Export/Import (10 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `create_backup` | Create data backup | backup_type?, include_images? |
| `restore_backup` | Restore from backup | backup_id, confirm? |
| `list_backups` | List available backups | (none) |
| `delete_backup` | Delete a backup | backup_id, confirm? |
| `export_data` | Export data to file | data_type, format, period? |
| `import_products` | Import products from file | file_path, format? |
| `import_customers` | Import customers from file | file_path, format? |
| `sync_to_cloud` | Sync data to cloud | (none) |
| `sync_from_cloud` | Sync data from cloud | (none) |
| `get_sync_status` | Get cloud sync status | (none) |

### 10. Barcode/Scanner (5 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `scan_barcode` | Activate barcode scanner | (none) |
| `lookup_barcode` | Look up product by barcode | barcode |
| `assign_barcode` | Assign barcode to product | product_name, barcode |
| `remove_barcode` | Remove barcode from product | product_name |
| `get_products_without_barcode` | Get products without barcodes | (none) |

### 11. Navigation/UI (12 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `navigate_to_screen` | Navigate to app screen | screen_name |
| `go_back` | Go to previous screen | (none) |
| `go_home` | Go to home/dashboard | (none) |
| `open_quick_sale` | Open quick sale screen | (none) |
| `open_add_product` | Open add product screen | (none) |
| `open_inventory` | Open inventory screen | (none) |
| `open_customers` | Open customers screen | (none) |
| `open_analytics` | Open analytics screen | (none) |
| `open_settings` | Open settings screen | (none) |
| `open_credit_ledger` | Open credit ledger | (none) |
| `show_help` | Show help for topic | topic? |
| `show_tutorial` | Show app tutorial | tutorial_name? |

### 12. Machine Learning (5 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `classify_product` | Classify product from image | image_path |
| `suggest_price` | Suggest price for product | product_name |
| `predict_stock_needs` | Predict restocking needs | product_name?, days_ahead? |
| `get_demand_forecast` | Get demand forecast | product_name, period? |
| `retrain_classifier` | Retrain product classifier | (none) |

### 13. Voice Framework (6 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `set_voice_language` | Set voice recognition language | language |
| `toggle_voice_feedback` | Enable/disable voice feedback | enabled |
| `repeat_last_response` | Repeat last response | (none) |
| `get_voice_commands` | Get available commands | category? |
| `train_voice_command` | Train custom voice command | phrase, action |
| `test_voice_recognition` | Test voice recognition | (none) |

### 14. Validation & Confirmation (3 tools)

| Tool Name | Description | Parameters |
|-----------|-------------|------------|
| `confirm_action` | Confirm pending action | action_id, confirmed |
| `cancel_action` | Cancel pending action | action_id? |
| `get_pending_confirmations` | Get pending confirmations | (none) |

## Execution Results

The `DukaFunctionExecutor` returns `FunctionExecutionResult` sealed class:

```kotlin
sealed class FunctionExecutionResult {
    data class Success(
        val functionName: String,
        val message: String,
        val data: Any? = null
    ) : FunctionExecutionResult()

    data class Error(
        val functionName: String,
        val errorMessage: String
    ) : FunctionExecutionResult()

    data class NeedsConfirmation(
        val functionName: String,
        val message: String,
        val confirmationId: String,
        val data: Any? = null
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
        val availableStock: Int,
        val requestedQuantity: Int
    ) : FunctionExecutionResult()

    data class NavigationAction(
        val destination: String,
        val params: Map<String, Any?> = emptyMap()
    ) : FunctionExecutionResult()

    data class RequiresPin(
        val message: String,
        val actionId: String
    ) : FunctionExecutionResult()
}
```

## Data Models

### ProductEntity

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val barcode: String? = null,
    val category: String,
    val currentStock: Int = 0,
    val minStockThreshold: Int = 10,
    val buyingPrice: Double,
    val sellingPrice: Double,
    val unit: String = "pieces",  // pieces, kg, liters, packets, boxes, bags
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### CustomerEntity

```kotlin
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String? = null,
    val address: String? = null,
    val notes: String? = null,
    val creditLimit: Double? = null,  // Maximum credit allowed
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
```

### SaleEntity

```kotlin
@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val customerId: String? = null,
    val quantity: Int,
    val unitPrice: Double,
    val totalAmount: Double,
    val saleType: String,  // "cash" or "credit"
    val timestamp: Long = System.currentTimeMillis()
)
```

## Dependency Injection

### FunctionGemmaModule

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object FunctionGemmaModule {

    @Provides @Singleton
    fun provideFunctionGemmaInference(
        @ApplicationContext context: Context
    ): FunctionGemmaInference = FunctionGemmaInference(context)

    @Provides @Singleton
    fun provideFunctionGemmaParser(): FunctionGemmaParser = FunctionGemmaParser()

    @Provides @Singleton
    fun provideDukaFunctionExecutor(
        productRepository: ProductRepository,
        saleRepository: SaleRepository,
        customerRepository: CustomerRepository,
        creditRepository: CreditRepository,
        paymentRepository: PaymentRepository,
        inventoryLogDao: InventoryLogDao
    ): DukaFunctionExecutor = DukaFunctionExecutor(
        productRepository, saleRepository, customerRepository,
        creditRepository, paymentRepository, inventoryLogDao
    )

    @Provides @Singleton
    fun provideFunctionGemmaService(
        inference: FunctionGemmaInference,
        parser: FunctionGemmaParser,
        executor: DukaFunctionExecutor
    ): FunctionGemmaService = FunctionGemmaService(inference, parser, executor)
}
```

## Utility Classes

### DateUtils

**File:** `app/src/main/java/com/example/dukaai/util/DateUtils.kt`

Provides date/time operations for analytics and filtering:

```kotlin
object DateUtils {
    fun getStartOfDay(): Long
    fun getEndOfDay(): Long
    fun getStartOfDay(timestamp: Long): Long
    fun getEndOfDay(timestamp: Long): Long
    fun getStartOfWeek(): Long
    fun getStartOfMonth(): Long
    fun getEndOfMonth(): Long
    fun getDaysAgo(days: Int): Long
    fun getStartOfYesterday(): Long
    fun getEndOfYesterday(): Long
    fun formatDate(timestamp: Long): String      // "YYYY-MM-DD"
    fun formatDateTime(timestamp: Long): String  // "YYYY-MM-DD HH:mm"
    fun getHourOfDay(timestamp: Long): Int       // 0-23
}
```

## Usage Examples

### Basic Sale Recording

**Voice Input:** "Sell 3 Coca-Cola"

**Parsed Function Call:**
```kotlin
ParsedFunctionCall(
    functionName = "record_sale",
    arguments = mapOf(
        "product_name" to "Coca-Cola",
        "quantity" to 3
    )
)
```

**Result:**
```kotlin
FunctionExecutionResult.Success(
    functionName = "record_sale",
    message = "Sold 3 Coca-Cola for K45.00",
    data = mapOf(
        "saleId" to "abc123",
        "totalAmount" to 45.0,
        "productName" to "Coca-Cola",
        "quantity" to 3
    )
)
```

### Credit Sale

**Voice Input:** "John bought 5 bread on credit"

**Parsed Function Call:**
```kotlin
ParsedFunctionCall(
    functionName = "record_credit_sale",
    arguments = mapOf(
        "customer_name" to "John",
        "product_name" to "bread",
        "quantity" to 5
    )
)
```

### Customer Balance Check

**Voice Input:** "How much does Mary owe?"

**Parsed Function Call:**
```kotlin
ParsedFunctionCall(
    functionName = "get_customer_balance",
    arguments = mapOf("customer_name" to "Mary")
)
```

### Payment Recording

**Voice Input:** "John paid 500 kwacha"

**Parsed Function Call:**
```kotlin
ParsedFunctionCall(
    functionName = "record_payment",
    arguments = mapOf(
        "customer_name" to "John",
        "amount" to 500.0
    )
)
```

## Error Handling

### Product Not Found

When a product cannot be found, the system returns suggestions:

```kotlin
FunctionExecutionResult.ProductNotFound(
    productName = "Cocacola",
    suggestions = listOf("Coca-Cola", "Coca-Cola Zero", "Coca-Cola 2L")
)
```

### Customer Not Found

Similar handling for customers:

```kotlin
FunctionExecutionResult.CustomerNotFound(
    customerName = "Jon",
    suggestions = listOf("John", "Jonathan", "Jones")
)
```

### Insufficient Stock

```kotlin
FunctionExecutionResult.InsufficientStock(
    productName = "Bread",
    availableStock = 2,
    requestedQuantity = 5
)
```

## Fallback Pattern Matching

When the TFLite model is unavailable, the system uses regex-based pattern matching:

**Supported Patterns:**
- Sale: `sell/sold [quantity] [product]`
- Stock Check: `how many [product]`, `check stock [product]`
- Payment: `[customer] paid [amount]`, `received [amount] from [customer]`
- Balance: `how much does [customer] owe`, `[customer]'s balance`
- Add Product: `add product [name] at [price]`
- Add Customer: `add customer [name] [phone]`
- Analytics: `sales today`, `revenue this week`
- Low Stock: `low stock alerts`

## File Structure

```
app/src/main/java/com/example/dukaai/
├── ml/functiongemma/
│   ├── DukaToolSchema.kt          # 121 tool declarations
│   ├── DukaFunctionExecutor.kt    # Tool execution logic
│   ├── FunctionGemmaParser.kt     # Output parsing
│   ├── FunctionGemmaInference.kt  # TFLite model wrapper
│   └── FunctionGemmaService.kt    # Orchestration layer
├── di/
│   └── FunctionGemmaModule.kt     # Hilt DI module
├── data/
│   ├── local/entity/
│   │   ├── ProductEntity.kt
│   │   ├── CustomerEntity.kt
│   │   ├── SaleEntity.kt
│   │   ├── CreditEntity.kt
│   │   └── PaymentEntity.kt
│   └── repository/
│       ├── ProductRepository.kt
│       ├── SaleRepository.kt
│       ├── CustomerRepository.kt
│       ├── CreditRepository.kt
│       └── PaymentRepository.kt
└── util/
    └── DateUtils.kt               # Date/time utilities
```

## Currency

All monetary values are in Zambian Kwacha (ZMW/K). The executor formats currency using:

```kotlin
private fun formatCurrency(amount: Double): String {
    return "K${String.format("%.2f", amount)}"
}
```

## Local Language Support

The system supports Zambian terminology:
- "pa ng'ong'ole" - Credit sale (buy now, pay later)
- "ndalama" - Money/payment

## Version History

| Version | Changes |
|---------|---------|
| 1.0 | Initial FunctionGemma with 10 tools |
| 2.0 | Expanded to 121 tools across 14 categories |

---

*Documentation generated for DukaAI FunctionGemma NLU System*
