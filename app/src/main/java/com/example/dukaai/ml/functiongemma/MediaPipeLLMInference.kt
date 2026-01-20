package com.example.dukaai.ml.functiongemma

import android.content.Context
import android.util.Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaPipe LLM Inference implementation for FunctionGemma.
 *
 * This class uses Google's MediaPipe LLM Inference API to run Gemma models
 * on-device for function calling capabilities.
 *
 * Model Setup:
 * 1. Download Gemma model from Kaggle: https://www.kaggle.com/models/google/gemma
 * 2. Choose the 2B-it variant optimized for mobile (gemma-2b-it-gpu-int4.bin)
 * 3. Place in app's files directory or assets
 *
 * Usage:
 * ```kotlin
 * val inference = MediaPipeLLMInference(context)
 * inference.initialize("/path/to/gemma-2b-it-gpu-int4.bin")
 * val result = inference.generateResponse("Sell 3 Coca-Cola")
 * ```
 */
@Singleton
class MediaPipeLLMInference @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "MediaPipeLLMInference"

        // Default model filename (place in app's files directory)
        const val DEFAULT_MODEL_NAME = "gemma-2b-it-gpu-int4.bin"

        // Model configuration
        private const val MAX_TOKENS = 512
        private const val TEMPERATURE = 0.7f
        private const val TOP_K = 40
        private const val TOP_P = 0.95f
        private const val RANDOM_SEED = 42

        // FunctionGemma special tokens
        const val START_FUNCTION_CALL = "<start_function_call>"
        const val END_FUNCTION_CALL = "<end_function_call>"
        const val ESCAPE_TOKEN = "<escape>"
    }

    private var llmInference: LlmInference? = null
    private var isInitialized = false
    private var lastError: String? = null

    /**
     * Initialize the MediaPipe LLM with the specified model path.
     *
     * @param modelPath Full path to the Gemma model file (.bin)
     * @return Result indicating success or failure
     */
    suspend fun initialize(modelPath: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) {
            return@withContext Result.success(Unit)
        }

        try {
            val path = modelPath ?: findModelPath()

            if (path == null) {
                lastError = "Model file not found. Please download Gemma model."
                Log.w(TAG, lastError!!)
                return@withContext Result.failure(
                    MediaPipeException("Model not found. Download from Kaggle and place in app files.")
                )
            }

            Log.d(TAG, "Initializing MediaPipe LLM with model: $path")

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(path)
                .setMaxTokens(MAX_TOKENS)
                .setTemperature(TEMPERATURE)
                .setTopK(TOP_K)
                .setRandomSeed(RANDOM_SEED)
                .build()

            llmInference = LlmInference.createFromOptions(context, options)
            isInitialized = true

            Log.d(TAG, "MediaPipe LLM initialized successfully")
            Result.success(Unit)

        } catch (e: Exception) {
            lastError = e.message
            Log.e(TAG, "Failed to initialize MediaPipe LLM", e)
            Result.failure(MediaPipeException("Failed to initialize: ${e.message}", e))
        }
    }

    /**
     * Find the model file in common locations.
     */
    private fun findModelPath(): String? {
        // Check app's internal files directory
        val filesDir = File(context.filesDir, DEFAULT_MODEL_NAME)
        if (filesDir.exists()) {
            Log.d(TAG, "Found model in internal files: ${filesDir.absolutePath}")
            return filesDir.absolutePath
        }

        // Check external files directory (primary location for large models)
        val externalDir = context.getExternalFilesDir(null)?.let {
            File(it, DEFAULT_MODEL_NAME)
        }
        if (externalDir?.exists() == true) {
            Log.d(TAG, "Found model in external files: ${externalDir.absolutePath}")
            return externalDir.absolutePath
        }

        // Check for any .bin file in internal files directory
        context.filesDir.listFiles()?.find { it.extension == "bin" }?.let {
            Log.d(TAG, "Found .bin model in internal files: ${it.absolutePath}")
            return it.absolutePath
        }

        // Check for any .bin file in external files directory
        context.getExternalFilesDir(null)?.listFiles()?.find { it.extension == "bin" }?.let {
            Log.d(TAG, "Found .bin model in external files: ${it.absolutePath}")
            return it.absolutePath
        }

        Log.d(TAG, "No model file found")
        return null
    }

    /**
     * Generate a function call response for the given user input.
     *
     * @param userInput The user's natural language command
     * @param toolDeclarations Optional tool declarations in FunctionGemma format
     * @return The model's response containing function call(s)
     */
    suspend fun generateFunctionCall(
        userInput: String,
        toolDeclarations: String = ""
    ): Result<String> = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            val initResult = initialize()
            if (initResult.isFailure) {
                return@withContext Result.failure(initResult.exceptionOrNull()!!)
            }
        }

        val inference = llmInference
        if (inference == null) {
            return@withContext Result.failure(MediaPipeException("LLM not initialized"))
        }

        try {
            // Build prompt with function calling format
            val prompt = buildFunctionCallPrompt(userInput, toolDeclarations)
            Log.d(TAG, "Generating response for: $userInput")

            // Generate response
            val response = inference.generateResponse(prompt)

            // Extract function call from response
            val functionCall = extractFunctionCall(response)

            Log.d(TAG, "Generated function call: $functionCall")
            Result.success(functionCall)

        } catch (e: Exception) {
            Log.e(TAG, "Generation failed", e)
            Result.failure(MediaPipeException("Generation failed: ${e.message}", e))
        }
    }

    /**
     * Build a prompt formatted for function calling.
     */
    private fun buildFunctionCallPrompt(userInput: String, toolDeclarations: String): String {
        return buildString {
            // System instruction for function calling
            append("You are a voice assistant for DukaAI, a retail shop management app in Zambia. ")
            append("Convert user commands into function calls. ")
            append("Use the format: $START_FUNCTION_CALL function_name(param=$ESCAPE_TOKEN value$ESCAPE_TOKEN) $END_FUNCTION_CALL\n\n")

            // Add tool declarations if provided
            if (toolDeclarations.isNotEmpty()) {
                append("Available functions:\n")
                append(toolDeclarations)
                append("\n\n")
            }

            // Add examples
            append("Examples:\n")
            append("User: Sell 3 Coca-Cola\n")
            append("Assistant: $START_FUNCTION_CALL record_sale(product_name=${ESCAPE_TOKEN}Coca-Cola$ESCAPE_TOKEN, quantity=3) $END_FUNCTION_CALL\n\n")

            append("User: John paid 500\n")
            append("Assistant: $START_FUNCTION_CALL record_payment(customer_name=${ESCAPE_TOKEN}John$ESCAPE_TOKEN, amount=500) $END_FUNCTION_CALL\n\n")

            // User input
            append("User: $userInput\n")
            append("Assistant: ")
        }
    }

    /**
     * Extract function call from model response.
     */
    private fun extractFunctionCall(response: String): String {
        // Try to find function call tokens
        val startIdx = response.indexOf(START_FUNCTION_CALL)
        val endIdx = response.indexOf(END_FUNCTION_CALL)

        return if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
            response.substring(startIdx, endIdx + END_FUNCTION_CALL.length)
        } else {
            // If no tokens found, wrap the response
            "$START_FUNCTION_CALL $response $END_FUNCTION_CALL"
        }
    }

    /**
     * Generate a response asynchronously.
     *
     * @param prompt The input prompt
     * @return Result with the generated response
     */
    suspend fun generateResponseAsync(
        prompt: String
    ): Result<String> = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            return@withContext Result.failure(MediaPipeException("LLM not initialized"))
        }

        val inference = llmInference
            ?: return@withContext Result.failure(MediaPipeException("LLM not initialized"))

        try {
            // Use synchronous generation wrapped in coroutine
            val response = inference.generateResponse(prompt)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(MediaPipeException("Async generation failed: ${e.message}", e))
        }
    }

    /**
     * Check if the LLM is ready for inference.
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Check if a model file exists.
     */
    fun isModelAvailable(): Boolean = findModelPath() != null

    /**
     * Get the last error message.
     */
    fun getLastError(): String? = lastError

    /**
     * Get model status information.
     */
    fun getStatus(): MediaPipeStatus {
        return MediaPipeStatus(
            isInitialized = isInitialized,
            modelAvailable = isModelAvailable(),
            modelPath = findModelPath(),
            lastError = lastError
        )
    }

    /**
     * Release resources.
     */
    fun close() {
        llmInference?.close()
        llmInference = null
        isInitialized = false
        Log.d(TAG, "MediaPipe LLM resources released")
    }
}

/**
 * Status information for MediaPipe LLM.
 */
data class MediaPipeStatus(
    val isInitialized: Boolean,
    val modelAvailable: Boolean,
    val modelPath: String?,
    val lastError: String?
)

/**
 * Exception for MediaPipe errors.
 */
class MediaPipeException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
