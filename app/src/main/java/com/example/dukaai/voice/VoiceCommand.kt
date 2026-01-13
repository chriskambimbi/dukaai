package com.example.dukaai.voice

/**
 * Represents different types of voice commands supported by the app
 */
enum class VoiceCommandType {
    RECORD_SALE,
    ADD_PRODUCT,
    CHECK_STOCK,
    RECORD_PAYMENT,
    ADD_CUSTOMER,
    VIEW_ANALYTICS,
    LOW_STOCK_ALERT,
    SEARCH,
    NAVIGATE,
    UNKNOWN
}

/**
 * Voice command with parsed intent and parameters
 */
data class VoiceCommand(
    val type: VoiceCommandType,
    val originalText: String,
    val language: String,
    val confidence: Float,
    val parameters: Map<String, Any> = emptyMap()
) {
    /**
     * Check if command has sufficient confidence for execution
     */
    fun isConfident(): Boolean = confidence >= CONFIDENCE_THRESHOLD

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.7f // 70% confidence threshold
    }
}

/**
 * Result of voice command execution
 */
sealed class VoiceCommandResult {
    data class Success(val message: String, val data: Any? = null) : VoiceCommandResult()
    data class Failure(val error: String, val reason: String) : VoiceCommandResult()
    data class NeedsConfirmation(val command: VoiceCommand, val prompt: String) : VoiceCommandResult()
}

/**
 * Voice command language
 */
enum class VoiceLanguage(val code: String, val displayName: String) {
    ENGLISH("en-US", "English"),
    NYANJA("ny-ZM", "Nyanja"),  // Chinyanja
    BEMBA("bem-ZM", "Bemba");   // Chibemba

    companion object {
        fun fromCode(code: String): VoiceLanguage {
            return values().find { it.code == code } ?: ENGLISH
        }
    }
}

/**
 * Voice recognition state
 */
sealed class VoiceRecognitionState {
    object Idle : VoiceRecognitionState()
    object Listening : VoiceRecognitionState()
    data class Processing(val partialText: String) : VoiceRecognitionState()
    data class Success(val text: String, val confidence: Float = 0f) : VoiceRecognitionState()
    data class Error(val message: String) : VoiceRecognitionState()
}
