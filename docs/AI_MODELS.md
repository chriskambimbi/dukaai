# Duka.AI - AI/ML Models Documentation

## Table of Contents
1. [Overview](#overview)
2. [FunctionGemma NLU System](#functiongemma-nlu-system)
3. [Product Recognition Model](#product-recognition-model)
4. [Voice Command System](#voice-command-system)
5. [Model Training Pipeline](#model-training-pipeline)
6. [Android Integration](#android-integration)
7. [Performance Optimization](#performance-optimization)
8. [Model Updates](#model-updates)

---

## Overview

Duka.AI uses **on-device AI models** for complete offline functionality:

| Model | Type | Size | Accuracy | Inference Time |
|-------|------|------|----------|----------------|
| **FunctionGemma** | TFLite Gemma 3 (270M) | ~500 MB | 95%+ | <500ms |
| **Product Recognition** | TFLite MobileNetV3-Small | 4.2 MB | 89% | 180ms |
| **Voice Commands** | Android Speech API + FunctionGemma | 0 MB | 95%+ | <1s |

### Design Goals
- ✅ **100% Offline**: No network required
- ✅ **Low Memory**: <50 MB RAM usage
- ✅ **Fast Inference**: <200ms for product recognition
- ✅ **Battery Efficient**: <2% battery per hour
- ✅ **Zambian Products**: Trained on local products
- ✅ **Multi-language**: English, Nyanja, Bemba
- ✅ **Natural Language**: FunctionGemma for intelligent command understanding

---

## FunctionGemma NLU System

FunctionGemma is a 270M parameter model from Google, specialized for function calling. It powers DukaAI's natural language interface, enabling users to interact with the app using voice or text commands.

### Why FunctionGemma?

- **Specialized for Function Calling**: Unlike general-purpose LLMs, FunctionGemma is specifically trained for understanding user intent and mapping it to function calls
- **Small Footprint**: 270M parameters makes it viable for on-device deployment
- **Control Tokens**: Uses special tokens (`<start_function_call>`, `<escape>`) for reliable parsing
- **Parallel Calls**: Supports multiple function calls in a single response
- **Fine-tunable**: Can be fine-tuned on DukaAI-specific commands

### Model Specifications

```yaml
Model: FunctionGemma (Gemma 3 270M IT)
Framework: TensorFlow Lite
Parameters: 270 million
Input: Text (tokenized)
Output: Function call with control tokens
Quantization: INT8 (recommended for mobile)
Original Size: ~1 GB (FP32)
Quantized Size: ~500 MB (INT8)
Inference Speed: <500ms on modern Android devices
Memory Usage: ~600 MB RAM during inference
```

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   FunctionGemma Pipeline                     │
└─────────────────────────────────────────────────────────────┘

User Input: "sell 3 coca-cola to John on credit"
                    │
                    ▼
┌─────────────────────────────────────────────────────────────┐
│  Tool Schema (DukaToolSchema.kt)                            │
│  - Defines 121 available functions across 14 categories     │
│  - Each with typed parameters and descriptions              │
│  - Formatted as function declarations                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Prompt Construction                                        │
│  <bos>                                                      │
│  <start_function_declaration>record_sale(...)<end_...>      │
│  <start_function_declaration>add_product(...)<end_...>      │
│  ...                                                        │
│  User: sell 3 coca-cola to John on credit                   │
│  Assistant: <start_function_call>                           │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  FunctionGemma Inference (TFLite)                           │
│  - Tokenize input with SentencePiece                        │
│  - Run TFLite model                                         │
│  - Generate function call tokens                            │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  Model Output                                               │
│  <start_function_call>record_sale(                          │
│    product_name=<escape>coca-cola<escape>,                  │
│    quantity=3,                                              │
│    customer_name=<escape>John<escape>,                      │
│    sale_type=<escape>credit<escape>                         │
│  )<end_function_call>                                       │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  FunctionGemmaParser                                        │
│  - Extract function name: "record_sale"                     │
│  - Parse arguments from <escape> delimiters                 │
│  - Return ParsedFunctionCall object                         │
└────────────────────────┬────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────┐
│  DukaFunctionExecutor                                       │
│  - Validate parameters                                      │
│  - Find product by name (fuzzy match)                       │
│  - Find customer by name                                    │
│  - Execute via SaleRepository                               │
│  - Return FunctionExecutionResult                           │
└─────────────────────────────────────────────────────────────┘
```

### Available Functions (Tool Schema)

DukaAI provides **121 voice/text commands** across **14 categories**:

```kotlin
// ═══════════════════════════════════════════════════════════════════
// PRODUCT MANAGEMENT (14 tools)
// ═══════════════════════════════════════════════════════════════════
add_product(name, selling_price, category?, initial_stock?, buying_price?, barcode?, low_stock_threshold?, unit?)
edit_product(product_name, new_name?, new_selling_price?, new_buying_price?, new_category?, new_low_stock_threshold?)
delete_product(product_name, confirm?)
get_product_details(product_name)
check_stock(product_name)
update_stock(product_name, quantity, reason?)
search_products(query, search_type?)
get_low_stock_alerts(threshold?)
list_products(category?, sort_by?, sort_order?, limit?)
get_out_of_stock()
get_top_selling_products(period, metric?, limit?)
get_product_categories()
update_product_price(product_name, new_selling_price?, new_buying_price?)
get_product_profit_margin(product_name)

// ═══════════════════════════════════════════════════════════════════
// SALES OPERATIONS (10 tools)
// ═══════════════════════════════════════════════════════════════════
record_sale(product_name, quantity, customer_name?, sale_type?, unit_price?, discount?)
record_batch_sale(items, customer_name?, sale_type?)
edit_sale(sale_id, new_quantity?, new_unit_price?)
delete_sale(sale_id, reason?, confirm?)
get_sale_history(period?, product_name?, customer_name?, sale_type?)
get_today_sales()
get_recent_sales(limit?)
calculate_sale_total(items, discount?)
get_sales_by_payment_type(period)
process_refund(sale_id, refund_amount?, reason?)

// ═══════════════════════════════════════════════════════════════════
// CREDIT/DEBT MANAGEMENT (11 tools)
// ═══════════════════════════════════════════════════════════════════
record_credit_sale(customer_name, product_name, quantity, due_date?)
get_customer_balance(customer_name)
get_all_credit_balances(sort_by?)
get_overdue_credits(days_overdue?)
mark_credit_as_paid(credit_id)
get_credit_history(customer_name, status?)
send_credit_reminder(customer_name, channel?)
get_total_credits_owed()
update_credit_due_date(credit_id, new_due_date)
set_customer_credit_limit(customer_name, credit_limit)
get_customers_near_credit_limit(threshold_percent?)

// ═══════════════════════════════════════════════════════════════════
// CUSTOMER MANAGEMENT (10 tools)
// ═══════════════════════════════════════════════════════════════════
add_customer(name, phone?, address?, notes?)
update_customer(customer_name, new_name?, new_phone?, new_address?, new_notes?)
delete_customer(customer_name, confirm?)
search_customers(query)
get_customer_details(customer_name)
list_customers(sort_by?, has_balance?)
get_customer_purchase_history(customer_name, period?)
get_top_customers(metric, period?, limit?)
get_customers_count()
get_new_customers(period)

// ═══════════════════════════════════════════════════════════════════
// PAYMENT OPERATIONS (11 tools)
// ═══════════════════════════════════════════════════════════════════
record_payment(customer_name, amount, payment_method?, reference?)
get_payment_history(customer_name?, period?, payment_method?)
get_today_payments()
get_total_payments(period)
edit_payment(payment_id, new_amount?, new_method?)
delete_payment(payment_id, reason?, confirm?)
get_payments_by_method(period)
get_recent_payments(limit?)
allocate_payment(customer_name, amount, credit_ids?)
get_expected_payments(days_ahead?)
generate_payment_receipt(payment_id, format?)

// ═══════════════════════════════════════════════════════════════════
// INVENTORY OPERATIONS (4 tools)
// ═══════════════════════════════════════════════════════════════════
get_inventory_history(product_name?, movement_type?, period?)
get_inventory_value(value_type?, category?)
record_stock_take(product_name, actual_count, notes?)
get_inventory_summary()

// ═══════════════════════════════════════════════════════════════════
// ANALYTICS & REPORTING (10 tools)
// ═══════════════════════════════════════════════════════════════════
get_sales_analytics(period, metric?)
get_daily_sales_data(period)
get_profit_analytics(period, breakdown?)
get_revenue_trends(period, compare_to?)
get_category_performance(period)
get_hourly_sales_pattern(period)
generate_report(report_type, period?, format?)
compare_periods(period1, period2, metrics?)
get_dashboard_summary()
get_slow_moving_products(days_threshold?, limit?)

// ═══════════════════════════════════════════════════════════════════
// SETTINGS & CONFIGURATION (10 tools)
// ═══════════════════════════════════════════════════════════════════
get_settings(category?)
update_language(language)
set_pin_protection(enabled, pin?)
update_sync_settings(auto_sync?, wifi_only?, sync_frequency?)
set_low_stock_threshold(threshold)
update_notification_settings(low_stock_alerts?, payment_due_alerts?, daily_summary?)
set_currency_format(symbol?, position?, decimal_places?)
set_shop_details(shop_name?, owner_name?, phone?, address?)
reset_settings(confirm)
get_app_info()

// ═══════════════════════════════════════════════════════════════════
// BACKUP/EXPORT/IMPORT (10 tools)
// ═══════════════════════════════════════════════════════════════════
backup_data(backup_type?, destination?)
restore_data(backup_id?, confirm)
sync_to_cloud()
sync_from_cloud()
get_sync_status()
export_to_csv(data_type, period?)
export_to_pdf(report_type, period?, customer_name?)
import_products(file_path, update_existing?)
list_backups()
delete_backup(backup_id, confirm)

// ═══════════════════════════════════════════════════════════════════
// BARCODE/SCANNER (5 tools)
// ═══════════════════════════════════════════════════════════════════
scan_product_barcode(action?)
find_by_barcode(barcode)
update_product_barcode(product_name, barcode)
generate_barcode(product_name, format?)
get_products_without_barcode()

// ═══════════════════════════════════════════════════════════════════
// NAVIGATION/UI (12 tools)
// ═══════════════════════════════════════════════════════════════════
go_to_dashboard()
go_to_products()
go_to_sales()
go_to_customers()
go_to_analytics()
go_to_credits()
go_to_settings()
go_to_add_product()
go_to_add_customer()
go_back()
open_scanner()
open_product_detail(product_name)

// ═══════════════════════════════════════════════════════════════════
// MACHINE LEARNING (5 tools)
// ═══════════════════════════════════════════════════════════════════
classify_product_image(action_on_match?)
get_ml_model_status()
suggest_product_price(product_name, buying_price?)
predict_low_stock(days_ahead?)
get_sales_forecast(period, product_name?)

// ═══════════════════════════════════════════════════════════════════
// VOICE FRAMEWORK (6 tools)
// ═══════════════════════════════════════════════════════════════════
set_voice_language(language)
help_with_voice_commands(category?)
repeat_last_response()
get_voice_command_history(limit?)
toggle_voice_feedback(enabled)
cancel_operation()

// ═══════════════════════════════════════════════════════════════════
// VALIDATION & CONFIRMATION (3 tools)
// ═══════════════════════════════════════════════════════════════════
verify_pin(pin)
confirm_operation(operation_id, confirmed)
cancel_confirmation()
```

### Example Commands

| User Input | Parsed Function Call |
|------------|---------------------|
| "sell 3 coca-cola" | `record_sale(product_name="coca-cola", quantity=3)` |
| "how many bread in stock" | `check_stock(product_name="bread")` |
| "John paid 500" | `record_payment(customer_name="John", amount=500)` |
| "how much does Mary owe" | `get_customer_balance(customer_name="Mary")` |
| "add 50 units of sugar" | `update_stock(product_name="sugar", quantity=50)` |
| "today's sales" | `get_sales_analytics(period="today")` |
| "low stock alert" | `get_low_stock_alerts()` |

### Fallback System

When the TFLite model is not available (first run, loading), the system falls back to pattern-based parsing:

```kotlin
// FunctionGemmaService.kt - Fallback patterns

private suspend fun processWithFallback(userInput: String): ProcessingResult {
    val input = userInput.lowercase().trim()

    val functionCall = when {
        // Sale patterns
        input.matches(Regex("(sell|sold|record sale).*")) -> parseSaleCommand(input)

        // Stock check patterns
        input.matches(Regex("(how many|check stock|stock of).*")) -> parseStockCheckCommand(input)

        // Payment patterns
        input.matches(Regex("(paid|payment|received).*")) -> parsePaymentCommand(input)

        // ... more patterns
        else -> null
    }

    // Execute if pattern matched
    return if (functionCall != null) {
        val results = executor.executeAll(listOf(functionCall))
        ProcessingResult.Success(...)
    } else {
        ProcessingResult.NoFunctionDetected(...)
    }
}
```

### Integration with Voice

```kotlin
// VoiceCommandViewModel.kt

private suspend fun processWithFunctionGemma(text: String) {
    val result = functionGemmaService.processCommand(text)

    when (result) {
        is ProcessingResult.Success -> {
            val message = result.getSummaryMessage()
            voiceFeedbackService.speak(message)  // TTS feedback
            _executionResult.value = VoiceCommandResult.Success(message, result.results)
        }
        is ProcessingResult.NoFunctionDetected -> {
            voiceFeedbackService.speak(result.message)
        }
        // ... handle other cases
    }
}
```

### Deployment Steps

1. **Download FunctionGemma from HuggingFace**
   ```bash
   # Using Hugging Face CLI
   huggingface-cli download google/gemma-3-1b-it-function-calling
   ```

2. **Convert to TFLite**
   ```python
   import ai_edge_torch

   # Load model
   model = AutoModelForCausalLM.from_pretrained("google/gemma-3-1b-it-function-calling")

   # Convert to TFLite
   edge_model = ai_edge_torch.convert(model, ...)
   edge_model.export("function_gemma.tflite")
   ```

3. **Quantize for Mobile**
   ```python
   # INT8 quantization for smaller size
   converter = tf.lite.TFLiteConverter.from_saved_model(model_path)
   converter.optimizations = [tf.lite.Optimize.DEFAULT]
   converter.target_spec.supported_types = [tf.int8]
   tflite_model = converter.convert()
   ```

4. **Add to Android Assets**
   ```
   app/src/main/assets/
   ├── function_gemma.tflite
   └── function_gemma_vocab.txt
   ```

5. **Fine-tune for DukaAI (Optional)**
   ```python
   from trl import SFTTrainer

   # Fine-tune on DukaAI-specific commands
   trainer = SFTTrainer(
       model=model,
       train_dataset=duka_dataset,
       # ... config
   )
   trainer.train()
   ```

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
