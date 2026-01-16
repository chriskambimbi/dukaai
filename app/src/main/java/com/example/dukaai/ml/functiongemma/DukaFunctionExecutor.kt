package com.example.dukaai.ml.functiongemma

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
 */
@Singleton
class DukaFunctionExecutor @Inject constructor(
    private val productRepository: ProductRepository,
    private val saleRepository: SaleRepository,
    private val customerRepository: CustomerRepository,
    private val creditRepository: CreditRepository,
    private val paymentRepository: PaymentRepository
) {

    /**
     * Execute a parsed function call
     */
    suspend fun execute(call: ParsedFunctionCall): FunctionExecutionResult {
        return try {
            when (call.functionName) {
                "record_sale" -> executeRecordSale(call)
                "add_product" -> executeAddProduct(call)
                "update_stock" -> executeUpdateStock(call)
                "check_stock" -> executeCheckStock(call)
                "search_products" -> executeSearchProducts(call)
                "record_payment" -> executeRecordPayment(call)
                "add_customer" -> executeAddCustomer(call)
                "get_customer_balance" -> executeGetCustomerBalance(call)
                "get_sales_analytics" -> executeGetSalesAnalytics(call)
                "get_low_stock_alerts" -> executeGetLowStockAlerts(call)
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

    // ============ Function Implementations ============

    /**
     * Record a sale transaction
     */
    private suspend fun executeRecordSale(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val quantity = call.getIntArg("quantity", 1)
        val customerName = if (call.hasArg("customer_name")) call.getStringArg("customer_name") else null
        val saleType = call.getStringArg("sale_type", "cash").uppercase()

        // Find the product by name
        val products = productRepository.searchProducts(productName).first()
        val product = products.firstOrNull { it.name.equals(productName, ignoreCase = true) }
            ?: products.firstOrNull()

        if (product == null) {
            return FunctionExecutionResult.ProductNotFound(
                productName = productName,
                suggestions = products.take(3).map { it.name }
            )
        }

        // Check stock
        if (product.currentStock < quantity) {
            return FunctionExecutionResult.InsufficientStock(
                productName = product.name,
                requestedQuantity = quantity,
                availableStock = product.currentStock
            )
        }

        // Find customer if credit sale
        var customerId: String? = null
        if (saleType == "CREDIT" && customerName != null) {
            val customers = customerRepository.searchCustomers(customerName).first()
            val customer = customers.firstOrNull { it.name.equals(customerName, ignoreCase = true) }
                ?: customers.firstOrNull()

            if (customer == null) {
                return FunctionExecutionResult.CustomerNotFound(
                    customerName = customerName,
                    suggestions = customers.take(3).map { it.name }
                )
            }
            customerId = customer.id
        }

        // Create and record the sale
        val sale = SaleEntity(
            productId = product.id,
            customerId = customerId,
            quantity = quantity,
            unitPrice = product.sellingPrice,
            totalAmount = product.sellingPrice * quantity,
            saleType = if (customerId != null) "CREDIT" else "CASH"
        )

        val result = saleRepository.recordSale(sale)

        return if (result.isSuccess) {
            // If credit sale, record in credit ledger
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
                message = "Sale recorded: ${quantity}x ${product.name} for K${String.format("%.2f", sale.totalAmount)}",
                data = mapOf(
                    "product" to product.name,
                    "quantity" to quantity,
                    "total" to sale.totalAmount,
                    "saleType" to sale.saleType,
                    "remainingStock" to (product.currentStock - quantity)
                )
            )
        } else {
            FunctionExecutionResult.Error(
                functionName = "record_sale",
                errorMessage = result.exceptionOrNull()?.message ?: "Failed to record sale"
            )
        }
    }

    /**
     * Add a new product to inventory
     */
    private suspend fun executeAddProduct(call: ParsedFunctionCall): FunctionExecutionResult {
        val name = call.getStringArg("name")
        val sellingPrice = call.getDoubleArg("selling_price")
        val category = call.getStringArg("category", "general")
        val initialStock = call.getIntArg("initial_stock", 0)
        val buyingPrice = call.getDoubleArg("buying_price", sellingPrice * 0.7)

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
            sellingPrice = sellingPrice
        )

        val result = productRepository.insertProduct(product)

        return FunctionExecutionResult.Success(
            functionName = "add_product",
            message = "Added product: $name at K${String.format("%.2f", sellingPrice)}",
            data = mapOf(
                "id" to product.id,
                "name" to name,
                "price" to sellingPrice,
                "category" to category,
                "stock" to initialStock
            )
        )
    }

    /**
     * Update stock for an existing product
     */
    private suspend fun executeUpdateStock(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")
        val quantity = call.getIntArg("quantity")
        val reason = if (call.hasArg("reason")) call.getStringArg("reason") else null

        // Find the product
        val products = productRepository.searchProducts(productName).first()
        val product = products.firstOrNull { it.name.equals(productName, ignoreCase = true) }
            ?: products.firstOrNull()

        if (product == null) {
            return FunctionExecutionResult.ProductNotFound(
                productName = productName,
                suggestions = products.take(3).map { it.name }
            )
        }

        val newStock = product.currentStock + quantity

        // Prevent negative stock
        if (newStock < 0) {
            return FunctionExecutionResult.Error(
                functionName = "update_stock",
                errorMessage = "Cannot reduce stock below 0. Current stock: ${product.currentStock}, attempted change: $quantity"
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

    /**
     * Check stock level for a product
     */
    private suspend fun executeCheckStock(call: ParsedFunctionCall): FunctionExecutionResult {
        val productName = call.getStringArg("product_name")

        // Find the product
        val products = productRepository.searchProducts(productName).first()
        val product = products.firstOrNull { it.name.equals(productName, ignoreCase = true) }
            ?: products.firstOrNull()

        if (product == null) {
            return FunctionExecutionResult.ProductNotFound(
                productName = productName,
                suggestions = products.take(3).map { it.name }
            )
        }

        val isLowStock = product.currentStock <= product.minStockThreshold

        return FunctionExecutionResult.Success(
            functionName = "check_stock",
            message = if (isLowStock) {
                "${product.name} has ${product.currentStock} units - LOW STOCK WARNING"
            } else {
                "${product.name} has ${product.currentStock} units in stock"
            },
            data = mapOf(
                "product" to product.name,
                "currentStock" to product.currentStock,
                "minThreshold" to product.minStockThreshold,
                "isLowStock" to isLowStock
            )
        )
    }

    /**
     * Search for products
     */
    private suspend fun executeSearchProducts(call: ParsedFunctionCall): FunctionExecutionResult {
        val query = call.getStringArg("query")

        val products = productRepository.searchProducts(query).first()

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

    /**
     * Record a customer payment
     */
    private suspend fun executeRecordPayment(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")
        val amount = call.getDoubleArg("amount")
        val paymentMethod = call.getStringArg("payment_method", "cash").uppercase()

        // Find the customer
        val customers = customerRepository.searchCustomers(customerName).first()
        val customer = customers.firstOrNull { it.name.equals(customerName, ignoreCase = true) }
            ?: customers.firstOrNull()

        if (customer == null) {
            return FunctionExecutionResult.CustomerNotFound(
                customerName = customerName,
                suggestions = customers.take(3).map { it.name }
            )
        }

        // Get customer's outstanding credits
        val credits = creditRepository.getCreditsByCustomer(customer.id).first()
            .filter { it.status != "PAID" }
            .sortedBy { it.createdAt }

        if (credits.isEmpty()) {
            return FunctionExecutionResult.Error(
                functionName = "record_payment",
                errorMessage = "${customer.name} has no outstanding credit"
            )
        }

        // Apply payment to oldest credit first
        var remainingAmount = amount
        val paymentsRecorded = mutableListOf<String>()

        for (credit in credits) {
            if (remainingAmount <= 0) break

            val paymentAmount = minOf(remainingAmount, credit.amountRemaining)
            val payment = PaymentEntity(
                creditId = credit.id,
                amount = paymentAmount,
                paymentMethod = paymentMethod
            )

            creditRepository.recordPayment(credit.id, payment)
            paymentsRecorded.add("K${String.format("%.2f", paymentAmount)} for sale #${credit.saleId}")
            remainingAmount -= paymentAmount
        }

        // Get updated balance
        val newBalance = creditRepository.getCustomerTotalCredit(customer.id).first() ?: 0.0

        return FunctionExecutionResult.Success(
            functionName = "record_payment",
            message = "Recorded payment of K${String.format("%.2f", amount)} from ${customer.name}. New balance: K${String.format("%.2f", newBalance)}",
            data = mapOf(
                "customer" to customer.name,
                "amountPaid" to amount,
                "newBalance" to newBalance,
                "paymentsApplied" to paymentsRecorded
            )
        )
    }

    /**
     * Add a new customer
     */
    private suspend fun executeAddCustomer(call: ParsedFunctionCall): FunctionExecutionResult {
        val name = call.getStringArg("name")
        val phone = if (call.hasArg("phone")) call.getStringArg("phone") else null
        val address = if (call.hasArg("address")) call.getStringArg("address") else null

        val customer = CustomerEntity(
            name = name,
            phoneNumber = phone,
            address = address
        )

        val result = customerRepository.addCustomer(customer)

        return if (result.isSuccess) {
            FunctionExecutionResult.Success(
                functionName = "add_customer",
                message = "Added customer: $name" + (phone?.let { " ($it)" } ?: ""),
                data = mapOf(
                    "id" to customer.id,
                    "name" to name,
                    "phone" to phone,
                    "address" to address
                )
            )
        } else {
            FunctionExecutionResult.Error(
                functionName = "add_customer",
                errorMessage = result.exceptionOrNull()?.message ?: "Failed to add customer"
            )
        }
    }

    /**
     * Get customer credit balance
     */
    private suspend fun executeGetCustomerBalance(call: ParsedFunctionCall): FunctionExecutionResult {
        val customerName = call.getStringArg("customer_name")

        // Find the customer
        val customers = customerRepository.searchCustomers(customerName).first()
        val customer = customers.firstOrNull { it.name.equals(customerName, ignoreCase = true) }
            ?: customers.firstOrNull()

        if (customer == null) {
            return FunctionExecutionResult.CustomerNotFound(
                customerName = customerName,
                suggestions = customers.take(3).map { it.name }
            )
        }

        val balance = creditRepository.getCustomerTotalCredit(customer.id).first() ?: 0.0
        val creditStats = creditRepository.getCustomerCreditStats(customer.id)

        return FunctionExecutionResult.Success(
            functionName = "get_customer_balance",
            message = if (balance > 0) {
                "${customer.name} owes K${String.format("%.2f", balance)}"
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

    /**
     * Get sales analytics
     */
    private suspend fun executeGetSalesAnalytics(call: ParsedFunctionCall): FunctionExecutionResult {
        val period = call.getStringArg("period", "today")
        val metric = call.getStringArg("metric", "all")

        val (startDate, endDate) = when (period) {
            "today" -> DateUtils.getStartOfDay() to DateUtils.getEndOfDay()
            "yesterday" -> DateUtils.getStartOfYesterday() to DateUtils.getEndOfYesterday()
            "this_week" -> DateUtils.getStartOfWeek() to DateUtils.getEndOfDay()
            "this_month" -> DateUtils.getStartOfMonth() to DateUtils.getEndOfDay()
            else -> DateUtils.getStartOfDay() to DateUtils.getEndOfDay()
        }

        val salesStats = saleRepository.getSalesStats(startDate, endDate)

        val message = when (metric) {
            "revenue" -> "Revenue for $period: K${String.format("%.2f", salesStats.totalRevenue)}"
            "transactions" -> "Transactions for $period: ${salesStats.totalSales}"
            else -> "Sales for $period: ${salesStats.totalSales} transactions, K${String.format("%.2f", salesStats.totalRevenue)} revenue"
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

    /**
     * Get low stock alerts
     */
    private suspend fun executeGetLowStockAlerts(call: ParsedFunctionCall): FunctionExecutionResult {
        val lowStockProducts = productRepository.getLowStockProducts().first()
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
}
