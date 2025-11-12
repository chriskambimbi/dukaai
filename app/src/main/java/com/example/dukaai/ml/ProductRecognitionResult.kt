package com.example.dukaai.ml

/**
 * Result of product recognition from ML classifier
 * Contains product information and confidence score
 */
data class ProductRecognitionResult(
    val productName: String,
    val confidence: Float,
    val category: String? = null,
    val barcode: String? = null
) {
    /**
     * Whether the confidence is high enough for automatic recognition
     */
    fun isConfident(): Boolean = confidence >= CONFIDENCE_THRESHOLD

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.7f // 70% confidence threshold
    }
}

/**
 * Classification result with multiple predictions
 */
data class ClassificationResults(
    val results: List<ProductRecognitionResult>,
    val processingTimeMs: Long
) {
    /**
     * Get the top prediction
     */
    fun topResult(): ProductRecognitionResult? = results.firstOrNull()

    /**
     * Get confident results (above threshold)
     */
    fun confidentResults(): List<ProductRecognitionResult> =
        results.filter { it.isConfident() }
}
