package com.example.dukaai.voice

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for Text-to-Speech voice feedback
 * Supports English, Nyanja, and Bemba languages
 */
@Singleton
class VoiceFeedbackService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private var currentLanguage: VoiceLanguage = VoiceLanguage.ENGLISH
    private var isInitialized = false

    companion object {
        private const val TAG = "VoiceFeedbackService"

        // Utterance IDs for tracking
        private const val UTTERANCE_ID = "duka_ai_voice_feedback"
    }

    /**
     * Initialize TTS engine
     * Returns Flow that emits initialization status
     */
    fun initialize(language: VoiceLanguage = VoiceLanguage.ENGLISH): Flow<TtsInitializationState> = callbackFlow {
        if (isInitialized && currentLanguage == language) {
            trySend(TtsInitializationState.Ready)
            close()
            return@callbackFlow
        }

        currentLanguage = language

        try {
            tts = TextToSpeech(context) { status ->
                when (status) {
                    TextToSpeech.SUCCESS -> {
                        val locale = getLocaleForLanguage(language)
                        val result = tts?.setLanguage(locale)

                        when (result) {
                            TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                                Log.e(TAG, "Language not supported: ${language.displayName}")
                                trySend(TtsInitializationState.Error("Language not supported: ${language.displayName}"))
                                isInitialized = false
                            }
                            else -> {
                                Log.d(TAG, "TTS initialized successfully for ${language.displayName}")
                                tts?.setSpeechRate(0.9f) // Slightly slower for clarity
                                isInitialized = true
                                trySend(TtsInitializationState.Ready)
                            }
                        }
                    }
                    else -> {
                        Log.e(TAG, "TTS initialization failed with status: $status")
                        trySend(TtsInitializationState.Error("TTS initialization failed"))
                        isInitialized = false
                    }
                }
                close()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TTS", e)
            trySend(TtsInitializationState.Error("Failed to initialize: ${e.message}"))
            close()
        }

        awaitClose {
            // Don't cleanup here - let cleanup() handle it
        }
    }

    /**
     * Get locale for voice language
     */
    private fun getLocaleForLanguage(language: VoiceLanguage): Locale {
        return when (language) {
            VoiceLanguage.ENGLISH -> Locale.US
            VoiceLanguage.NYANJA -> Locale("ny", "ZM")  // Nyanja (Chinyanja)
            VoiceLanguage.BEMBA -> Locale("bem", "ZM")  // Bemba (Chibemba)
        }
    }

    /**
     * Speak text with current language
     */
    fun speak(text: String, onComplete: (() -> Unit)? = null) {
        if (!isInitialized) {
            Log.w(TAG, "TTS not initialized, cannot speak")
            onComplete?.invoke()
            return
        }

        try {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    Log.d(TAG, "Speech started: $text")
                }

                override fun onDone(utteranceId: String?) {
                    Log.d(TAG, "Speech completed: $text")
                    onComplete?.invoke()
                }

                override fun onError(utteranceId: String?) {
                    Log.e(TAG, "Speech error for: $text")
                    onComplete?.invoke()
                }
            })

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text", e)
            onComplete?.invoke()
        }
    }

    /**
     * Speak success message for command result
     */
    fun speakSuccess(result: VoiceCommandResult.Success) {
        when (currentLanguage) {
            VoiceLanguage.ENGLISH -> speak(result.message)
            VoiceLanguage.NYANJA -> {
                // Translate success message to Nyanja
                val nyanjaMessage = translateToNyanja(result.message)
                speak(nyanjaMessage)
            }
            VoiceLanguage.BEMBA -> {
                // Translate success message to Bemba
                val bembaMessage = translateToBemba(result.message)
                speak(bembaMessage)
            }
        }
    }

    /**
     * Speak error message
     */
    fun speakError(error: String) {
        val errorMessage = when (currentLanguage) {
            VoiceLanguage.ENGLISH -> "Sorry, $error"
            VoiceLanguage.NYANJA -> "Pepani, $error"
            VoiceLanguage.BEMBA -> "Mwasalafye, $error"
        }
        speak(errorMessage)
    }

    /**
     * Speak command confirmation
     */
    fun speakConfirmation(command: VoiceCommand) {
        val confirmationMessage = when (currentLanguage) {
            VoiceLanguage.ENGLISH -> buildEnglishConfirmation(command)
            VoiceLanguage.NYANJA -> buildNyanjaConfirmation(command)
            VoiceLanguage.BEMBA -> buildBembaConfirmation(command)
        }
        speak(confirmationMessage)
    }

    /**
     * Build English confirmation message
     */
    private fun buildEnglishConfirmation(command: VoiceCommand): String {
        return when (command.type) {
            VoiceCommandType.RECORD_SALE -> {
                val product = command.parameters["product"] as? String ?: "product"
                val quantity = command.parameters["quantity"] as? Int ?: 1
                "Recording sale of $quantity $product"
            }
            VoiceCommandType.ADD_PRODUCT -> {
                val product = command.parameters["product"] as? String ?: "product"
                "Adding new product: $product"
            }
            VoiceCommandType.CHECK_STOCK -> {
                val product = command.parameters["product"] as? String ?: "product"
                "Checking stock for $product"
            }
            VoiceCommandType.RECORD_PAYMENT -> {
                val amount = command.parameters["amount"] as? Double
                if (amount != null) {
                    "Recording payment of $amount kwacha"
                } else {
                    "Recording payment"
                }
            }
            VoiceCommandType.ADD_CUSTOMER -> {
                val name = command.parameters["name"] as? String
                if (name != null) {
                    "Adding customer: $name"
                } else {
                    "Adding new customer"
                }
            }
            VoiceCommandType.VIEW_ANALYTICS -> "Opening analytics"
            VoiceCommandType.LOW_STOCK_ALERT -> "Checking low stock items"
            VoiceCommandType.SEARCH -> {
                val query = command.parameters["query"] as? String ?: "items"
                "Searching for $query"
            }
            VoiceCommandType.NAVIGATE -> "Navigating"
            VoiceCommandType.UNKNOWN -> "I didn't understand that command"
        }
    }

    /**
     * Build Nyanja confirmation message
     */
    private fun buildNyanjaConfirmation(command: VoiceCommand): String {
        return when (command.type) {
            VoiceCommandType.RECORD_SALE -> {
                val product = command.parameters["product"] as? String ?: "katundu"
                val quantity = command.parameters["quantity"] as? Int ?: 1
                "Ndikuikamo kugulitsa kwa $quantity $product"
            }
            VoiceCommandType.ADD_PRODUCT -> {
                val product = command.parameters["product"] as? String ?: "katundu"
                "Ndikuonjezamo katundu: $product"
            }
            VoiceCommandType.CHECK_STOCK -> {
                val product = command.parameters["product"] as? String ?: "katundu"
                "Ndikuonamo katundu kotsala kwa $product"
            }
            VoiceCommandType.RECORD_PAYMENT -> {
                val amount = command.parameters["amount"] as? Double
                if (amount != null) {
                    "Ndikuikamo kulipira kwa ndalama $amount"
                } else {
                    "Ndikuikamo kulipira"
                }
            }
            VoiceCommandType.ADD_CUSTOMER -> {
                val name = command.parameters["name"] as? String
                if (name != null) {
                    "Ndikuonjezamo kasitomala: $name"
                } else {
                    "Ndikuonjezamo kasitomala watsopano"
                }
            }
            VoiceCommandType.VIEW_ANALYTICS -> "Ndikutsegulamo lipoti"
            VoiceCommandType.LOW_STOCK_ALERT -> "Ndikuonamo katundu kachepa"
            VoiceCommandType.SEARCH -> {
                val query = command.parameters["query"] as? String ?: "zinthu"
                "Ndikufunamo $query"
            }
            VoiceCommandType.NAVIGATE -> "Ndikupitamo"
            VoiceCommandType.UNKNOWN -> "Sindinamve bwino mawu anuwo"
        }
    }

    /**
     * Build Bemba confirmation message
     */
    private fun buildBembaConfirmation(command: VoiceCommand): String {
        return when (command.type) {
            VoiceCommandType.RECORD_SALE -> {
                val product = command.parameters["product"] as? String ?: "ifintu"
                val quantity = command.parameters["quantity"] as? Int ?: 1
                "Ndelembapo ukusula kwa $quantity $product"
            }
            VoiceCommandType.ADD_PRODUCT -> {
                val product = command.parameters["product"] as? String ?: "ifintu"
                "Ndeonjezapo ifintu: $product"
            }
            VoiceCommandType.CHECK_STOCK -> {
                val product = command.parameters["product"] as? String ?: "ifintu"
                "Ndemonapo ifintu ifishalafye kwa $product"
            }
            VoiceCommandType.RECORD_PAYMENT -> {
                val amount = command.parameters["amount"] as? Double
                if (amount != null) {
                    "Ndelembapo ukulipisha kwa indalama $amount"
                } else {
                    "Ndelembapo ukulipisha"
                }
            }
            VoiceCommandType.ADD_CUSTOMER -> {
                val name = command.parameters["name"] as? String
                if (name != null) {
                    "Ndeonjezapo kasitoma: $name"
                } else {
                    "Ndeonjezapo kasitoma umupya"
                }
            }
            VoiceCommandType.VIEW_ANALYTICS -> "Ndetungililapo ilyashi"
            VoiceCommandType.LOW_STOCK_ALERT -> "Ndemonapo ifintu fipwa"
            VoiceCommandType.SEARCH -> {
                val query = command.parameters["query"] as? String ?: "ifintu"
                "Ndefwailapo $query"
            }
            VoiceCommandType.NAVIGATE -> "Ndeya"
            VoiceCommandType.UNKNOWN -> "Tanamfwike nomba ameno"
        }
    }

    /**
     * Translate generic success message to Nyanja
     */
    private fun translateToNyanja(message: String): String {
        return when {
            message.contains("success", ignoreCase = true) -> "Kwatheka"
            message.contains("added", ignoreCase = true) -> "Laonjezeka"
            message.contains("recorded", ignoreCase = true) -> "Lalembeka"
            message.contains("updated", ignoreCase = true) -> "Lasinthika"
            message.contains("deleted", ignoreCase = true) -> "Lachotsedwa"
            message.contains("found", ignoreCase = true) -> "Lapezeka"
            else -> message // Return original if no translation
        }
    }

    /**
     * Translate generic success message to Bemba
     */
    private fun translateToBemba(message: String): String {
        return when {
            message.contains("success", ignoreCase = true) -> "Kwabomba"
            message.contains("added", ignoreCase = true) -> "Lyaonjezeka"
            message.contains("recorded", ignoreCase = true) -> "Lyalembeka"
            message.contains("updated", ignoreCase = true) -> "Lyacinjika"
            message.contains("deleted", ignoreCase = true) -> "Lyafyulwa"
            message.contains("found", ignoreCase = true) -> "Lyasangika"
            else -> message // Return original if no translation
        }
    }

    /**
     * Stop speaking
     */
    fun stopSpeaking() {
        try {
            tts?.stop()
            Log.d(TAG, "Speech stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech", e)
        }
    }

    /**
     * Check if currently speaking
     */
    fun isSpeaking(): Boolean {
        return tts?.isSpeaking ?: false
    }

    /**
     * Set speech rate (0.5 = slow, 1.0 = normal, 2.0 = fast)
     */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
    }

    /**
     * Set speech pitch (0.5 = low, 1.0 = normal, 2.0 = high)
     */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
    }

    /**
     * Change language
     */
    fun setLanguage(language: VoiceLanguage) {
        if (currentLanguage == language) return

        currentLanguage = language
        val locale = getLocaleForLanguage(language)
        val result = tts?.setLanguage(locale)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language not supported: ${language.displayName}, falling back to English")
            tts?.setLanguage(Locale.US)
        }
    }

    /**
     * Check if TTS is initialized and ready
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
            Log.d(TAG, "TTS resources cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up TTS", e)
        }
    }
}

/**
 * TTS initialization state
 */
sealed class TtsInitializationState {
    object Ready : TtsInitializationState()
    data class Error(val message: String) : TtsInitializationState()
}
