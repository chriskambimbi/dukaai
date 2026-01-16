# AI Kantemba Manager - Product Recognition Model

## Model Specifications
- **Architecture**: MobileNetV3-Small (Transfer Learning)
- **Input**: 224x224x3 RGB images
- **Output**: 9 product classes
- **Size**: 1.34 MB (INT8 quantized)
- **Format**: TensorFlow Lite

## Performance Metrics
- **Test Accuracy**: 20.93%
- **Top-3 Accuracy**: 55.81%
- **Precision**: 0.0000
- **Recall**: 0.0000
- **Inference Time**: 180.0ms (CPU average)

## Product Classes (9)
1. chibuku
2. coca_cola
3. fanta
4. mealie_meal
5. monster_energy
6. mosi_lager
7. pepsi
8. sprite
9. white_sugar

## Training Details
- **Training Time**: 4.2 minutes
- **Total Epochs**: 30
- **Dataset Size**: 344 images
- **Training Samples**: 237
- **Validation Samples**: 64
- **Test Samples**: 43

## Android Integration (Kotlin)
```kotlin
val tfliteModel = loadModelFile("kantemba_product_classifier.tflite")
val interpreter = Interpreter(tfliteModel)

val inputBuffer = preprocessImage(bitmap)
val outputBuffer = Array(1) { FloatArray(9) }
interpreter.run(inputBuffer, outputBuffer)

val predictedClass = outputBuffer[0].indices.maxByOrNull { outputBuffer[0][it] }
val confidence = outputBuffer[0][predictedClass]
```

## Generated
2025-11-13 09:07:19 UTC
