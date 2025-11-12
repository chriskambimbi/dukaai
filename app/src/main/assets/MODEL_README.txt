ML Model Setup Instructions
============================

To enable product recognition, you need to add a TensorFlow Lite model file.

REQUIRED FILE:
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
