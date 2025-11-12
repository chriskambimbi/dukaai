package com.example.dukaai.data.sync

import android.util.Log
import com.example.dukaai.data.local.DukaDatabase
import com.example.dukaai.data.local.entity.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Sync Service
 * Handles synchronization between local Room database and Firebase Firestore
 */
@Singleton
class FirebaseSyncService @Inject constructor(
    private val database: DukaDatabase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    companion object {
        private const val TAG = "FirebaseSyncService"
        private const val COLLECTION_PRODUCTS = "products"
        private const val COLLECTION_SALES = "sales"
        private const val COLLECTION_CUSTOMERS = "customers"
        private const val COLLECTION_CREDITS = "credits"
        private const val COLLECTION_PAYMENTS = "payments"
        private const val COLLECTION_INVENTORY_LOGS = "inventory_logs"
        private const val FIELD_TIMESTAMP = "updatedAt"
        private const val FIELD_USER_ID = "userId"
    }

    /**
     * Get current user ID (required for multi-user sync)
     */
    private fun getUserId(): String? = auth.currentUser?.uid

    /**
     * Get user-specific collection reference
     */
    private fun getUserCollection(collectionName: String) =
        firestore.collection("users")
            .document(getUserId() ?: throw IllegalStateException("User not authenticated"))
            .collection(collectionName)

    /**
     * Sync all entities to cloud
     */
    suspend fun syncToCloud(): SyncResult {
        return try {
            if (getUserId() == null) {
                return SyncResult.Failure("User not authenticated")
            }

            var totalSynced = 0
            val conflicts = mutableListOf<SyncConflict>()

            // Sync products
            totalSynced += syncProductsToCloud()

            // Sync sales
            totalSynced += syncSalesToCloud()

            // Sync customers
            totalSynced += syncCustomersToCloud()

            // Sync credits
            totalSynced += syncCreditsToCloud()

            // Sync payments
            totalSynced += syncPaymentsToCloud()

            // Sync inventory logs
            totalSynced += syncInventoryLogsToCloud()

            SyncResult.Success(totalSynced, conflicts)
        } catch (e: Exception) {
            Log.e(TAG, "Sync to cloud failed", e)
            SyncResult.Failure(e.message ?: "Unknown error")
        }
    }

    /**
     * Sync all entities from cloud
     */
    suspend fun syncFromCloud(): SyncResult {
        return try {
            if (getUserId() == null) {
                return SyncResult.Failure("User not authenticated")
            }

            var totalSynced = 0
            val conflicts = mutableListOf<SyncConflict>()

            // Sync products
            totalSynced += syncProductsFromCloud()

            // Sync sales
            totalSynced += syncSalesFromCloud()

            // Sync customers
            totalSynced += syncCustomersFromCloud()

            // Sync credits
            totalSynced += syncCreditsFromCloud()

            // Sync payments
            totalSynced += syncPaymentsFromCloud()

            // Sync inventory logs
            totalSynced += syncInventoryLogsFromCloud()

            SyncResult.Success(totalSynced, conflicts)
        } catch (e: Exception) {
            Log.e(TAG, "Sync from cloud failed", e)
            SyncResult.Failure(e.message ?: "Unknown error")
        }
    }

    /**
     * Two-way sync: sync to cloud first, then from cloud
     */
    suspend fun performTwoWaySync(): SyncResult {
        return try {
            val uploadResult = syncToCloud()
            val downloadResult = syncFromCloud()

            when {
                uploadResult is SyncResult.Success && downloadResult is SyncResult.Success -> {
                    SyncResult.Success(
                        uploadResult.syncedCount + downloadResult.syncedCount,
                        uploadResult.conflicts + downloadResult.conflicts
                    )
                }
                else -> {
                    SyncResult.Partial(
                        syncedCount = ((uploadResult as? SyncResult.Success)?.syncedCount ?: 0) +
                                ((downloadResult as? SyncResult.Success)?.syncedCount ?: 0),
                        failedCount = 0
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Two-way sync failed", e)
            SyncResult.Failure(e.message ?: "Unknown error")
        }
    }

    // ==================== Products ====================

    private suspend fun syncProductsToCloud(): Int {
        val products = database.productDao().getAllProducts().first()
        var count = 0

        products.forEach { product ->
            try {
                val data = mapOf(
                    "id" to product.id,
                    "name" to product.name,
                    "barcode" to product.barcode,
                    "category" to product.category,
                    "currentStock" to product.currentStock,
                    "minStockThreshold" to product.minStockThreshold,
                    "buyingPrice" to product.buyingPrice,
                    "sellingPrice" to product.sellingPrice,
                    "imageUrl" to product.imageUrl,
                    "createdAt" to product.createdAt,
                    "updatedAt" to product.updatedAt,
                    FIELD_USER_ID to getUserId()
                )

                getUserCollection(COLLECTION_PRODUCTS)
                    .document(product.id)
                    .set(data, SetOptions.merge())
                    .await()

                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync product ${product.id}", e)
            }
        }

        return count
    }

    private suspend fun syncProductsFromCloud(): Int {
        val snapshot = getUserCollection(COLLECTION_PRODUCTS).get().await()
        var count = 0

        snapshot.documents.forEach { doc ->
            try {
                val product = documentToProduct(doc)
                database.productDao().insertProduct(product)
                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync product from cloud", e)
            }
        }

        return count
    }

    // ==================== Sales ====================

    private suspend fun syncSalesToCloud(): Int {
        val sales = database.saleDao().getAllSales().first()
        var count = 0

        sales.forEach { sale ->
            try {
                val data = mapOf(
                    "id" to sale.id,
                    "productId" to sale.productId,
                    "customerId" to sale.customerId,
                    "quantity" to sale.quantity,
                    "unitPrice" to sale.unitPrice,
                    "totalAmount" to sale.totalAmount,
                    "saleType" to sale.saleType,
                    "timestamp" to sale.timestamp,
                    FIELD_USER_ID to getUserId()
                )

                getUserCollection(COLLECTION_SALES)
                    .document(sale.id)
                    .set(data, SetOptions.merge())
                    .await()

                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync sale ${sale.id}", e)
            }
        }

        return count
    }

    private suspend fun syncSalesFromCloud(): Int {
        val snapshot = getUserCollection(COLLECTION_SALES).get().await()
        var count = 0

        snapshot.documents.forEach { doc ->
            try {
                val sale = documentToSale(doc)
                database.saleDao().insertSale(sale)
                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync sale from cloud", e)
            }
        }

        return count
    }

    // ==================== Customers ====================

    private suspend fun syncCustomersToCloud(): Int {
        val customers = database.customerDao().getAllCustomers().first()
        var count = 0

        customers.forEach { customer ->
            try {
                val data = mapOf(
                    "id" to customer.id,
                    "name" to customer.name,
                    "phoneNumber" to customer.phoneNumber,
                    "address" to customer.address,
                    "notes" to customer.notes,
                    "createdAt" to customer.createdAt,
                    "updatedAt" to customer.updatedAt,
                    FIELD_USER_ID to getUserId()
                )

                getUserCollection(COLLECTION_CUSTOMERS)
                    .document(customer.id)
                    .set(data, SetOptions.merge())
                    .await()

                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync customer ${customer.id}", e)
            }
        }

        return count
    }

    private suspend fun syncCustomersFromCloud(): Int {
        val snapshot = getUserCollection(COLLECTION_CUSTOMERS).get().await()
        var count = 0

        snapshot.documents.forEach { doc ->
            try {
                val customer = documentToCustomer(doc)
                database.customerDao().insertCustomer(customer)
                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync customer from cloud", e)
            }
        }

        return count
    }

    // ==================== Credits ====================

    private suspend fun syncCreditsToCloud(): Int {
        val credits = database.creditLedgerDao().getAllCreditRecords().first()
        var count = 0

        for (credit in credits) {
            try {
                val data = mapOf(
                    "id" to credit.id,
                    "customerId" to credit.customerId,
                    "saleId" to credit.saleId,
                    "amount" to credit.amount,
                    "amountPaid" to credit.amountPaid,
                    "amountRemaining" to credit.amountRemaining,
                    "dueDate" to credit.dueDate,
                    "status" to credit.status,
                    "createdAt" to credit.createdAt,
                    "updatedAt" to credit.updatedAt,
                    FIELD_USER_ID to getUserId()
                )

                getUserCollection(COLLECTION_CREDITS)
                    .document(credit.id)
                    .set(data, SetOptions.merge())
                    .await()

                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync credit ${credit.id}", e)
            }
        }

        return count
    }

    private suspend fun syncCreditsFromCloud(): Int {
        val snapshot = getUserCollection(COLLECTION_CREDITS).get().await()
        var count = 0

        snapshot.documents.forEach { doc ->
            try {
                val credit = documentToCredit(doc)
                database.creditLedgerDao().insertCredit(credit)
                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync credit from cloud", e)
            }
        }

        return count
    }

    // ==================== Payments ====================

    private suspend fun syncPaymentsToCloud(): Int {
        val payments = database.paymentDao().getAllPayments().first()
        var count = 0

        payments.forEach { payment ->
            try {
                val data = mapOf(
                    "id" to payment.id,
                    "creditId" to payment.creditId,
                    "amount" to payment.amount,
                    "paymentMethod" to payment.paymentMethod,
                    "notes" to payment.notes,
                    "timestamp" to payment.timestamp,
                    FIELD_USER_ID to getUserId()
                )

                getUserCollection(COLLECTION_PAYMENTS)
                    .document(payment.id)
                    .set(data, SetOptions.merge())
                    .await()

                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync payment ${payment.id}", e)
            }
        }

        return count
    }

    private suspend fun syncPaymentsFromCloud(): Int {
        val snapshot = getUserCollection(COLLECTION_PAYMENTS).get().await()
        var count = 0

        snapshot.documents.forEach { doc ->
            try {
                val payment = documentToPayment(doc)
                database.paymentDao().insertPayment(payment)
                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync payment from cloud", e)
            }
        }

        return count
    }

    // ==================== Inventory Logs ====================

    private suspend fun syncInventoryLogsToCloud(): Int {
        val logs = database.inventoryLogDao().getAllLogs().first()
        var count = 0

        logs.forEach { log ->
            try {
                val data = mapOf(
                    "id" to log.id,
                    "productId" to log.productId,
                    "actionType" to log.actionType,
                    "quantityChange" to log.quantityChange,
                    "previousStock" to log.previousStock,
                    "newStock" to log.newStock,
                    "reason" to log.reason,
                    "timestamp" to log.timestamp,
                    FIELD_USER_ID to getUserId()
                )

                getUserCollection(COLLECTION_INVENTORY_LOGS)
                    .document(log.id)
                    .set(data, SetOptions.merge())
                    .await()

                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync inventory log ${log.id}", e)
            }
        }

        return count
    }

    private suspend fun syncInventoryLogsFromCloud(): Int {
        val snapshot = getUserCollection(COLLECTION_INVENTORY_LOGS).get().await()
        var count = 0

        snapshot.documents.forEach { doc ->
            try {
                val log = documentToInventoryLog(doc)
                database.inventoryLogDao().insertLog(log)
                count++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync inventory log from cloud", e)
            }
        }

        return count
    }

    // ==================== Document Converters ====================

    private fun documentToProduct(doc: DocumentSnapshot): ProductEntity {
        return ProductEntity(
            id = doc.getString("id") ?: doc.id,
            name = doc.getString("name") ?: "",
            barcode = doc.getString("barcode"),
            category = doc.getString("category") ?: "",
            currentStock = doc.getLong("currentStock")?.toInt() ?: 0,
            minStockThreshold = doc.getLong("minStockThreshold")?.toInt() ?: 10,
            buyingPrice = doc.getDouble("buyingPrice") ?: 0.0,
            sellingPrice = doc.getDouble("sellingPrice") ?: 0.0,
            imageUrl = doc.getString("imageUrl"),
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
    }

    private fun documentToSale(doc: DocumentSnapshot): SaleEntity {
        return SaleEntity(
            id = doc.getString("id") ?: doc.id,
            productId = doc.getString("productId") ?: "",
            customerId = doc.getString("customerId"),
            quantity = doc.getLong("quantity")?.toInt() ?: 0,
            unitPrice = doc.getDouble("unitPrice") ?: 0.0,
            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
            saleType = doc.getString("saleType") ?: "CASH",
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        )
    }

    private fun documentToCustomer(doc: DocumentSnapshot): CustomerEntity {
        return CustomerEntity(
            id = doc.getString("id") ?: doc.id,
            name = doc.getString("name") ?: "",
            phoneNumber = doc.getString("phoneNumber"),
            address = doc.getString("address"),
            notes = doc.getString("notes"),
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
    }

    private fun documentToCredit(doc: DocumentSnapshot): CreditLedgerEntity {
        return CreditLedgerEntity(
            id = doc.getString("id") ?: doc.id,
            customerId = doc.getString("customerId") ?: "",
            saleId = doc.getString("saleId") ?: "",
            amount = doc.getDouble("amount") ?: 0.0,
            amountPaid = doc.getDouble("amountPaid") ?: 0.0,
            amountRemaining = doc.getDouble("amountRemaining") ?: 0.0,
            dueDate = doc.getLong("dueDate"),
            status = doc.getString("status") ?: "PENDING",
            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
    }

    private fun documentToPayment(doc: DocumentSnapshot): PaymentEntity {
        return PaymentEntity(
            id = doc.getString("id") ?: doc.id,
            creditId = doc.getString("creditId") ?: "",
            amount = doc.getDouble("amount") ?: 0.0,
            paymentMethod = doc.getString("paymentMethod") ?: "CASH",
            notes = doc.getString("notes"),
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        )
    }

    private fun documentToInventoryLog(doc: DocumentSnapshot): InventoryLogEntity {
        return InventoryLogEntity(
            id = doc.getString("id") ?: doc.id,
            productId = doc.getString("productId") ?: "",
            actionType = doc.getString("actionType") ?: "",
            quantityChange = doc.getLong("quantityChange")?.toInt() ?: 0,
            previousStock = doc.getLong("previousStock")?.toInt() ?: 0,
            newStock = doc.getLong("newStock")?.toInt() ?: 0,
            reason = doc.getString("reason"),
            timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
        )
    }

    // Helper extension for Flow.first()
    private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.first(): T {
        var result: T? = null
        collect { value ->
            result = value
            return@collect
        }
        return result!!
    }
}
