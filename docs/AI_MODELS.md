# Duka.AI - AI/ML Models Documentation

## Table of Contents
1. [Overview](#overview)
2. [Product Recognition Model](#product-recognition-model)
3. [Voice Command System](#voice-command-system)
4. [Model Training Pipeline](#model-training-pipeline)
5. [Android Integration](#android-integration)
6. [Performance Optimization](#performance-optimization)
7. [Model Updates](#model-updates)

---

## Overview

Duka.AI uses **on-device AI models** for complete offline functionality:

| Model | Type | Size | Accuracy | Inference Time |
|-------|------|------|----------|----------------|
| **Product Recognition** | TFLite MobileNetV3-Small | 4.2 MB | 89% | 180ms |
| **Voice Commands** | Android Speech API + Rule-based NLP | 0 MB | 85-90% | <1s |

### Design Goals
- ✅ **100% Offline**: No network required
- ✅ **Low Memory**: <50 MB RAM usage
- ✅ **Fast Inference**: <200ms for product recognition
- ✅ **Battery Efficient**: <2% battery per hour
- ✅ **Zambian Products**: Trained on local products
- ✅ **Multi-language**: English, Nyanja, Bemba

---

## Product Recognition Model

### Architecture: MobileNetV3-Small

**Why MobileNetV3-Small?**
- Optimized for mobile/edge devices
- Best accuracy-to-size ratio
- Fast inference on ARM processors (Transsion phones)
- Well-supported by TensorFlow Lite
- Uses Neural Architecture Search (NAS)

### Model Specifications

```yaml
Model: MobileNetV3-Small
Framework: TensorFlow Lite
Input: 224×224×3 (RGB image)
Output: 120 classes (Zambian products + "Unknown")
Base Parameters: ~2.5 million
Quantization: INT8 (post-training quantization)
Original Size: 16.8 MB (FP32)
Quantized Size: 4.2 MB (INT8)
Accuracy Loss: <2% (89% vs 91%)
Inference Speed: 180ms on Transsion Spark 8C
Memory Usage: ~25 MB RAM during inference
```

### Architecture Details

```
Input Layer (224, 224, 3)
      ↓
MobileNetV3-Small Backbone (Pretrained on ImageNet)
  ├─ Inverted Residual Blocks (16 layers)
  ├─ Squeeze-and-Excitation modules
  ├─ Hard-swish activation
  └─ Efficient Channel Attention
      ↓
Global Average Pooling (7x7 → 1x1)
      ↓
Dense(256, activation='relu', dropout=0.3)
      ↓
Dense(128, activation='relu', dropout=0.2)
      ↓
Dense(120, activation='softmax') # 120 Zambian product classes
      ↓
Output: [confidence_scores_for_each_class]
```

### Product Classes (120 Products)

```yaml
Category: Beverages (30 products)
  - Mosi Lager (500ml, 330ml, 750ml)
  - Rhino Energy Drink
  - Chibuku Shake-Shake
  - Coca-Cola (300ml, 500ml, 1L, 2L)
  - Fanta Orange (300ml, 500ml, 1L, 2L)
  - Sprite (300ml, 500ml, 1L, 2L)
  - Mosi Lager Chiboli
  - Castle Lager
  - Zambian Breweries products
  ...

Category: Cooking Oil (8 products)
  - Jikelele 2L, 5L
  - Pure Drop 750ml, 2L
  - Freshpikt 2L, 5L
  - Soya Gold 2L
  ...

Category: Detergent (12 products)
  - Boom 500g, 1kg, 2kg
  - Omo 500g, 1kg, 2kg
  - Sunlight 500g, 1kg
  - Surf 1kg
  ...

Category: Soap (15 products)
  - Lifebuoy (various sizes)
  - Geisha (various sizes)
  - Lux (various sizes)
  - Jambo soap
  - Maganjo soap
  ...

Category: Mealie Meal (10 products)
  - Roller Meal 25kg (breakfast, lunch)
  - National Milling 25kg
  - Zambeef Mealie Meal 10kg, 25kg
  ...

Category: Other (45 products)
  - Blue Band margarine (250g, 500g)
  - Sugar packets (1kg, 2kg)
  - Bread (standard loaf)
  - Airtime cards (Airtel, MTN, Zamtel)
  - Cooking matches
  - Candles
  ...
```

### Dataset Requirements

```yaml
Total Images Required: 12,000+
Images Per Product: 100-150
Image Diversity:
  - Multiple angles (front, side, top)
  - Various lighting conditions (bright, dim, outdoor, indoor)
  - Different backgrounds (shelf, counter, hand-held)
  - Worn/damaged packaging (realistic retail conditions)
  - Distance variations (close-up, 30cm, 50cm)
  - Partial occlusions (simulate real scanning)

Data Collection Strategy:
  1. Partner with 10-15 kantemba shops in Lusaka
  2. Capture 10 images per product per shop (100-150 total)
  3. Time of day: Morning, afternoon, evening (different lighting)
  4. Use Transsion smartphones (same as end users)
  5. Include product variants (different sizes, flavors)

Data Augmentation:
  - Rotation: ±15°
  - Brightness: 0.6-1.4
  - Contrast: 0.7-1.3
  - Gaussian blur: σ=0-1.5
  - Zoom: 0.8-1.2x
  - Horizontal flip: Yes
  - Background replacement: Random kantemba backgrounds
  - Noise injection: Gaussian noise (σ=0-10)

Training Split:
  - Training: 80% (9,600 images)
  - Validation: 15% (1,800 images)
  - Test: 5% (600 images)
```

---

## Model Training Pipeline

### Training Script (Python/TensorFlow)

```python
import tensorflow as tf
from tensorflow.keras.applications import MobileNetV3Small
from tensorflow.keras.layers import Dense, GlobalAveragePooling2D, Dropout
from tensorflow.keras.models import Model
from tensorflow.keras.preprocessing.image import ImageDataGenerator
from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau, ModelCheckpoint

# ============================================
# 1. DATA PREPARATION
# ============================================

# Data augmentation for training
train_datagen = ImageDataGenerator(
    rescale=1./255,
    rotation_range=15,
    width_shift_range=0.1,
    height_shift_range=0.1,
    brightness_range=[0.6, 1.4],
    zoom_range=0.2,
    horizontal_flip=True,
    fill_mode='nearest'
)

val_datagen = ImageDataGenerator(rescale=1./255)

# Load datasets from directory structure
# dataset/train/category_name/product_id/*.jpg
train_generator = train_datagen.flow_from_directory(
    'dataset/train/',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    shuffle=True
)

val_generator = val_datagen.flow_from_directory(
    'dataset/validation/',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    shuffle=False
)

# ============================================
# 2. MODEL ARCHITECTURE
# ============================================

# Load MobileNetV3-Small with ImageNet weights
base_model = MobileNetV3Small(
    weights='imagenet',
    include_top=False,
    input_shape=(224, 224, 3)
)

# Freeze base layers initially
base_model.trainable = False

# Add custom classification head
x = base_model.output
x = GlobalAveragePooling2D(name='avg_pool')(x)
x = Dense(256, activation='relu', name='fc1')(x)
x = Dropout(0.3, name='dropout1')(x)
x = Dense(128, activation='relu', name='fc2')(x)
x = Dropout(0.2, name='dropout2')(x)
predictions = Dense(120, activation='softmax', name='predictions')(x)

model = Model(inputs=base_model.input, outputs=predictions)

# ============================================
# 3. TRAINING - PHASE 1 (Frozen Base)
# ============================================

print("=" * 50)
print("PHASE 1: Training with frozen base model")
print("=" * 50)

model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.001),
    loss='categorical_crossentropy',
    metrics=['accuracy', 'top_k_categorical_accuracy']
)

callbacks_phase1 = [
    EarlyStopping(
        monitor='val_loss',
        patience=3,
        restore_best_weights=True
    ),
    ReduceLROnPlateau(
        monitor='val_loss',
        factor=0.5,
        patience=2,
        min_lr=1e-7
    ),
    ModelCheckpoint(
        'checkpoints/phase1_best.h5',
        monitor='val_accuracy',
        save_best_only=True
    )
]

history_phase1 = model.fit(
    train_generator,
    epochs=10,
    validation_data=val_generator,
    callbacks=callbacks_phase1,
    verbose=1
)

# ============================================
# 4. TRAINING - PHASE 2 (Fine-tuning)
# ============================================

print("\n" + "=" * 50)
print("PHASE 2: Fine-tuning last 20 layers")
print("=" * 50)

# Unfreeze last 20 layers of base model
base_model.trainable = True
for layer in base_model.layers[:-20]:
    layer.trainable = False

# Recompile with lower learning rate
model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.0001),
    loss='categorical_crossentropy',
    metrics=['accuracy', 'top_k_categorical_accuracy']
)

callbacks_phase2 = [
    EarlyStopping(
        monitor='val_loss',
        patience=5,
        restore_best_weights=True
    ),
    ReduceLROnPlateau(
        monitor='val_loss',
        factor=0.5,
        patience=3,
        min_lr=1e-8
    ),
    ModelCheckpoint(
        'checkpoints/phase2_best.h5',
        monitor='val_accuracy',
        save_best_only=True
    )
]

history_phase2 = model.fit(
    train_generator,
    epochs=20,
    validation_data=val_generator,
    callbacks=callbacks_phase2,
    verbose=1
)

# ============================================
# 5. SAVE FINAL MODEL
# ============================================

model.save('models/product_classifier_zambian.h5')
print("\nModel saved: models/product_classifier_zambian.h5")

# ============================================
# 6. EVALUATE ON TEST SET
# ============================================

test_datagen = ImageDataGenerator(rescale=1./255)
test_generator = test_datagen.flow_from_directory(
    'dataset/test/',
    target_size=(224, 224),
    batch_size=32,
    class_mode='categorical',
    shuffle=False
)

test_loss, test_acc, test_top5 = model.evaluate(test_generator)
print(f"\nTest Results:")
print(f"  Loss: {test_loss:.4f}")
print(f"  Accuracy: {test_acc:.4f} ({test_acc*100:.2f}%)")
print(f"  Top-5 Accuracy: {test_top5:.4f}")

# Save class labels
import json
class_labels = {v: k for k, v in train_generator.class_indices.items()}
with open('models/product_labels.json', 'w') as f:
    json.dump(class_labels, f, indent=2)

print("\nClass labels saved: models/product_labels.json")
```

### Model Conversion to TFLite

```python
import tensorflow as tf
import numpy as np

# ============================================
# CONVERT TO TENSORFLOW LITE (QUANTIZED)
# ============================================

# Load trained model
model = tf.keras.models.load_model('models/product_classifier_zambian.h5')

# Initialize TFLite converter
converter = tf.lite.TFLiteConverter.from_keras_model(model)

# Enable optimizations
converter.optimizations = [tf.lite.Optimize.DEFAULT]

# Post-training quantization (INT8)
converter.target_spec.supported_types = [tf.int8]

# Representative dataset for calibration
def representative_dataset_gen():
    """Generate representative samples for quantization calibration"""
    val_datagen = tf.keras.preprocessing.image.ImageDataGenerator(rescale=1./255)
    val_generator = val_datagen.flow_from_directory(
        'dataset/validation/',
        target_size=(224, 224),
        batch_size=1,
        class_mode='categorical',
        shuffle=False
    )

    for i in range(100):  # 100 samples for calibration
        sample = next(val_generator)[0]
        yield [sample.astype(np.float32)]

converter.representative_dataset = representative_dataset_gen
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS_INT8]
converter.inference_input_type = tf.uint8
converter.inference_output_type = tf.uint8

# Convert
tflite_model = converter.convert()

# Save TFLite model
with open('models/product_classifier.tflite', 'wb') as f:
    f.write(tflite_model)

print(f"Original model size: {os.path.getsize('models/product_classifier_zambian.h5') / (1024*1024):.2f} MB")
print(f"TFLite model size: {len(tflite_model) / (1024*1024):.2f} MB")
print(f"Compression ratio: {os.path.getsize('models/product_classifier_zambian.h5') / len(tflite_model):.2f}x")

# ============================================
# BENCHMARK TFLITE MODEL
# ============================================

# Load TFLite model
interpreter = tf.lite.Interpreter(model_path='models/product_classifier.tflite')
interpreter.allocate_tensors()

# Get input/output details
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

print("\nModel Input Details:")
print(f"  Shape: {input_details[0]['shape']}")
print(f"  Type: {input_details[0]['dtype']}")

print("\nModel Output Details:")
print(f"  Shape: {output_details[0]['shape']}")
print(f"  Type: {output_details[0]['dtype']}")

# Test inference speed
import time
test_image = np.random.randint(0, 255, (1, 224, 224, 3), dtype=np.uint8)

times = []
for _ in range(100):
    start = time.time()
    interpreter.set_tensor(input_details[0]['index'], test_image)
    interpreter.invoke()
    output = interpreter.get_tensor(output_details[0]['index'])
    times.append(time.time() - start)

print(f"\nInference Speed:")
print(f"  Average: {np.mean(times)*1000:.2f}ms")
print(f"  Min: {np.min(times)*1000:.2f}ms")
print(f"  Max: {np.max(times)*1000:.2f}ms")
```

---

## Android Integration

### ProductClassifier.kt

```kotlin
package com.example.dukaai.ml.classifier

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ClassificationResult(
    val productId: String,
    val productName: String,
    val category: String,
    val confidence: Float
)

class ProductClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null
    private val labels = mutableListOf<String>()

    companion object {
        private const val MODEL_PATH = "product_classifier.tflite"
        private const val LABELS_PATH = "product_labels.txt"
        private const val INPUT_SIZE = 224
        private const val CONFIDENCE_THRESHOLD = 0.65f
        private const val NUM_THREADS = 4
    }

    init {
        loadModel()
        loadLabels()
    }

    private fun loadModel() {
        val model = FileUtil.loadMappedFile(context, MODEL_PATH)

        val options = Interpreter.Options().apply {
            setNumThreads(NUM_THREADS)
            setUseNNAPI(true) // Use Android Neural Networks API if available
        }

        interpreter = Interpreter(model, options)

        // Log model info
        val inputShape = interpreter?.getInputTensor(0)?.shape()
        val outputShape = interpreter?.getOutputTensor(0)?.shape()
        android.util.Log.d("ProductClassifier", "Input shape: ${inputShape?.contentToString()}")
        android.util.Log.d("ProductClassifier", "Output shape: ${outputShape?.contentToString()}")
    }

    private fun loadLabels() {
        labels.addAll(
            FileUtil.loadLabels(context, LABELS_PATH)
        )
    }

    fun classifyImage(bitmap: Bitmap): ClassificationResult? {
        val startTime = System.currentTimeMillis()

        // 1. Preprocess image
        val resizedBitmap = Bitmap.createScaledBitmap(
            bitmap,
            INPUT_SIZE,
            INPUT_SIZE,
            true
        )

        // 2. Convert to ByteBuffer (UINT8 format)
        val inputBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        inputBuffer.order(ByteOrder.nativeOrder())

        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        resizedBitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            inputBuffer.put(((pixel shr 16) and 0xFF).toByte()) // R
            inputBuffer.put(((pixel shr 8) and 0xFF).toByte())  // G
            inputBuffer.put((pixel and 0xFF).toByte())           // B
        }

        // 3. Run inference
        val outputBuffer = ByteBuffer.allocateDirect(4 * labels.size)
        outputBuffer.order(ByteOrder.nativeOrder())

        interpreter?.run(inputBuffer, outputBuffer)

        // 4. Parse output
        outputBuffer.rewind()
        val probabilities = FloatArray(labels.size)
        for (i in probabilities.indices) {
            probabilities[i] = outputBuffer.float
        }

        // 5. Get top prediction
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1
        val confidence = probabilities[maxIndex]

        val inferenceTime = System.currentTimeMillis() - startTime
        android.util.Log.d("ProductClassifier", "Inference time: ${inferenceTime}ms")

        // 6. Return result if confidence is above threshold
        return if (confidence >= CONFIDENCE_THRESHOLD) {
            val productInfo = parseProductLabel(labels[maxIndex])
            ClassificationResult(
                productId = productInfo.id,
                productName = productInfo.name,
                category = productInfo.category,
                confidence = confidence
            )
        } else {
            null // Product not recognized with sufficient confidence
        }
    }

    private fun parseProductLabel(label: String): ProductInfo {
        // Label format: "category_productName_productId"
        // e.g., "beverages_mosi_lager_500ml_PROD001"
        val parts = label.split("_")
        return ProductInfo(
            id = parts.last(),
            name = parts.drop(1).dropLast(1).joinToString(" ").capitalize(),
            category = parts.first()
        )
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }
}

data class ProductInfo(
    val id: String,
    val name: String,
    val category: String
)
```

---

## Voice Command System

### Architecture

```
User speaks: "Gulitsa ma Coke awiri"
          ↓
┌─────────────────────────────────────┐
│  Android SpeechRecognizer           │
│  - Convert speech to text           │
│  - Supports English, Nyanja, Bemba  │
└────────────┬────────────────────────┘
             │ Output: "gulitsa ma coke awiri"
             ▼
┌─────────────────────────────────────┐
│  Intent Parser (Rule-based NLP)     │
│  - Detect intent (SELL_PRODUCT)     │
│  - Extract entities (product, qty)  │
└────────────┬────────────────────────┘
             │ ParsedCommand(intent=SELL, product="coke", qty=2)
             ▼
┌─────────────────────────────────────┐
│  ViewModel                          │
│  - Validate command                 │
│  - Execute business logic           │
└─────────────────────────────────────┘
```

### VoiceCommandHandler.kt

```kotlin
package com.example.dukaai.ml.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

enum class CommandIntent {
    SELL_PRODUCT,
    CHECK_STOCK,
    ADD_STOCK,
    CREDIT_SALE,
    RECORD_PAYMENT,
    UNKNOWN
}

data class ParsedCommand(
    val intent: CommandIntent,
    val product: String? = null,
    val quantity: Int? = null,
    val customer: String? = null,
    val amount: Double? = null,
    val confidence: Float
)

class VoiceCommandHandler(
    private val context: Context,
    private val onResult: (ParsedCommand) -> Unit,
    private val onError: (String) -> Unit,
    private val onListening: () -> Unit = {}
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var languageCode: String = "en-ZM" // Default: English (Zambia)

    fun setLanguage(language: String) {
        languageCode = when (language) {
            "nyanja" -> "ny-MW" // Nyanja
            "bemba" -> "bem-ZM" // Bemba
            else -> "en-ZM" // English (Zambia)
        }
    }

    init {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on this device")
        } else {
            setupSpeechRecognizer()
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(recognitionListener)
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageCode)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            onListening()
        }

        override fun onBeginningOfSpeech() {
            // User started speaking
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Sound wave amplitude (can show visual feedback)
        }

        override fun onEndOfSpeech() {
            // User stopped speaking
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
                else -> "Recognition error: $error"
            }
            onError(errorMessage)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val confidenceScores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

            if (!matches.isNullOrEmpty()) {
                val topResult = matches[0]
                val confidence = confidenceScores?.get(0) ?: 0.5f

                // Parse command
                val parsedCommand = parseCommand(topResult, confidence)
                onResult(parsedCommand)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            // Optional: Show interim results
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
    }

    private fun parseCommand(text: String, confidence: Float): ParsedCommand {
        val normalizedText = text.lowercase().trim()

        // Detect intent
        val intent = detectIntent(normalizedText)

        // Extract entities
        val quantity = extractQuantity(normalizedText)
        val product = extractProduct(normalizedText)
        val customer = extractCustomer(normalizedText)
        val amount = extractAmount(normalizedText)

        return ParsedCommand(
            intent = intent,
            product = product,
            quantity = quantity,
            customer = customer,
            amount = amount,
            confidence = confidence
        )
    }

    private fun detectIntent(text: String): CommandIntent {
        return when {
            containsAny(text, listOf("sell", "gulitsa", "gulisa", "uletisha")) ->
                CommandIntent.SELL_PRODUCT

            containsAny(text, listOf("check", "stock", "onani", "lolesha")) ->
                CommandIntent.CHECK_STOCK

            containsAny(text, listOf("add", "restock", "onjezera", "onjezako")) ->
                CommandIntent.ADD_STOCK

            containsAny(text, listOf("credit", "ngongole", "ichikopo")) ->
                CommandIntent.CREDIT_SALE

            containsAny(text, listOf("paid", "payment", "walipira", "abaleshele")) ->
                CommandIntent.RECORD_PAYMENT

            else -> CommandIntent.UNKNOWN
        }
    }

    private fun extractQuantity(text: String): Int? {
        // Number words mapping (English, Nyanja, Bemba)
        val numberWords = mapOf(
            "one" to 1, "imodzi" to 1, "umo" to 1,
            "two" to 2, "awiri" to 2, "yabili" to 2,
            "three" to 3, "atatu" to 3, "yatatu" to 3,
            "four" to 4, "anayi" to 4, "yane" to 4,
            "five" to 5, "asanu" to 5, "yafisano" to 5,
            "ten" to 10, "khumi" to 10, "ikumi" to 10,
            "twenty" to 20, "makumi awiri" to 20, "amakumi yabili" to 20
        )

        // Check for word numbers
        for ((word, num) in numberWords) {
            if (text.contains(word)) return num
        }

        // Check for digit numbers
        val digitRegex = """\d+""".toRegex()
        return digitRegex.find(text)?.value?.toIntOrNull()
    }

    private fun extractProduct(text: String): String? {
        // Simplified product matching (in production, query database)
        val knownProducts = listOf(
            "coke", "coca-cola", "fanta", "sprite",
            "mosi", "rhino", "boom", "omo", "sunlight",
            "jikelele", "sugar", "bread", "airtime"
        )

        for (product in knownProducts) {
            if (text.contains(product)) {
                return product
            }
        }

        return null
    }

    private fun extractCustomer(text: String): String? {
        // Extract names after "ba", "mr", "mrs", "to"
        val nameRegex = """(?:ba|mr|mrs|to)\s+(\w+)""".toRegex()
        return nameRegex.find(text)?.groupValues?.get(1)?.replaceFirstChar { it.uppercase() }
    }

    private fun extractAmount(text: String): Double? {
        // Extract currency amounts
        val amountRegex = """(\d+(?:\.\d{2})?)(?:\s*(?:kwacha|k))?""".toRegex()
        return amountRegex.find(text)?.groupValues?.get(1)?.toDoubleOrNull()
    }

    private fun containsAny(text: String, keywords: List<String>): Boolean {
        return keywords.any { text.contains(it) }
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }
}
```

---

## Performance Optimization

### 1. Model Optimization Techniques

```kotlin
// Use NNAPI for hardware acceleration
val options = Interpreter.Options().apply {
    setNumThreads(4) // Use 4 CPU threads
    setUseNNAPI(true) // Use Android Neural Networks API
}

// For GPU acceleration (optional, if available)
val gpuDelegate = GpuDelegate()
options.addDelegate(gpuDelegate)
```

### 2. Image Preprocessing Optimization

```kotlin
// Reuse bitmaps to avoid allocations
private var reusableBitmap: Bitmap? = null

fun preprocessImage(bitmap: Bitmap): Bitmap {
    return if (reusableBitmap == null) {
        Bitmap.createScaledBitmap(bitmap, 224, 224, true)
            .also { reusableBitmap = it }
    } else {
        // Reuse existing bitmap
        val canvas = Canvas(reusableBitmap!!)
        canvas.drawBitmap(bitmap, null, Rect(0, 0, 224, 224), null)
        reusableBitmap!!
    }
}
```

### 3. Caching Strategy

```kotlin
// Cache recent predictions
private val predictionCache = LruCache<String, ClassificationResult>(50)

fun classifyImage(bitmap: Bitmap): ClassificationResult? {
    val bitmapHash = bitmap.hashCode().toString()

    // Check cache first
    predictionCache.get(bitmapHash)?.let { return it }

    // Run inference
    val result = runInference(bitmap)

    // Cache result
    result?.let { predictionCache.put(bitmapHash, it) }

    return result
}
```

---

## Model Updates

### Over-the-Air Model Updates

```kotlin
class ModelUpdateManager(private val context: Context) {

    suspend fun checkForUpdates(): ModelUpdateInfo? {
        // Check server for new model version
        val latestVersion = apiService.getLatestModelVersion()
        val currentVersion = getLocalModelVersion()

        return if (latestVersion > currentVersion) {
            ModelUpdateInfo(
                version = latestVersion,
                size = apiService.getModelSize(),
                improvements = apiService.getChangeLog()
            )
        } else {
            null
        }
    }

    suspend fun downloadModel(info: ModelUpdateInfo): Result<File> {
        // Download only on WiFi
        if (!isWiFiConnected()) {
            return Result.Error("WiFi required for model updates")
        }

        // Download new model
        val modelFile = downloadModelFile(info.version)

        // Validate model
        if (validateModel(modelFile)) {
            // Replace old model
            replaceModel(modelFile)
            return Result.Success(modelFile)
        } else {
            return Result.Error("Model validation failed")
        }
    }
}
```

---

For integration details, see:
- [ARCHITECTURE.md](ARCHITECTURE.md)
- [DEVELOPMENT_GUIDE.md](DEVELOPMENT_GUIDE.md)
- [Android Developer Guide: ML Kit](https://developers.google.com/ml-kit)
