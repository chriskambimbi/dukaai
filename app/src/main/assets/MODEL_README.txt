ML Model Setup Instructions
============================

DukaAI uses two ML models:
1. Product Classifier - For product recognition from images
2. FunctionGemma - For voice command understanding (NLU)

========================================
1. PRODUCT CLASSIFIER
========================================

REQUIRED FILES:
- product_classifier.tflite

STEPS:

1. Train or Obtain a Model
   - Use TensorFlow Model Maker for quick training
   - Or use transfer learning with MobileNetV2
   - Or download a pre-trained product classifier

2. Convert to TensorFlow Lite
   - If you have a TensorFlow/Keras model, convert it:

     converter = tf.lite.TFLiteConverter.from_keras_model(model)
     tflite_model = converter.convert()
     with open('product_classifier.tflite', 'wb') as f:
         f.write(tflite_model)

3. Place Model Here
   - Copy product_classifier.tflite to this directory
   - File path should be: app/src/main/assets/product_classifier.tflite

4. Update Labels (if needed)
   - Edit product_labels.txt to match your model's output classes
   - Format: ProductName|Category|Barcode
   - One product per line

RECOMMENDED MODEL SPECS:
- Input: 224x224 RGB image
- Output: Float array matching number of labels
- Quantized (8-bit) for better performance
- Size: < 10 MB

TESTING:
- Use sample products from your store
- Ensure good lighting when taking photos
- Test with different angles and distances

For more details, see: ml/README.md

========================================
2. FUNCTIONGEMMA (Voice NLU)
========================================

DukaAI supports multiple backends for voice command understanding:

OPTION A: MEDIAPIPE LLM (Recommended)
-------------------------------------
Uses Google's MediaPipe LLM Inference API with Gemma models.

1. Download Gemma Model from Kaggle:
   https://www.kaggle.com/models/google/gemma/tfLite/gemma-2b-it-gpu-int4

   Or use the smaller variant:
   https://www.kaggle.com/models/google/gemma/tfLite/gemma-1.1-2b-it-gpu-int4

2. Place the model file in app's files directory:
   - Copy gemma-2b-it-gpu-int4.bin to the device
   - Use adb: adb push gemma-2b-it-gpu-int4.bin /data/data/com.example.dukaai/files/

3. The app will automatically detect and use MediaPipe when model is present.

OPTION B: TFLITE MODEL
----------------------
REQUIRED FILES:
- function_gemma.tflite    (FunctionGemma model converted to TFLite)
- tokenizer.json           (Tokenizer vocabulary - already included!)

CONVERSION STEPS:
   cd model_conversion
   pip install -r requirements.txt
   python3 convert_functiongemma_to_tflite.py
   cp output/android_assets/function_gemma.tflite app/src/main/assets/

Note: TFLite conversion is complex for LLMs. MediaPipe is recommended.

OPTION C: FALLBACK MODE (Default)
---------------------------------
If no model files are present, DukaAI uses pattern-matching
based fallback for basic voice command understanding.

Supported commands:
- "Sell 3 Coca-Cola"
- "John paid 500"
- "How many Mealie Meal in stock?"
- "Who owes me money?"
- "Sales today"
- "Go home" / "Open inventory"
- And more...

BACKEND PRIORITY:
1. MediaPipe LLM (if model available)
2. TFLite model (if available)
3. Pattern matching fallback (always available)

CURRENT STATUS:
- tokenizer.json: INCLUDED (33MB)
- MediaPipe model: Download from Kaggle
- TFLite model: Not available (use MediaPipe instead)

For detailed instructions, see: model_conversion/README.md
