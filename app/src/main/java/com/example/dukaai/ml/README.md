# ML Infrastructure for Product Recognition

This package contains the machine learning infrastructure for Duka.AI product recognition.

## Overview

The ML system uses TensorFlow Lite to recognize products from camera images, enabling:
- Automatic product identification
- Barcode scanning
- Quick product lookup during sales

## Components

### 1. ProductClassifier Interface
- Defines the contract for product classification
- Allows multiple implementations (TFLite, MLKit, cloud APIs)

### 2. TFLiteProductClassifier
- TensorFlow Lite implementation
- Runs models on-device for fast, offline recognition
- Supports GPU acceleration for better performance

### 3. ProductRecognitionResult
- Data class containing recognition results
- Includes product name, confidence score, category, and barcode
- Confidence threshold of 70% for automatic recognition

### 4. ImageUtils
- Image preprocessing utilities
- Handles image loading, resizing, rotation, and enhancement
- Ensures images are properly formatted for ML models

### 5. BarcodeScanner
- Interface for barcode detection
- Can be implemented using ML Kit or ZXing
- Returns barcode value and format

## Setup Instructions

### Step 1: Add TensorFlow Lite Model

1. Train or obtain a product classification model
2. Convert it to TensorFlow Lite format (.tflite)
3. Place the model file in: `app/src/main/assets/product_classifier.tflite`

### Step 2: Add Product Labels

Create a labels file with your products:
- File location: `app/src/main/assets/product_labels.txt`
- Format: One product per line
- Enhanced format: `ProductName|Category|Barcode`

Example:
```
Coca-Cola 500ml|Beverages|5449000000996
Bread Loaf|Food|BREAD001
Cooking Oil 2L|Food|OIL001
```

### Step 3: Create Assets Directory

```bash
mkdir -p app/src/main/assets
```

### Step 4: Optional - Add ML Kit for Barcode Scanning

To enable barcode scanning, add ML Kit dependency:

```kotlin
// In app/build.gradle.kts
implementation("com.google.mlkit:barcode-scanning:17.2.0")
```

Then implement the `scanBarcode` method in `DefaultBarcodeScanner`.

## Usage Example

### Initialize the Classifier

```kotlin
val classifier = TFLiteProductClassifier(context)
classifier.initialize()
```

### Classify a Product

```kotlin
val bitmap: Bitmap = // ... get from camera or gallery
val results = classifier.classifyProduct(bitmap, maxResults = 5)

// Get top result
val topResult = results.topResult()
if (topResult != null && topResult.isConfident()) {
    println("Product: ${topResult.productName}")
    println("Confidence: ${topResult.confidence * 100}%")
    println("Category: ${topResult.category}")
}

// Get all confident results
val confidentResults = results.confidentResults()
confidentResults.forEach { result ->
    println("${result.productName}: ${result.confidence * 100}%")
}
```

### Clean Up

```kotlin
classifier.close()
```

## Model Training

### Recommended Approach

1. **Collect Data**: Take photos of products in your store
2. **Label Data**: Annotate images with product names
3. **Train Model**: Use TensorFlow or PyTorch
4. **Convert to TFLite**: Optimize for mobile
5. **Test**: Validate accuracy on real-world images

### Model Requirements

- Input: 224x224 RGB image (configurable)
- Output: Float array of confidence scores
- Format: TensorFlow Lite (.tflite)
- Size: < 10 MB recommended for mobile

### Transfer Learning

For best results, use transfer learning with a pre-trained model:
- MobileNetV2
- EfficientNet-Lite
- NASNet-Mobile

## Performance Optimization

### GPU Acceleration

Uncomment GPU delegate in `TFLiteProductClassifier.kt`:

```kotlin
val options = Interpreter.Options().apply {
    setNumThreads(numThreads)
    addDelegate(GpuDelegate()) // Enable GPU
}
```

### Model Quantization

- Use quantized models (8-bit) for faster inference
- Trade-off: Slight accuracy loss for better performance

### Image Preprocessing

- Resize images to model input size
- Normalize pixel values
- Apply data augmentation during training

## Integration with Duka.AI

### Camera Scanner Screen

The classifier will be integrated with the Camera Scanner screen to:
1. Capture product images
2. Run classification
3. Display top results
4. Allow user to confirm or select alternative

### Quick Sale Screen

- Scan product barcode or take photo
- Auto-populate product details
- Add to cart with one tap

### Product Management

- Add new products by taking photos
- Train custom models with your inventory
- Improve recognition over time

## Troubleshooting

### Model Not Found Error

- Ensure model file is in `assets/` directory
- Check file name matches `modelPath` parameter
- Verify build includes assets in APK

### Low Accuracy

- Collect more training data
- Increase model size/complexity
- Use better lighting in photos
- Apply data augmentation

### Slow Performance

- Enable GPU acceleration
- Use quantized models
- Reduce image resolution
- Decrease `numThreads` if overloading CPU

## Future Enhancements

- [ ] Cloud-based model updates
- [ ] On-device model training
- [ ] Multi-language product labels
- [ ] Price tag OCR
- [ ] Expiry date detection
- [ ] Shelf stock counting
- [ ] Product recommendation engine

## Resources

- [TensorFlow Lite Guide](https://www.tensorflow.org/lite/guide)
- [ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning)
- [Model Maker for TFLite](https://www.tensorflow.org/lite/models/modify/model_maker)
- [TFLite Model Optimization](https://www.tensorflow.org/lite/performance/model_optimization)
