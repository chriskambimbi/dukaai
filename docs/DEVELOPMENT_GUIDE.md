# Duka.AI - Development Guide

## Table of Contents
1. [Prerequisites](#prerequisites)
2. [Environment Setup](#environment-setup)
3. [Project Structure](#project-structure)
4. [Building the App](#building-the-app)
5. [Running Tests](#running-tests)
6. [Code Style & Conventions](#code-style--conventions)
7. [Common Development Tasks](#common-development-tasks)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

```yaml
Development Machine:
  - OS: Windows 10/11, macOS 12+, or Linux (Ubuntu 20.04+)
  - RAM: 8 GB minimum, 16 GB recommended
  - Storage: 10 GB free space

Software:
  - Android Studio: Iguana (2023.2.1) or later
  - JDK: OpenJDK 11 or later
  - Kotlin: 1.9.0+ (bundled with Android Studio)
  - Android SDK: API 24-36
  - Git: 2.30+

Optional:
  - Python 3.8+ (for ML model training)
  - TensorFlow 2.14+ (for model training)
  - Docker (for containerized builds)
```

### Android SDK Requirements

```bash
# Install via Android Studio SDK Manager or command line
sdkmanager "platforms;android-36"
sdkmanager "platforms;android-24"
sdkmanager "build-tools;34.0.0"
sdkmanager "ndk;25.2.9519653"
sdkmanager "cmake;3.22.1"
```

---

## Environment Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/dukaai.git
cd dukaai
```

### 2. Open in Android Studio

1. Launch Android Studio
2. Click **File → Open**
3. Navigate to the `dukaai` directory
4. Click **OK**
5. Wait for Gradle sync to complete (2-5 minutes)

### 3. Configure local.properties

Create `local.properties` in the project root:

```properties
# Android SDK location
sdk.dir=/Users/yourusername/Library/Android/sdk

# Optional: NDK location (for native code)
ndk.dir=/Users/yourusername/Library/Android/sdk/ndk/25.2.9519653
```

### 4. Sync Gradle Dependencies

```bash
./gradlew build
```

### 5. Connect Device or Emulator

**Physical Device:**
1. Enable Developer Options on Android device
2. Enable USB Debugging
3. Connect via USB
4. Accept RSA fingerprint on device

**Emulator:**
1. Open AVD Manager in Android Studio
2. Create new virtual device (Pixel 6, API 34 recommended)
3. Start emulator

---

## Project Structure

```
dukaai/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/dukaai/
│   │   │   │   ├── data/              # Data layer
│   │   │   │   │   ├── database/      # Room database
│   │   │   │   │   │   ├── DukaDatabase.kt
│   │   │   │   │   │   ├── dao/       # Data Access Objects
│   │   │   │   │   │   └── entities/  # Database entities
│   │   │   │   │   ├── repository/    # Repository implementations
│   │   │   │   │   └── preferences/   # DataStore preferences
│   │   │   │   │
│   │   │   │   ├── domain/            # Business logic layer
│   │   │   │   │   ├── models/        # Domain models
│   │   │   │   │   ├── repository/    # Repository interfaces
│   │   │   │   │   └── usecases/      # Use cases
│   │   │   │   │
│   │   │   │   ├── ml/                # ML/AI layer
│   │   │   │   │   ├── classifier/    # Product classifier
│   │   │   │   │   └── voice/         # Voice command handler
│   │   │   │   │
│   │   │   │   ├── ui/                # Presentation layer
│   │   │   │   │   ├── screens/       # Compose screens
│   │   │   │   │   ├── components/    # Reusable UI components
│   │   │   │   │   ├── navigation/    # Navigation graph
│   │   │   │   │   ├── theme/         # Material 3 theme
│   │   │   │   │   └── viewmodels/    # ViewModels
│   │   │   │   │
│   │   │   │   ├── di/                # Dependency injection (Hilt)
│   │   │   │   └── utils/             # Utility classes
│   │   │   │
│   │   │   ├── assets/                # ML models & static assets
│   │   │   │   ├── product_classifier.tflite
│   │   │   │   └── product_labels.txt
│   │   │   │
│   │   │   ├── res/                   # Android resources
│   │   │   │   ├── values/            # Strings, colors, themes
│   │   │   │   ├── values-ny/         # Nyanja translations
│   │   │   │   └── values-bem/        # Bemba translations
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── androidTest/               # Instrumented tests
│   │   └── test/                      # Unit tests
│   │
│   └── build.gradle.kts               # App-level Gradle config
│
├── docs/                              # Documentation
├── ml/                                # ML training scripts
├── build.gradle.kts                   # Project-level Gradle config
├── settings.gradle.kts
├── gradle.properties
└── README.md
```

---

## Building the App

### Debug Build

```bash
# Command line
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

**Android Studio:**
1. Click **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Wait for build to complete
3. Click **locate** in notification to find APK

### Release Build

```bash
# Generate signed APK
./gradlew assembleRelease
```

**Configure signing** in `app/build.gradle.kts`:

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../keystore/dukaai.keystore")
            storePassword = "your_store_password"
            keyAlias = "dukaai"
            keyPassword = "your_key_password"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### Install on Device

```bash
# Install debug build
./gradlew installDebug

# Install and run
./gradlew installDebug && adb shell am start -n com.example.dukaai/.MainActivity
```

---

## Running Tests

### Unit Tests

```bash
# Run all unit tests
./gradlew test

# Run tests for specific variant
./gradlew testDebugUnitTest

# Run with coverage
./gradlew testDebugUnitTest jacocoTestReport
```

**Android Studio:**
1. Right-click on `test` directory
2. Click **Run 'Tests in com.example.dukaai'**

### Instrumented Tests

```bash
# Run on connected device/emulator
./gradlew connectedAndroidTest

# Run specific test class
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.dukaai.ProductDaoTest
```

### Example Test Structure

```kotlin
// Unit Test (test/)
class LogSaleUseCaseTest {
    @Test
    fun `logSale updates inventory correctly`() = runTest {
        // Given
        val product = Product(id = "1", currentStock = 10)
        val useCase = LogSaleUseCase(mockRepository)

        // When
        val result = useCase(product, quantity = 2)

        // Then
        assertEquals(8, result.data?.remainingStock)
    }
}

// Instrumented Test (androidTest/)
@RunWith(AndroidJUnit4::class)
class ProductDaoTest {
    @Test
    fun insertAndReadProduct() = runBlocking {
        // Given
        val product = ProductEntity(id = "1", name = "Test")
        database.productDao().insert(product)

        // When
        val loaded = database.productDao().getById("1")

        // Then
        assertEquals("Test", loaded.name)
    }
}
```

---

## Code Style & Conventions

### Kotlin Style Guide

Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

**Key Points:**
- Use 4 spaces for indentation (not tabs)
- Maximum line length: 120 characters
- Use camelCase for variables and functions
- Use PascalCase for classes
- Use UPPER_SNAKE_CASE for constants

```kotlin
// Good
class ProductViewModel(
    private val getProductsUseCase: GetProductsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "ProductViewModel"
        private const val MAX_RETRIES = 3
    }

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    fun loadProducts() {
        viewModelScope.launch {
            val result = getProductsUseCase()
            _products.value = result
        }
    }
}
```

### Compose Best Practices

```kotlin
// Use remember for expensive calculations
@Composable
fun ProductList(products: List<Product>) {
    val sortedProducts = remember(products) {
        products.sortedBy { it.name }
    }

    LazyColumn {
        items(
            items = sortedProducts,
            key = { it.id } // Stable keys for performance
        ) { product ->
            ProductCard(product)
        }
    }
}

// Extract composables for reusability
@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Text(product.name)
    }
}
```

### Naming Conventions

| Type | Convention | Example |
|------|------------|---------|
| Package | lowercase | `com.example.dukaai.data` |
| Class | PascalCase | `ProductViewModel` |
| Function | camelCase | `loadProducts()` |
| Variable | camelCase | `currentStock` |
| Constant | UPPER_SNAKE_CASE | `MAX_RETRIES` |
| Layout file | snake_case | `activity_main.xml` |
| Drawable | snake_case | `ic_camera_24.xml` |
| String resource | snake_case | `app_name` |

---

## Common Development Tasks

### Add a New Screen

1. **Create Composable**
```kotlin
// ui/screens/newfeature/NewFeatureScreen.kt
@Composable
fun NewFeatureScreen(
    viewModel: NewFeatureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Feature") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        // Screen content
    }
}
```

2. **Create ViewModel**
```kotlin
// ui/viewmodels/NewFeatureViewModel.kt
@HiltViewModel
class NewFeatureViewModel @Inject constructor(
    private val useCase: SomeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<NewFeatureState>(NewFeatureState.Loading)
    val state: StateFlow<NewFeatureState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            // Load data
        }
    }
}
```

3. **Add to Navigation Graph**
```kotlin
// ui/navigation/NavigationGraph.kt
composable("new_feature") {
    NewFeatureScreen(
        onNavigateBack = { navController.popBackStack() }
    )
}
```

### Add a New Database Entity

1. **Create Entity**
```kotlin
@Entity(tableName = "new_table")
data class NewEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String
)
```

2. **Create DAO**
```kotlin
@Dao
interface NewEntityDao {
    @Query("SELECT * FROM new_table")
    fun getAll(): Flow<List<NewEntity>>

    @Insert
    suspend fun insert(entity: NewEntity)
}
```

3. **Update Database Version**
```kotlin
@Database(
    entities = [ProductEntity::class, NewEntity::class],
    version = 2 // Increment version
)
abstract class DukaDatabase : RoomDatabase() {
    abstract fun newEntityDao(): NewEntityDao
}
```

4. **Create Migration**
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "CREATE TABLE IF NOT EXISTS new_table (id TEXT PRIMARY KEY NOT NULL, name TEXT NOT NULL)"
        )
    }
}
```

### Add Localization

1. **Add strings.xml for new language**
```xml
<!-- res/values-ny/strings.xml (Nyanja) -->
<resources>
    <string name="app_name">Duka.AI</string>
    <string name="sell">Gulitsa</string>
    <string name="product">Malonda</string>
</resources>
```

2. **Use in code**
```kotlin
@Composable
fun SellButton() {
    Button(onClick = { /*...*/ }) {
        Text(stringResource(R.string.sell))
    }
}
```

---

## Troubleshooting

### Common Issues

#### 1. Gradle Sync Failed

```bash
# Clear Gradle cache
./gradlew clean
rm -rf ~/.gradle/caches/

# Restart Android Studio
# File → Invalidate Caches / Restart
```

#### 2. App Crashes on Launch

```bash
# View logcat
adb logcat -s "AndroidRuntime:E"

# Check for common issues:
# - Missing permissions in AndroidManifest.xml
# - Database migration errors
# - Dependency injection configuration
```

#### 3. TFLite Model Not Found

```bash
# Ensure model is in assets folder
ls app/src/main/assets/

# Clean and rebuild
./gradlew clean assembleDebug
```

#### 4. Room Database Schema Mismatch

```bash
# Delete app data
adb shell pm clear com.example.dukaai

# Or provide migration
@Database(version = 2, exportSchema = true)
```

### Debug Mode

Enable debug logging in `app/build.gradle.kts`:

```kotlin
android {
    buildTypes {
        debug {
            buildConfigField("boolean", "DEBUG_MODE", "true")
            buildConfigField("String", "API_URL", "\"https://dev.api.dukaai.com\"")
        }
    }
}
```

---

## Resources

- [Android Developer Documentation](https://developer.android.com/)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Kotlin Language Reference](https://kotlinlang.org/docs/)
- [TensorFlow Lite for Android](https://www.tensorflow.org/lite/android)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

---

## Next Steps

1. Set up development environment
2. Build and run the app
3. Explore codebase structure
4. Read [ARCHITECTURE.md](ARCHITECTURE.md) for design patterns
5. Check [FEATURE_SPECS.md](FEATURE_SPECS.md) for feature details
6. Join development discussions

For questions, open an issue on GitHub or contact the team.
