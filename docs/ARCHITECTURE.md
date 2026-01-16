# Duka.AI - System Architecture Documentation

## Table of Contents
1. [Overview](#overview)
2. [Architectural Patterns](#architectural-patterns)
3. [Layer Breakdown](#layer-breakdown)
4. [Component Design](#component-design)
5. [Data Flow](#data-flow)
6. [Offline-First Strategy](#offline-first-strategy)
7. [Performance Optimizations](#performance-optimizations)
8. [Security Architecture](#security-architecture)

---

## Overview

Duka.AI follows a **Clean Architecture** approach with **MVVM (Model-View-ViewModel)** pattern, optimized for offline-first operation on resource-constrained devices.

### Design Principles
- **Separation of Concerns**: Clear boundaries between layers
- **Dependency Inversion**: Depend on abstractions, not concretions
- **Single Responsibility**: Each class has one reason to change
- **Offline-First**: All features work without network
- **Performance**: Optimized for low-end Android devices
- **Testability**: Easily testable components

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│                   (Jetpack Compose UI)                       │
│                                                              │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐           │
│  │  Home      │  │  Products  │  │   Credit   │           │
│  │ Dashboard  │  │ Management │  │   Ledger   │           │
│  │  Screen    │  │   Screen   │  │   Screen   │           │
│  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘           │
│        │                │                │                   │
│        └────────────────┴────────────────┘                   │
│                         │                                     │
└─────────────────────────┼─────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                    VIEWMODEL LAYER                           │
│                  (Lifecycle-Aware State)                     │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │ DashboardViewModel│  │ ProductViewModel │                │
│  │ - State flow     │  │ - State flow     │                │
│  │ - Events         │  │ - Events         │                │
│  └────────┬─────────┘  └────────┬─────────┘                │
│           │                      │                           │
│           └──────────┬───────────┘                           │
│                      │                                       │
└──────────────────────┼───────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                    DOMAIN LAYER                              │
│                  (Business Logic)                            │
│                                                              │
│  ┌──────────────────────────────────────────────────┐       │
│  │                 Use Cases                        │       │
│  │ - LogSaleUseCase                                 │       │
│  │ - AddProductUseCase                              │       │
│  │ - RecordCreditSaleUseCase                        │       │
│  │ - RecordPaymentUseCase                           │       │
│  │ - GetLowStockItemsUseCase                        │       │
│  │ - GenerateSalesReportUseCase                     │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                              │
│                (Repositories & Data Sources)                 │
│                                                              │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │  Repositories    │  │  Data Sources    │                │
│  │ - ProductRepo    │  │ - Room Database  │                │
│  │ - SalesRepo      │  │ - DataStore      │                │
│  │ - CreditRepo     │  │ - FileStorage    │                │
│  │ - CustomerRepo   │  │ - (Cloud Sync)   │                │
│  └──────────────────┘  └──────────────────┘                │
│                                                              │
└──────────────────────┬───────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                      ML/AI LAYER                             │
│                   (On-Device Models)                         │
│                                                              │
│  ┌──────────────────────────────────────────────────┐       │
│  │  Product Recognition (TFLite)                    │       │
│  │  - MobileNetV3-Small (4.2 MB)                    │       │
│  │  - 120 product classes                           │       │
│  │  - 89% accuracy                                  │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  ┌──────────────────────────────────────────────────┐       │
│  │  Voice Command System                            │       │
│  │  - Android SpeechRecognizer                      │       │
│  │  - FunctionGemma NLU (270M params)               │       │
│  │  - Multi-language support                        │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
│  ┌──────────────────────────────────────────────────┐       │
│  │  FunctionGemma Service                           │       │
│  │  - Natural language → function calling           │       │
│  │  - On-device TFLite inference                    │       │
│  │  - Pattern-based fallback                        │       │
│  └──────────────────────────────────────────────────┘       │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## Architectural Patterns

### 1. Clean Architecture Layers

#### **Presentation Layer**
- **Technology**: Jetpack Compose + Material 3
- **Responsibility**: UI rendering, user interaction, display state
- **Components**:
  - `Screens`: Full-screen composables
  - `Components`: Reusable UI elements
  - `ViewModels`: UI state management
- **Rules**:
  - No business logic
  - Only depends on ViewModels
  - Pure UI code

#### **Domain Layer**
- **Technology**: Pure Kotlin
- **Responsibility**: Business logic, use cases, domain models
- **Components**:
  - `Use Cases`: Single-purpose business operations
  - `Domain Models`: Business entities
  - `Repository Interfaces`: Data contracts
- **Rules**:
  - No Android dependencies
  - Framework-agnostic
  - Highly testable

#### **Data Layer**
- **Technology**: Room, DataStore, File I/O
- **Responsibility**: Data persistence, retrieval, caching
- **Components**:
  - `Repositories`: Implement domain interfaces
  - `Data Sources`: Room DAOs, DataStore, APIs
  - `Entities`: Database models
  - `Mappers`: Convert between layers
- **Rules**:
  - Single source of truth
  - Handles data mapping
  - Manages caching strategy

#### **ML/AI Layer**
- **Technology**: TensorFlow Lite, Android SpeechRecognizer
- **Responsibility**: AI inference, voice processing
- **Components**:
  - `ProductClassifier`: Image recognition
  - `VoiceCommandHandler`: Speech-to-text + NLP
- **Rules**:
  - Isolated from business logic
  - Provides predictions/results only
  - Performance-optimized

---

### 2. MVVM Pattern Implementation

```kotlin
// Example: Product Sale Flow

// 1. USER ACTION (UI Layer)
@Composable
fun SaleScreen(viewModel: SaleViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()

    ScanProductButton(
        onClick = { viewModel.onEvent(SaleEvent.ScanProduct) }
    )

    when (state) {
        is SaleState.Loading -> LoadingIndicator()
        is SaleState.ProductDetected -> ConfirmSaleDialog(...)
        is SaleState.SaleCompleted -> SuccessMessage(...)
    }
}

// 2. VIEWMODEL (Presentation Logic)
@HiltViewModel
class SaleViewModel @Inject constructor(
    private val logSaleUseCase: LogSaleUseCase,
    private val productClassifier: ProductClassifier
) : ViewModel() {

    private val _state = MutableStateFlow<SaleState>(SaleState.Idle)
    val state: StateFlow<SaleState> = _state.asStateFlow()

    fun onEvent(event: SaleEvent) {
        when (event) {
            is SaleEvent.ScanProduct -> scanProduct()
            is SaleEvent.ConfirmSale -> confirmSale(event.product, event.quantity)
        }
    }

    private fun scanProduct() = viewModelScope.launch {
        _state.value = SaleState.Loading
        val result = productClassifier.classifyImage(image)
        _state.value = SaleState.ProductDetected(result)
    }

    private fun confirmSale(product: Product, quantity: Int) = viewModelScope.launch {
        val result = logSaleUseCase(product, quantity)
        _state.value = when (result) {
            is Result.Success -> SaleState.SaleCompleted
            is Result.Error -> SaleState.Error(result.message)
        }
    }
}

// 3. USE CASE (Business Logic)
class LogSaleUseCase @Inject constructor(
    private val salesRepository: SalesRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(
        product: Product,
        quantity: Int,
        saleType: SaleType = SaleType.CASH,
        customerId: String? = null
    ): Result<Sale> {
        // Business logic
        if (product.currentStock < quantity) {
            return Result.Error("Insufficient stock")
        }

        val sale = Sale(
            productId = product.id,
            quantity = quantity,
            unitPrice = product.sellingPrice,
            totalAmount = product.sellingPrice * quantity,
            saleType = saleType,
            customerId = customerId,
            saleDate = System.currentTimeMillis()
        )

        // Update inventory
        productRepository.updateStock(
            productId = product.id,
            newStock = product.currentStock - quantity
        )

        // Record sale
        return salesRepository.insertSale(sale)
    }
}

// 4. REPOSITORY (Data Access)
class SalesRepositoryImpl @Inject constructor(
    private val salesDao: SalesDao
) : SalesRepository {
    override suspend fun insertSale(sale: Sale): Result<Sale> {
        return try {
            salesDao.insert(sale.toEntity())
            Result.Success(sale)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Failed to record sale")
        }
    }
}
```

---

## Layer Breakdown

### Presentation Layer Details

```
ui/
├── screens/
│   ├── dashboard/
│   │   ├── DashboardScreen.kt          # Main dashboard UI
│   │   ├── DashboardViewModel.kt       # Dashboard state
│   │   └── components/                 # Dashboard-specific components
│   ├── products/
│   │   ├── ProductListScreen.kt        # Product list
│   │   ├── ProductDetailScreen.kt      # Product details
│   │   ├── AddProductScreen.kt         # Add/edit product
│   │   └── ProductViewModel.kt
│   ├── sales/
│   │   ├── QuickSaleScreen.kt          # Fast sale interface
│   │   ├── CameraScanner.kt            # Camera UI
│   │   ├── VoiceSaleScreen.kt          # Voice command UI
│   │   └── SaleViewModel.kt
│   ├── credit/
│   │   ├── CreditLedgerScreen.kt       # Credit overview
│   │   ├── CustomerDetailScreen.kt     # Customer details
│   │   ├── RecordPaymentScreen.kt      # Payment recording
│   │   └── CreditViewModel.kt
│   └── analytics/
│       ├── AnalyticsScreen.kt          # Reports & insights
│       └── AnalyticsViewModel.kt
├── components/
│   ├── ProductCard.kt                  # Reusable product card
│   ├── ScanButton.kt                   # Camera scan button
│   ├── VoiceButton.kt                  # Voice input button
│   ├── LowStockBadge.kt                # Stock indicator
│   └── CurrencyInput.kt                # Kwacha input field
├── navigation/
│   ├── NavigationGraph.kt              # Navigation routes
│   └── BottomNavigation.kt             # Bottom nav bar
└── theme/
    ├── Color.kt                        # Material 3 colors
    ├── Type.kt                         # Typography
    └── Theme.kt                        # Theme configuration
```

### Domain Layer Details

```
domain/
├── models/
│   ├── Product.kt                      # Product domain model
│   ├── Sale.kt                         # Sale domain model
│   ├── Customer.kt                     # Customer domain model
│   ├── CreditTransaction.kt            # Credit transaction model
│   └── SalesReport.kt                  # Analytics model
├── repository/                         # Repository interfaces
│   ├── ProductRepository.kt
│   ├── SalesRepository.kt
│   ├── CustomerRepository.kt
│   └── AnalyticsRepository.kt
└── usecases/
    ├── product/
    │   ├── AddProductUseCase.kt
    │   ├── UpdateStockUseCase.kt
    │   ├── GetLowStockItemsUseCase.kt
    │   └── SearchProductsUseCase.kt
    ├── sales/
    │   ├── LogSaleUseCase.kt
    │   ├── GetTodaysSalesUseCase.kt
    │   └── GetTopSellingProductsUseCase.kt
    ├── credit/
    │   ├── RecordCreditSaleUseCase.kt
    │   ├── RecordPaymentUseCase.kt
    │   ├── GetCustomerDebtUseCase.kt
    │   └── GetOverdueCustomersUseCase.kt
    └── analytics/
        ├── GenerateDailySummaryUseCase.kt
        ├── CalculateProfitMarginsUseCase.kt
        └── GetSalesTrendsUseCase.kt
```

### Data Layer Details

```
data/
├── database/
│   ├── DukaDatabase.kt                 # Room database
│   ├── dao/
│   │   ├── ProductDao.kt               # Product CRUD operations
│   │   ├── SalesDao.kt                 # Sales CRUD operations
│   │   ├── CustomerDao.kt              # Customer CRUD operations
│   │   └── CreditDao.kt                # Credit CRUD operations
│   └── entities/
│       ├── ProductEntity.kt            # Product table
│       ├── SaleEntity.kt               # Sales table
│       ├── CustomerEntity.kt           # Customers table
│       ├── CreditLedgerEntity.kt       # Credit ledger table
│       └── PaymentEntity.kt            # Payments table
├── repository/
│   ├── ProductRepositoryImpl.kt        # Product repo implementation
│   ├── SalesRepositoryImpl.kt          # Sales repo implementation
│   ├── CustomerRepositoryImpl.kt       # Customer repo implementation
│   └── AnalyticsRepositoryImpl.kt      # Analytics repo implementation
├── preferences/
│   ├── UserPreferences.kt              # App settings
│   └── PreferencesManager.kt           # DataStore wrapper
└── mappers/
    ├── ProductMapper.kt                # Entity ↔ Domain mapping
    ├── SaleMapper.kt
    └── CustomerMapper.kt
```

### ML/AI Layer Details

```
ml/
├── classifier/
│   ├── ProductClassifier.kt            # TFLite wrapper interface
│   ├── TFLiteProductClassifier.kt      # TFLite implementation
│   ├── ImagePreprocessor.kt            # Image preprocessing
│   └── ClassificationResult.kt         # Result data class
├── functiongemma/                       # FunctionGemma NLU System
│   ├── DukaToolSchema.kt               # Tool/function definitions (10 tools)
│   ├── FunctionGemmaInference.kt       # TFLite inference engine
│   ├── FunctionGemmaParser.kt          # Parse function calls from output
│   ├── DukaFunctionExecutor.kt         # Execute functions against repos
│   └── FunctionGemmaService.kt         # High-level orchestration
├── BarcodeScanner.kt                    # ML Kit barcode scanning
└── ImageUtils.kt                        # Image processing utilities
```

#### FunctionGemma Architecture

FunctionGemma is a 270M parameter model specialized for function calling that powers DukaAI's natural language interface.

```
User Input (Voice/Text)
        │
        ▼
┌─────────────────────────────────────┐
│  FunctionGemmaService               │
│  - Orchestrates command processing  │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────┐
│  FunctionGemmaInference (TFLite)    │
│  - Load model from assets           │
│  - GPU acceleration (optional)      │
│  - Generate function calls          │
└────────────┬────────────────────────┘
             │ Model Output (with control tokens)
             ▼
┌─────────────────────────────────────┐
│  FunctionGemmaParser                │
│  - Extract function name            │
│  - Parse arguments with <escape>    │
│  - Support parallel calls           │
└────────────┬────────────────────────┘
             │ ParsedFunctionCall(name, args)
             ▼
┌─────────────────────────────────────┐
│  DukaFunctionExecutor               │
│  - Map to repository operations     │
│  - Execute with transaction safety  │
│  - Return structured results        │
└────────────┬────────────────────────┘
             │ FunctionExecutionResult
             ▼
┌─────────────────────────────────────┐
│  Voice/UI Feedback                  │
│  - TTS confirmation                 │
│  - UI state update                  │
└─────────────────────────────────────┘
```

**Available Functions (10 tools):**
| Function | Description |
|----------|-------------|
| `record_sale` | Record a sale transaction |
| `add_product` | Add new product to inventory |
| `update_stock` | Update product stock levels |
| `check_stock` | Check product inventory |
| `search_products` | Search for products |
| `record_payment` | Record customer payment |
| `add_customer` | Add new customer |
| `get_customer_balance` | Check customer credit balance |
| `get_sales_analytics` | Get sales statistics |
| `get_low_stock_alerts` | Get low stock warnings |

---

## Data Flow

### Example: Camera-Based Product Sale

```
User Action: Tap "Scan Product" Button
              │
              ▼
┌─────────────────────────────────────────┐
│  UI Layer (SaleScreen)                  │
│  - Capture camera image                 │
│  - Show loading state                   │
└─────────────┬───────────────────────────┘
              │ viewModel.onEvent(ScanProduct)
              ▼
┌─────────────────────────────────────────┐
│  ViewModel (SaleViewModel)              │
│  - Update state to Loading              │
│  - Call ML classifier                   │
└─────────────┬───────────────────────────┘
              │ classifyImage(bitmap)
              ▼
┌─────────────────────────────────────────┐
│  ML Layer (ProductClassifier)           │
│  - Preprocess image (resize, normalize) │
│  - Run TFLite inference                 │
│  - Return product ID + confidence       │
└─────────────┬───────────────────────────┘
              │ Result: "PROD_001" (Mosi Lager, 92%)
              ▼
┌─────────────────────────────────────────┐
│  ViewModel                              │
│  - Update state to ProductDetected      │
│  - Show confirmation dialog             │
└─────────────┬───────────────────────────┘
              │
              ▼ User confirms sale
┌─────────────────────────────────────────┐
│  ViewModel                              │
│  - Call LogSaleUseCase                  │
└─────────────┬───────────────────────────┘
              │ logSale(product, quantity=1)
              ▼
┌─────────────────────────────────────────┐
│  Domain Layer (LogSaleUseCase)          │
│  - Validate stock availability          │
│  - Calculate total amount               │
│  - Create Sale object                   │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Data Layer (SalesRepository)           │
│  1. Insert sale into database           │
│  2. Update product stock                │
│  3. Log inventory change                │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  Database (Room)                        │
│  - Write to sales table                 │
│  - Update products table                │
│  - Insert inventory_log entry           │
└─────────────┬───────────────────────────┘
              │ Success
              ▼
┌─────────────────────────────────────────┐
│  ViewModel                              │
│  - Update state to SaleCompleted        │
│  - Trigger success animation            │
└─────────────┬───────────────────────────┘
              │
              ▼
┌─────────────────────────────────────────┐
│  UI Layer                               │
│  - Show "Sale Recorded! K 12.00"        │
│  - Play voice feedback                  │
│  - Auto-dismiss after 2 seconds         │
└─────────────────────────────────────────┘
```

---

## Offline-First Strategy

### Core Principles
1. **Local-First**: All data stored locally by default
2. **No Network Dependencies**: App works 100% offline
3. **Sync When Available**: Background sync when WiFi detected
4. **Conflict Resolution**: Last-write-wins strategy
5. **Cache Everything**: Product images, ML models, all assets

### Implementation

```kotlin
// 1. Single Source of Truth: Room Database
@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        CustomerEntity::class,
        CreditLedgerEntity::class
    ],
    version = 1
)
abstract class DukaDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun salesDao(): SalesDao
    abstract fun customerDao(): CustomerDao
    abstract fun creditDao(): CreditDao
}

// 2. Repository Pattern with Offline Support
class ProductRepositoryImpl(
    private val productDao: ProductDao,
    private val cloudSync: CloudSyncService? = null // Optional
) : ProductRepository {

    // Always read from local database
    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
            .map { entities -> entities.map { it.toDomain() } }
    }

    // Write locally first, sync later
    override suspend fun addProduct(product: Product): Result<Unit> {
        return try {
            productDao.insert(product.toEntity())

            // Mark for sync if cloud available
            cloudSync?.queueForSync(product)

            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message)
        }
    }
}

// 3. Background Sync (WorkManager)
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        // Only sync on WiFi + charging + battery > 20%
        if (!isWiFiConnected() || !isCharging() || batteryLevel < 20) {
            return Result.retry()
        }

        // Sync data to cloud
        syncProducts()
        syncSales()
        syncCustomers()

        return Result.success()
    }
}
```

---

## Performance Optimizations

### 1. Database Optimizations

```kotlin
// Indexed queries for fast lookups
@Dao
interface ProductDao {
    // Index on name for search
    @Query("SELECT * FROM products WHERE name LIKE :query")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    // Index on current_stock for low stock alerts
    @Query("SELECT * FROM products WHERE current_stock <= min_stock_threshold")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    // Composite index for sales queries
    @Query("""
        SELECT * FROM sales
        WHERE sale_date BETWEEN :startDate AND :endDate
        ORDER BY sale_date DESC
    """)
    fun getSalesByDateRange(startDate: Long, endDate: Long): Flow<List<SaleEntity>>
}
```

### 2. ML Model Optimizations

- **Quantization**: INT8 quantization (16.8 MB → 4.2 MB)
- **Multi-threading**: Use 4 CPU threads for inference
- **NNAPI**: Leverage Android Neural Networks API
- **Image Preprocessing**: Resize to 224x224 before inference
- **Caching**: Cache recent predictions

### 3. UI Performance

- **LazyColumn**: Virtual scrolling for lists
- **Derivedstate**: Compute expensive values once
- **remember**: Avoid recomposition
- **Flow Collection**: Collect as State for automatic updates

```kotlin
@Composable
fun ProductListScreen(viewModel: ProductViewModel) {
    val products by viewModel.products.collectAsState()

    LazyColumn {
        items(
            items = products,
            key = { product -> product.id } // Stable keys
        ) { product ->
            ProductCard(
                product = product,
                onClick = { viewModel.onProductClick(product) }
            )
        }
    }
}
```

---

## Security Architecture

### 1. Data Protection

```kotlin
// Encrypted SharedPreferences for sensitive data
class SecurePreferences(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePIN(pin: String) {
        encryptedPrefs.edit().putString("user_pin", pin).apply()
    }
}
```

### 2. App Lock (PIN/Biometric)

```kotlin
class AppLockManager(context: Context) {
    private val biometricPrompt = BiometricPrompt(...)

    fun authenticateUser(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Duka.AI")
            .setSubtitle("Verify to access business data")
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
```

### 3. Data Privacy

- **No Analytics by Default**: User must opt-in
- **No Third-Party SDKs**: Except essential ones (Crashlytics)
- **Local Data Only**: No cloud without permission
- **Data Export**: Users can export all their data
- **Data Deletion**: Complete data wipe option

---

## Technology Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **UI Framework** | Jetpack Compose | Modern, declarative, less boilerplate |
| **Database** | Room (SQLite) | Robust, offline-first, battle-tested |
| **Dependency Injection** | Hilt | Android-first, compile-time safety |
| **Async** | Kotlin Coroutines + Flow | Native, efficient, reactive |
| **ML Framework** | TensorFlow Lite | Best mobile ML support, optimized |
| **Image Loading** | Coil | Compose-first, lightweight |
| **Testing** | JUnit + Mockk + Turbine | Comprehensive testing stack |
| **Build System** | Gradle KTS | Type-safe, IDE support |

---

## Next Steps

1. Implement dependency injection with Hilt
2. Set up Room database with migrations
3. Create repository layer with offline support
4. Integrate TFLite model
5. Build core UI screens with Compose

For implementation details, see:
- [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)
- [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)
- [AI_MODELS.md](AI_MODELS.md)
