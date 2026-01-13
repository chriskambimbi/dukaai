package com.example.dukaai.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for voice recognition using Android SpeechRecognizer
 * Supports multi-language voice commands
 */
@Singleton
class VoiceCommandService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private var currentLanguage: VoiceLanguage = VoiceLanguage.ENGLISH

    companion object {
        private const val TAG = "VoiceCommandService"
    }

    /**
     * Check if speech recognition is available on this device
     */
    fun isAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Set the language for voice recognition
     */
    fun setLanguage(language: VoiceLanguage) {
        currentLanguage = language
        Log.d(TAG, "Language set to: ${language.displayName}")
    }

    /**
     * Start listening for voice commands
     * Returns a Flow that emits recognition state changes
     */
    fun startListening(): Flow<VoiceRecognitionState> = callbackFlow {
        if (!isAvailable()) {
            trySend(VoiceRecognitionState.Error("Speech recognition not available"))
            close()
            return@callbackFlow
        }

        // Initialize speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d(TAG, "Ready for speech")
                trySend(VoiceRecognitionState.Listening)
            }

            override fun onBeginningOfSpeech() {
                Log.d(TAG, "Speech started")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level changed - could be used for visual feedback
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Audio buffer received
            }

            override fun onEndOfSpeech() {
                Log.d(TAG, "Speech ended")
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No speech match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error"
                }
                Log.e(TAG, "Recognition error: $errorMessage (code: $error)")
                trySend(VoiceRecognitionState.Error(errorMessage))
                close()
            }

            override fun onResults(results: Bundle?) {
                results?.let {
                    val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val confidenceScores = it.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

                    if (!matches.isNullOrEmpty()) {
                        val bestMatch = matches[0]
                        val bestConfidence = confidenceScores?.getOrNull(0) ?: 0f

                        Log.d(TAG, "Recognition result: $bestMatch (confidence: $bestConfidence)")
                        trySend(VoiceRecognitionState.Success(bestMatch, bestConfidence))
                    } else {
                        trySend(VoiceRecognitionState.Error("No results"))
                    }
                }
                close()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.let {
                    val matches = it.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (!matches.isNullOrEmpty()) {
                        val partialText = matches[0]
                        Log.d(TAG, "Partial result: $partialText")
                        trySend(VoiceRecognitionState.Processing(partialText))
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Reserved for future use
            }
        }

        speechRecognizer?.setRecognitionListener(recognitionListener)

        // Create intent for speech recognition
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage.code)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }

        // Start recognition
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech recognition", e)
            trySend(VoiceRecognitionState.Error("Failed to start: ${e.message}"))
            close()
        }

        // Clean up when flow is cancelled
        awaitClose {
            stopListening()
        }
    }

    /**
     * Stop listening and clean up resources
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
            Log.d(TAG, "Speech recognition stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping speech recognition", e)
        }
    }

    /**
     * Check if currently listening
     */
    fun isListening(): Boolean {
        return speechRecognizer != null
    }

    /**
     * Clean up resources
     */
    fun cleanup() {
        stopListening()
    }
}
