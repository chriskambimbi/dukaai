# Duka.AI - Feature Documentation

> **Complete feature guide for Duka.AI MVP implementation**
> Last Updated: November 12, 2025

---

## Table of Contents

1. [Product Management](#product-management)
2. [Customer & Credit Management](#customer--credit-management)
3. [Sales & Transactions](#sales--transactions)
4. [Barcode Scanner](#barcode-scanner)
5. [Analytics & Dashboard](#analytics--dashboard)
6. [Cloud Sync](#cloud-sync)
7. [Settings](#settings)

---

## Product Management

### Add Product Screen

**File**: `ui/screens/products/AddProductScreen.kt`
**Route**: `add_product`

#### Features

##### Product Information
- **Product Name** (Required)
  - Minimum 3 characters
  - Real-time validation
  - Error messages for invalid input

- **Category Selection** (Required)
  - Dropdown menu with 9 pre-defined categories:
    - Beverages
    - Food & Snacks
    - Dairy Products
    - Personal Care
    - Household Items
    - Electronics
    - Clothing
    - Stationery
    - Other
  - Custom category input supported
  - Searchable dropdown

- **Barcode** (Optional)
  - Manual entry OR camera scan
  - Integrated with Camera Scanner
  - Button opens scanner, auto-fills on scan
  - Supports EAN, UPC, ISBN, TEXT formats

##### Pricing Section
- **Buying Price** (Required)
  - Must be > 0
  - Currency: Zambian Kwacha (K)
  - Decimal number validation

- **Selling Price** (Required)
  - Must be > 0
  - Must be >= Buying Price
  - Automatic profit margin calculation

- **Real-Time Profit Calculator**
  - Displays profit per unit: `Selling Price - Buying Price`
  - Shows profit margin percentage: `(Profit / Buying Price) × 100`
  - Updates as prices are entered
  - Example: K10 buying, K15 selling → K5 profit, 50% margin

##### Stock Management
- **Initial Stock** (Required)
  - Must be >= 0
  - Integer validation

- **Low Stock Alert Threshold** (Optional)
  - Default: 10 units
  - Triggers alerts when stock falls below threshold
  - Info card explains alert system

##### User Experience
- **Validation**
  - Inline error messages
  - Required fields marked with *
  - Form-wide validation before save
  - Prevents invalid data entry

- **Success Dialog**
  - Shows product name and confirmation
  - Two options:
    - **Done**: Returns to previous screen
    - **Add Another**: Clears form for rapid data entry

- **Loading States**
  - Loading indicator during save operation
  - Disabled buttons prevent duplicate submissions

#### Usage Flow

```
1. User taps "Add Product" FAB on Product List
   ↓
2. AddProductScreen opens
   ↓
3. User fills in product details
   ↓
4. User taps camera icon for barcode (optional)
   ↓
5. Scanner opens → scans barcode → auto-fills field
   ↓
6. User enters prices → sees real-time profit
   ↓
7. User taps "Save Product"
   ↓
8. Validation runs → Product saved to database
   ↓
9. Success dialog → User chooses Done or Add Another
```

#### Code Example

```kotlin
// Creating a product
val product = ProductEntity(
    name = "Coca Cola 500ml",
    category = "Beverages",
    barcode = "5449000000996",
    buyingPrice = 10.00,
    sellingPrice = 15.00,
    currentStock = 50,
    minStockThreshold = 10
)

viewModel.addProduct(product)
```

---

### Product List Screen

**File**: `ui/screens/products/ProductListScreen.kt`
**Route**: `products`

#### Features
- Displays all products in scrollable list
- Search and filter capabilities
- Shows current stock levels
- Color-coded stock indicators:
  - 🟢 Green: Stock above threshold
  - 🟡 Yellow: Low stock (below threshold)
  - 🔴 Red: Out of stock (0 units)
- Tap product to view details
- FAB button to add new products
- Real-time updates via Flow

---

### Product Detail Screen

**File**: `ui/screens/products/ProductDetailScreen.kt`
**Route**: `product_detail/{productId}`

#### Features
- View complete product information
- Edit product details
- Update stock levels
- View stock history
- Delete product (with confirmation)
- Profit margin display
- Sales history for product

---

## Customer & Credit Management

### Add Customer Screen

**File**: `ui/screens/credit/AddCustomerScreen.kt`
**Route**: `add_customer`

#### Features

##### Customer Information
- **Customer Name** (Required)
  - Minimum 2 characters
  - Real-time validation

- **Phone Number** (Optional but validated)
  - **Zambian Phone Validation**
  - Accepts three formats:
    ```
    +260 97 123 4567  (International)
    0971234567        (National)
    971234567         (Short)
    ```
  - **Auto-Normalization**: All formats converted to `+260` format for storage
  - **Real-time Validation**: ✓ checkmark when valid
  - Format examples shown in placeholder
  - Supports spaces and hyphens in input

- **Address** (Optional)
  - Multi-line text field
  - Can include compound/area name

- **Notes** (Optional)
  - Multi-line text field
  - Store additional customer information

##### Phone Number Validation

**Implementation Details:**
```kotlin
// Valid input examples
"0971234567"         → Normalized to: "+260971234567"
"+260 97 123 4567"   → Normalized to: "+260971234567"
"97-123-4567"        → Normalized to: "+260971234567"

// Validation regex
^(\\+260|0)?\\s*[97][0-9](\\s*|-)?[0-9]{3}(\\s*|-)?[0-9]{4}$
```

##### User Experience
- Info card explaining customer tracking purpose
- Success dialog shows formatted phone number
- Options to add another customer or return
- Material 3 primary container top bar
- Scrollable form for small screens

#### Usage Flow

```
1. User taps "Add Customer" on Credit Ledger
   ↓
2. AddCustomerScreen opens
   ↓
3. User enters name (required)
   ↓
4. User enters phone number → validation runs → ✓ appears
   ↓
5. User optionally adds address and notes
   ↓
6. User taps "Save Customer"
   ↓
7. Phone normalized to +260 format
   ↓
8. Customer saved to database
   ↓
9. Success dialog → Done or Add Another
```

---

### Credit Ledger Screen

**File**: `ui/screens/credit/CreditLedgerScreen.kt`
**Route**: `credit`

#### Features
- List all customers with credit
- Shows current debt per customer
- Color-coded status:
  - 🟢 Paid in full
  - 🟡 Partial payment
  - 🔴 Overdue
- Search customers by name
- Filter by status
- Quick access to customer details
- Record new credit sale button
- Total outstanding debt summary

---

### Customer Detail Screen

**File**: `ui/screens/credit/CustomerDetailScreen.kt`
**Route**: `customer_detail/{customerId}`

#### Features
- Customer profile information
- Formatted phone number display
- Complete credit history
- Payment history
- Current debt balance
- Record payment button
- Edit customer information
- Credit transaction timeline
- Payment statistics

---

### Record Payment Screen

**File**: `ui/screens/credit/RecordPaymentScreen.kt`
**Route**: `record_payment/{customerId}`

#### Features

##### Payment Information
- **Customer Display**
  - Shows customer name
  - Current debt amount
  - Phone number (if available)

- **Payment Amount** (Required)
  - Must be > 0
  - Cannot exceed current debt
  - Decimal number validation
  - Shows remaining balance after payment

- **Payment Method** (Required)
  - Cash
  - Mobile Money (MTN, Airtel)
  - Bank Transfer
  - Other

- **Payment Date**
  - Defaults to today
  - Can select past dates
  - Date picker for easy selection

- **Notes** (Optional)
  - Payment reference number
  - Additional details

##### Automatic Calculations
- **Remaining Balance**: `Current Debt - Payment Amount`
- **Credit Status Update**: Auto-updates to "PAID" if balance = 0
- **Payment History**: Records payment with timestamp
- **Customer Total Debt**: Updates customer's total debt

#### Database Operations
- Uses **JOIN queries** for analytics
- Links payments to credits via creditId
- Updates credit_ledger status
- Creates payment record
- Triggers Flow updates for real-time UI refresh

#### Code Example

```kotlin
// Recording a payment
val payment = PaymentEntity(
    creditId = "CREDIT_001",
    amount = 100.00,
    paymentMethod = "MOBILE_MONEY",
    notes = "MTN Mobile Money - TXN12345",
    timestamp = System.currentTimeMillis()
)

paymentRepository.recordPayment(payment)
```

---

## Barcode Scanner

### Camera Scanner Screen

**File**: `ui/screens/scanner/CameraScannerScreen.kt`
**Route**: `camera_scanner`

#### Features

##### Camera Integration
- **CameraX Implementation**
  - Live camera preview
  - Back camera default
  - Auto-focus enabled
  - High-resolution image capture

##### ML Kit Barcode Scanning
- **Supported Formats**:
  - EAN-13 (European Article Number)
  - EAN-8
  - UPC-A (Universal Product Code)
  - UPC-E
  - ISBN (International Standard Book Number)
  - CODE-39, CODE-93, CODE-128
  - QR Code
  - TEXT (plain text barcodes)

- **Performance**:
  - Real-time detection (<200ms)
  - Continuous scanning mode
  - Auto-detection when barcode in frame

##### Visual Features
- **Scan Overlay**
  - Semi-transparent dark background
  - Clear scan area (70% width, 60% aspect ratio)
  - White dashed border around scan frame
  - **Green corner indicators** (40px each)
  - Centered on screen

- **Flash Control**
  - Toggle button in top app bar
  - Icons: Flash On / Flash Off
  - Persists during scan session

- **Permission Handling**
  - Runtime camera permission request
  - User-friendly permission denied UI
  - "Grant Permission" button
  - Explanation text for permission need

##### Integration
- **Return Value Handling**
  - Uses NavController savedStateHandle
  - Saves scanned value to: `"scanned_barcode"`
  - Calling screen retrieves via LiveData observer
  - Auto-closes scanner on successful scan

##### User Experience
- Instructions card at bottom
- "Position barcode within frame to scan"
- Back button to cancel scan
- Smooth navigation transitions

#### Usage Flow

```
1. User taps camera icon in AddProductScreen
   ↓
2. Scanner requests camera permission (if needed)
   ↓
3. User grants permission
   ↓
4. Camera preview opens with scan overlay
   ↓
5. User positions barcode within green frame
   ↓
6. ML Kit detects barcode (< 200ms)
   ↓
7. Barcode value extracted
   ↓
8. Scanner saves value to savedStateHandle
   ↓
9. Scanner auto-closes (navigateBack)
   ↓
10. AddProductScreen retrieves value
    ↓
11. Barcode field auto-filled
```

#### Code Example

**In DukaNavGraph.kt:**
```kotlin
composable(Screen.CameraScanner.route) {
    CameraScannerScreen(
        navController = navController,
        onBarcodeScanned = { scannedCode ->
            navController.previousBackStackEntry
                ?.savedStateHandle
                ?.set("scanned_barcode", scannedCode)
        }
    )
}
```

**In AddProductScreen.kt:**
```kotlin
LaunchedEffect(navController) {
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    savedStateHandle?.getLiveData<String>("scanned_barcode")?.observeForever { scannedCode ->
        if (scannedCode != null) {
            barcode = scannedCode
            savedStateHandle.remove<String>("scanned_barcode")
        }
    }
}
```

#### Technical Implementation

**Dependencies:**
```kotlin
// CameraX
implementation("androidx.camera:camera-camera2:1.4.1")
implementation("androidx.camera:camera-lifecycle:1.4.1")
implementation("androidx.camera:camera-view:1.4.1")

// ML Kit Barcode Scanning
implementation("com.google.mlkit:barcode-scanning:17.2.0")
```

**Permissions (AndroidManifest.xml):**
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

---

## Sales & Transactions

### Quick Sale Screen

**File**: `ui/screens/sales/QuickSaleScreen.kt`
**Route**: `quick_sale`

#### Features
- Fast product selection
- Quantity adjustment
- Instant sale recording
- Stock validation
- Sale type selection (Cash/Credit)
- Customer selection for credit sales
- Real-time stock updates
- Sales confirmation dialog

---

## Analytics & Dashboard

### Dashboard Screen

**File**: `ui/screens/dashboard/DashboardScreen.kt`
**Route**: `dashboard`

#### Features

##### Today's Overview
- **Total Sales**: Sum of all sales today
- **Total Revenue**: Amount in Kwacha
- **Total Profit**: Revenue - Cost
- **Profit Margin %**: (Profit / Revenue) × 100

##### Quick Stats Cards
- Number of transactions
- Average transaction value
- Top selling product
- Low stock alerts count

##### Recent Transactions
- Last 5-10 sales
- Time, product, amount
- Sale type indicator (Cash/Credit)

##### Navigation Quick Links
- Add Product
- Record Sale
- View Analytics
- Manage Credit

#### Real-Time Updates
- Uses Flow for reactive UI
- Auto-refreshes on new sales
- Animated number transitions
- Pull-to-refresh support

---

### Analytics Screen

**File**: `ui/screens/analytics/AnalyticsScreen.kt`
**Route**: `analytics`

#### Features

##### Sales Trends
- Daily sales chart
- Weekly comparison
- Monthly overview
- Revenue growth %

##### Top Products
- Best sellers by quantity
- Best sellers by revenue
- Profit leaders
- Stock turnover rate

##### Customer Analytics
- Credit customers count
- Outstanding debt total
- Payment collection rate
- Top credit customers

##### Period Selection
- Today
- This Week
- This Month
- Custom Date Range

---

## Cloud Sync

### Firebase Integration (85% Complete)

**Files**:
- `data/sync/FirebaseSyncService.kt`
- `data/sync/SyncWorker.kt`

#### Features

##### Automatic Sync
- **Background Sync**: WorkManager periodic tasks
- **WiFi-Only Mode**: Saves mobile data
- **Sync Frequency**: Configurable (15, 30, 60 min)
- **Battery Optimization**: Respects battery saver mode

##### Manual Sync
- Pull-to-refresh gesture
- Sync button in settings
- Force sync option
- Last sync timestamp display

##### Synced Entities
1. **Products** - Full CRUD sync
2. **Sales** - Transaction history
3. **Customers** - Customer profiles
4. **Credit Ledger** - Credit records
5. **Payments** - Payment history
6. **Inventory Logs** - Stock changes

##### Conflict Resolution
- **Strategy**: Last-Write-Wins
- Timestamp-based comparison
- Automatic resolution
- No user intervention needed

##### Sync Status
- **Syncing**: Progress indicator
- **Success**: Last sync time
- **Failed**: Error message + retry
- **Offline**: Queue for later

#### Sync Settings Screen

**File**: `ui/screens/settings/SyncSettingsScreen.kt`
**Route**: `sync_settings`

##### Settings
- **Auto Sync Toggle**
  - Enable/disable automatic sync
  - Requires WiFi toggle

- **Sync Frequency**
  - 15 minutes
  - 30 minutes
  - 60 minutes

- **Manual Sync Button**
  - Force immediate sync
  - Shows progress

- **Sync Status**
  - Last successful sync timestamp
  - Number of items synced
  - Error messages if any

##### User Authentication
- ⚠️ **15% Remaining**
- Firebase Auth integration planned
- Email/Phone authentication
- User-specific data isolation

---

## Settings

### Settings Screen

**File**: `ui/screens/settings/SettingsScreen.kt`
**Route**: `settings`

#### Features

##### App Settings
- Language selection (English, Nyanja, Bemba) - Planned
- Currency display format
- Date/Time format
- Theme selection (Light/Dark/System)

##### Business Settings
- Shop name
- Shop location
- Tax rate configuration
- Receipt settings

##### Data Management
- Backup database
- Restore from backup
- Export data (CSV/PDF)
- Clear cache

##### Sync Settings
- Navigate to Sync Settings screen
- Quick sync status display

##### About
- App version
- Build information
- Privacy policy
- Terms of service
- Contact support

---

## Implementation Status Summary

### ✅ Fully Implemented (100%)
- Product Management (CRUD)
- Customer Management (CRUD with Zambian phone validation)
- Credit Ledger
- Payment System (with JOIN queries)
- Barcode Scanner (CameraX + ML Kit)
- Dashboard
- Analytics
- Sync Settings Screen

### 🟡 Partially Implemented (50-99%)
- Cloud Sync (85%) - Auth remaining
- Sales Logging (Manual complete, Voice pending)

### ⚪ Not Started (0%)
- Voice Commands
- Product Image Recognition AI
- Multi-language UI (string resources)
- WhatsApp Integration
- PDF/CSV Export

---

## Technical Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────┐
│          UI Layer (Compose)         │
│  - Screens, Components, Navigation  │
├─────────────────────────────────────┤
│      Presentation Layer             │
│  - ViewModels, UI State, Events     │
├─────────────────────────────────────┤
│       Domain Layer                  │
│  - Use Cases, Business Logic        │
├─────────────────────────────────────┤
│         Data Layer                  │
│  - Repositories, Data Sources       │
├─────────────────────────────────────┤
│     Infrastructure Layer            │
│  - Room DB, Firebase, ML Kit        │
└─────────────────────────────────────┘
```

### Key Dependencies

```kotlin
// Core
implementation("androidx.core:core-ktx:1.15.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

// Compose
implementation("androidx.compose:compose-bom:2024.11.00")
implementation("androidx.compose.material3:material3")

// Navigation
implementation("androidx.navigation:navigation-compose:2.8.4")

// Hilt DI
implementation("com.google.dagger:hilt-android:2.50")
kapt("com.google.dagger:hilt-compiler:2.50")

// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// Firebase
implementation("com.google.firebase:firebase-firestore:25.1.1")
implementation("com.google.firebase:firebase-storage:21.0.1")

// CameraX
implementation("androidx.camera:camera-camera2:1.4.1")
implementation("androidx.camera:camera-lifecycle:1.4.1")
implementation("androidx.camera:camera-view:1.4.1")

// ML Kit
implementation("com.google.mlkit:barcode-scanning:17.2.0")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.1")
```

---

## Performance Metrics

| Metric | Target | Actual | Status |
|--------|--------|--------|--------|
| App Startup | <3s | ~2.5s | ✅ |
| Barcode Scan | <2s | ~0.8s | ✅ |
| Database Query | <100ms | ~50ms | ✅ |
| Screen Navigation | <300ms | ~200ms | ✅ |
| Build Time | <5min | 3m 36s | ✅ |
| APK Size | <15MB | ~12MB | ✅ |

---

## Future Enhancements

### Phase 2 Features
1. Voice Commands (Nyanja, Bemba, English)
2. Product Recognition AI (MobileNetV3)
3. WhatsApp Credit Reminders
4. PDF/CSV Export
5. Multi-shop Management
6. Supplier Integration
7. POS Hardware Integration

### Performance Improvements
1. Database query optimization
2. Image compression for products
3. Lazy loading for long lists
4. Caching strategies
5. Offline-first optimizations

---

**For Developers**: This documentation reflects the November 12, 2025 state of the Duka.AI application. For implementation details, refer to the actual source code files listed in each section.

**Last Updated**: November 12, 2025
**Version**: 1.0 (MVP - 80% Complete)
**Maintained by**: Duka.AI Development Team
