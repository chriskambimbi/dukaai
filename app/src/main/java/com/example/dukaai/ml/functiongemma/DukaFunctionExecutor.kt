package com.example.dukaai.ml.functiongemma

import com.example.dukaai.data.local.dao.InventoryLogDao
import com.example.dukaai.data.local.entity.CustomerEntity
import com.example.dukaai.data.local.entity.PaymentEntity
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.data.local.entity.SaleEntity
import com.example.dukaai.data.local.entity.CreditLedgerEntity
import com.example.dukaai.data.repository.CreditRepository
import com.example.dukaai.data.repository.CustomerRepository
import com.example.dukaai.data.repository.PaymentRepository
import com.example.dukaai.data.repository.ProductRepository
import com.example.dukaai.data.repository.SaleRepository
import com.example.dukaai.util.DateUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Executes parsed function calls from FunctionGemma against DukaAI repositories.
 *
 * This class bridges the gap between natural language commands (via FunctionGemma)
 * and the actual data operations (via Room repositories).
 *
 * Supports 121 tools across 14 categories:
 * - Product Management (14)
 * - Sales Operations (10)
 * - Credit Management (11)
 * - Customer Management (10)
 * - Payment Operations (11)
 * - Inventory Operations (4)
 * - Analytics & Reporting (10)
 * - Settings & Configuration (10)
 * - Backup/Export/Import (10)
 * - Barcode/Scanner (5)
 * - Navigation/UI (12)
 * - Machine Learning (5)
 * - Voice Framework (6)
 * - Validation & Confirmation (3)
 */
@Singleton
class DukaFunctionExecutor @Inject constructor(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val creditRepository: CreditRepository,
    private val paymentRepository: PaymentRepository,
    private val inventoryLogDao: InventoryLogDao
) {

    /**
     * Execute a parsed function call
     */
    suspend fun execute(call: ParsedFunctionCall): FunctionExecutionResult {
        return try {
            when (call.functionName) {
                // ============ PRODUCT MANAGEMENT (14) ============
                "add_product" -> executeAddProduct(call)
                "edit_product" -> executeEditProduct(call)
                "delete_product" -> executeDeleteProduct(call)
                "get_product_details" -> executeGetProductDetails(call)
                "check_stock" -> executeCheckStock(call)
                "update_stock" -> executeUpdateStock(call)
                "search_products" -> executeSearchProducts(call)
                "get_low_stock_alerts" -> executeGetLowStockAlerts(call)
                "list_products" -> executeListProducts(call)
                "get_out_of_stock" -> executeGetOutOfStock(call)
                "get_top_selling_products" -> executeGetTopSellingProducts(call)
                "get_product_categories" -> executeGetProductCategories(call)
                "update_product_price" -> executeUpdateProductPrice(call)
                "get_product_profit_margin" -> executeGetProductProfitMargin(call)

                // ============ SALES OPERATIONS (10) ============
                "record_sale" -> executeRecordSale(call)
                "record_batch_sale" -> executeRecordBatchSale(call)
                "edit_sale" -> executeEditSale(call)
                "delete_sale" -> executeDeleteSale(call)
                "get_sale_history" -> executeGetSaleHistory(call)
                "get_today_sales" -> executeGetTodaySales(call)
                "get_recent_sales" -> executeGetRecentSales(call)
                "calculate_sale_total" -> executeCalculateSaleTotal(call)
                "get_sales_by_payment_type" -> executeGetSalesByPaymentType(call)
                "process_refund" -> executeProcessRefund(call)

                // ============ CREDIT MANAGEMENT (11) ============
                "record_credit_sale" -> executeRecordCreditSale(call)
                "get_customer_balance" -> executeGetCustomerBalance(call)
                "get_all_credit_balances" -> executeGetAllCreditBalances(call)
                "get_overdue_credits" -> executeGetOverdueCredits(call)
                "mark_credit_as_paid" -> executeMarkCreditAsPaid(call)
                "get_credit_history" -> executeGetCreditHistory(call)
                "send_credit_reminder" -> executeSendCreditReminder(call)
                "get_total_credits_owed" -> executeGetTotalCreditsOwed(call)
                "update_credit_due_date" -> executeUpdateCreditDueDate(call)
                "set_customer_credit_limit" -> executeSetCustomerCreditLimit(call)
                "get_customers_near_credit_limit" -> executeGetCustomersNearCreditLimit(call)

                // ============ CUSTOMER MANAGEMENT (10) ============
                "add_customer" -> executeAddCustomer(call)
                "update_customer" -> executeUpdateCustomer(call)
                "delete_customer" -> executeDeleteCustomer(call)
                "search_customers" -> executeSearchCustomers(call)
                "get_customer_details" -> executeGetCustomerDetails(call)
                "list_customers" -> executeListCustomers(call)
                "get_customer_purchase_history" -> executeGetCustomerPurchaseHistory(call)
                "get_top_customers" -> executeGetTopCustomers(call)
                "get_customers_count" -> executeGetCustomersCount(call)
                "get_new_customers" -> executeGetNewCustomers(call)

                // ============ PAYMENT OPERATIONS (11) ============
                "record_payment" -> executeRecordPayment(call)
                "get_payment_history" -> executeGetPaymentHistory(call)
                "get_today_payments" -> executeGetTodayPayments(call)
                "get_total_payments" -> executeGetTotalPayments(call)
                "edit_payment" -> executeEditPayment(call)
                "delete_payment" -> executeDeletePayment(call)
                "get_payments_by_method" -> executeGetPaymentsByMethod(call)
                "get_recent_payments" -> executeGetRecentPayments(call)
                "allocate_payment" -> executeAllocatePayment(call)
                "get_expected_payments" -> executeGetExpectedPayments(call)
                "generate_payment_receipt" -> executeGeneratePaymentReceipt(call)

                // ============ INVENTORY OPERATIONS (4) ============
                "get_inventory_history" -> executeGetInventoryHistory(call)
                "get_inventory_value" -> executeGetInventoryValue(call)
                "record_stock_take" -> executeRecordStockTake(call)
                "get_inventory_summary" -> executeGetInventorySummary(call)

                // ============ ANALYTICS & REPORTING (10) ============
                "get_sales_analytics" -> executeGetSalesAnalytics(call)
                "get_daily_sales_data" -> executeGetDailySalesData(call)
                "get_profit_analytics" -> executeGetProfitAnalytics(call)
                "get_revenue_trends" -> executeGetRevenueTrends(call)
                "get_category_performance" -> executeGetCategoryPerformance(call)
                "get_hourly_sales_pattern" -> executeGetHourlySalesPattern(call)
                "generate_report" -> executeGenerateReport(call)
                "compare_periods" -> executeComparePeriods(call)
                "get_dashboard_summary" -> executeGetDashboardSummary(call)
                "get_slow_moving_products" -> executeGetSlowMovingProducts(call)

                // ============ SETTINGS & CONFIGURATION (10) ============
                "get_settings" -> executeGetSettings(call)
                "update_language" -> executeUpdateLanguage(call)
                "set_pin_protection" -> executeSetPinProtection(call)
                "update_sync_settings" -> executeUpdateSyncSettings(call)
                "set_low_stock_threshold" -> executeSetLowStockThreshold(call)
                "update_notification_settings" -> executeUpdateNotificationSettings(call)
                "set_currency_format" -> executeSetCurrencyFormat(call)
                "set_shop_details" -> executeSetShopDetails(call)
                "reset_settings" -> executeResetSettings(call)
                "get_app_info" -> executeGetAppInfo(call)

                // ============ BACKUP/EXPORT/IMPORT (10) ============
                "backup_data" -> executeBackupData(call)
                "restore_data" -> executeRestoreData(call)
                "sync_to_cloud" -> executeSyncToCloud(call)
                "sync_from_cloud" -> executeSyncFromCloud(call)
                "get_sync_status" -> executeGetSyncStatus(call)
                "export_to_csv" -> executeExportToCsv(call)
                "export_to_pdf" -> executeExportToPdf(call)
                "import_products" -> executeImportProducts(call)
                "list_backups" -> executeListBackups(call)
                "delete_backup" -> executeDeleteBackup(call)

                // ============ BARCODE/SCANNER (5) ============
                "scan_product_barcode" -> executeScanProductBarcode(call)
                "find_by_barcode" -> executeFindByBarcode(call)
                "update_product_barcode" -> executeUpdateProductBarcode(call)
                "generate_barcode" -> executeGenerateBarcode(call)
                "get_products_without_barcode" -> executeGetProductsWithoutBarcode(call)

                // ============ NAVIGATION/UI (12) ============
                "go_to_dashboard" -> executeNavigation("dashboard")
                "go_to_products" -> executeNavigation("products")
                "go_to_sales" -> executeNavigation("sales")
                "go_to_customers" -> executeNavigation("customers")
                "go_to_analytics" -> executeNavigation("analytics")
                "go_to_credits" -> executeNavigation("credits")
                "go_to_settings" -> executeNavigation("settings")
                "go_to_add_product" -> executeNavigation("add_product")
                "go_to_add_customer" -> executeNavigation("add_customer")
                "go_back" -> executeNavigation("back")
                "open_scanner" -> executeNavigation("scanner")
                "open_product_detail" -> executeOpenProductDetail(call)

                // ============ MACHINE LEARNING (5) ============
                "classify_product_image" -> executeClassifyProductImage(call)
                "get_ml_model_status" -> executeGetMlModelStatus(call)
                "suggest_product_price" -> executeSuggestProductPrice(call)
                "predict_low_stock" -> executePredictLowStock(call)
                "get_sales_forecast" -> executeGetSalesForecast(call)

                // ============ VOICE FRAMEWORK (6) ============
                "set_voice_language" -> executeSetVoiceLanguage(call)
                "help_with_voice_commands" -> executeHelpWithVoiceCommands(call)
                "repeat_last_response" -> executeRepeatLastResponse(call)
                "get_voice_command_history" -> executeGetVoiceCommandHistory(call)
                "toggle_voice_feedback" -> executeToggleVoiceFeedback(call)
                "cancel_operation" -> executeCancelOperation(call)

                // ============ VALIDATION & CONFIRMATION (3) ============
                "verify_pin" -> executeVerifyPin(call)
                "confirm_operation" -> executeConfirmOperation(call)
                "cancel_confirmation" -> executeCancelConfirmation(call)

                else -> FunctionExecutionResult.Error(
                    functionName = call.functionName,
                    errorMessage = "Unknown function: ${call.functionName}",
                    errorCode = "UNKNOWN_FUNCTION"
                )
            }
        } catch (e: Exception) {
            FunctionExecutionResult.Error(
                functionName = call.functionName,
                errorMessage = e.message ?: "Execution failed",
                errorCode = "EXECUTION_ERROR"
            )
        }
    }

    /**
     * Execute multiple function calls (parallel calls support)
     */
    suspend fun executeAll(calls: List<ParsedFunctionCall>): List<FunctionExecutionResult> {
        return calls.map { execute(it) }
    }

    // ============================================================================
    // PRODUCT MANAGEMENT IMPLEMENTATIONS (14)
    // ============================================================================

    private suspend fun executeAddProduct(call: ParsedFunctionCall): FunctionExecutionResult {
        val name = call.getStringArg("name")
        val sellingPrice = call.getDoubleArg("selling_price")
        val category = call.getStringArg("category", "general")
        val initialStock = call.getIntArg("initial_stock", 0)
        val buyingPrice = call.getDoubleArg("buying_price", sellingPrice * 0.7)
        val barcode = call.getStringArgOrNull("barcode")
        val lowStockThreshold = call.getIntArg("low_stock_threshold", 10)
        val unit = call.getStringArg("unit", "pieces")

        // Check if product already exists
        val existingProducts = productRepository.searchProducts(name).first()
        val exists = existingProducts.any { it.name.equals(name, ignoreCase = true) }

        if (exists) {
            return FunctionExecutionResult.NeedsConfirmation(
                functionName = "add_product",
                message = "A product named '$name' already exists. Would you like to update it instead?",
                details = mapOf("existingProduct" to existingProducts.first { it.name.equals(name, ignoreCase = true) })
            )
        }

        val product = ProductEntity(
            name = name,
            category = category,
            currentStock = initialStock,
            buyingPrice = buyingPrice,
            sellingPrice = sellingPrice,
            barcode = barcode,
            minStockThreshold = lowStockThreshold,
            unit = unit
        )

        productRepository.insertProduct(product)

        return FunctionExecutionResult.Success(
            functionName = "add_product",
            message = "Added product: $name at K${formatCurrency(sellingPrice)}",
            data = mapOf(
                "id" to product.id,
                "name" to name,
                "price" to sellingPrice,
                "category" to category,
                "stock" to initialStock
            )
        )
    }

    private suspend fun executeEditProduct(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val newName = call.getStringArgOrNull("new_name")
        val newSellingPrice = call.getDoubleArgOrNull("new_selling_price")
        val newBuyingPrice = call.getDoubleArgOrNull("new_buying_price")
        val newCategory = call.getStringArgOrNull("new_category")
        val newThreshold = call.getIntArgOrNull("new_low_stock_threshold")

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(
                productName = productName,
                suggestions = getSuggestions(productName)
            )

        val updatedProduct = product.copy(
            name = newName ?: product.name,
            sellingPrice = newSellingPrice ?: product.sellingPrice,
            buyingPrice = newBuyingPrice ?: product.buyingPrice,
            category = newCategory ?: product.category,
            minStockThreshold = newThreshold ?: product.minStockThreshold,
            updatedAt = System.currentTimeMillis()
        )

        productRepository.updateProduct(updatedProduct)

        return FunctionExecutionResult.Success(
            functionName = "edit_product",
            message = "Updated product: ${updatedProduct.name}",
            data = mapOf(
                "id" to updatedProduct.id,
                "name" to updatedProduct.name,
                "sellingPrice" to updatedProduct.sellingPrice,
                "buyingPrice" to updatedProduct.buyingPrice,
                "category" to updatedProduct.category
            )
        )
    }

    private suspend fun executeDeleteProduct(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val confirm = call.getBooleanArg("confirm", false)

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(
                productName = productName,
                suggestions = getSuggestions(productName)
            )

        if (!confirm) {
            return FunctionExecutionResult.NeedsConfirmation(
                functionName = "delete_product",
                message = "Are you sure you want to delete '${product.name}'? This cannot be undone.",
                details = mapOf(
                    "productId" to product.id,
                    "productName" to product.name,
                    "currentStock" to product.currentStock
                )
            )
        }

        productRepository.deleteProduct(product)

        return FunctionExecutionResult.Success(
            functionName = "delete_product",
            message = "Deleted product: ${product.name}",
            data = mapOf("deletedProductId" to product.id)
        )
    }

    private suspend fun executeGetProductDetails(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(
                productName = productName,
                suggestions = getSuggestions(productName)
            )

        val profitMargin = if (product.buyingPrice > 0) {
            ((product.sellingPrice - product.buyingPrice) / product.buyingPrice) * 100
        } else 0.0

        return FunctionExecutionResult.Success(
            functionName = "get_product_details",
            message = "${product.name}: K${formatCurrency(product.sellingPrice)}, Stock: ${product.currentStock}",
            data = mapOf(
                "id" to product.id,
                "name" to product.name,
                "sellingPrice" to product.sellingPrice,
                "buyingPrice" to product.buyingPrice,
                "currentStock" to product.currentStock,
                "category" to product.category,
                "barcode" to product.barcode,
                "profitMargin" to profitMargin,
                "minStockThreshold" to product.minStockThreshold,
                "isLowStock" to (product.currentStock <= product.minStockThreshold)
            )
        )
    }

    private suspend fun executeCheckStock(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(
                productName = productName,
                suggestions = getSuggestions(productName)
            )

        val isLowStock = product.currentStock <= product.minStockThreshold

        return FunctionExecutionResult.Success(
            functionName = "check_stock",
            message = if (isLowStock) {
                "${product.name} has ${product.currentStock} ${product.unit} - LOW STOCK WARNING"
            } else {
                "${product.name} has ${product.currentStock} ${product.unit} in stock"
            },
            data = mapOf(
                "product" to product.name,
                "currentStock" to product.currentStock,
                "unit" to product.unit,
                "minThreshold" to product.minStockThreshold,
                "isLowStock" to isLowStock
            )
        )
    }

    private suspend fun executeUpdateStock(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val quantity = call.getIntArg("quantity")
        val reason = call.getStringArgOrNull("reason")

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(
                productName = productName,
                suggestions = getSuggestions(productName)
            )

        val newStock = product.currentStock + quantity

        if (newStock < 0) {
            return FunctionExecutionResult.Error(
                functionName = "update_stock",
                errorMessage = "Cannot reduce stock below 0. Current: ${product.currentStock}, Change: $quantity"
            )
        }

        productRepository.updateStock(product.id, newStock, reason)

        return FunctionExecutionResult.Success(
            functionName = "update_stock",
            message = "Updated ${product.name} stock: ${product.currentStock} → $newStock",
            data = mapOf(
                "product" to product.name,
                "previousStock" to product.currentStock,
                "newStock" to newStock,
                "change" to quantity
            )
        )
    }

    private suspend fun executeSearchProducts(call: ParsedFunctionCall): FunctionExecutionResult {
        val query = call.getStringArg("query")
        val searchType = call.getStringArg("search_type", "all")

        val products = when (searchType) {
            "category" -> productRepository.getProductsByCategory(query).first()
            "barcode" -> {
                val product = productRepository.getProductByBarcode(query)
                if (product != null) listOf(product) else emptyList()
            }
            else -> productRepository.searchProducts(query).first()
        }

        return FunctionExecutionResult.Success(
            functionName = "search_products",
            message = "Found ${products.size} products matching '$query'",
            data = products.map { product ->
                mapOf(
                    "name" to product.name,
                    "price" to product.sellingPrice,
                    "stock" to product.currentStock,
                    "category" to product.category
                )
            }
        )
    }

    private suspend fun executeGetLowStockAlerts(call: ParsedFunctionCall): FunctionExecutionResult {
        val customThreshold = call.getIntArgOrNull("threshold")

        val lowStockProducts = if (customThreshold != null) {
            productRepository.getAllProducts().first().filter { it.currentStock <= customThreshold }
        } else {
            productRepository.getLowStockProducts().first()
        }
        val outOfStockProducts = productRepository.getOutOfStockProducts().first()

        val message = buildString {
            if (outOfStockProducts.isNotEmpty()) {
                append("${outOfStockProducts.size} products OUT OF STOCK. ")
            }
            if (lowStockProducts.isNotEmpty()) {
                append("${lowStockProducts.size} products running low on stock.")
            }
            if (outOfStockProducts.isEmpty() && lowStockProducts.isEmpty()) {
                append("All products have sufficient stock.")
            }
        }

        return FunctionExecutionResult.Success(
            functionName = "get_low_stock_alerts",
            message = message,
            data = mapOf(
                "outOfStock" to outOfStockProducts.map { mapOf("name" to it.name, "stock" to it.currentStock) },
                "lowStock" to lowStockProducts.map { mapOf("name" to it.name, "stock" to it.currentStock, "threshold" to it.minStockThreshold) },
                "outOfStockCount" to outOfStockProducts.size,
                "lowStockCount" to lowStockProducts.size
            )
        )
    }

    private suspend fun executeListProducts(call: ParsedFunctionCall): FunctionExecutionResult {
        val category = call.getStringArgOrNull("category")
        val sortBy = call.getStringArg("sort_by", "name")
        val sortOrder = call.getStringArg("sort_order", "asc")
        val limit = call.getIntArg("limit", 50)

        var products = if (category != null) {
            productRepository.getProductsByCategory(category).first()
        } else {
            productRepository.getAllProducts().first()
        }

        // Sort
        products = when (sortBy) {
            "price" -> if (sortOrder == "desc") products.sortedByDescending { it.sellingPrice } else products.sortedBy { it.sellingPrice }
            "stock" -> if (sortOrder == "desc") products.sortedByDescending { it.currentStock } else products.sortedBy { it.currentStock }
            "created_date" -> if (sortOrder == "desc") products.sortedByDescending { it.createdAt } else products.sortedBy { it.createdAt }
            else -> if (sortOrder == "desc") products.sortedByDescending { it.name } else products.sortedBy { it.name }
        }

        products = products.take(limit)

        return FunctionExecutionResult.Success(
            functionName = "list_products",
            message = "Listed ${products.size} products" + (category?.let { " in category '$it'" } ?: ""),
            data = products.map { mapOf("name" to it.name, "price" to it.sellingPrice, "stock" to it.currentStock, "category" to it.category) }
        )
    }

    private suspend fun executeGetOutOfStock(call: ParsedFunctionCall): FunctionExecutionResult {
        val products = productRepository.getOutOfStockProducts().first()

        return FunctionExecutionResult.Success(
            functionName = "get_out_of_stock",
            message = if (products.isEmpty()) "No products are out of stock" else "${products.size} products are out of stock",
            data = products.map { mapOf("name" to it.name, "category" to it.category) }
        )
    }

    private suspend fun executeGetTopSellingProducts(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "this_month")
        val metric = call.getStringArg("metric", "quantity")
        val limit = call.getIntArg("limit", 10)

        val (startDate, endDate) = getDateRangeForPeriod(period)
        val sales = saleRepository.getSalesByDate(startDate, endDate).first()

        // Group by product and calculate metrics
        val productSales = sales.groupBy { it.productId }

        // Get product details and calculate metrics
        val productMetrics = productSales.mapNotNull { (productId, productSalesList) ->
            val product = productRepository.getProductById(productId).firstOrNull() ?: return@mapNotNull null
            val totalQuantity = productSalesList.sumOf { it.quantity }
            val totalRevenue = productSalesList.sumOf { it.totalAmount }
            val totalProfit = productSalesList.sumOf { (it.unitPrice - product.buyingPrice) * it.quantity }
            Triple(product, mapOf("quantity" to totalQuantity, "revenue" to totalRevenue, "profit" to totalProfit), totalQuantity)
        }

        val sorted = when (metric) {
            "revenue" -> productMetrics.sortedByDescending { (it.second["revenue"] as Double) }
            "profit" -> productMetrics.sortedByDescending { (it.second["profit"] as Double) }
            else -> productMetrics.sortedByDescending { it.third }
        }.take(limit)

        return FunctionExecutionResult.Success(
            functionName = "get_top_selling_products",
            message = "Top ${sorted.size} selling products for $period",
            data = sorted.map { (product, metrics, _) ->
                mapOf(
                    "name" to product.name,
                    "quantity" to metrics["quantity"],
                    "revenue" to metrics["revenue"],
                    "profit" to metrics["profit"]
                )
            }
        )
    }

    private suspend fun executeGetProductCategories(call: ParsedFunctionCall): FunctionExecutionResult {
        val categories = productRepository.getAllCategories().first()
        val allProducts = productRepository.getAllProducts().first()

        val categoryCounts = allProducts.groupBy { it.category }.mapValues { it.value.size }

        return FunctionExecutionResult.Success(
            functionName = "get_product_categories",
            message = "Found ${categories.size} product categories",
            data = categories.map { category ->
                mapOf("category" to category, "productCount" to (categoryCounts[category] ?: 0))
            }
        )
    }

    private suspend fun executeUpdateProductPrice(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val newSellingPrice = call.getDoubleArgOrNull("new_selling_price")
        val newBuyingPrice = call.getDoubleArgOrNull("new_buying_price")

        if (newSellingPrice == null && newBuyingPrice == null) {
            return FunctionExecutionResult.Error(
                functionName = "update_product_price",
                errorMessage = "Must specify either new_selling_price or new_buying_price"
            )
        }

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(productName, getSuggestions(productName))

        val updatedProduct = product.copy(
            sellingPrice = newSellingPrice ?: product.sellingPrice,
            buyingPrice = newBuyingPrice ?: product.buyingPrice,
            updatedAt = System.currentTimeMillis()
        )

        productRepository.updateProduct(updatedProduct)

        return FunctionExecutionResult.Success(
            functionName = "update_product_price",
            message = "Updated price for ${product.name}",
            data = mapOf(
                "product" to product.name,
                "oldSellingPrice" to product.sellingPrice,
                "newSellingPrice" to updatedProduct.sellingPrice,
                "oldBuyingPrice" to product.buyingPrice,
                "newBuyingPrice" to updatedProduct.buyingPrice
            )
        )
    }

    private suspend fun executeGetProductProfitMargin(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(productName, getSuggestions(productName))

        val profit = product.sellingPrice - product.buyingPrice
        val marginPercent = if (product.buyingPrice > 0) {
            (profit / product.buyingPrice) * 100
        } else 0.0

        return FunctionExecutionResult.Success(
            functionName = "get_product_profit_margin",
            message = "${product.name}: K${formatCurrency(profit)} profit (${String.format("%.1f", marginPercent)}% margin)",
            data = mapOf(
                "product" to product.name,
                "buyingPrice" to product.buyingPrice,
                "sellingPrice" to product.sellingPrice,
                "profitPerUnit" to profit,
                "marginPercent" to marginPercent
            )
        )
    }

    // ============================================================================
    // SALES OPERATIONS IMPLEMENTATIONS (10)
    // ============================================================================

    private suspend fun executeRecordSale(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val quantity = call.getIntArg("quantity", 1)
        val customerName = call.getStringArgOrNull("customer_name")
        val saleType = call.getStringArg("sale_type", "cash").uppercase()
        val unitPriceOverride = call.getDoubleArgOrNull("unit_price")
        val discount = call.getDoubleArg("discount", 0.0)

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(productName, getSuggestions(productName))

        if (product.currentStock < quantity) {
            return FunctionExecutionResult.InsufficientStock(product.name, quantity, product.currentStock)
        }

        var customerId: String? = null
        if (saleType == "CREDIT" && customerName != null) {
            val customer = findCustomerByName(customerName)
                ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))
            customerId = customer.id
        }

        val unitPrice = unitPriceOverride ?: product.sellingPrice
        val totalAmount = (unitPrice * quantity) - discount

        val sale = SaleEntity(
            productId = product.id,
            customerId = customerId,
            quantity = quantity,
            unitPrice = unitPrice,
            totalAmount = totalAmount,
            saleType = if (customerId != null) "CREDIT" else "CASH"
        )

        val result = saleRepository.recordSale(sale)

        return if (result.isSuccess) {
            if (saleType == "CREDIT" && customerId != null) {
                val credit = CreditLedgerEntity(
                    customerId = customerId,
                    saleId = sale.id,
                    amount = sale.totalAmount,
                    amountPaid = 0.0,
                    amountRemaining = sale.totalAmount,
                    status = "UNPAID"
                )
                creditRepository.recordCredit(credit)
            }

            FunctionExecutionResult.Success(
                functionName = "record_sale",
                message = "Sale recorded: ${quantity}x ${product.name} for K${formatCurrency(totalAmount)}",
                data = mapOf(
                    "product" to product.name,
                    "quantity" to quantity,
                    "total" to totalAmount,
                    "saleType" to sale.saleType,
                    "remainingStock" to (product.currentStock - quantity)
                )
            )
        } else {
            FunctionExecutionResult.Error("record_sale", result.exceptionOrNull()?.message ?: "Failed to record sale")
        }
    }

    private suspend fun executeRecordBatchSale(call: ParsedFunctionCall): FunctionExecutionResult {
        val items = call.getStringArg("items") // Format: "product:qty,product:qty"
        val customerName = call.getStringArgOrNull("customer_name")
        val saleType = call.getStringArg("sale_type", "cash").uppercase()

        val itemList = items.split(",").map { item ->
            val parts = item.trim().split(":")
            if (parts.size != 2) throw IllegalArgumentException("Invalid item format: $item")
            parts[0].trim() to parts[1].trim().toInt()
        }

        var customerId: String? = null
        if (saleType == "CREDIT" && customerName != null) {
            val customer = findCustomerByName(customerName)
                ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))
            customerId = customer.id
        }

        val sales = mutableListOf<SaleEntity>()
        var totalAmount = 0.0

        for ((productName, quantity) in itemList) {
            val product = findProductByName(productName)
                ?: return FunctionExecutionResult.ProductNotFound(productName, getSuggestions(productName))

            if (product.currentStock < quantity) {
                return FunctionExecutionResult.InsufficientStock(product.name, quantity, product.currentStock)
            }

            val sale = SaleEntity(
                productId = product.id,
                customerId = customerId,
                quantity = quantity,
                unitPrice = product.sellingPrice,
                totalAmount = product.sellingPrice * quantity,
                saleType = if (customerId != null) "CREDIT" else "CASH"
            )
            sales.add(sale)
            totalAmount += sale.totalAmount
        }

        val result = saleRepository.recordBulkSales(sales)

        return if (result.isSuccess) {
            if (saleType == "CREDIT" && customerId != null) {
                val credit = CreditLedgerEntity(
                    customerId = customerId,
                    saleId = sales.first().id,
                    amount = totalAmount,
                    amountPaid = 0.0,
                    amountRemaining = totalAmount,
                    status = "UNPAID"
                )
                creditRepository.recordCredit(credit)
            }

            FunctionExecutionResult.Success(
                functionName = "record_batch_sale",
                message = "Batch sale recorded: ${sales.size} items for K${formatCurrency(totalAmount)}",
                data = mapOf(
                    "itemCount" to sales.size,
                    "total" to totalAmount,
                    "saleType" to saleType
                )
            )
        } else {
            FunctionExecutionResult.Error("record_batch_sale", result.exceptionOrNull()?.message ?: "Failed")
        }
    }

    private suspend fun executeEditSale(call: ParsedFunctionCall): FunctionExecutionResult {
        val saleId = call.getStringArg("sale_id")
        val newQuantity = call.getIntArgOrNull("new_quantity")
        val newUnitPrice = call.getDoubleArgOrNull("new_unit_price")

        val sale = saleRepository.getSaleById(saleId).firstOrNull()
            ?: return FunctionExecutionResult.Error("edit_sale", "Sale not found: $saleId")

        // Note: Full edit implementation would need to handle stock adjustments
        return FunctionExecutionResult.NeedsConfirmation(
            functionName = "edit_sale",
            message = "Sale editing requires careful stock adjustment. Please confirm.",
            details = mapOf("saleId" to saleId, "newQuantity" to newQuantity, "newUnitPrice" to newUnitPrice)
        )
    }

    private suspend fun executeDeleteSale(call: ParsedFunctionCall): FunctionExecutionResult {
        val saleId = call.getStringArg("sale_id")
        val reason = call.getStringArgOrNull("reason")
        val confirm = call.getBooleanArg("confirm", false)

        val sale = saleRepository.getSaleById(saleId).firstOrNull()
            ?: return FunctionExecutionResult.Error("delete_sale", "Sale not found: $saleId")

        if (!confirm) {
            return FunctionExecutionResult.NeedsConfirmation(
                functionName = "delete_sale",
                message = "Delete sale #$saleId? Stock will be restored.",
                details = mapOf("saleId" to saleId, "amount" to sale.totalAmount)
            )
        }

        val result = saleRepository.deleteSale(saleId)

        return if (result.isSuccess) {
            FunctionExecutionResult.Success(
                functionName = "delete_sale",
                message = "Sale deleted. Stock restored.",
                data = mapOf("deletedSaleId" to saleId, "reason" to reason)
            )
        } else {
            FunctionExecutionResult.Error("delete_sale", result.exceptionOrNull()?.message ?: "Failed")
        }
    }

    private suspend fun executeGetSaleHistory(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "today")
        val productName = call.getStringArgOrNull("product_name")
        val customerName = call.getStringArgOrNull("customer_name")
        val saleTypeFilter = call.getStringArgOrNull("sale_type")

        val (startDate, endDate) = getDateRangeForPeriod(period)
        var sales = saleRepository.getSalesByDate(startDate, endDate).first()

        // Apply filters
        if (productName != null) {
            val product = findProductByName(productName)
            if (product != null) {
                sales = sales.filter { it.productId == product.id }
            }
        }
        if (customerName != null) {
            val customer = findCustomerByName(customerName)
            if (customer != null) {
                sales = sales.filter { it.customerId == customer.id }
            }
        }
        if (saleTypeFilter != null && saleTypeFilter != "all") {
            sales = sales.filter { it.saleType.equals(saleTypeFilter, ignoreCase = true) }
        }

        val totalRevenue = sales.sumOf { it.totalAmount }

        return FunctionExecutionResult.Success(
            functionName = "get_sale_history",
            message = "${sales.size} sales totaling K${formatCurrency(totalRevenue)} for $period",
            data = mapOf(
                "salesCount" to sales.size,
                "totalRevenue" to totalRevenue,
                "period" to period
            )
        )
    }

    private suspend fun executeGetTodaySales(call: ParsedFunctionCall): FunctionExecutionResult {
        val sales = saleRepository.getTodaySales().first()
        val totalRevenue = sales.sumOf { it.totalAmount }
        val totalItems = sales.sumOf { it.quantity }

        return FunctionExecutionResult.Success(
            functionName = "get_today_sales",
            message = "Today: ${sales.size} sales, K${formatCurrency(totalRevenue)} revenue, $totalItems items sold",
            data = mapOf(
                "salesCount" to sales.size,
                "totalRevenue" to totalRevenue,
                "totalItems" to totalItems
            )
        )
    }

    private suspend fun executeGetRecentSales(call: ParsedFunctionCall): FunctionExecutionResult {
        val limit = call.getIntArg("limit", 10)
        val sales = saleRepository.getAllSales().first().take(limit)

        return FunctionExecutionResult.Success(
            functionName = "get_recent_sales",
            message = "Last $limit sales retrieved",
            data = sales.map { mapOf("id" to it.id, "amount" to it.totalAmount, "quantity" to it.quantity, "type" to it.saleType) }
        )
    }

    private suspend fun executeCalculateSaleTotal(call: ParsedFunctionCall): FunctionExecutionResult {
        val items = call.getStringArg("items")
        val discount = call.getDoubleArg("discount", 0.0)

        val itemList = items.split(",").map { item ->
            val parts = item.trim().split(":")
            parts[0].trim() to parts[1].trim().toInt()
        }

        var subtotal = 0.0
        val itemDetails = mutableListOf<Map<String, Any>>()

        for ((productName, quantity) in itemList) {
            val product = findProductByName(productName)
            if (product != null) {
                val itemTotal = product.sellingPrice * quantity
                subtotal += itemTotal
                itemDetails.add(mapOf("product" to product.name, "quantity" to quantity, "unitPrice" to product.sellingPrice, "total" to itemTotal))
            }
        }

        val total = subtotal - discount

        return FunctionExecutionResult.Success(
            functionName = "calculate_sale_total",
            message = "Total: K${formatCurrency(total)}" + if (discount > 0) " (K${formatCurrency(discount)} discount)" else "",
            data = mapOf("subtotal" to subtotal, "discount" to discount, "total" to total, "items" to itemDetails)
        )
    }

    private suspend fun executeGetSalesByPaymentType(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "today")
        val (startDate, endDate) = getDateRangeForPeriod(period)

        val sales = saleRepository.getSalesByDate(startDate, endDate).first()
        val cashSales = sales.filter { it.saleType == "CASH" }
        val creditSales = sales.filter { it.saleType == "CREDIT" }

        return FunctionExecutionResult.Success(
            functionName = "get_sales_by_payment_type",
            message = "Cash: ${cashSales.size} (K${formatCurrency(cashSales.sumOf { it.totalAmount })}), Credit: ${creditSales.size} (K${formatCurrency(creditSales.sumOf { it.totalAmount })})",
            data = mapOf(
                "cash" to mapOf("count" to cashSales.size, "total" to cashSales.sumOf { it.totalAmount }),
                "credit" to mapOf("count" to creditSales.size, "total" to creditSales.sumOf { it.totalAmount })
            )
        )
    }

    private suspend fun executeProcessRefund(call: ParsedFunctionCall): FunctionExecutionResult {
        val saleId = call.getStringArg("sale_id")
        val refundAmount = call.getDoubleArgOrNull("refund_amount")
        val reason = call.getStringArgOrNull("reason")

        val sale = saleRepository.getSaleById(saleId).firstOrNull()
            ?: return FunctionExecutionResult.Error("process_refund", "Sale not found: $saleId")

        val actualRefund = refundAmount ?: sale.totalAmount

        return FunctionExecutionResult.NeedsConfirmation(
            functionName = "process_refund",
            message = "Process refund of K${formatCurrency(actualRefund)} for sale #$saleId?",
            details = mapOf("saleId" to saleId, "refundAmount" to actualRefund, "reason" to reason)
        )
    }

    // ============================================================================
    // CREDIT MANAGEMENT IMPLEMENTATIONS (11)
    // ============================================================================

    private suspend fun executeRecordCreditSale(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val productName = call.getStringArg("product_name")
        val quantity = call.getIntArg("quantity", 1)
        val dueDateStr = call.getStringArgOrNull("due_date")

        // This delegates to recordSale with credit type
        val saleCall = ParsedFunctionCall(
            functionName = "record_sale",
            arguments = mapOf(
                "product_name" to productName,
                "quantity" to quantity,
                "customer_name" to customerName,
                "sale_type" to "credit"
            )
        )

        return executeRecordSale(saleCall)
    }

    private suspend fun executeGetCustomerBalance(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        val balance = creditRepository.getCustomerTotalCredit(customer.id).first() ?: 0.0
        val creditStats = creditRepository.getCustomerCreditStats(customer.id)

        return FunctionExecutionResult.Success(
            functionName = "get_customer_balance",
            message = if (balance > 0) {
                "${customer.name} owes K${formatCurrency(balance)}"
            } else {
                "${customer.name} has no outstanding balance"
            },
            data = mapOf(
                "customer" to customer.name,
                "balance" to balance,
                "unpaidCount" to creditStats.unpaidCount,
                "totalCredits" to creditStats.totalCredits,
                "overdueCount" to creditStats.overdueCount
            )
        )
    }

    private suspend fun executeGetAllCreditBalances(call: ParsedFunctionCall): FunctionExecutionResult {
        val sortBy = call.getStringArg("sort_by", "amount_desc")

        val customersWithDebt = customerRepository.getCustomersByDebtWithAmounts()

        val sorted = when (sortBy) {
            "amount_asc" -> customersWithDebt.sortedBy { it.totalDebt }
            "name" -> customersWithDebt.sortedBy { it.customer.name }
            "oldest" -> customersWithDebt // Would need additional data
            else -> customersWithDebt.sortedByDescending { it.totalDebt }
        }

        val totalOwed = sorted.sumOf { it.totalDebt }

        return FunctionExecutionResult.Success(
            functionName = "get_all_credit_balances",
            message = "${sorted.size} customers owe K${formatCurrency(totalOwed)} total",
            data = sorted.map { mapOf("customer" to it.customer.name, "balance" to it.totalDebt) }
        )
    }

    private suspend fun executeGetOverdueCredits(call: ParsedFunctionCall): FunctionExecutionResult {
        val daysOverdue = call.getIntArg("days_overdue", 0)

        val overdueCredits = creditRepository.getOverdueCredits().first()
        val now = System.currentTimeMillis()
        val filtered = if (daysOverdue > 0) {
            val cutoff = now - (daysOverdue * 24 * 60 * 60 * 1000L)
            overdueCredits.filter { it.dueDate != null && it.dueDate < cutoff }
        } else {
            overdueCredits
        }

        val totalOverdue = filtered.sumOf { it.amountRemaining }

        return FunctionExecutionResult.Success(
            functionName = "get_overdue_credits",
            message = "${filtered.size} overdue credits totaling K${formatCurrency(totalOverdue)}",
            data = filtered.map { mapOf("id" to it.id, "amount" to it.amountRemaining, "customerId" to it.customerId) }
        )
    }

    private suspend fun executeMarkCreditAsPaid(call: ParsedFunctionCall): FunctionExecutionResult {
        val creditId = call.getStringArg("credit_id")

        val result = creditRepository.markAsPaid(creditId)

        return if (result.isSuccess) {
            FunctionExecutionResult.Success(
                functionName = "mark_credit_as_paid",
                message = "Credit marked as paid",
                data = mapOf("creditId" to creditId)
            )
        } else {
            FunctionExecutionResult.Error("mark_credit_as_paid", result.exceptionOrNull()?.message ?: "Failed")
        }
    }

    private suspend fun executeGetCreditHistory(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val statusFilter = call.getStringArgOrNull("status")

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        var credits = creditRepository.getCreditsByCustomer(customer.id).first()

        if (statusFilter != null && statusFilter != "all") {
            credits = credits.filter { it.status.equals(statusFilter, ignoreCase = true) }
        }

        return FunctionExecutionResult.Success(
            functionName = "get_credit_history",
            message = "${credits.size} credit entries for ${customer.name}",
            data = credits.map { mapOf("id" to it.id, "amount" to it.amount, "remaining" to it.amountRemaining, "status" to it.status) }
        )
    }

    private suspend fun executeSendCreditReminder(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val channel = call.getStringArg("channel", "sms")

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        val balance = creditRepository.getCustomerTotalCredit(customer.id).first() ?: 0.0

        if (customer.phoneNumber == null) {
            return FunctionExecutionResult.Error(
                functionName = "send_credit_reminder",
                errorMessage = "${customer.name} has no phone number registered"
            )
        }

        // Note: Actual SMS/WhatsApp sending would be handled by UI layer
        return FunctionExecutionResult.Success(
            functionName = "send_credit_reminder",
            message = "Reminder prepared for ${customer.name} (${customer.phoneNumber}) via $channel. Balance: K${formatCurrency(balance)}",
            data = mapOf(
                "customer" to customer.name,
                "phone" to customer.phoneNumber,
                "balance" to balance,
                "channel" to channel,
                "action" to "send_reminder"
            )
        )
    }

    private suspend fun executeGetTotalCreditsOwed(call: ParsedFunctionCall): FunctionExecutionResult {
        val totalOwed = creditRepository.getTotalOutstandingCredit().first() ?: 0.0
        val stats = creditRepository.getOverallCreditStats()

        return FunctionExecutionResult.Success(
            functionName = "get_total_credits_owed",
            message = "Total outstanding credit: K${formatCurrency(totalOwed)}",
            data = mapOf(
                "totalOwed" to totalOwed,
                "unpaidCount" to stats.unpaidCount,
                "overdueCount" to stats.overdueCount
            )
        )
    }

    private suspend fun executeUpdateCreditDueDate(call: ParsedFunctionCall): FunctionExecutionResult {
        val creditId = call.getStringArg("credit_id")
        val newDueDate = call.getStringArg("new_due_date")

        val credit = creditRepository.getCreditById(creditId).firstOrNull()
            ?: return FunctionExecutionResult.Error("update_credit_due_date", "Credit not found: $creditId")

        val dueDateMillis = parseDateString(newDueDate)
        val updatedCredit = credit.copy(dueDate = dueDateMillis)
        creditRepository.updateCredit(updatedCredit)

        return FunctionExecutionResult.Success(
            functionName = "update_credit_due_date",
            message = "Due date updated",
            data = mapOf("creditId" to creditId, "newDueDate" to newDueDate)
        )
    }

    private suspend fun executeSetCustomerCreditLimit(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val creditLimit = call.getDoubleArg("credit_limit")

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        val updatedCustomer = customer.copy(
            creditLimit = creditLimit,
            updatedAt = System.currentTimeMillis()
        )
        customerRepository.updateCustomer(updatedCustomer)

        return FunctionExecutionResult.Success(
            functionName = "set_customer_credit_limit",
            message = "Credit limit for ${customer.name} set to K${formatCurrency(creditLimit)}",
            data = mapOf("customer" to customer.name, "creditLimit" to creditLimit)
        )
    }

    private suspend fun executeGetCustomersNearCreditLimit(call: ParsedFunctionCall): FunctionExecutionResult {
        val thresholdPercent = call.getIntArg("threshold_percent", 80)

        val customersWithDebt = customerRepository.getCustomersByDebtWithAmounts()
        val nearLimit = customersWithDebt.filter { cwd ->
            val limit = cwd.customer.creditLimit ?: Double.MAX_VALUE
            if (limit > 0) {
                (cwd.totalDebt / limit) * 100 >= thresholdPercent
            } else false
        }

        return FunctionExecutionResult.Success(
            functionName = "get_customers_near_credit_limit",
            message = "${nearLimit.size} customers at or above ${thresholdPercent}% of their credit limit",
            data = nearLimit.map {
                mapOf(
                    "customer" to it.customer.name,
                    "balance" to it.totalDebt,
                    "limit" to (it.customer.creditLimit ?: 0.0),
                    "percentUsed" to if ((it.customer.creditLimit ?: 0.0) > 0) (it.totalDebt / it.customer.creditLimit!!) * 100 else 0.0
                )
            }
        )
    }

    // ============================================================================
    // CUSTOMER MANAGEMENT IMPLEMENTATIONS (10)
    // ============================================================================

    private suspend fun executeAddCustomer(call: ParsedFunctionCall): FunctionExecutionResult {
        val name = call.getStringArg("name")
        val phone = call.getStringArgOrNull("phone")
        val address = call.getStringArgOrNull("address")
        val notes = call.getStringArgOrNull("notes")

        val customer = CustomerEntity(
            name = name,
            phoneNumber = phone,
            address = address,
            notes = notes
        )

        val result = customerRepository.addCustomer(customer)

        return if (result.isSuccess) {
            FunctionExecutionResult.Success(
                functionName = "add_customer",
                message = "Added customer: $name" + (phone?.let { " ($it)" } ?: ""),
                data = mapOf("id" to customer.id, "name" to name, "phone" to phone)
            )
        } else {
            FunctionExecutionResult.Error("add_customer", result.exceptionOrNull()?.message ?: "Failed")
        }
    }

    private suspend fun executeUpdateCustomer(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val newName = call.getStringArgOrNull("new_name")
        val newPhone = call.getStringArgOrNull("new_phone")
        val newAddress = call.getStringArgOrNull("new_address")
        val newNotes = call.getStringArgOrNull("new_notes")

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        val updatedCustomer = customer.copy(
            name = newName ?: customer.name,
            phoneNumber = newPhone ?: customer.phoneNumber,
            address = newAddress ?: customer.address,
            notes = newNotes ?: customer.notes,
            updatedAt = System.currentTimeMillis()
        )

        customerRepository.updateCustomer(updatedCustomer)

        return FunctionExecutionResult.Success(
            functionName = "update_customer",
            message = "Updated customer: ${updatedCustomer.name}",
            data = mapOf("id" to customer.id, "name" to updatedCustomer.name)
        )
    }

    private suspend fun executeDeleteCustomer(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val confirm = call.getBooleanArg("confirm", false)

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        val balance = creditRepository.getCustomerTotalCredit(customer.id).first() ?: 0.0

        if (balance > 0) {
            return FunctionExecutionResult.Error(
                functionName = "delete_customer",
                errorMessage = "${customer.name} has outstanding balance of K${formatCurrency(balance)}. Cannot delete."
            )
        }

        if (!confirm) {
            return FunctionExecutionResult.NeedsConfirmation(
                functionName = "delete_customer",
                message = "Delete customer ${customer.name}?",
                details = mapOf("customerId" to customer.id, "customerName" to customer.name)
            )
        }

        customerRepository.deleteCustomer(customer)

        return FunctionExecutionResult.Success(
            functionName = "delete_customer",
            message = "Deleted customer: ${customer.name}",
            data = mapOf("deletedCustomerId" to customer.id)
        )
    }

    private suspend fun executeSearchCustomers(call: ParsedFunctionCall): FunctionExecutionResult {
        val query = call.getStringArg("query")

        val customers = customerRepository.searchCustomers(query).first()

        return FunctionExecutionResult.Success(
            functionName = "search_customers",
            message = "Found ${customers.size} customers matching '$query'",
            data = customers.map { mapOf("name" to it.name, "phone" to it.phoneNumber, "id" to it.id) }
        )
    }

    private suspend fun executeGetCustomerDetails(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        val creditInfo = customerRepository.getCustomerWithCreditInfo(customer.id)
        val paymentStats = paymentRepository.getCustomerPaymentStats(customer.id)

        return FunctionExecutionResult.Success(
            functionName = "get_customer_details",
            message = "${customer.name}: Balance K${formatCurrency(creditInfo?.totalCredit ?: 0.0)}",
            data = mapOf(
                "id" to customer.id,
                "name" to customer.name,
                "phone" to customer.phoneNumber,
                "address" to customer.address,
                "balance" to (creditInfo?.totalCredit ?: 0.0),
                "totalPayments" to paymentStats.totalAmount,
                "creditLimit" to customer.creditLimit
            )
        )
    }

    private suspend fun executeListCustomers(call: ParsedFunctionCall): FunctionExecutionResult {
        val sortBy = call.getStringArg("sort_by", "name")
        val hasBalance = call.getBooleanArg("has_balance", false)

        var customers = if (hasBalance) {
            customerRepository.getCustomersWithCredit()
        } else {
            customerRepository.getAllCustomers().first()
        }

        customers = when (sortBy) {
            "balance" -> {
                val withDebt = customerRepository.getCustomersByDebtWithAmounts()
                withDebt.map { it.customer }
            }
            "recent_activity" -> customers.sortedByDescending { it.updatedAt }
            else -> customers.sortedBy { it.name }
        }

        return FunctionExecutionResult.Success(
            functionName = "list_customers",
            message = "Listed ${customers.size} customers",
            data = customers.map { mapOf("name" to it.name, "phone" to it.phoneNumber) }
        )
    }

    private suspend fun executeGetCustomerPurchaseHistory(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val period = call.getStringArg("period", "all_time")

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        val sales = saleRepository.getSalesByCustomer(customer.id).first()
        val totalSpent = sales.sumOf { it.totalAmount }

        return FunctionExecutionResult.Success(
            functionName = "get_customer_purchase_history",
            message = "${customer.name}: ${sales.size} purchases, K${formatCurrency(totalSpent)} total",
            data = mapOf(
                "customer" to customer.name,
                "purchaseCount" to sales.size,
                "totalSpent" to totalSpent
            )
        )
    }

    private suspend fun executeGetTopCustomers(call: ParsedFunctionCall): FunctionExecutionResult {
        val metric = call.getStringArg("metric", "total_purchases")
        val limit = call.getIntArg("limit", 10)

        val customers = customerRepository.getAllCustomers().first()

        val customerMetrics = customers.map { customer ->
            val sales = saleRepository.getSalesByCustomer(customer.id).first()
            val totalSpent = sales.sumOf { it.totalAmount }
            Triple(customer, sales.size, totalSpent)
        }

        val sorted = when (metric) {
            "frequency" -> customerMetrics.sortedByDescending { it.second }
            "average_purchase" -> customerMetrics.sortedByDescending { if (it.second > 0) it.third / it.second else 0.0 }
            else -> customerMetrics.sortedByDescending { it.third }
        }.take(limit)

        return FunctionExecutionResult.Success(
            functionName = "get_top_customers",
            message = "Top ${sorted.size} customers by $metric",
            data = sorted.map { (customer, count, total) ->
                mapOf("name" to customer.name, "purchases" to count, "totalSpent" to total)
            }
        )
    }

    private suspend fun executeGetCustomersCount(call: ParsedFunctionCall): FunctionExecutionResult {
        val count = customerRepository.getCustomerCount()

        return FunctionExecutionResult.Success(
            functionName = "get_customers_count",
            message = "Total customers: $count",
            data = mapOf("count" to count)
        )
    }

    private suspend fun executeGetNewCustomers(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "this_month")
        val (startDate, _) = getDateRangeForPeriod(period)

        val allCustomers = customerRepository.getAllCustomers().first()
        val newCustomers = allCustomers.filter { it.createdAt >= startDate }

        return FunctionExecutionResult.Success(
            functionName = "get_new_customers",
            message = "${newCustomers.size} new customers $period",
            data = newCustomers.map { mapOf("name" to it.name, "phone" to it.phoneNumber, "addedAt" to it.createdAt) }
        )
    }

    // ============================================================================
    // PAYMENT OPERATIONS IMPLEMENTATIONS (11)
    // ============================================================================

    private suspend fun executeRecordPayment(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val amount = call.getDoubleArg("amount")
        val paymentMethod = call.getStringArg("payment_method", "CASH").uppercase()
        val reference = call.getStringArgOrNull("reference")

        val customer = findCustomerByName(customerName)
            ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))

        val credits = creditRepository.getCreditsByCustomer(customer.id).first()
            .filter { it.status != "PAID" }
            .sortedBy { it.createdAt }

        if (credits.isEmpty()) {
            return FunctionExecutionResult.Error("record_payment", "${customer.name} has no outstanding credit")
        }

        var remainingAmount = amount
        val paymentsRecorded = mutableListOf<String>()

        for (credit in credits) {
            if (remainingAmount <= 0) break

            val paymentAmount = minOf(remainingAmount, credit.amountRemaining)
            val payment = PaymentEntity(
                creditId = credit.id,
                amount = paymentAmount,
                paymentMethod = paymentMethod,
                notes = reference
            )

            creditRepository.recordPayment(credit.id, payment)
            paymentsRecorded.add("K${formatCurrency(paymentAmount)}")
            remainingAmount -= paymentAmount
        }

        val newBalance = creditRepository.getCustomerTotalCredit(customer.id).first() ?: 0.0

        return FunctionExecutionResult.Success(
            functionName = "record_payment",
            message = "Recorded K${formatCurrency(amount)} from ${customer.name}. New balance: K${formatCurrency(newBalance)}",
            data = mapOf(
                "customer" to customer.name,
                "amountPaid" to amount,
                "newBalance" to newBalance,
                "paymentMethod" to paymentMethod
            )
        )
    }

    private suspend fun executeGetPaymentHistory(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArgOrNull("customer_name")
        val period = call.getStringArg("period", "all")
        val paymentMethod = call.getStringArgOrNull("payment_method")

        var payments = if (customerName != null) {
            val customer = findCustomerByName(customerName)
                ?: return FunctionExecutionResult.CustomerNotFound(customerName, getCustomerSuggestions(customerName))
            paymentRepository.getPaymentsByCustomer(customer.id)
        } else if (period != "all") {
            val (startDate, endDate) = getDateRangeForPeriod(period)
            paymentRepository.getPaymentsByDateRange(startDate, endDate).first()
        } else {
            paymentRepository.getAllPayments().first()
        }

        if (paymentMethod != null) {
            payments = payments.filter { it.paymentMethod.equals(paymentMethod, ignoreCase = true) }
        }

        val totalAmount = payments.sumOf { it.amount }

        return FunctionExecutionResult.Success(
            functionName = "get_payment_history",
            message = "${payments.size} payments totaling K${formatCurrency(totalAmount)}",
            data = mapOf("paymentCount" to payments.size, "totalAmount" to totalAmount)
        )
    }

    private suspend fun executeGetTodayPayments(call: ParsedFunctionCall): FunctionExecutionResult {
        val payments = paymentRepository.getTodayPayments().first()
        val totalAmount = payments.sumOf { it.amount }

        return FunctionExecutionResult.Success(
            functionName = "get_today_payments",
            message = "Today: ${payments.size} payments, K${formatCurrency(totalAmount)}",
            data = mapOf("paymentCount" to payments.size, "totalAmount" to totalAmount)
        )
    }

    private suspend fun executeGetTotalPayments(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "today")
        val (startDate, endDate) = getDateRangeForPeriod(period)

        val stats = paymentRepository.getPaymentStats(startDate, endDate)

        return FunctionExecutionResult.Success(
            functionName = "get_total_payments",
            message = "Payments for $period: K${formatCurrency(stats.totalAmount)}",
            data = mapOf(
                "totalAmount" to stats.totalAmount,
                "paymentCount" to stats.totalPayments,
                "averagePayment" to stats.averagePayment
            )
        )
    }

    private suspend fun executeEditPayment(call: ParsedFunctionCall): FunctionExecutionResult {
        val paymentId = call.getStringArg("payment_id")

        return FunctionExecutionResult.NeedsConfirmation(
            functionName = "edit_payment",
            message = "Payment editing requires careful balance recalculation. Please confirm.",
            details = mapOf("paymentId" to paymentId)
        )
    }

    private suspend fun executeDeletePayment(call: ParsedFunctionCall): FunctionExecutionResult {
        val paymentId = call.getStringArg("payment_id")
        val confirm = call.getBooleanArg("confirm", false)

        val payment = paymentRepository.getPaymentById(paymentId).firstOrNull()
            ?: return FunctionExecutionResult.Error("delete_payment", "Payment not found: $paymentId")

        if (!confirm) {
            return FunctionExecutionResult.NeedsConfirmation(
                functionName = "delete_payment",
                message = "Delete payment of K${formatCurrency(payment.amount)}? Customer balance will be updated.",
                details = mapOf("paymentId" to paymentId, "amount" to payment.amount)
            )
        }

        paymentRepository.deletePayment(payment)

        return FunctionExecutionResult.Success(
            functionName = "delete_payment",
            message = "Payment deleted",
            data = mapOf("deletedPaymentId" to paymentId)
        )
    }

    private suspend fun executeGetPaymentsByMethod(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "today")

        val methodStats = paymentRepository.getPaymentMethodStats()

        return FunctionExecutionResult.Success(
            functionName = "get_payments_by_method",
            message = "Payment methods breakdown",
            data = methodStats.map { (method, stats) ->
                mapOf("method" to method, "count" to stats.count, "total" to stats.totalAmount, "percent" to stats.percentage)
            }
        )
    }

    private suspend fun executeGetRecentPayments(call: ParsedFunctionCall): FunctionExecutionResult {
        val limit = call.getIntArg("limit", 10)

        val payments = paymentRepository.getAllPayments().first().take(limit)

        return FunctionExecutionResult.Success(
            functionName = "get_recent_payments",
            message = "Last $limit payments",
            data = payments.map { mapOf("id" to it.id, "amount" to it.amount, "method" to it.paymentMethod) }
        )
    }

    private suspend fun executeAllocatePayment(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val amount = call.getDoubleArg("amount")

        // This is similar to record_payment but with specific credit allocation
        return executeRecordPayment(call)
    }

    private suspend fun executeGetExpectedPayments(call: ParsedFunctionCall): FunctionExecutionResult {
        val daysAhead = call.getIntArg("days_ahead", 7)

        val creditsDueSoon = creditRepository.getCreditsDueSoon(daysAhead)
        val totalExpected = creditsDueSoon.sumOf { it.amountRemaining }

        return FunctionExecutionResult.Success(
            functionName = "get_expected_payments",
            message = "${creditsDueSoon.size} payments expected in next $daysAhead days, K${formatCurrency(totalExpected)}",
            data = creditsDueSoon.map { mapOf("creditId" to it.id, "amount" to it.amountRemaining, "dueDate" to it.dueDate) }
        )
    }

    private suspend fun executeGeneratePaymentReceipt(call: ParsedFunctionCall): FunctionExecutionResult {
        val paymentId = call.getStringArg("payment_id")
        val format = call.getStringArg("format", "text")

        val payment = paymentRepository.getPaymentById(paymentId).firstOrNull()
            ?: return FunctionExecutionResult.Error("generate_payment_receipt", "Payment not found: $paymentId")

        return FunctionExecutionResult.Success(
            functionName = "generate_payment_receipt",
            message = "Receipt ready for payment #$paymentId",
            data = mapOf(
                "paymentId" to paymentId,
                "amount" to payment.amount,
                "method" to payment.paymentMethod,
                "format" to format,
                "action" to "generate_receipt"
            )
        )
    }

    // ============================================================================
    // INVENTORY OPERATIONS IMPLEMENTATIONS (4)
    // ============================================================================

    private suspend fun executeGetInventoryHistory(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArgOrNull("product_name")
        val movementType = call.getStringArgOrNull("movement_type")
        val period = call.getStringArg("period", "all")

        var logs = if (productName != null) {
            val product = findProductByName(productName) ?: return FunctionExecutionResult.ProductNotFound(productName, getSuggestions(productName))
            inventoryLogDao.getLogsByProduct(product.id).first()
        } else if (movementType != null && movementType != "all") {
            inventoryLogDao.getLogsByActionType(movementType.uppercase()).first()
        } else if (period != "all") {
            val (startDate, endDate) = getDateRangeForPeriod(period)
            inventoryLogDao.getLogsByDateRange(startDate, endDate).first()
        } else {
            inventoryLogDao.getAllLogs().first()
        }

        return FunctionExecutionResult.Success(
            functionName = "get_inventory_history",
            message = "${logs.size} inventory movements",
            data = logs.take(50).map {
                mapOf("productId" to it.productId, "action" to it.actionType, "change" to it.quantityChange, "newStock" to it.newStock)
            }
        )
    }

    private suspend fun executeGetInventoryValue(call: ParsedFunctionCall): FunctionExecutionResult {
        val valueType = call.getStringArg("value_type", "both")
        val category = call.getStringArgOrNull("category")

        var products = if (category != null) {
            productRepository.getProductsByCategory(category).first()
        } else {
            productRepository.getAllProducts().first()
        }

        val costValue = products.sumOf { it.buyingPrice * it.currentStock }
        val retailValue = products.sumOf { it.sellingPrice * it.currentStock }
        val potentialProfit = retailValue - costValue

        return FunctionExecutionResult.Success(
            functionName = "get_inventory_value",
            message = "Inventory: Cost K${formatCurrency(costValue)}, Retail K${formatCurrency(retailValue)}, Potential profit K${formatCurrency(potentialProfit)}",
            data = mapOf(
                "costValue" to costValue,
                "retailValue" to retailValue,
                "potentialProfit" to potentialProfit,
                "productCount" to products.size,
                "totalUnits" to products.sumOf { it.currentStock }
            )
        )
    }

    private suspend fun executeRecordStockTake(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val actualCount = call.getIntArg("actual_count")
        val notes = call.getStringArgOrNull("notes")

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(productName, getSuggestions(productName))

        val discrepancy = actualCount - product.currentStock

        if (discrepancy != 0) {
            productRepository.updateStock(product.id, actualCount, "Stock take: $notes")
        }

        return FunctionExecutionResult.Success(
            functionName = "record_stock_take",
            message = if (discrepancy == 0) {
                "${product.name} stock verified: $actualCount units"
            } else {
                "${product.name} adjusted from ${product.currentStock} to $actualCount (${if (discrepancy > 0) "+" else ""}$discrepancy)"
            },
            data = mapOf(
                "product" to product.name,
                "previousStock" to product.currentStock,
                "newStock" to actualCount,
                "discrepancy" to discrepancy
            )
        )
    }

    private suspend fun executeGetInventorySummary(call: ParsedFunctionCall): FunctionExecutionResult {
        val products = productRepository.getAllProducts().first()
        val lowStock = productRepository.getLowStockProducts().first()
        val outOfStock = productRepository.getOutOfStockProducts().first()

        val totalValue = products.sumOf { it.sellingPrice * it.currentStock }
        val categories = products.map { it.category }.distinct().size

        return FunctionExecutionResult.Success(
            functionName = "get_inventory_summary",
            message = "${products.size} products, K${formatCurrency(totalValue)} value, ${lowStock.size} low, ${outOfStock.size} out of stock",
            data = mapOf(
                "totalProducts" to products.size,
                "totalValue" to totalValue,
                "totalUnits" to products.sumOf { it.currentStock },
                "categories" to categories,
                "lowStockCount" to lowStock.size,
                "outOfStockCount" to outOfStock.size
            )
        )
    }

    // ============================================================================
    // ANALYTICS & REPORTING IMPLEMENTATIONS (10)
    // ============================================================================

    private suspend fun executeGetSalesAnalytics(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "today")
        val metric = call.getStringArg("metric", "all")

        val (startDate, endDate) = getDateRangeForPeriod(period)
        val salesStats = saleRepository.getSalesStats(startDate, endDate)

        val message = when (metric) {
            "revenue" -> "Revenue for $period: K${formatCurrency(salesStats.totalRevenue)}"
            "profit" -> "Sales for $period: ${salesStats.totalSales} transactions"
            "transactions" -> "Transactions for $period: ${salesStats.totalSales}"
            else -> "Sales for $period: ${salesStats.totalSales} transactions, K${formatCurrency(salesStats.totalRevenue)} revenue"
        }

        return FunctionExecutionResult.Success(
            functionName = "get_sales_analytics",
            message = message,
            data = mapOf(
                "period" to period,
                "totalSales" to salesStats.totalSales,
                "totalRevenue" to salesStats.totalRevenue,
                "averageSaleValue" to salesStats.averageSaleValue,
                "totalItemsSold" to salesStats.totalItemsSold
            )
        )
    }

    private suspend fun executeGetDailySalesData(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "this_week")
        val (startDate, endDate) = getDateRangeForPeriod(period)

        val sales = saleRepository.getSalesByDate(startDate, endDate).first()

        // Group by day
        val dailyData = sales.groupBy { DateUtils.formatDate(it.timestamp) }
            .mapValues { (_, daySales) ->
                mapOf("sales" to daySales.size, "revenue" to daySales.sumOf { it.totalAmount })
            }

        return FunctionExecutionResult.Success(
            functionName = "get_daily_sales_data",
            message = "Daily sales data for $period",
            data = dailyData
        )
    }

    private suspend fun executeGetProfitAnalytics(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "this_month")
        val breakdown = call.getStringArg("breakdown", "summary")

        val (startDate, endDate) = getDateRangeForPeriod(period)
        val sales = saleRepository.getSalesByDate(startDate, endDate).first()

        var totalRevenue = 0.0
        var totalCost = 0.0

        for (sale in sales) {
            val product = productRepository.getProductById(sale.productId).firstOrNull()
            if (product != null) {
                totalRevenue += sale.totalAmount
                totalCost += product.buyingPrice * sale.quantity
            }
        }

        val profit = totalRevenue - totalCost
        val marginPercent = if (totalRevenue > 0) (profit / totalRevenue) * 100 else 0.0

        return FunctionExecutionResult.Success(
            functionName = "get_profit_analytics",
            message = "$period: K${formatCurrency(profit)} profit (${String.format("%.1f", marginPercent)}% margin)",
            data = mapOf(
                "revenue" to totalRevenue,
                "cost" to totalCost,
                "profit" to profit,
                "marginPercent" to marginPercent
            )
        )
    }

    private suspend fun executeGetRevenueTrends(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "this_week")
        val compareTo = call.getStringArgOrNull("compare_to")

        val (currentStart, currentEnd) = getDateRangeForPeriod(period)
        val currentStats = saleRepository.getSalesStats(currentStart, currentEnd)

        val comparisonData = if (compareTo != null) {
            val (prevStart, prevEnd) = getDateRangeForPeriod(compareTo)
            val prevStats = saleRepository.getSalesStats(prevStart, prevEnd)
            val change = if (prevStats.totalRevenue > 0) {
                ((currentStats.totalRevenue - prevStats.totalRevenue) / prevStats.totalRevenue) * 100
            } else 0.0
            mapOf("previousRevenue" to prevStats.totalRevenue, "changePercent" to change)
        } else emptyMap()

        return FunctionExecutionResult.Success(
            functionName = "get_revenue_trends",
            message = "$period revenue: K${formatCurrency(currentStats.totalRevenue)}",
            data = mapOf("currentRevenue" to currentStats.totalRevenue) + comparisonData
        )
    }

    private suspend fun executeGetCategoryPerformance(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "this_month")
        val (startDate, endDate) = getDateRangeForPeriod(period)

        val sales = saleRepository.getSalesByDate(startDate, endDate).first()
        val products = productRepository.getAllProducts().first().associateBy { it.id }

        val categoryPerformance = sales.groupBy { products[it.productId]?.category ?: "Unknown" }
            .mapValues { (_, categorySales) ->
                mapOf(
                    "salesCount" to categorySales.size,
                    "revenue" to categorySales.sumOf { it.totalAmount },
                    "units" to categorySales.sumOf { it.quantity }
                )
            }

        return FunctionExecutionResult.Success(
            functionName = "get_category_performance",
            message = "Category performance for $period",
            data = categoryPerformance
        )
    }

    private suspend fun executeGetHourlySalesPattern(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "today")
        val (startDate, endDate) = getDateRangeForPeriod(period)

        val sales = saleRepository.getSalesByDate(startDate, endDate).first()

        val hourlyPattern = sales.groupBy { DateUtils.getHourOfDay(it.timestamp) }
            .mapValues { (_, hourSales) ->
                mapOf("sales" to hourSales.size, "revenue" to hourSales.sumOf { it.totalAmount })
            }

        val peakHour = hourlyPattern.maxByOrNull { (it.value["sales"] as? Int) ?: 0 }?.key

        return FunctionExecutionResult.Success(
            functionName = "get_hourly_sales_pattern",
            message = "Peak hour: ${peakHour ?: "N/A"}:00",
            data = mapOf("hourlyData" to hourlyPattern, "peakHour" to peakHour)
        )
    }

    private suspend fun executeGenerateReport(call: ParsedFunctionCall): FunctionExecutionResult {
        val reportType = call.getStringArg("report_type")
        val format = call.getStringArg("format", "text")

        return FunctionExecutionResult.Success(
            functionName = "generate_report",
            message = "Generating $reportType report in $format format...",
            data = mapOf("reportType" to reportType, "format" to format, "action" to "generate_report")
        )
    }

    private suspend fun executeComparePeriods(call: ParsedFunctionCall): FunctionExecutionResult {
        val period1 = call.getStringArg("period1")
        val period2 = call.getStringArg("period2")

        val (start1, end1) = getDateRangeForPeriod(period1)
        val (start2, end2) = getDateRangeForPeriod(period2)

        val stats1 = saleRepository.getSalesStats(start1, end1)
        val stats2 = saleRepository.getSalesStats(start2, end2)

        val revenueChange = if (stats2.totalRevenue > 0) {
            ((stats1.totalRevenue - stats2.totalRevenue) / stats2.totalRevenue) * 100
        } else 0.0

        return FunctionExecutionResult.Success(
            functionName = "compare_periods",
            message = "$period1 vs $period2: Revenue ${if (revenueChange >= 0) "+" else ""}${String.format("%.1f", revenueChange)}%",
            data = mapOf(
                "period1" to mapOf("revenue" to stats1.totalRevenue, "sales" to stats1.totalSales),
                "period2" to mapOf("revenue" to stats2.totalRevenue, "sales" to stats2.totalSales),
                "revenueChange" to revenueChange
            )
        )
    }

    private suspend fun executeGetDashboardSummary(call: ParsedFunctionCall): FunctionExecutionResult {
        val todaySales = saleRepository.getTodaySales().first()
        val todayRevenue = todaySales.sumOf { it.totalAmount }
        val lowStock = productRepository.getLowStockProducts().first().size
        val totalProducts = productRepository.getAllProducts().first().size
        val totalCredit = creditRepository.getTotalOutstandingCredit().first() ?: 0.0
        val todayPayments = paymentRepository.getTodayPayments().first().sumOf { it.amount }

        return FunctionExecutionResult.Success(
            functionName = "get_dashboard_summary",
            message = "Today: K${formatCurrency(todayRevenue)} sales, ${todaySales.size} transactions",
            data = mapOf(
                "todayRevenue" to todayRevenue,
                "todaySalesCount" to todaySales.size,
                "todayPayments" to todayPayments,
                "totalProducts" to totalProducts,
                "lowStockAlerts" to lowStock,
                "totalCreditsOwed" to totalCredit
            )
        )
    }

    private suspend fun executeGetSlowMovingProducts(call: ParsedFunctionCall): FunctionExecutionResult {
        val daysThreshold = call.getIntArg("days_threshold", 30)
        val limit = call.getIntArg("limit", 20)

        val cutoffDate = System.currentTimeMillis() - (daysThreshold * 24 * 60 * 60 * 1000L)
        val allProducts = productRepository.getAllProducts().first()
        val recentSales = saleRepository.getSalesByDate(cutoffDate, System.currentTimeMillis()).first()

        val productsSold = recentSales.map { it.productId }.toSet()
        val slowMoving = allProducts.filter { it.id !in productsSold && it.currentStock > 0 }.take(limit)

        return FunctionExecutionResult.Success(
            functionName = "get_slow_moving_products",
            message = "${slowMoving.size} products haven't sold in $daysThreshold days",
            data = slowMoving.map { mapOf("name" to it.name, "stock" to it.currentStock, "price" to it.sellingPrice) }
        )
    }

    // ============================================================================
    // SETTINGS & CONFIGURATION (10) - Mostly return actions for UI layer
    // ============================================================================

    private suspend fun executeGetSettings(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "get_settings",
            message = "Opening settings...",
            data = mapOf("action" to "open_settings")
        )
    }

    private suspend fun executeUpdateLanguage(call: ParsedFunctionCall): FunctionExecutionResult {
        val language = call.getStringArg("language")
        return FunctionExecutionResult.Success(
            functionName = "update_language",
            message = "Language will be changed to $language",
            data = mapOf("language" to language, "action" to "update_language")
        )
    }

    private suspend fun executeSetPinProtection(call: ParsedFunctionCall): FunctionExecutionResult {
        val enabled = call.getBooleanArg("enabled", false)
        return FunctionExecutionResult.RequiresPin(
            operation = "set_pin_protection",
            message = if (enabled) "Please set a 4-digit PIN" else "Please verify PIN to disable protection"
        )
    }

    private suspend fun executeUpdateSyncSettings(call: ParsedFunctionCall): FunctionExecutionResult {
        val autoSync = call.getBooleanArgOrNull("auto_sync")
        val wifiOnly = call.getBooleanArgOrNull("wifi_only")
        return FunctionExecutionResult.Success(
            functionName = "update_sync_settings",
            message = "Sync settings will be updated",
            data = mapOf("autoSync" to autoSync, "wifiOnly" to wifiOnly, "action" to "update_sync_settings")
        )
    }

    private suspend fun executeSetLowStockThreshold(call: ParsedFunctionCall): FunctionExecutionResult {
        val threshold = call.getIntArg("threshold")
        return FunctionExecutionResult.Success(
            functionName = "set_low_stock_threshold",
            message = "Default low stock threshold set to $threshold",
            data = mapOf("threshold" to threshold, "action" to "update_settings")
        )
    }

    private suspend fun executeUpdateNotificationSettings(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "update_notification_settings",
            message = "Notification settings will be updated",
            data = mapOf("action" to "update_notification_settings")
        )
    }

    private suspend fun executeSetCurrencyFormat(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "set_currency_format",
            message = "Currency format will be updated",
            data = mapOf("action" to "update_currency_format")
        )
    }

    private suspend fun executeSetShopDetails(call: ParsedFunctionCall): FunctionExecutionResult {
        val shopName = call.getStringArgOrNull("shop_name")
        val ownerName = call.getStringArgOrNull("owner_name")
        return FunctionExecutionResult.Success(
            functionName = "set_shop_details",
            message = "Shop details will be updated",
            data = mapOf("shopName" to shopName, "ownerName" to ownerName, "action" to "update_shop_details")
        )
    }

    private suspend fun executeResetSettings(call: ParsedFunctionCall): FunctionExecutionResult {
        val confirm = call.getBooleanArg("confirm", false)
        if (!confirm) {
            return FunctionExecutionResult.NeedsConfirmation(
                functionName = "reset_settings",
                message = "Reset all settings to default? This cannot be undone.",
                details = emptyMap()
            )
        }
        return FunctionExecutionResult.Success(
            functionName = "reset_settings",
            message = "Settings will be reset to defaults",
            data = mapOf("action" to "reset_settings")
        )
    }

    private suspend fun executeGetAppInfo(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "get_app_info",
            message = "DukaAI v1.0 - Inventory & Sales Manager for Zambian Retailers",
            data = mapOf("version" to "1.0", "minSdk" to 24, "targetSdk" to 36)
        )
    }

    // ============================================================================
    // BACKUP/EXPORT/IMPORT (10) - Return actions for UI layer
    // ============================================================================

    private suspend fun executeBackupData(call: ParsedFunctionCall): FunctionExecutionResult {
        val backupType = call.getStringArg("backup_type", "full")
        val destination = call.getStringArg("destination", "local")
        return FunctionExecutionResult.Success(
            functionName = "backup_data",
            message = "Starting $backupType backup to $destination...",
            data = mapOf("backupType" to backupType, "destination" to destination, "action" to "backup_data")
        )
    }

    private suspend fun executeRestoreData(call: ParsedFunctionCall): FunctionExecutionResult {
        val confirm = call.getBooleanArg("confirm", false)
        if (!confirm) {
            return FunctionExecutionResult.NeedsConfirmation(
                functionName = "restore_data",
                message = "Restore will replace current data. Are you sure?",
                details = emptyMap()
            )
        }
        return FunctionExecutionResult.Success(
            functionName = "restore_data",
            message = "Starting data restore...",
            data = mapOf("action" to "restore_data")
        )
    }

    private suspend fun executeSyncToCloud(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "sync_to_cloud",
            message = "Starting cloud sync...",
            data = mapOf("action" to "sync_to_cloud")
        )
    }

    private suspend fun executeSyncFromCloud(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "sync_from_cloud",
            message = "Pulling latest data from cloud...",
            data = mapOf("action" to "sync_from_cloud")
        )
    }

    private suspend fun executeGetSyncStatus(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "get_sync_status",
            message = "Checking sync status...",
            data = mapOf("action" to "get_sync_status")
        )
    }

    private suspend fun executeExportToCsv(call: ParsedFunctionCall): FunctionExecutionResult {
        val dataType = call.getStringArg("data_type")
        return FunctionExecutionResult.Success(
            functionName = "export_to_csv",
            message = "Exporting $dataType to CSV...",
            data = mapOf("dataType" to dataType, "action" to "export_to_csv")
        )
    }

    private suspend fun executeExportToPdf(call: ParsedFunctionCall): FunctionExecutionResult {
        val reportType = call.getStringArg("report_type")
        return FunctionExecutionResult.Success(
            functionName = "export_to_pdf",
            message = "Generating $reportType PDF...",
            data = mapOf("reportType" to reportType, "action" to "export_to_pdf")
        )
    }

    private suspend fun executeImportProducts(call: ParsedFunctionCall): FunctionExecutionResult {
        val filePath = call.getStringArg("file_path")
        return FunctionExecutionResult.Success(
            functionName = "import_products",
            message = "Importing products from $filePath...",
            data = mapOf("filePath" to filePath, "action" to "import_products")
        )
    }

    private suspend fun executeListBackups(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "list_backups",
            message = "Loading backup list...",
            data = mapOf("action" to "list_backups")
        )
    }

    private suspend fun executeDeleteBackup(call: ParsedFunctionCall): FunctionExecutionResult {
        val backupId = call.getStringArg("backup_id")
        val confirm = call.getBooleanArg("confirm", false)
        if (!confirm) {
            return FunctionExecutionResult.NeedsConfirmation(
                functionName = "delete_backup",
                message = "Delete backup $backupId?",
                details = mapOf("backupId" to backupId)
            )
        }
        return FunctionExecutionResult.Success(
            functionName = "delete_backup",
            message = "Backup deleted",
            data = mapOf("backupId" to backupId, "action" to "delete_backup")
        )
    }

    // ============================================================================
    // BARCODE/SCANNER (5)
    // ============================================================================

    private suspend fun executeScanProductBarcode(call: ParsedFunctionCall): FunctionExecutionResult {
        val action = call.getStringArg("action", "find_product")
        return FunctionExecutionResult.NavigationAction(
            destination = "scanner",
            params = mapOf("action" to action)
        )
    }

    private suspend fun executeFindByBarcode(call: ParsedFunctionCall): FunctionExecutionResult {
        val barcode = call.getStringArg("barcode")
        val product = productRepository.getProductByBarcode(barcode)

        return if (product != null) {
            FunctionExecutionResult.Success(
                functionName = "find_by_barcode",
                message = "Found: ${product.name} - K${formatCurrency(product.sellingPrice)}",
                data = mapOf("product" to product.name, "price" to product.sellingPrice, "stock" to product.currentStock)
            )
        } else {
            FunctionExecutionResult.ProductNotFound(
                productName = "barcode: $barcode",
                suggestions = emptyList()
            )
        }
    }

    private suspend fun executeUpdateProductBarcode(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val barcode = call.getStringArg("barcode")

        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(productName, getSuggestions(productName))

        val updatedProduct = product.copy(barcode = barcode, updatedAt = System.currentTimeMillis())
        productRepository.updateProduct(updatedProduct)

        return FunctionExecutionResult.Success(
            functionName = "update_product_barcode",
            message = "Barcode for ${product.name} set to $barcode",
            data = mapOf("product" to product.name, "barcode" to barcode)
        )
    }

    private suspend fun executeGenerateBarcode(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val format = call.getStringArg("format", "EAN_13")

        return FunctionExecutionResult.Success(
            functionName = "generate_barcode",
            message = "Generating $format barcode for $productName...",
            data = mapOf("productName" to productName, "format" to format, "action" to "generate_barcode")
        )
    }

    private suspend fun executeGetProductsWithoutBarcode(call: ParsedFunctionCall): FunctionExecutionResult {
        val products = productRepository.getAllProducts().first().filter { it.barcode.isNullOrEmpty() }

        return FunctionExecutionResult.Success(
            functionName = "get_products_without_barcode",
            message = "${products.size} products without barcode",
            data = products.map { mapOf("name" to it.name, "id" to it.id) }
        )
    }

    // ============================================================================
    // NAVIGATION/UI (12)
    // ============================================================================

    private fun executeNavigation(destination: String): FunctionExecutionResult {
        return FunctionExecutionResult.NavigationAction(
            destination = destination,
            params = emptyMap()
        )
    }

    private suspend fun executeOpenProductDetail(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val product = findProductByName(productName)
            ?: return FunctionExecutionResult.ProductNotFound(productName, getSuggestions(productName))

        return FunctionExecutionResult.NavigationAction(
            destination = "product_detail",
            params = mapOf("productId" to product.id)
        )
    }

    // ============================================================================
    // MACHINE LEARNING (5)
    // ============================================================================

    private suspend fun executeClassifyProductImage(call: ParsedFunctionCall): FunctionExecutionResult {
        val actionOnMatch = call.getStringArg("action_on_match", "just_identify")
        return FunctionExecutionResult.Success(
            functionName = "classify_product_image",
            message = "Opening camera for product recognition...",
            data = mapOf("actionOnMatch" to actionOnMatch, "action" to "classify_product_image")
        )
    }

    private suspend fun executeGetMlModelStatus(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "get_ml_model_status",
            message = "ML models: Product classifier loaded, FunctionGemma active",
            data = mapOf(
                "productClassifier" to "loaded",
                "functionGemma" to "active",
                "voiceRecognition" to "ready"
            )
        )
    }

    private suspend fun executeSuggestProductPrice(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val buyingPrice = call.getDoubleArgOrNull("buying_price")

        val suggestedMargin = 0.30 // 30% default margin
        val suggestedPrice = if (buyingPrice != null) {
            buyingPrice * (1 + suggestedMargin)
        } else {
            val similarProducts = productRepository.searchProducts(productName).first()
            if (similarProducts.isNotEmpty()) {
                similarProducts.map { it.sellingPrice }.average()
            } else 0.0
        }

        return FunctionExecutionResult.Success(
            functionName = "suggest_product_price",
            message = "Suggested price for $productName: K${formatCurrency(suggestedPrice)}",
            data = mapOf("suggestedPrice" to suggestedPrice, "margin" to suggestedMargin * 100)
        )
    }

    private suspend fun executePredictLowStock(call: ParsedFunctionCall): FunctionExecutionResult {
        val daysAhead = call.getIntArg("days_ahead", 7)

        // Simple prediction based on recent sales velocity
        val products = productRepository.getAllProducts().first()
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val recentSales = saleRepository.getSalesByDate(weekAgo, System.currentTimeMillis()).first()

        val predictions = products.mapNotNull { product ->
            val productSales = recentSales.filter { it.productId == product.id }
            val dailyVelocity = productSales.sumOf { it.quantity } / 7.0
            val predictedStock = product.currentStock - (dailyVelocity * daysAhead)

            if (predictedStock <= product.minStockThreshold) {
                mapOf(
                    "name" to product.name,
                    "currentStock" to product.currentStock,
                    "predictedStock" to predictedStock.toInt(),
                    "daysUntilLow" to if (dailyVelocity > 0) ((product.currentStock - product.minStockThreshold) / dailyVelocity).toInt() else 999
                )
            } else null
        }

        return FunctionExecutionResult.Success(
            functionName = "predict_low_stock",
            message = "${predictions.size} products predicted to run low in $daysAhead days",
            data = predictions
        )
    }

    private suspend fun executeGetSalesForecast(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "next_week")
        val productName = call.getStringArgOrNull("product_name")

        val daysToForecast = if (period == "next_month") 30 else 7

        // Simple forecast based on historical average
        val monthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        val historicalSales = saleRepository.getSalesByDate(monthAgo, System.currentTimeMillis()).first()

        val forecast = if (productName != null) {
            val product = findProductByName(productName)
            if (product != null) {
                val productSales = historicalSales.filter { it.productId == product.id }
                val dailyAvg = productSales.sumOf { it.totalAmount } / 30.0
                dailyAvg * daysToForecast
            } else 0.0
        } else {
            val dailyAvg = historicalSales.sumOf { it.totalAmount } / 30.0
            dailyAvg * daysToForecast
        }

        return FunctionExecutionResult.Success(
            functionName = "get_sales_forecast",
            message = "Forecast for $period: K${formatCurrency(forecast)}",
            data = mapOf("forecast" to forecast, "period" to period, "daysToForecast" to daysToForecast)
        )
    }

    // ============================================================================
    // VOICE FRAMEWORK (6)
    // ============================================================================

    private suspend fun executeSetVoiceLanguage(call: ParsedFunctionCall): FunctionExecutionResult {
        val language = call.getStringArg("language")
        return FunctionExecutionResult.Success(
            functionName = "set_voice_language",
            message = "Voice language set to $language",
            data = mapOf("language" to language, "action" to "set_voice_language")
        )
    }

    private suspend fun executeHelpWithVoiceCommands(call: ParsedFunctionCall): FunctionExecutionResult {
        val category = call.getStringArg("category", "all")

        val helpText = when (category) {
            "sales" -> "Say 'sell 3 coca-cola' or 'record sale of bread'"
            "products" -> "Say 'check stock of sugar' or 'add product milk at 20 kwacha'"
            "customers" -> "Say 'how much does John owe' or 'add customer Mary'"
            "credits" -> "Say 'John paid 500' or 'show overdue credits'"
            "analytics" -> "Say 'today's sales' or 'this month's revenue'"
            "navigation" -> "Say 'go to dashboard' or 'open scanner'"
            else -> "Try: 'sell 3 coca-cola', 'check stock of bread', 'John paid 500', 'today's sales'"
        }

        return FunctionExecutionResult.Success(
            functionName = "help_with_voice_commands",
            message = helpText,
            data = mapOf("category" to category, "examples" to helpText)
        )
    }

    private suspend fun executeRepeatLastResponse(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "repeat_last_response",
            message = "Repeating last response...",
            data = mapOf("action" to "repeat_last_response")
        )
    }

    private suspend fun executeGetVoiceCommandHistory(call: ParsedFunctionCall): FunctionExecutionResult {
        val limit = call.getIntArg("limit", 10)
        return FunctionExecutionResult.Success(
            functionName = "get_voice_command_history",
            message = "Loading last $limit voice commands...",
            data = mapOf("limit" to limit, "action" to "get_voice_command_history")
        )
    }

    private suspend fun executeToggleVoiceFeedback(call: ParsedFunctionCall): FunctionExecutionResult {
        val enabled = call.getBooleanArg("enabled", true)
        return FunctionExecutionResult.Success(
            functionName = "toggle_voice_feedback",
            message = "Voice feedback ${if (enabled) "enabled" else "disabled"}",
            data = mapOf("enabled" to enabled, "action" to "toggle_voice_feedback")
        )
    }

    private suspend fun executeCancelOperation(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "cancel_operation",
            message = "Operation cancelled",
            data = mapOf("action" to "cancel_operation")
        )
    }

    // ============================================================================
    // VALIDATION & CONFIRMATION (3)
    // ============================================================================

    private suspend fun executeVerifyPin(call: ParsedFunctionCall): FunctionExecutionResult {
        val pin = call.getStringArg("pin")
        return FunctionExecutionResult.RequiresPin(
            operation = "verify_pin",
            message = "PIN verification required"
        )
    }

    private suspend fun executeConfirmOperation(call: ParsedFunctionCall): FunctionExecutionResult {
        val operationId = call.getStringArg("operation_id")
        val confirmed = call.getBooleanArg("confirmed", false)
        return FunctionExecutionResult.Success(
            functionName = "confirm_operation",
            message = if (confirmed) "Operation confirmed" else "Operation cancelled",
            data = mapOf("operationId" to operationId, "confirmed" to confirmed)
        )
    }

    private suspend fun executeCancelConfirmation(call: ParsedFunctionCall): FunctionExecutionResult {
        return FunctionExecutionResult.Success(
            functionName = "cancel_confirmation",
            message = "Confirmation cancelled",
            data = mapOf("action" to "cancel_confirmation")
        )
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private suspend fun findProductByName(name: String): ProductEntity? {
        val products = productRepository.searchProducts(name).first()
        return products.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: products.firstOrNull()
    }

    private suspend fun findCustomerByName(name: String): CustomerEntity? {
        val customers = customerRepository.searchCustomers(name).first()
        return customers.firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?: customers.firstOrNull()
    }

    private suspend fun getSuggestions(productName: String): List<String> {
        return productRepository.searchProducts(productName).first().take(3).map { it.name }
    }

    private suspend fun getCustomerSuggestions(customerName: String): List<String> {
        return customerRepository.searchCustomers(customerName).first().take(3).map { it.name }
    }

    private fun getDateRangeForPeriod(period: String): Pair<Long, Long> {
        return when (period) {
            "today" -> DateUtils.getStartOfDay() to DateUtils.getEndOfDay()
            "yesterday" -> DateUtils.getStartOfYesterday() to DateUtils.getEndOfYesterday()
            "this_week" -> DateUtils.getStartOfWeek() to DateUtils.getEndOfDay()
            "last_week" -> {
                val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
                val twoWeeksAgo = weekAgo - (7 * 24 * 60 * 60 * 1000L)
                twoWeeksAgo to weekAgo
            }
            "this_month" -> DateUtils.getStartOfMonth() to DateUtils.getEndOfDay()
            "last_month" -> {
                val monthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
                val twoMonthsAgo = monthAgo - (30 * 24 * 60 * 60 * 1000L)
                twoMonthsAgo to monthAgo
            }
            "last_30_days" -> {
                val thirtyDaysAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
                thirtyDaysAgo to System.currentTimeMillis()
            }
            "last_90_days" -> {
                val ninetyDaysAgo = System.currentTimeMillis() - (90 * 24 * 60 * 60 * 1000L)
                ninetyDaysAgo to System.currentTimeMillis()
            }
            "all_time" -> 0L to System.currentTimeMillis()
            else -> DateUtils.getStartOfDay() to DateUtils.getEndOfDay()
        }
    }

    private fun parseDateString(dateStr: String): Long {
        return when (dateStr.lowercase()) {
            "tomorrow" -> System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
            "next_week" -> System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
            "next_month" -> System.currentTimeMillis() + (30 * 24 * 60 * 60 * 1000L)
            else -> {
                // Try to parse ISO date format
                try {
                    java.text.SimpleDateFormat("yyyy-MM-dd").parse(dateStr)?.time ?: System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L) // Default to 1 week
                }
            }
        }
    }

    private fun formatCurrency(amount: Double): String {
        return String.format("%.2f", amount)
    }
}
