# Duka.AI - Database Schema Documentation

## Overview

Duka.AI uses **Room** (SQLite) as its local database for offline-first operation. The schema is designed for:
- **Fast queries**: Proper indexing for common operations
- **Data integrity**: Foreign keys and constraints
- **Scalability**: Support for 10,000+ transactions
- **Offline sync**: Timestamp tracking for cloud sync

---

## Entity Relationship Diagram

```
┌─────────────────────┐
│     PRODUCTS        │
│─────────────────────│
│ id (PK)             │
│ name                │◄─────────┐
│ category            │          │
│ barcode             │          │
│ image_path          │          │
│ buying_price        │          │
│ selling_price       │          │ 1:N
│ current_stock       │          │
│ min_stock_threshold │          │
│ is_active           │          │
│ created_at          │          │
│ updated_at          │          │
└─────────────────────┘          │
                                 │
                                 │
                    ┌────────────┴──────────┐
                    │       SALES           │
                    │───────────────────────│
                    │ id (PK)               │
                    │ product_id (FK)       │
                    │ quantity              │
                    │ unit_price            │
                    │ total_amount          │
                    │ sale_type             │
                    │ customer_id (FK)      │◄───────┐
                    │ notes                 │        │
                    │ sale_date             │        │
                    └───────────────────────┘        │
                                                     │
                                                     │ 1:N
┌─────────────────────┐                             │
│    CUSTOMERS        │─────────────────────────────┘
│─────────────────────│
│ id (PK)             │
│ name                │◄─────────┐
│ phone_number        │          │
│ address             │          │
│ photo_path          │          │
│ total_debt          │          │ 1:N
│ created_at          │          │
│ updated_at          │          │
└─────────────────────┘          │
         │                       │
         │                       │
         │ 1:N        ┌──────────┴─────────────┐
         │            │    CREDIT_LEDGER       │
         │            │────────────────────────│
         │            │ id (PK)                │
         │            │ customer_id (FK)       │
         │            │ sale_id (FK)           │
         │            │ amount                 │
         │            │ due_date               │
         │            │ status                 │
         │            │ notes                  │
         │            │ created_at             │
         │            └────────────────────────┘
         │
         │ 1:N
         │
         └───────────►┌────────────────────────┐
                      │      PAYMENTS          │
                      │────────────────────────│
                      │ id (PK)                │
                      │ customer_id (FK)       │
                      │ amount                 │
                      │ payment_date           │
                      │ notes                  │
                      └────────────────────────┘


┌─────────────────────┐
│  INVENTORY_LOGS     │
│─────────────────────│
│ id (PK)             │
│ product_id (FK)     │────────► PRODUCTS
│ action              │
│ quantity_change     │
│ reason              │
│ notes               │
│ timestamp           │
└─────────────────────┘
```

---

## Table Definitions

### 1. PRODUCTS Table

Stores all product information including stock levels.

```kotlin
@Entity(
    tableName = "products",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"]),
        Index(value = ["current_stock"]),
        Index(value = ["barcode"], unique = true)
    ]
)
data class ProductEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String, // UUID format: "PROD_001"

    @ColumnInfo(name = "name")
    val name: String, // e.g., "Mosi Lager 500ml"

    @ColumnInfo(name = "category")
    val category: String, // e.g., "beverages", "food", "toiletries"

    @ColumnInfo(name = "barcode")
    val barcode: String? = null, // Optional barcode

    @ColumnInfo(name = "image_path")
    val imagePath: String? = null, // Local file path to product image

    @ColumnInfo(name = "buying_price")
    val buyingPrice: Double, // Cost from supplier (ZMW)

    @ColumnInfo(name = "selling_price")
    val sellingPrice: Double, // Selling price to customer (ZMW)

    @ColumnInfo(name = "current_stock")
    val currentStock: Int = 0, // Current stock quantity

    @ColumnInfo(name = "min_stock_threshold")
    val minStockThreshold: Int = 5, // Low stock alert threshold

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true, // Soft delete flag

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

**Sample Data:**
```sql
INSERT INTO products VALUES (
    'PROD_001',
    'Mosi Lager 500ml',
    'beverages',
    '6001234567890',
    '/storage/emulated/0/dukaai/products/mosi_lager.jpg',
    10.00,
    12.00,
    45,
    10,
    1,
    1699564800000,
    1699564800000
);
```

**Common Queries:**
```kotlin
@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    fun getAllActiveProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE current_stock <= min_stock_threshold AND is_active = 1")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' AND is_active = 1")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE category = :category AND is_active = 1")
    fun getProductsByCategory(category: String): Flow<List<ProductEntity>>

    @Query("UPDATE products SET current_stock = :newStock, updated_at = :timestamp WHERE id = :productId")
    suspend fun updateStock(productId: String, newStock: Int, timestamp: Long)
}
```

---

### 2. SALES Table

Records all sales transactions (cash and credit).

```kotlin
@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["customer_id"]),
        Index(value = ["sale_date"]),
        Index(value = ["sale_type"])
    ]
)
data class SaleEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String, // UUID format: "SALE_001"

    @ColumnInfo(name = "product_id")
    val productId: String, // Foreign key to products

    @ColumnInfo(name = "quantity")
    val quantity: Int, // Number of units sold

    @ColumnInfo(name = "unit_price")
    val unitPrice: Double, // Price per unit at time of sale

    @ColumnInfo(name = "total_amount")
    val totalAmount: Double, // quantity * unit_price

    @ColumnInfo(name = "sale_type")
    val saleType: String = "cash", // "cash" or "credit"

    @ColumnInfo(name = "customer_id")
    val customerId: String? = null, // NULL for cash sales

    @ColumnInfo(name = "notes")
    val notes: String? = null, // Optional notes

    @ColumnInfo(name = "sale_date")
    val saleDate: Long = System.currentTimeMillis()
)
```

**Sample Data:**
```sql
INSERT INTO sales VALUES (
    'SALE_001',
    'PROD_001',
    2,
    12.00,
    24.00,
    'cash',
    NULL,
    'Quick sale via camera scan',
    1699564800000
);
```

**Common Queries:**
```kotlin
@Dao
interface SalesDao {
    @Insert
    suspend fun insert(sale: SaleEntity): Long

    @Query("SELECT * FROM sales WHERE sale_date >= :startDate AND sale_date < :endDate ORDER BY sale_date DESC")
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<SaleEntity>>

    @Query("SELECT SUM(total_amount) FROM sales WHERE sale_date >= :startDate AND sale_date < :endDate")
    fun getTotalRevenue(startDate: Long, endDate: Long): Flow<Double?>

    @Query("""
        SELECT product_id, SUM(quantity) as total_sold
        FROM sales
        WHERE sale_date >= :startDate
        GROUP BY product_id
        ORDER BY total_sold DESC
        LIMIT :limit
    """)
    fun getTopSellingProducts(startDate: Long, limit: Int = 5): Flow<List<TopSellerResult>>

    @Query("SELECT COUNT(*) FROM sales WHERE sale_date >= :startDate AND sale_date < :endDate")
    fun getTransactionCount(startDate: Long, endDate: Long): Flow<Int>
}

data class TopSellerResult(
    @ColumnInfo(name = "product_id") val productId: String,
    @ColumnInfo(name = "total_sold") val totalSold: Int
)
```

---

### 3. CUSTOMERS Table

Stores customer information for credit management.

```kotlin
@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone_number"]),
        Index(value = ["total_debt"])
    ]
)
data class CustomerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String, // UUID format: "CUST_001"

    @ColumnInfo(name = "name")
    val name: String, // e.g., "Ba John", "Mary Banda"

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null, // Optional: 0977123456

    @ColumnInfo(name = "address")
    val address: String? = null, // e.g., "Kalingalinga"

    @ColumnInfo(name = "photo_path")
    val photoPath: String? = null, // Local file path to photo

    @ColumnInfo(name = "total_debt")
    val totalDebt: Double = 0.0, // Sum of all unpaid credit

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
```

**Sample Data:**
```sql
INSERT INTO customers VALUES (
    'CUST_001',
    'Ba John',
    '0977123456',
    'Kalingalinga',
    NULL,
    174.00,
    1699564800000,
    1699564800000
);
```

**Common Queries:**
```kotlin
@Dao
interface CustomerDao {
    @Insert
    suspend fun insert(customer: CustomerEntity): Long

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE total_debt > 0 ORDER BY total_debt DESC")
    fun getCustomersWithDebt(): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%' OR phone_number LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>

    @Query("UPDATE customers SET total_debt = total_debt + :amount WHERE id = :customerId")
    suspend fun addToDebt(customerId: String, amount: Double)

    @Query("UPDATE customers SET total_debt = total_debt - :amount WHERE id = :customerId")
    suspend fun subtractFromDebt(customerId: String, amount: Double)
}
```

---

### 4. CREDIT_LEDGER Table

Tracks individual credit transactions and payment status.

```kotlin
@Entity(
    tableName = "credit_ledger",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["sale_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["sale_id"]),
        Index(value = ["status"]),
        Index(value = ["due_date"])
    ]
)
data class CreditLedgerEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "customer_id")
    val customerId: String,

    @ColumnInfo(name = "sale_id")
    val saleId: String,

    @ColumnInfo(name = "amount")
    val amount: Double, // Credit amount

    @ColumnInfo(name = "due_date")
    val dueDate: Long? = null, // Optional due date

    @ColumnInfo(name = "status")
    val status: String = "pending", // "pending", "paid", "partial", "overdue"

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
```

**Sample Data:**
```sql
INSERT INTO credit_ledger VALUES (
    'CREDIT_001',
    'CUST_001',
    'SALE_001',
    24.00,
    1701561600000,
    'pending',
    'Will pay on payday',
    1699564800000
);
```

**Common Queries:**
```kotlin
@Dao
interface CreditDao {
    @Insert
    suspend fun insert(credit: CreditLedgerEntity): Long

    @Query("SELECT * FROM credit_ledger WHERE customer_id = :customerId ORDER BY created_at DESC")
    fun getCreditsByCustomer(customerId: String): Flow<List<CreditLedgerEntity>>

    @Query("SELECT * FROM credit_ledger WHERE status = 'pending' OR status = 'partial' ORDER BY due_date ASC")
    fun getUnpaidCredits(): Flow<List<CreditLedgerEntity>>

    @Query("SELECT * FROM credit_ledger WHERE status = 'overdue' ORDER BY due_date ASC")
    fun getOverdueCredits(): Flow<List<CreditLedgerEntity>>

    @Query("UPDATE credit_ledger SET status = :newStatus WHERE id = :creditId")
    suspend fun updateStatus(creditId: String, newStatus: String)
}
```

---

### 5. PAYMENTS Table

Records payments made by customers against their credit.

```kotlin
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customer_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customer_id"]),
        Index(value = ["payment_date"])
    ]
)
data class PaymentEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "customer_id")
    val customerId: String,

    @ColumnInfo(name = "amount")
    val amount: Double, // Payment amount

    @ColumnInfo(name = "payment_date")
    val paymentDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
```

**Common Queries:**
```kotlin
@Dao
interface PaymentDao {
    @Insert
    suspend fun insert(payment: PaymentEntity): Long

    @Query("SELECT * FROM payments WHERE customer_id = :customerId ORDER BY payment_date DESC")
    fun getPaymentsByCustomer(customerId: String): Flow<List<PaymentEntity>>

    @Query("SELECT SUM(amount) FROM payments WHERE customer_id = :customerId")
    fun getTotalPayments(customerId: String): Flow<Double?>
}
```

---

### 6. INVENTORY_LOGS Table

Tracks all inventory changes for auditing.

```kotlin
@Entity(
    tableName = "inventory_logs",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["product_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["product_id"]),
        Index(value = ["action"]),
        Index(value = ["timestamp"])
    ]
)
data class InventoryLogEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "product_id")
    val productId: String,

    @ColumnInfo(name = "action")
    val action: String, // "restock", "sale", "adjustment", "damage", "theft"

    @ColumnInfo(name = "quantity_change")
    val quantityChange: Int, // Positive for additions, negative for reductions

    @ColumnInfo(name = "reason")
    val reason: String? = null,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
```

**Common Queries:**
```kotlin
@Dao
interface InventoryLogDao {
    @Insert
    suspend fun insert(log: InventoryLogEntity): Long

    @Query("SELECT * FROM inventory_logs WHERE product_id = :productId ORDER BY timestamp DESC")
    fun getLogsByProduct(productId: String): Flow<List<InventoryLogEntity>>

    @Query("""
        SELECT * FROM inventory_logs
        WHERE action IN ('damage', 'theft')
        AND timestamp >= :startDate
        ORDER BY timestamp DESC
    """)
    fun getLosses(startDate: Long): Flow<List<InventoryLogEntity>>
}
```

---

## Database Views

For complex queries, we create views:

```kotlin
@DatabaseView(
    "SELECT " +
    "p.id, " +
    "p.name, " +
    "p.category, " +
    "p.current_stock, " +
    "p.min_stock_threshold, " +
    "p.current_stock * p.buying_price as stock_value, " +
    "CASE " +
    "  WHEN p.current_stock <= p.min_stock_threshold THEN 'low' " +
    "  WHEN p.current_stock = 0 THEN 'out' " +
    "  ELSE 'ok' " +
    "END as stock_status " +
    "FROM products p " +
    "WHERE p.is_active = 1",
    viewName = "product_stock_status"
)
data class ProductStockStatusView(
    val id: String,
    val name: String,
    val category: String,
    @ColumnInfo(name = "current_stock") val currentStock: Int,
    @ColumnInfo(name = "min_stock_threshold") val minStockThreshold: Int,
    @ColumnInfo(name = "stock_value") val stockValue: Double,
    @ColumnInfo(name = "stock_status") val stockStatus: String
)
```

---

## Indexes Strategy

### Performance-Critical Indexes

```kotlin
// Products
@Index(value = ["name"]) // For search
@Index(value = ["category"]) // For filtering
@Index(value = ["current_stock"]) // For low stock queries

// Sales
@Index(value = ["sale_date"]) // For date range queries
@Index(value = ["product_id"]) // For product sales history
@Index(value = ["customer_id"]) // For customer purchase history

// Credit Ledger
@Index(value = ["customer_id", "status"]) // Composite for customer debt queries
@Index(value = ["due_date"]) // For overdue checks
```

---

## Migration Strategy

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Add new column
        database.execSQL("ALTER TABLE products ADD COLUMN supplier_id TEXT")
    }
}

@Database(
    entities = [...],
    version = 2
)
abstract class DukaDatabase : RoomDatabase() {
    companion object {
        fun build(context: Context): DukaDatabase {
            return Room.databaseBuilder(
                context,
                DukaDatabase::class.java,
                "duka_database"
            )
            .addMigrations(MIGRATION_1_2)
            .build()
        }
    }
}
```

---

## Sample Data for Testing

```sql
-- Products
INSERT INTO products VALUES ('PROD_001', 'Mosi Lager 500ml', 'beverages', NULL, NULL, 10.00, 12.00, 45, 10, 1, 1699564800000, 1699564800000);
INSERT INTO products VALUES ('PROD_002', 'Coca-Cola 500ml', 'beverages', NULL, NULL, 8.00, 10.00, 60, 20, 1, 1699564800000, 1699564800000);
INSERT INTO products VALUES ('PROD_003', 'Boom Detergent 1kg', 'toiletries', NULL, NULL, 18.00, 22.00, 15, 10, 1, 1699564800000, 1699564800000);

-- Customers
INSERT INTO customers VALUES ('CUST_001', 'Ba John', '0977123456', 'Kalingalinga', NULL, 174.00, 1699564800000, 1699564800000);
INSERT INTO customers VALUES ('CUST_002', 'Mary Banda', '0966555444', 'Mtendere', NULL, 85.00, 1699564800000, 1699564800000);

-- Sales
INSERT INTO sales VALUES ('SALE_001', 'PROD_001', 2, 12.00, 24.00, 'cash', NULL, NULL, 1699564800000);
INSERT INTO sales VALUES ('SALE_002', 'PROD_002', 5, 10.00, 50.00, 'credit', 'CUST_001', NULL, 1699564900000);
```

---

## Query Performance Guidelines

1. **Always use indexes** for WHERE, ORDER BY, and JOIN columns
2. **Avoid SELECT *** - specify columns needed
3. **Use LIMIT** for large result sets
4. **Batch inserts** for multiple records
5. **Use transactions** for related operations

```kotlin
// Good: Batch insert with transaction
@Transaction
suspend fun insertSalesWithInventoryUpdate(sales: List<Sale>) {
    sales.forEach { sale ->
        salesDao.insert(sale)
        productDao.updateStock(sale.productId, ...)
    }
}
```

---

For implementation details, see:
- [ARCHITECTURE.md](ARCHITECTURE.md)
- [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)
