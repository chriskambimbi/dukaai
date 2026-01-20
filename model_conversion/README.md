# FunctionGemma Model Conversion

This directory contains scripts to convert Google's FunctionGemma model to TFLite format for Android deployment.

## Prerequisites

1. Python 3.8+
2. 16GB+ RAM recommended
3. CUDA (optional, for faster conversion)

## Setup

```bash
cd model_conversion
pip install -r requirements.txt
```

## Converting the Model

### Basic Conversion

```bash
python3 convert_functiongemma_to_tflite.py
```

This will:
1. Download `google/functiongemma-270m-it` from HuggingFace
2. Export the tokenizer
3. Convert to ONNX format
4. Convert ONNX to TFLite
5. Apply INT8 quantization
6. Output files to `./output/`

### Custom Output Directory

```bash
python3 convert_functiongemma_to_tflite.py --output-dir /path/to/output
```

## Output Files

After conversion, you'll find:

```
output/
├── function_gemma.tflite          # Quantized model (~70-135MB)
├── function_gemma_fp32.tflite     # Full precision model (~540MB)
├── tokenizer.json                 # Tokenizer vocabulary
├── special_tokens_map.json        # Special token mappings
└── android_assets/                # Ready for Android
    ├── function_gemma.tflite
    └── tokenizer.json
```

## Android Integration

### 1. Copy Assets

Copy the files from `output/android_assets/` to your Android project:

```bash
cp output/android_assets/* ../app/src/main/assets/
```

Or use the provided script:
```bash
cp output/android_assets/function_gemma.tflite ../app/src/main/assets/
cp output/android_assets/tokenizer.json ../app/src/main/assets/
```

### 2. Build Configuration

The `build.gradle.kts` should include:

```kotlin
android {
    // Don't compress TFLite models
    androidResources {
        noCompress += listOf("tflite")
    }
}

dependencies {
    implementation("org.tensorflow:tensorflow-lite:2.14.0")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
}
```

### 3. Usage in Kotlin

```kotlin
val inference = FunctionGemmaInference(context)
inference.initialize()

val result = inference.generateFunctionCall("Sell 3 Coca-Cola")
// Result: <start_function_call> record_sale(product_name=<escape>Coca-Cola<escape>, quantity=3) <end_function_call>
```

## Troubleshooting

### Out of Memory

If you get OOM errors during conversion:
- Use a machine with more RAM (16GB+)
- Try the float16 conversion instead of full precision
- Close other applications

### ONNX Export Fails

If ONNX export fails:
- Try updating optimum: `pip install --upgrade optimum`
- Use the manual export method in the script

### TFLite Conversion Fails

If TFLite conversion fails:
- Ensure TensorFlow is correctly installed
- Try without quantization first
- Check ONNX model validity with `onnx.checker.check_model()`

## Model Size

| Format | Size |
|--------|------|
| PyTorch (original) | ~1.1GB |
| ONNX | ~540MB |
| TFLite (FP32) | ~540MB |
| TFLite (INT8) | ~70-135MB |

## Alternative: MediaPipe

For a simpler integration, consider using MediaPipe LLM Inference API:

```kotlin
dependencies {
    implementation("com.google.mediapipe:tasks-genai:0.10.14")
}
```

See [MediaPipe LLM documentation](https://developers.google.com/mediapipe/solutions/genai/llm_inference) for details.
