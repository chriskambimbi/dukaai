# Duka.AI - Development Roadmap

## Vision

Transform inventory management for 100,000+ informal retailers across Zambia by 2026.

---

## Project Timeline

### Phase 1: MVP Development (Weeks 1-10) ✅ In Progress

**Goal:** Feature-complete offline app with core functionality

#### Week 1-2: Foundation & Setup ✅ COMPLETED
- [x] Project initialization (Android + Jetpack Compose)
- [x] Database schema design (Room)
- [x] Dependency injection setup (Hilt)
- [x] Navigation structure
- [x] Basic UI theme (Material 3)

#### Week 3-4: ML Model Development
- [ ] Collect product image dataset (12,000+ images)
- [ ] Train MobileNetV3 model (120 Zambian products)
- [ ] Quantize model to TFLite (INT8)
- [ ] Test on Transsion devices
- [ ] Achieve 89%+ accuracy

#### Week 5-6: Core Features ✅ MOSTLY COMPLETED
- [x] Product management (CRUD operations)
  - [x] Add Product Screen (complete with barcode scanning)
  - [x] Product List Screen
  - [x] Product Detail Screen
  - [x] Profit margin calculator
  - [x] Stock validation
- [x] Camera-based barcode scanning (ML Kit + CameraX)
  - [x] Real-time barcode detection
  - [x] Support for EAN, UPC, ISBN, TEXT formats
  - [x] Flash control, permission handling
  - [x] Auto-fill integration with AddProductScreen
- [x] Quick sale logging
- [x] Real-time inventory tracking
- [x] Low stock alerts
- [ ] Product image recognition AI (dataset collection in progress)

#### Week 7: Credit Management ✅ COMPLETED
- [x] Customer profile creation
  - [x] Add Customer Screen with Zambian phone validation
  - [x] Phone number normalization (+260 format)
  - [x] Customer data persistence
- [x] Credit sale recording
- [x] Payment tracking
  - [x] Record Payment Screen
  - [x] Payment history with JOIN queries
  - [x] Customer payment analytics
- [x] Debt calculation
  - [x] Automatic debt tracking
  - [x] Payment impact on balances
- [x] Credit ledger UI
  - [x] Credit Ledger Screen
  - [x] Customer Detail Screen
  - [x] Payment history display

#### Week 8: Analytics Dashboard ✅ MOSTLY COMPLETED
- [x] Daily sales summary
- [x] Top selling products
- [x] Profit margin calculations
- [x] Sales trends visualization
- [x] Dashboard Screen with real-time metrics
- [x] Analytics Screen
- [ ] Export functionality (CSV/PDF) - Planned for Phase 2

#### Week 8.5: Cloud Sync (Bonus) ✅ 85% COMPLETED
- [x] Firebase project setup
- [x] Cloud Firestore integration
- [x] Bidirectional sync service for all entities
  - [x] Products, Sales, Customers
  - [x] Credit Ledger, Payments
  - [x] Inventory Logs
- [x] Sync Settings Screen
  - [x] Manual/Automatic sync toggle
  - [x] WiFi-only mode
  - [x] Sync frequency control
- [x] WorkManager background sync
- [x] Conflict resolution (last-write-wins)
- [ ] User authentication (15% remaining)

#### Week 9: Voice Commands
- [ ] Integrate Android SpeechRecognizer
- [ ] Implement intent parser
- [ ] Multi-language support (English, Nyanja, Bemba)
- [ ] Voice feedback system
- [ ] Accuracy testing

#### Week 10: Testing & Polish
- [ ] Unit tests (80% coverage)
- [ ] UI/UX refinements
- [ ] Performance optimization
- [ ] Battery usage optimization
- [ ] Bug fixes

**Deliverable:** Stable MVP build ready for alpha testing

---

### Phase 2: Alpha Testing (Weeks 11-14)

**Goal:** Validate core features with real users

#### Week 11-12: Alpha Release
- [ ] Recruit 10 kantemba owners in Lusaka compounds
  - Kalingalinga: 3 shops
  - Mtendere: 3 shops
  - Chawama: 2 shops
  - Kanyama: 2 shops
- [ ] Deploy alpha build via Firebase App Distribution
- [ ] Conduct in-person onboarding sessions
- [ ] Set up feedback collection (in-app + WhatsApp group)

#### Week 13-14: Feedback & Iteration
- [ ] Daily check-ins with alpha testers
- [ ] Collect usage analytics (Firebase Analytics)
- [ ] Identify pain points and bugs
- [ ] Implement high-priority fixes
- [ ] Measure key metrics:
  - Daily active users (target: 70%)
  - Average session duration (target: 15+ min)
  - Sales logged per day (target: 50+)
  - Crash-free rate (target: 99%+)

**Deliverable:** Validated product-market fit, refined feature set

---

### Phase 3: Beta Testing (Weeks 15-18)

**Goal:** Scale to 50 users, ensure stability

#### Week 15-16: Beta Launch
- [ ] Recruit 50 additional shopkeepers
- [ ] Expand to more compounds (Matero, Garden, Ng'ombe)
- [ ] Deploy beta build on Google Play (closed track)
- [ ] Create tutorial videos (English, Nyanja, Bemba)
- [ ] Set up support system (WhatsApp Business)

#### Week 17-18: Optimization
- [ ] Fix critical bugs from beta feedback
- [ ] Optimize ML model (reduce size/improve accuracy)
- [ ] Improve voice recognition accuracy
- [ ] Add most-requested features
- [ ] Performance tuning for low-end devices

**Key Metrics:**
- User retention (7-day): >60%
- Average rating: >4.2 stars
- Sales per user per day: >40
- Battery drain: <5% per hour

**Deliverable:** Production-ready app

---

### Phase 4: Public Launch (Week 19-20)

**Goal:** Launch to Zambian market

#### Week 19: Pre-Launch
- [ ] Finalize app store listing
  - Screenshots (English, Nyanja, Bemba)
  - Demo video (2 minutes)
  - App description (SEO optimized)
- [ ] Prepare marketing materials
  - Social media posts (Facebook, Twitter)
  - Flyers for compounds
  - Radio ad script (local stations)
- [ ] Set up support infrastructure
  - FAQ page
  - WhatsApp support line
  - Email support

#### Week 20: Launch
- [ ] Submit to Google Play Store (public release)
- [ ] Announce on social media
- [ ] Contact local tech press (Lusaka Times, Zambia Reports)
- [ ] Run targeted Facebook ads (Lusaka shopkeepers)
- [ ] Monitor reviews and respond promptly

**Target:**
- 500 downloads in first week
- 1,000 downloads in first month
- 4.0+ star rating

**Deliverable:** Publicly available app

---

### Phase 5: Growth & Iteration (Months 2-6)

**Goal:** Reach 5,000+ active users

#### Month 2-3: Feature Expansion
- [ ] WhatsApp credit reminders
- [ ] Cloud backup/sync (optional, WiFi only)
- [ ] Multi-shop management
- [ ] Supplier integration
- [ ] Advanced reports (monthly, quarterly)

#### Month 4-5: Geographic Expansion
- [ ] Expand to other Zambian cities
  - Kitwe
  - Ndola
  - Livingstone
  - Kabwe
- [ ] Partner with local business associations
- [ ] Run workshops in new cities

#### Month 6: Monetization
- [ ] Introduce premium features
  - Cloud backup: K 10/month
  - Multi-shop: K 20/month
  - Advanced analytics: K 15/month
- [ ] Freemium model (core features always free)
- [ ] Payment via mobile money (MTN, Airtel)

**Target:**
- 5,000 active users
- 20% premium conversion rate
- K 50,000 monthly recurring revenue

---

### Phase 6: Scale & Sustainability (Year 2)

**Goal:** Become the #1 kantemba management app in Zambia

#### Q1 2026: Product Maturity
- [ ] AI-powered inventory forecasting
- [ ] Product price recommendations
- [ ] Competitor price tracking
- [ ] Customer loyalty features
- [ ] POS hardware integration (optional)

#### Q2 2026: Regional Expansion
- [ ] Expand to neighboring countries
  - Malawi
  - Zimbabwe
  - Tanzania
- [ ] Adapt product database for each country
- [ ] Localize to additional languages (Tonga, Lozi)

#### Q3 2026: Ecosystem Building
- [ ] Partner with suppliers (wholesale discounts)
- [ ] Integrate with mobile money platforms
- [ ] Launch shopkeeper community forum
- [ ] Annual Duka.AI user conference

#### Q4 2026: Long-term Vision
- [ ] 100,000+ active users
- [ ] K 500,000+ monthly revenue
- [ ] Expand to other informal sectors (salons, mechanics)
- [ ] Launch Duka.AI Pro for medium businesses

---

## Key Milestones

| Milestone | Target Date | Status | Actual Completion |
|-----------|-------------|--------|-------------------|
| Foundation Setup | Week 2 | ✅ Complete | November 2025 |
| Core Features | Week 6 | ✅ Complete | November 2025 |
| Credit Management | Week 7 | ✅ Complete | November 2025 |
| Analytics Dashboard | Week 8 | ✅ Complete | November 2025 |
| Cloud Sync (Bonus) | - | 🟡 85% Complete | November 2025 |
| Barcode Scanner | - | ✅ Complete | November 2025 |
| MVP Complete | Week 10 | 🟡 80% Complete | Target: December 2025 |
| Voice Commands | Week 9 | 🟡 In Progress | - |
| Product Recognition AI | Week 4 | 🟡 Dataset Collection | - |
| Alpha Launch (10 users) | Week 11 | ⚪ Not Started | - |
| Beta Launch (50 users) | Week 15 | ⚪ Not Started | - |
| Public Launch | Week 20 | ⚪ Not Started | - |
| 1,000 Downloads | Month 2 | ⚪ Not Started | - |
| 5,000 Active Users | Month 6 | ⚪ Not Started | - |
| Profitability | Month 9 | ⚪ Not Started | - |
| 100,000 Users | Year 2 Q4 | ⚪ Not Started | - |

---

## Success Metrics

### User Engagement
- **Daily Active Users (DAU):** Target 70% of registered users
- **Session Duration:** Average 20+ minutes per day
- **Sales Logged:** 50+ transactions per user per day
- **Retention Rate:** 60%+ after 30 days

### Product Quality
- **Crash-Free Rate:** 99%+
- **App Rating:** 4.2+ stars
- **AI Accuracy:** 89%+ for product recognition
- **Voice Accuracy:** 85%+ for voice commands

### Business Impact
- **Time Saved:** 30+ minutes per shopkeeper per day
- **Stockout Reduction:** 40% fewer out-of-stock incidents
- **Credit Recovery:** 25% increase in credit repayment
- **Revenue Growth:** 15% increase in shop revenue (user-reported)

---

## Risk Mitigation

| Risk | Impact | Mitigation Strategy |
|------|--------|---------------------|
| Low adoption rate | High | Free tier, extensive training, word-of-mouth incentives |
| Poor internet (for initial download) | Medium | Offer offline installation (APK via WhatsApp) |
| Device compatibility issues | High | Test on 10+ Transsion phone models |
| Competition from existing solutions | Medium | Offline-first + local language = unique value prop |
| Data privacy concerns | Medium | Clear privacy policy, local-only storage by default |
| Low ML accuracy on new products | High | Continuous model updates, manual entry fallback |

---

## Resource Requirements

### Team (MVP Phase)
- **1x Project Lead / Product Manager**
- **1x Android Developer** (Kotlin, Compose)
- **1x ML Engineer** (TensorFlow, computer vision)
- **1x UI/UX Designer**
- **1x QA Tester** (part-time)

### Budget (MVP Phase)
- **Development:** K 150,000 (salaries, 10 weeks)
- **Infrastructure:** K 10,000 (Firebase, cloud storage)
- **Dataset Collection:** K 15,000 (travel, incentives)
- **Testing Devices:** K 20,000 (10 Transsion phones)
- **Marketing:** K 25,000 (ads, materials)
- **Total:** K 220,000 (~$8,500 USD)

---

## Next Steps

### This Week
1. Complete database schema implementation
2. Set up dependency injection with Hilt
3. Begin ML dataset collection
4. Design core UI screens in Figma

### This Month
1. Complete MVP development (Weeks 1-10)
2. Train and test ML model
3. Recruit alpha testers
4. Prepare launch materials

### This Quarter
1. Launch alpha and beta testing
2. Iterate based on feedback
3. Achieve product-market fit
4. Launch publicly on Google Play

---

## Contributing to Roadmap

Have ideas for features or improvements? We'd love to hear from you!

1. Open an issue on GitHub with tag `roadmap-suggestion`
2. Join our community discussions
3. Vote on feature requests

**Let's build the future of Zambian retail together!**

---

## Recent Achievements (November 2025)

### Major Implementations
1. **Complete Barcode Scanner** - CameraX + ML Kit integration with real-time scanning
2. **Payment System Enhancement** - SQL JOIN queries for customer payment analytics
3. **Cloud Sync Infrastructure** - Firebase integration with 85% feature completion
4. **All CRUD Screens** - Complete UI for products, customers, payments, and credit

### Development Velocity
- **Build Time**: 3m 36s (optimized)
- **APK Size**: ~12 MB
- **Code Quality**: BUILD SUCCESSFUL, no compilation errors
- **Feature Completion**: 80% of MVP features implemented

### Next Priorities
1. Complete voice command implementation
2. Collect and train product recognition AI model
3. Final testing and polish
4. Prepare for alpha launch

---

Last Updated: November 12, 2025
Next Review: December 2025
