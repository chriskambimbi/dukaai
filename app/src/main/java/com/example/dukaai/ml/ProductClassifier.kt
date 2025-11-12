package com.example.dukaai.ml

import android.graphics.Bitmap

/**
 * Interface for product classification using ML models
 * Implementations can use TensorFlow Lite, MLKit, or other ML frameworks
 */
interface ProductClassifier {
    /**
     * Classify a product from an image
     * @param bitmap The image containing the product
     * @param maxResults Maximum number of results to return
     * @return Classification results with predictions
     */
    fun classifyProduct(bitmap: Bitmap, maxResults: Int = 5): ClassificationResults

    /**
     * Initialize the classifier and load the ML model
     * Should be called before first use
     */
    fun initialize()

    /**
     * Clean up resources used by the classifier
     * Should be called when the classifier is no longer needed
     */
    fun close()

    /**
     * Check if the classifier is ready to use
     */
    fun isInitialized(): Boolean
}
