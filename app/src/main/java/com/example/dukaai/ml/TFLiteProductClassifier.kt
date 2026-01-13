package com.example.dukaai.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.MappedByteBuffer
import kotlin.math.min

/**
 * TensorFlow Lite implementation of ProductClassifier
 * Uses a pre-trained TFLite model to classify products
 */
class TFLiteProductClassifier(
    private val context: Context,
    private val modelPath: String = "product_classifier.tflite",
    private val labelsPath: String = "product_labels.txt",
    private val numThreads: Int = 4
) : ProductClassifier {

    private var interpreter: Interpreter? = null
    private var labels: List<String> = emptyList()
    private var imageProcessor: ImageProcessor? = null

    private var inputImageWidth: Int = 224
    private var inputImageHeight: Int = 224
    private var isModelLoaded = false

    companion object {
        private const val TAG = "TFLiteProductClassifier"
        private const val MEAN = 127.5f
        private const val STD = 127.5f
    }

    override suspend fun initialize() {
        withContext(Dispatchers.IO) {
            try {
                // Load the TFLite model
                val modelFile: MappedByteBuffer = FileUtil.loadMappedFile(context, modelPath)
                val options = Interpreter.Options().apply {
                    setNumThreads(numThreads)
                    // Use GPU delegate if available for better performance
                    // addDelegate(GpuDelegate())
                }
                interpreter = Interpreter(modelFile, options)

                // Get input tensor dimensions
                val inputShape = interpreter?.getInputTensor(0)?.shape()
                if (inputShape != null && inputShape.size >= 2) {
                    inputImageHeight = inputShape[1]
                    inputImageWidth = inputShape[2]
                }

                // Load labels
                labels = loadLabels(labelsPath)

                // Initialize image processor
                imageProcessor = ImageProcessor.Builder()
                    .add(ResizeWithCropOrPadOp(inputImageHeight, inputImageWidth))
                    .add(ResizeOp(inputImageHeight, inputImageWidth, ResizeOp.ResizeMethod.BILINEAR))
                    .add(NormalizeOp(MEAN, STD))
                    .build()

                isModelLoaded = true
                Log.d(TAG, "Model initialized successfully. Input size: ${inputImageWidth}x${inputImageHeight}")
                Log.d(TAG, "Loaded ${labels.size} labels")
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing model", e)
                isModelLoaded = false
            }
        }
    }

    override suspend fun classifyProduct(bitmap: Bitmap, maxResults: Int): ClassificationResults = withContext(Dispatchers.Default) {
        if (!isModelLoaded) {
            initialize() // Lazy initialization
            if (!isModelLoaded) { // Check again after attempting to initialize
                Log.w(TAG, "Model not initialized. Returning empty results.")
                return@withContext ClassificationResults(emptyList(), 0)
            }
        }

        val startTime = System.currentTimeMillis()

        try {
            // Preprocess image
            var tensorImage = TensorImage.fromBitmap(bitmap)
            tensorImage = imageProcessor?.process(tensorImage) ?: tensorImage

            // Run inference
            val outputArray = Array(1) { FloatArray(labels.size) }
            interpreter?.run(tensorImage.buffer, outputArray)

            // Process results
            val results = processOutputArray(outputArray[0], maxResults)
            val processingTime = System.currentTimeMillis() - startTime

            return@withContext ClassificationResults(results, processingTime)
        } catch (e: Exception) {
            Log.e(TAG, "Error during classification", e)
            return@withContext ClassificationResults(emptyList(), System.currentTimeMillis() - startTime)
        }
    }

    override fun close() {
        interpreter?.close()
        interpreter = null
        isModelLoaded = false
        Log.d(TAG, "Classifier closed")
    }

    override fun isInitialized(): Boolean = isModelLoaded

    /**
     * Load labels from assets
     */
    private fun loadLabels(path: String): List<String> {
        val labels = mutableListOf<String>()
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open(path)))
            var line = reader.readLine()
            while (line != null) {
                labels.add(line)
                line = reader.readLine()
            }
            reader.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error loading labels", e)
            // Return default labels for demo purposes
            return getDefaultLabels()
        }
        return labels
    }

    /**
     * Process output array and return top results
     */
    private fun processOutputArray(output: FloatArray, maxResults: Int): List<ProductRecognitionResult> {
        // Create list of (index, confidence) pairs
        val results = output.mapIndexed { index, confidence ->
            Pair(index, confidence)
        }

        // Sort by confidence descending and take top results
        val topResults = results.sortedByDescending { it.second }
            .take(min(maxResults, output.size))

        // Convert to ProductRecognitionResult
        return topResults.map { (index, confidence) ->
            val labelParts = labels.getOrNull(index)?.split("|") ?: listOf("Unknown")
            ProductRecognitionResult(
                productName = labelParts.getOrNull(0) ?: "Unknown",
                confidence = confidence,
                category = labelParts.getOrNull(1),
                barcode = labelParts.getOrNull(2)
            )
        }
    }

    /**
     * Get default labels for demo/development
     * In production, these should be loaded from the labels file
     */
    private fun getDefaultLabels(): List<String> {
        return listOf(
            "Coca-Cola 500ml|Beverages|5449000000996",
            "Fanta Orange 500ml|Beverages|5449000011152",
            "Sprite 500ml|Beverages|5449000000439",
            "Nshima Meal|Food|NSHIMA001",
            "Bread Loaf|Food|BREAD001",
            "Cooking Oil 2L|Food|OIL001",
            "Rice 2kg|Food|RICE001",
            "Sugar 2kg|Food|SUGAR001",
            "Salt 500g|Food|SALT001",
            "Soap Bar|Household|SOAP001",
            "Detergent 1kg|Household|DETERGENT001",
            "Airtime K10|Airtime|AIRTIME10",
            "Airtime K20|Airtime|AIRTIME20",
            "Airtime K50|Airtime|AIRTIME50",
            "Batteries AA|Household|BATTERY001"
        )
    }
}
