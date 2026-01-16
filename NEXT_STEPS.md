# Duka.AI - Recommended Next Steps

**Date**: November 13, 2025
**Current Status**: MVP 80% Complete (Week 8 of 10)
**Build Status**: ✅ BUILD SUCCESSFUL

---

## 📊 Current Project Status

### ✅ What's Complete (80%)

**Core Features**:
- ✅ Product Management (CRUD) with barcode scanner integration
- ✅ Customer & Credit Management with Zambian phone validation
- ✅ Payment System with SQL JOIN queries
- ✅ Sales Tracking & Quick Sale
- ✅ Analytics Dashboard with real-time metrics
- ✅ Cloud Sync (85% - Firebase integration complete)
- ✅ CameraX + ML Kit Barcode Scanning
- ✅ ML Model Integration (needs retraining for production)

**Architecture**:
- ✅ Room Database (7 entities, complete schema)
- ✅ Hilt Dependency Injection
- ✅ MVVM Architecture (7 ViewModels)
- ✅ Repository Pattern
- ✅ Jetpack Compose UI (12+ screens)
- ✅ Material 3 Design

**Technical Debt**:
- ⚠️ 15+ TODOs in codebase (mostly feature placeholders)
- ⚠️ Missing CustomerViewModel (customer screens use CreditViewModel)
- ⚠️ Minimal test coverage (1 unit test, 1 instrumented test)
- ⚠️ ML model accuracy: 20.93% (needs retraining)

---

## 🎯 Prioritized Next Steps

### PRIORITY 1: CRITICAL FOR MVP (Week 9-10)

#### 1.1 Complete Missing ViewModels & Fix TODOs
**Estimated Time**: 1-2 days
**Status**: High Priority

**Tasks**:
- [ ] **Create CustomerViewModel** (currently missing)
  - Customer list management
  - Customer search & filtering
  - Customer profile updates
  - Integration with CustomerDetailScreen

- [ ] **Fix Critical TODOs**:
  - `BarcodeScanner.kt:58` - Already implemented in CameraScannerScreen, remove placeholder
  - `ProductDetailScreen.kt:171` - Implement navigation to QuickSale with pre-selected product
  - `CustomerDetailScreen.kt:260` - Implement WhatsApp reminder integration

**Files to Update**:
```
app/src/main/java/com/example/dukaai/ui/viewmodel/CustomerViewModel.kt [CREATE]
app/src/main/java/com/example/dukaai/ui/screens/credit/CustomerDetailScreen.kt
app/src/main/java/com/example/dukaai/ui/screens/products/ProductDetailScreen.kt
```

---

#### 1.2 Testing Infrastructure (Week 10)
**Estimated Time**: 2-3 days
**Status**: Critical for Production

**Current State**:
- Unit Tests: 1 file (placeholder)
- Instrumented Tests: 1 file (placeholder)
- Target: 80% code coverage

**Tasks**:
- [ ] **Unit Tests for Repositories**:
  - ProductRepository tests
  - CustomerRepository tests
  - SaleRepository tests
  - PaymentRepository tests (test JOIN queries)

- [ ] **Unit Tests for ViewModels**:
  - DashboardViewModel tests
  - ProductViewModel tests
  - SaleViewModel tests
  - CreditViewModel tests

- [ ] **UI Tests**:
  - Navigation flow tests
  - Add product flow test
  - Quick sale flow test
  - Credit recording flow test
  - Payment recording flow test

- [ ] **Integration Tests**:
  - Database operations
  - Repository + ViewModel integration
  - Barcode scanner integration test

**Sample Test Structure**:
```kotlin
// app/src/test/java/com/example/dukaai/repository/ProductRepositoryTest.kt
// app/src/test/java/com/example/dukaai/viewmodel/ProductViewModelTest.kt
// app/src/androidTest/java/com/example/dukaai/ui/ProductFlowTest.kt
```

---

#### 1.3 UI/UX Polish & Bug Fixes
**Estimated Time**: 2-3 days
**Status**: High Priority

**Tasks**:
- [ ] **Validation Enhancements**:
  - Add input validation feedback animations
  - Improve error messages (user-friendly Nyanja/Bemba translations)
  - Add loading states to all async operations

- [ ] **Navigation Improvements**:
  - Add back button handling
  - Implement deep linking for product/customer details
  - Add navigation animations

- [ ] **Performance Optimization**:
  - Lazy loading for product/customer lists
  - Optimize database queries (add indexes)
  - Image loading optimization
  - Memory leak detection & fixes

- [ ] **Accessibility**:
  - Add content descriptions for images
  - Ensure minimum touch target sizes (48dp)
  - Test with TalkBack
  - High contrast mode support

---

### PRIORITY 2: IMPORTANT FOR LAUNCH (Pre-Alpha)

#### 2.1 User Authentication (Complete Firebase Auth)
**Estimated Time**: 2 days
**Status**: 15% remaining for cloud sync

**Tasks**:
- [ ] Firebase Authentication setup
  - Phone number authentication (Zambian +260 format)
  - Anonymous authentication (for offline-first users)
  - User profile management

- [ ] User onboarding flow
  - Welcome screen
  - Shop setup (shop name, location)
  - Optional cloud sync setup

- [ ] Multi-device sync
  - Device registration
  - Conflict resolution UI
  - Sync status indicators

---

#### 2.2 Settings Implementation
**Estimated Time**: 1-2 days
**Status**: Settings screen has placeholder TODOs

**Tasks**:
- [ ] **Complete Settings Features**:
  - Notification settings (low stock alerts, payment reminders)
  - Data backup/restore (local + cloud)
  - Clear data with confirmation dialog
  - PIN/Password protection setup
  - Privacy policy integration
  - Help & support screen

- [ ] **Language Settings**:
  - English, Nyanja, Bemba toggle
  - String resource localization
  - RTL layout support (if needed)

---

#### 2.3 Localization (Multi-language Support)
**Estimated Time**: 3-4 days
**Status**: Not started

**Tasks**:
- [ ] **Create String Resources**:
  - `values/strings.xml` (English - default)
  - `values-ny/strings.xml` (Nyanja/Chinyanja)
  - `values-bem/strings.xml` (Bemba)

- [ ] **Translate UI Elements**:
  - All screen titles
  - Button labels
  - Form labels & validation messages
  - Error messages
  - Success messages
  - Navigation labels

- [ ] **Currency & Number Formatting**:
  - Zambian Kwacha (ZMW) formatting
  - Number formatting for local conventions
  - Date/time formatting

**Example**:
```xml
<!-- values/strings.xml -->
<string name="add_product">Add Product</string>
<string name="quick_sale">Quick Sale</string>

<!-- values-ny/strings.xml -->
<string name="add_product">Onjezani Katundu</string>
<string name="quick_sale">Kugulitsa Msanga</string>
```

---

### PRIORITY 3: FUTURE ENHANCEMENTS (Phase 2)

#### 3.1 Voice Commands (Week 9 - Deferred to Phase 2)
**Estimated Time**: 1-2 weeks
**Status**: Not started (complex feature)

**Recommendation**: **Defer to Phase 2** - Focus on core stability for alpha launch

**Rationale**:
- Voice recognition in local languages requires extensive dataset
- Speech-to-text accuracy testing needed
- Complex intent parsing for multi-language commands
- Alpha testers can provide feedback on necessity

**If implementing now**:
- [ ] Android SpeechRecognizer integration
- [ ] Intent parser for commands ("record sale", "add K50 airtime")
- [ ] Multi-language support (English, Nyanja, Bemba)
- [ ] Voice feedback system
- [ ] Accuracy testing & tuning

---

#### 3.2 ML Model Retraining (Ongoing - Phase 2)
**Estimated Time**: 2-4 weeks (dataset collection)
**Status**: Integrated but needs retraining

**Current State**:
- Model integrated successfully
- Accuracy: 20.93% (trained on 43 samples)
- Target: 89%+ accuracy

**Recommendation**: **Start dataset collection in parallel**

**Tasks**:
- [ ] **Dataset Collection** (2-4 weeks):
  - Collect 100-200 images per product
  - Focus on 20 most common products first
  - Multiple angles, lighting conditions
  - Real kantemba shop environments

- [ ] **Model Retraining**:
  - Train MobileNetV3 with balanced dataset
  - Achieve 89%+ validation accuracy
  - Quantize to TFLite (INT8)
  - Test on Transsion devices

- [ ] **Deployment**:
  - Replace `product_classifier.tflite`
  - Update `product_labels.txt`
  - Test inference performance
  - Deploy via app update

---

#### 3.3 WhatsApp Integration (Phase 2)
**Estimated Time**: 3-5 days
**Status**: Placeholder TODO in CustomerDetailScreen

**Features**:
- [ ] WhatsApp credit reminders
  - Scheduled reminders for outstanding debts
  - Customizable reminder templates
  - Respect user preferences (opt-out)

- [ ] WhatsApp Business API integration
  - Send payment links
  - Share receipts
  - Customer communication history

---

#### 3.4 Advanced Analytics & Reporting (Phase 2)
**Estimated Time**: 1 week
**Status**: Basic analytics complete

**Features**:
- [ ] CSV/PDF export functionality
- [ ] Monthly/quarterly reports
- [ ] Sales forecasting
- [ ] Inventory turnover analysis
- [ ] Customer segmentation
- [ ] Profit trend analysis

---

### PRIORITY 4: PRE-LAUNCH PREPARATION

#### 4.1 Alpha Testing Preparation
**Estimated Time**: 1 week
**Status**: Not started

**Tasks**:
- [ ] **Recruit Alpha Testers** (10 kantemba owners in Lusaka):
  - Kalingalinga: 3 shops
  - Mtendere: 3 shops
  - Chawama: 2 shops
  - Kanyama: 2 shops

- [ ] **Setup Testing Infrastructure**:
  - Firebase App Distribution
  - Crash reporting (Firebase Crashlytics)
  - Analytics tracking (Firebase Analytics)
  - User feedback collection (in-app + WhatsApp group)

- [ ] **Create Testing Materials**:
  - Alpha tester onboarding guide
  - Testing scenarios & checklists
  - Feedback survey (Google Forms)
  - WhatsApp support group

- [ ] **Alpha Release Build**:
  - Signed APK with crash reporting
  - Version name: 0.9.0-alpha
  - Minimum testing period: 2 weeks

---

#### 4.2 App Store Preparation
**Estimated Time**: 3-5 days
**Status**: Not started

**Tasks**:
- [ ] **Google Play Console Setup**:
  - Developer account registration
  - App listing creation
  - Content rating questionnaire

- [ ] **App Store Assets**:
  - App icon (512x512, 1024x1024)
  - Feature graphic (1024x500)
  - Screenshots (phone & tablet) - English, Nyanja, Bemba
  - Promotional video (2 minutes, local language)

- [ ] **App Description**:
  - English version
  - Nyanja version
  - Bemba version
  - SEO optimization (keywords: "kantemba", "inventory", "Zambia")

- [ ] **Privacy Policy**:
  - Data collection disclosure
  - Firebase usage
  - User rights (GDPR-compliant)

---

## 📋 Recommended Development Sequence

### Week 9 (Current - Finish MVP)

**Days 1-2**: Critical Bug Fixes & ViewModels
- Create CustomerViewModel
- Fix critical TODOs
- Navigation improvements

**Days 3-4**: Testing Infrastructure
- Setup test framework
- Write repository tests
- Write ViewModel tests

**Days 5-7**: UI Polish & Validation
- Input validation improvements
- Error message localization
- Performance optimization

---

### Week 10 (MVP Completion & Testing)

**Days 1-3**: UI Tests & Integration Tests
- Navigation flow tests
- Critical path testing
- Performance testing

**Days 4-5**: Settings & Localization
- Complete settings features
- Basic string localization (English + Nyanja)
- Currency formatting

**Days 6-7**: Alpha Preparation
- Bug fixes from testing
- Alpha build preparation
- Testing documentation

---

### Week 11-12 (Alpha Testing)

**Week 11**:
- Deploy alpha build
- Onboard 10 testers
- Daily check-ins

**Week 12**:
- Collect feedback
- Fix critical bugs
- Iterate based on user feedback

---

## 🔧 Technical Debt to Address

### High Priority
1. **Missing CustomerViewModel** - Customer management needs dedicated ViewModel
2. **Test Coverage** - Currently <5%, target 80%
3. **TODOs in Production Code** - 15+ placeholders need implementation or removal
4. **ML Model Quality** - 20.93% accuracy not production-ready

### Medium Priority
5. **Error Handling** - Improve user-facing error messages
6. **Loading States** - Add loading indicators for async operations
7. **Input Validation** - More robust validation with better feedback
8. **Database Indexes** - Optimize queries for large datasets

### Low Priority
9. **Code Comments** - Add KDoc comments for public APIs
10. **Proguard Rules** - Optimize for release build
11. **Crashlytics Integration** - Better crash reporting
12. **Analytics Events** - Track user behavior for improvements

---

## 📊 Success Metrics for MVP Launch

### Technical Metrics
- ✅ Build Success: 100%
- ⚠️ Test Coverage: <5% → Target: 80%
- ✅ Crash-free Rate: Not measured → Target: 99%+
- ✅ App Size: 12 MB → Target: <15 MB

### User Metrics (Alpha Testing)
- Daily Active Users: Target 70%
- Session Duration: Target 15+ minutes
- Sales Logged: Target 50+ per day per user
- Feature Adoption: Track usage of each feature
- User Satisfaction: Target 4.0+ rating

### Performance Metrics
- App Startup: Target <3 seconds
- Product Scan: Target <2 seconds (barcode)
- Query Response: Target <500ms
- Battery Drain: Target <5% per hour

---

## 🚀 Quick Wins (Can Do Today)

### 1. Create CustomerViewModel (1-2 hours)
```kotlin
// app/src/main/java/com/example/dukaai/ui/viewmodel/CustomerViewModel.kt
@HiltViewModel
class CustomerViewModel @Inject constructor(
    private val customerRepository: CustomerRepository
) : ViewModel() {

    val customers: StateFlow<List<CustomerEntity>> =
        customerRepository.getAllCustomers()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> {
        return customerRepository.searchCustomers(query)
    }

    // ... other functions
}
```

### 2. Remove Obsolete BarcodeScanner TODO (5 minutes)
- `ml/BarcodeScanner.kt:58` - The actual implementation is in `CameraScannerScreen.kt`
- Remove placeholder code or update to reference working implementation

### 3. Add Database Indexes (30 minutes)
```kotlin
// In DukaDatabase.kt
@Database(
    entities = [...],
    version = 2, // Increment version
    exportSchema = false
)
abstract class DukaDatabase : RoomDatabase() {
    // Add indexes for frequently queried fields
    @androidx.room.Index(value = ["barcode"], unique = true)
    @androidx.room.Index(value = ["name"])
    @androidx.room.Index(value = ["category"])
}
```

### 4. Add Loading States (1 hour)
Add loading indicators to screens that fetch data asynchronously

---

## 📞 Questions to Consider

1. **Voice Commands**: Is this essential for MVP, or can it wait for Phase 2?
   - **Recommendation**: Phase 2 - focus on stability first

2. **ML Model**: Start dataset collection now or after alpha testing?
   - **Recommendation**: Start parallel collection, deploy in Phase 2

3. **Languages**: Which language(s) for alpha testing? All three or English + Nyanja?
   - **Recommendation**: English + Nyanja for alpha, add Bemba for beta

4. **Testing Scope**: How much test coverage before alpha?
   - **Recommendation**: Minimum 60% for critical paths (sales, payments, inventory)

5. **Cloud Sync**: Complete Firebase Auth now or after alpha?
   - **Recommendation**: Complete now - needed for multi-device testing

---

## 💡 Key Recommendations

### For This Week
1. ✅ **Focus on MVP Completion** - Week 9-10 goals
2. ✅ **Prioritize Testing** - Cannot launch without tests
3. ✅ **Defer Voice Commands** - Complex feature, not critical for alpha
4. ✅ **Start Dataset Collection** - Parallel track for ML improvement

### For Alpha Launch
1. ✅ **Feature-complete but focused** - Core features only
2. ✅ **Stable & well-tested** - 80% test coverage
3. ✅ **Localized** - At least English + Nyanja
4. ✅ **Observable** - Crashlytics + Analytics integrated

### For Production Launch
1. ✅ **User-validated** - Alpha/Beta feedback incorporated
2. ✅ **Performance-optimized** - Battery, speed, memory
3. ✅ **Fully localized** - All 3 languages
4. ✅ **ML Model retrained** - 89%+ accuracy

---

## 📚 Resources Needed

### Team Resources
- **Android Developer**: Focus on testing, bug fixes, polish
- **QA Tester** (part-time): Alpha testing coordination
- **UI/UX Designer** (optional): Final polish pass
- **Translator**: Nyanja & Bemba localization

### Infrastructure
- Firebase project (already setup)
- Google Play Developer Account ($25 one-time)
- Firebase App Distribution (free)
- Crash reporting & analytics (free tier)

### Testing Devices
- Minimum 3-5 Transsion phones (Tecno, Infinix)
- Different Android versions (7.0, 8.0, 9.0, 10+)
- Various screen sizes

---

## 🎯 Definition of Done (MVP)

MVP is complete when:
- [x] All core features implemented (products, sales, customers, credit, payments)
- [x] Barcode scanner working
- [ ] 80% test coverage achieved
- [ ] Zero critical bugs
- [ ] <5 known medium bugs
- [ ] Firebase auth complete
- [ ] English + Nyanja localization
- [ ] Settings functional
- [ ] Performance meets targets
- [ ] Alpha build deployed successfully

---

**Current Progress**: 80% Complete
**Estimated to MVP Complete**: 1-2 weeks (with focused effort)
**Alpha Launch Target**: Week 11 (late November 2025)
**Public Launch Target**: Week 20 (January 2026)

---

*Last Updated: November 13, 2025*
*Next Review: November 20, 2025*
