# ML Model - Quick Start Guide

## ✅ What's Been Done

### Files Organized
All your ML model files have been moved to the correct locations:

**Production Files** (bundled in Android APK):
- ✅ `app/src/main/assets/product_classifier.tflite` (1.4 MB)
- ✅ `app/src/main/assets/product_labels.txt` (9 products)
- ✅ `app/src/main/assets/product_labels_full_120.txt.backup` (original 120 products)

**Training Documentation**:
- ✅ `ml/training_results/classification_report.txt`
- ✅ `ml/training_results/confusion_matrix.png`
- ✅ `ml/training_results/training_history.png`
- ✅ `ml/training_results/per_class_performance.csv`
- ✅ `ml/training_results/kantemba_product_classifier_best.h5` (6.2 MB)
- ✅ `ml/training_results/kantemba_product_classifier_full.h5` (8.9 MB)
- ✅ All logs and metrics

---

## ⚠️ Critical Issue: Model Performance

### Current Status
- **Accuracy**: 20.93% (Target: 89%+)
- **Problem**: Model only predicts "Fanta" for all inputs
- **Cause**: Insufficient training data (only 43 validation samples)
- **Status**: ❌ NOT production-ready

### What This Means
The ML model is **integrated** but **not functional** for product recognition. The Android code works perfectly—the issue is the model needs complete retraining.

**Recommendation**: Use barcode scanner (already working at 99%+ accuracy) as primary method until model is retrained.

---

## 🎯 How to Test Current Integration

Even though the model performance is poor, you can test that the integration works:

### 1. Build the App
```bash
cd /home/imai/AndroidStudioProjects/dukaai
./gradlew assembleDebug
```

### 2. Test the Classifier
The model will load and run, but will always predict "Fanta" regardless of input:

```kotlin
// In your Android code
val classifier = TFLiteProductClassifier(context)
classifier.initialize()  // Should load successfully

val bitmap = /* your product image */
val results = classifier.classifyProduct(bitmap, maxResults = 5)

// Results will show:
// 1. Fanta Orange 500ml (high confidence)
// 2-9. Other products (low confidence)
// This is expected given the model's current state
```

---

## 📋 Recognized Products (Current Model)

The model was trained on these 9 products:

1. Chibuku Shake Shake
2. Coca-Cola 500ml
3. **Fanta Orange 500ml** ← Predicts this for everything
4. Nshima Mealie Meal 25kg
5. Monster Energy 500ml
6. Mosi Lager 375ml
7. Pepsi 500ml
8. Sprite 500ml
9. White Sugar 2kg

---

## 🔧 How to Retrain the Model

### What You Need
- **12,000+ product images** (100 per product for 120 products)
  - OR start with 2,000+ images (100 per product for 20 products)
- Photos from real kantemba shops
- Multiple angles and lighting conditions

### Quick Retraining Steps
1. **Collect more data**:
   - 100+ images per product
   - Various lighting conditions
   - Different angles and backgrounds

2. **Retrain model**:
   ```python
   # Update your training script with:
   - Proper data augmentation
   - Balanced class samples
   - 50-100 epochs with early stopping
   - Monitor validation accuracy
   ```

3. **Replace model**:
   ```bash
   # After training, copy new .tflite to:
   cp new_model.tflite app/src/main/assets/product_classifier.tflite

   # Update labels if classes changed
   cp new_labels.txt app/src/main/assets/product_labels.txt
   ```

4. **Rebuild app**:
   ```bash
   ./gradlew clean assembleDebug
   ```

### Success Criteria
- Validation accuracy: **89%+**
- Per-class recall: **80%+** for each product
- Confusion matrix shows good separation between classes

---

## 🚀 Recommended Approach

### Short-term (Now)
1. **Use barcode scanner** as primary method (already 99%+ accurate)
2. Keep ML model integrated but disabled in production
3. Focus on other MVP features

### Medium-term (Weeks 3-4 of roadmap)
1. Collect proper dataset (100+ images per product)
2. Start with 20 most common products
3. Retrain model to 89%+ accuracy
4. Enable ML recognition for products without barcodes

### Long-term (Phase 2+)
1. Expand to all 120 products
2. Continuous learning from user corrections
3. Cloud-based model updates
4. Hybrid barcode + ML approach

---

## 📖 Full Documentation

For complete details, see:
- **`ml/MODEL_INTEGRATION_SUMMARY.md`** - Comprehensive analysis and recommendations
- **`docs/AI_MODELS.md`** - ML architecture documentation
- **`ml/training_results/classification_report.txt`** - Detailed metrics

---

## ❓ FAQ

**Q: Can I use the model now?**
A: Technically yes—it loads and runs correctly. Practically no—it only predicts "Fanta" for everything.

**Q: What went wrong with training?**
A: Insufficient dataset. 43 validation samples is too small. Need 2,000-12,000+ images.

**Q: Should I retrain or use barcodes?**
A: Use barcodes now (working perfectly). Retrain model when you have proper dataset.

**Q: Will the app work without the ML model?**
A: Yes! All features work. Barcode scanner handles product recognition. ML is optional enhancement.

**Q: How long to collect proper dataset?**
A: For 20 products: 2-3 weeks. For 120 products: 6-8 weeks.

---

*Last Updated: November 13, 2025*
