package com.example.dukaai.ml.functiongemma

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.gpu.GpuDelegate
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import javax.inject.Inject
import javax.inject.Singleton

/**
 * FunctionGemma TFLite inference engine for on-device function calling.
 *
 * FunctionGemma is a 270M parameter model specialized for function calling.
 * This engine handles model loading, tokenization, and inference.
 *
 * Model: google/functiongemma-270m-it converted to TFLite
 * Tokenizer: SentencePiece-based tokenizer exported as JSON vocabulary
 *
 * Usage:
 * 1. Place function_gemma.tflite in assets/
 * 2. Place tokenizer.json in assets/
 * 3. Call initialize() before first use
 * 4. Call generateFunctionCall() with user input
 */
/**
 * Inference backend types for FunctionGemma.
 */
enum class InferenceBackend {
    MEDIAPIPE,      // MediaPipe LLM Inference (recommended)
    TFLITE,         // Direct TFLite model
    FALLBACK        // Pattern matching fallback
}

@Singleton
class FunctionGemmaInference @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "FunctionGemmaInference"
        private const val MODEL_FILE = "function_gemma.tflite"
        private const val TOKENIZER_FILE = "tokenizer.json"
        private const val VOCAB_FILE = "function_gemma_vocab.txt"

        // Model configuration (FunctionGemma-270M)
        private const val MAX_INPUT_LENGTH = 512
        private const val MAX_OUTPUT_LENGTH = 256
        private const val VOCAB_SIZE = 256000  // Gemma vocabulary size

        // Special tokens for FunctionGemma
        const val BOS_TOKEN = "<bos>"
        const val EOS_TOKEN = "<eos>"
        const val START_FUNCTION_CALL = "<start_function_call>"
        const val END_FUNCTION_CALL = "<end_function_call>"
        const val START_FUNCTION_DECLARATION = "<start_function_declaration>"
        const val END_FUNCTION_DECLARATION = "<end_function_declaration>"
        const val START_FUNCTION_RESPONSE = "<start_function_response>"
        const val END_FUNCTION_RESPONSE = "<end_function_response>"
        const val ESCAPE_TOKEN = "<escape>"
        const val PAD_TOKEN = "<pad>"
        const val UNK_TOKEN = "<unk>"
    }

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var isInitialized = false

    // MediaPipe LLM inference (preferred backend)
    private var mediaPipeInference: MediaPipeLLMInference? = null
    private var activeBackend: InferenceBackend = InferenceBackend.FALLBACK

    // Tokenizer data structures
    private var vocabulary: Map<String, Int> = emptyMap()
    private var reverseVocabulary: Map<Int, String> = emptyMap()
    private var merges: List<Pair<String, String>> = emptyList()
    private var tokenizerLoaded = false

    // Special token IDs (will be loaded from tokenizer)
    private var bosTokenId: Int = 2
    private var eosTokenId: Int = 1
    private var padTokenId: Int = 0
    private var unkTokenId: Int = 3
    private var startFunctionCallId: Int = 0
    private var endFunctionCallId: Int = 0
    private var escapeTokenId: Int = 0

    // Model status
    private var modelAvailable = false
    private var lastError: String? = null

    /**
     * Initialize the FunctionGemma model.
     *
     * Tries backends in order of preference:
     * 1. MediaPipe LLM (if model file available)
     * 2. TFLite model (if model file available)
     * 3. Fallback pattern matching (always available)
     *
     * @return Result indicating success or failure with error details
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) {
            return@withContext Result.success(Unit)
        }

        try {
            Log.d(TAG, "Initializing FunctionGemma...")

            // Load tokenizer first
            loadTokenizer()

            // Try MediaPipe first (preferred for LLM inference)
            if (tryInitializeMediaPipe()) {
                activeBackend = InferenceBackend.MEDIAPIPE
                Log.d(TAG, "Using MediaPipe LLM backend")
                isInitialized = true
                return@withContext Result.success(Unit)
            }

            // Try TFLite model
            if (checkModelExists()) {
                val modelBuffer = loadModelFile()
                val options = createInterpreterOptions()
                interpreter = Interpreter(modelBuffer, options)
                modelAvailable = true
                activeBackend = InferenceBackend.TFLITE
                Log.d(TAG, "Using TFLite backend")
                isInitialized = true
                return@withContext Result.success(Unit)
            }

            // Fall back to pattern matching
            Log.w(TAG, "No ML model available, using fallback pattern matching")
            activeBackend = InferenceBackend.FALLBACK
            modelAvailable = false
            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            lastError = e.message
            Log.e(TAG, "Failed to initialize FunctionGemma", e)
            Result.failure(FunctionGemmaException("Failed to initialize FunctionGemma: ${e.message}", e))
        }
    }

    /**
     * Try to initialize MediaPipe LLM backend.
     */
    private suspend fun tryInitializeMediaPipe(): Boolean {
        return try {
            val mpInference = MediaPipeLLMInference(context)
            if (mpInference.isModelAvailable()) {
                val result = mpInference.initialize()
                if (result.isSuccess) {
                    mediaPipeInference = mpInference
                    true
                } else {
                    Log.d(TAG, "MediaPipe initialization failed: ${result.exceptionOrNull()?.message}")
                    false
                }
            } else {
                Log.d(TAG, "MediaPipe model not available")
                false
            }
        } catch (e: Exception) {
            Log.d(TAG, "MediaPipe not available: ${e.message}")
            false
        }
    }

    /**
     * Check if the model file exists in assets
     */
    private fun checkModelExists(): Boolean {
        return try {
            context.assets.open(MODEL_FILE).use { true }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Load tokenizer from JSON file or fallback to vocabulary file
     */
    private fun loadTokenizer() {
        // Try to load tokenizer.json first (exported by conversion script)
        if (tryLoadTokenizerJson()) {
            tokenizerLoaded = true
            Log.d(TAG, "Tokenizer loaded from tokenizer.json")
            return
        }

        // Fallback to vocabulary file
        if (tryLoadVocabularyFile()) {
            tokenizerLoaded = true
            Log.d(TAG, "Vocabulary loaded from $VOCAB_FILE")
            return
        }

        // Use default vocabulary for development
        Log.w(TAG, "No tokenizer found, using default vocabulary")
        vocabulary = createDefaultVocabulary()
        reverseVocabulary = vocabulary.entries.associate { it.value to it.key }
        initializeSpecialTokenIds()
    }

    /**
     * Try to load tokenizer from JSON file
     */
    private fun tryLoadTokenizerJson(): Boolean {
        return try {
            val jsonString = context.assets.open(TOKENIZER_FILE).bufferedReader().readText()
            val json = JSONObject(jsonString)

            // Load vocabulary
            val vocabJson = json.optJSONObject("vocab")
            if (vocabJson != null) {
                val vocabMap = mutableMapOf<String, Int>()
                vocabJson.keys().forEach { key ->
                    vocabMap[key] = vocabJson.getInt(key)
                }
                vocabulary = vocabMap
            }

            // Load merges (for BPE tokenization)
            val mergesJson = json.optJSONArray("merges")
            if (mergesJson != null) {
                val mergesList = mutableListOf<Pair<String, String>>()
                for (i in 0 until mergesJson.length()) {
                    val merge = mergesJson.getString(i)
                    val parts = merge.split(" ")
                    if (parts.size == 2) {
                        mergesList.add(Pair(parts[0], parts[1]))
                    }
                }
                merges = mergesList
            }

            // Load special tokens
            val addedTokens = json.optJSONArray("added_tokens")
            if (addedTokens != null) {
                for (i in 0 until addedTokens.length()) {
                    val token = addedTokens.getJSONObject(i)
                    val content = token.getString("content")
                    val id = token.getInt("id")
                    vocabulary = vocabulary + (content to id)
                }
            }

            reverseVocabulary = vocabulary.entries.associate { it.value to it.key }
            initializeSpecialTokenIds()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Could not load tokenizer.json: ${e.message}")
            false
        }
    }

    /**
     * Try to load vocabulary from plain text file
     */
    private fun tryLoadVocabularyFile(): Boolean {
        return try {
            context.assets.open(VOCAB_FILE).bufferedReader().useLines { lines ->
                vocabulary = lines.mapIndexed { index, token ->
                    token to index
                }.toMap()
            }
            reverseVocabulary = vocabulary.entries.associate { it.value to it.key }
            initializeSpecialTokenIds()
            true
        } catch (e: Exception) {
            Log.d(TAG, "Could not load $VOCAB_FILE: ${e.message}")
            false
        }
    }

    /**
     * Initialize special token IDs from vocabulary
     */
    private fun initializeSpecialTokenIds() {
        vocabulary[BOS_TOKEN]?.let { bosTokenId = it }
        vocabulary[EOS_TOKEN]?.let { eosTokenId = it }
        vocabulary[PAD_TOKEN]?.let { padTokenId = it }
        vocabulary[UNK_TOKEN]?.let { unkTokenId = it }
        vocabulary[START_FUNCTION_CALL]?.let { startFunctionCallId = it }
        vocabulary[END_FUNCTION_CALL]?.let { endFunctionCallId = it }
        vocabulary[ESCAPE_TOKEN]?.let { escapeTokenId = it }
    }

    /**
     * Create default vocabulary for testing/development
     */
    private fun createDefaultVocabulary(): Map<String, Int> {
        return mapOf(
            PAD_TOKEN to 0,
            EOS_TOKEN to 1,
            BOS_TOKEN to 2,
            UNK_TOKEN to 3,
            START_FUNCTION_DECLARATION to 4,
            END_FUNCTION_DECLARATION to 5,
            START_FUNCTION_CALL to 6,
            END_FUNCTION_CALL to 7,
            START_FUNCTION_RESPONSE to 8,
            END_FUNCTION_RESPONSE to 9,
            ESCAPE_TOKEN to 10
        )
    }

    /**
     * Load model file from assets
     */
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(MODEL_FILE)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    /**
     * Create interpreter options with GPU acceleration if available
     */
    private fun createInterpreterOptions(): Interpreter.Options {
        val options = Interpreter.Options()

        // Try to use GPU delegate if available
        try {
            val compatList = CompatibilityList()
            if (compatList.isDelegateSupportedOnThisDevice) {
                gpuDelegate = GpuDelegate()
                options.addDelegate(gpuDelegate)
            }
        } catch (e: Exception) {
            // GPU delegate not available, continue with CPU
        }

        // Set number of threads
        options.setNumThreads(4)

        return options
    }

    /**
     * Run inference to generate function calls from user input.
     *
     * Uses the best available backend:
     * 1. MediaPipe LLM (if initialized)
     * 2. TFLite model (if available)
     * 3. Fallback pattern matching
     *
     * @param userInput The user's natural language input
     * @param toolDeclarations The available tools in FunctionGemma format
     * @return The model's output containing function call(s)
     */
    suspend fun generateFunctionCall(
        userInput: String,
        toolDeclarations: String = DukaToolSchema.toFunctionGemmaFormat()
    ): Result<String> = withContext(Dispatchers.Default) {
        if (!isInitialized) {
            initialize()
        }

        try {
            Log.d(TAG, "Generating function call using backend: $activeBackend")

            // Use the active backend
            return@withContext when (activeBackend) {
                InferenceBackend.MEDIAPIPE -> {
                    // Use MediaPipe LLM
                    val mpInference = mediaPipeInference
                    if (mpInference != null) {
                        mpInference.generateFunctionCall(userInput, toolDeclarations)
                    } else {
                        Log.w(TAG, "MediaPipe not available, falling back")
                        Result.success(fallbackInference(userInput))
                    }
                }

                InferenceBackend.TFLITE -> {
                    // Use TFLite model
                    val prompt = buildPrompt(userInput, toolDeclarations)
                    Log.d(TAG, "Prompt length: ${prompt.length} characters")

                    val inputIds = tokenize(prompt)
                    Log.d(TAG, "Tokenized to ${inputIds.size} tokens")

                    val outputIds = runInference(inputIds)
                    val output = decode(outputIds)
                    Log.d(TAG, "Generated output: $output")
                    Result.success(output)
                }

                InferenceBackend.FALLBACK -> {
                    Log.d(TAG, "Using fallback pattern matching")
                    Result.success(fallbackInference(userInput))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed", e)
            // Try fallback on error
            try {
                Result.success(fallbackInference(userInput))
            } catch (fallbackError: Exception) {
                Result.failure(FunctionGemmaException("Inference failed: ${e.message}", e))
            }
        }
    }

    /**
     * Get the currently active inference backend.
     */
    fun getActiveBackend(): InferenceBackend = activeBackend

    /**
     * Fallback inference when model is not available
     * Uses pattern matching to generate function calls
     */
    private fun fallbackInference(userInput: String): String {
        val input = userInput.lowercase().trim()

        // Pattern matching for common commands
        return when {
            // Sales
            input.matches(Regex("sell\\s+(\\d+)\\s+(.+)")) -> {
                val match = Regex("sell\\s+(\\d+)\\s+(.+)").find(input)!!
                val quantity = match.groupValues[1]
                val product = match.groupValues[2].trim()
                "$START_FUNCTION_CALL record_sale(product_name=$ESCAPE_TOKEN$product$ESCAPE_TOKEN, quantity=$quantity) $END_FUNCTION_CALL"
            }
            input.matches(Regex("sold\\s+(\\d+)\\s+(.+)")) -> {
                val match = Regex("sold\\s+(\\d+)\\s+(.+)").find(input)!!
                val quantity = match.groupValues[1]
                val product = match.groupValues[2].trim()
                "$START_FUNCTION_CALL record_sale(product_name=$ESCAPE_TOKEN$product$ESCAPE_TOKEN, quantity=$quantity) $END_FUNCTION_CALL"
            }

            // Credit sales (pa ng'ong'ole)
            input.contains("ng'ong'ole") || input.contains("credit") -> {
                val nameMatch = Regex("(\\w+)\\s+bought").find(input)
                val quantityMatch = Regex("(\\d+)\\s+(\\w+)").find(input)
                if (nameMatch != null && quantityMatch != null) {
                    val customer = nameMatch.groupValues[1]
                    val quantity = quantityMatch.groupValues[1]
                    val product = quantityMatch.groupValues[2]
                    "$START_FUNCTION_CALL record_credit_sale(customer_name=$ESCAPE_TOKEN$customer$ESCAPE_TOKEN, product_name=$ESCAPE_TOKEN$product$ESCAPE_TOKEN, quantity=$quantity) $END_FUNCTION_CALL"
                } else {
                    "$START_FUNCTION_CALL record_credit_sale() $END_FUNCTION_CALL"
                }
            }

            // Payments
            input.matches(Regex("(\\w+)\\s+paid\\s+(\\d+)")) -> {
                val match = Regex("(\\w+)\\s+paid\\s+(\\d+)").find(input)!!
                val customer = match.groupValues[1]
                val amount = match.groupValues[2]
                "$START_FUNCTION_CALL record_payment(customer_name=$ESCAPE_TOKEN$customer$ESCAPE_TOKEN, amount=$amount) $END_FUNCTION_CALL"
            }
            input.matches(Regex("received\\s+(\\d+)\\s+from\\s+(\\w+)")) -> {
                val match = Regex("received\\s+(\\d+)\\s+from\\s+(\\w+)").find(input)!!
                val amount = match.groupValues[1]
                val customer = match.groupValues[2]
                "$START_FUNCTION_CALL record_payment(customer_name=$ESCAPE_TOKEN$customer$ESCAPE_TOKEN, amount=$amount) $END_FUNCTION_CALL"
            }

            // Stock checks
            input.contains("how many") || input.contains("stock") && input.contains("?") -> {
                val productMatch = Regex("how many\\s+(.+?)\\s+(in|do|are)").find(input)
                    ?: Regex("stock\\s+(.+?)\\?").find(input)
                val product = productMatch?.groupValues?.get(1) ?: "product"
                "$START_FUNCTION_CALL check_stock(product_name=$ESCAPE_TOKEN$product$ESCAPE_TOKEN) $END_FUNCTION_CALL"
            }

            // Balance checks
            input.contains("how much") && (input.contains("owe") || input.contains("balance")) -> {
                val nameMatch = Regex("(does|how much)\\s+(\\w+)\\s+owe").find(input)
                val customer = nameMatch?.groupValues?.get(2) ?: "customer"
                "$START_FUNCTION_CALL get_customer_balance(customer_name=$ESCAPE_TOKEN$customer$ESCAPE_TOKEN) $END_FUNCTION_CALL"
            }

            // Analytics
            input.contains("sales today") || input.contains("today's sales") -> {
                "$START_FUNCTION_CALL get_today_sales() $END_FUNCTION_CALL"
            }
            input.contains("revenue") -> {
                val period = when {
                    input.contains("today") -> "today"
                    input.contains("week") -> "week"
                    input.contains("month") -> "month"
                    else -> "today"
                }
                "$START_FUNCTION_CALL get_revenue_summary(period=$ESCAPE_TOKEN$period$ESCAPE_TOKEN) $END_FUNCTION_CALL"
            }

            // Navigation
            input.contains("go home") || input.contains("go to home") -> {
                "$START_FUNCTION_CALL go_home() $END_FUNCTION_CALL"
            }
            input.contains("open") -> {
                val screenMatch = Regex("open\\s+(\\w+)").find(input)
                val screen = screenMatch?.groupValues?.get(1) ?: "home"
                "$START_FUNCTION_CALL navigate_to_screen(screen_name=$ESCAPE_TOKEN$screen$ESCAPE_TOKEN) $END_FUNCTION_CALL"
            }

            // Low stock alerts
            input.contains("low stock") -> {
                "$START_FUNCTION_CALL get_low_stock_alerts() $END_FUNCTION_CALL"
            }

            // Who owes money
            input.contains("who owes") || input.contains("all balances") || input.contains("credit balances") -> {
                "$START_FUNCTION_CALL get_all_credit_balances() $END_FUNCTION_CALL"
            }

            // Default - unable to parse
            else -> {
                Log.w(TAG, "Could not parse command: $userInput")
                "$START_FUNCTION_CALL unknown_command(input=$ESCAPE_TOKEN$userInput$ESCAPE_TOKEN) $END_FUNCTION_CALL"
            }
        }
    }

    /**
     * Build the prompt for FunctionGemma
     */
    private fun buildPrompt(userInput: String, toolDeclarations: String): String {
        return buildString {
            append(BOS_TOKEN)
            append("\n")

            // Add tool declarations
            append(toolDeclarations)
            append("\n\n")

            // Add user input
            append("User: ")
            append(userInput)
            append("\n\n")

            // Prompt for function call
            append("Assistant: Based on the user's request, I will call the appropriate function.\n")
            append(START_FUNCTION_CALL)
        }
    }

    /**
     * Tokenize text to token IDs using BPE-like tokenization
     */
    private fun tokenize(text: String): IntArray {
        val tokens = mutableListOf<Int>()

        // Add BOS token
        tokens.add(bosTokenId)

        // Check for special tokens first
        var remainingText = text
        while (remainingText.isNotEmpty()) {
            var matched = false

            // Try to match special tokens first
            for (specialToken in listOf(
                START_FUNCTION_CALL, END_FUNCTION_CALL,
                START_FUNCTION_DECLARATION, END_FUNCTION_DECLARATION,
                START_FUNCTION_RESPONSE, END_FUNCTION_RESPONSE,
                ESCAPE_TOKEN, BOS_TOKEN, EOS_TOKEN
            )) {
                if (remainingText.startsWith(specialToken)) {
                    vocabulary[specialToken]?.let { tokens.add(it) }
                    remainingText = remainingText.substring(specialToken.length)
                    matched = true
                    break
                }
            }

            if (!matched) {
                // Try to find longest matching token
                var longestMatch = ""
                var longestMatchId = unkTokenId

                for ((token, id) in vocabulary) {
                    if (token.length > longestMatch.length && remainingText.startsWith(token)) {
                        longestMatch = token
                        longestMatchId = id
                    }
                }

                if (longestMatch.isNotEmpty()) {
                    tokens.add(longestMatchId)
                    remainingText = remainingText.substring(longestMatch.length)
                } else {
                    // Fall back to character-level
                    val char = remainingText.first().toString()
                    tokens.add(vocabulary[char] ?: unkTokenId)
                    remainingText = remainingText.substring(1)
                }
            }

            // Prevent infinite loops
            if (tokens.size >= MAX_INPUT_LENGTH) break
        }

        // Pad to max length
        while (tokens.size < MAX_INPUT_LENGTH) {
            tokens.add(padTokenId)
        }

        return tokens.take(MAX_INPUT_LENGTH).toIntArray()
    }

    /**
     * Run model inference with autoregressive generation
     */
    private fun runInference(inputIds: IntArray): IntArray {
        val interpreter = this.interpreter
            ?: throw FunctionGemmaException("Model not initialized")

        // Prepare input buffer (INT32 for token IDs)
        val inputBuffer = ByteBuffer.allocateDirect(MAX_INPUT_LENGTH * 4)
            .order(ByteOrder.nativeOrder())
        inputIds.forEach { inputBuffer.putInt(it) }
        inputBuffer.rewind()

        // Prepare output buffer for logits
        // Shape: [1, MAX_OUTPUT_LENGTH, VOCAB_SIZE] or [1, MAX_OUTPUT_LENGTH]
        val outputBuffer = ByteBuffer.allocateDirect(MAX_OUTPUT_LENGTH * 4)
            .order(ByteOrder.nativeOrder())

        try {
            // Run inference
            interpreter.run(inputBuffer, outputBuffer)
        } catch (e: Exception) {
            Log.e(TAG, "TFLite inference error", e)
            throw FunctionGemmaException("TFLite inference failed: ${e.message}", e)
        }

        // Extract output IDs
        outputBuffer.rewind()
        val outputIds = IntArray(MAX_OUTPUT_LENGTH)
        for (i in 0 until MAX_OUTPUT_LENGTH) {
            outputIds[i] = outputBuffer.getInt()
        }

        return outputIds
    }

    /**
     * Decode token IDs back to text
     */
    private fun decode(tokenIds: IntArray): String {
        return buildString {
            for (id in tokenIds) {
                // Stop at EOS or PAD token
                if (id == eosTokenId || id == padTokenId) break
                // Skip BOS token
                if (id == bosTokenId) continue
                reverseVocabulary[id]?.let { append(it) }
            }
        }
    }

    /**
     * Check if the model is initialized and ready
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Check if the actual model is available (vs fallback mode)
     */
    fun isModelAvailable(): Boolean = modelAvailable

    /**
     * Get the last error message if initialization failed
     */
    fun getLastError(): String? = lastError

    /**
     * Get model status information
     */
    fun getStatus(): ModelStatus {
        return ModelStatus(
            isInitialized = isInitialized,
            modelAvailable = modelAvailable,
            tokenizerLoaded = tokenizerLoaded,
            vocabularySize = vocabulary.size,
            activeBackend = activeBackend,
            lastError = lastError
        )
    }

    /**
     * Release resources
     */
    fun close() {
        // Close MediaPipe
        mediaPipeInference?.close()
        mediaPipeInference = null

        // Close TFLite
        interpreter?.close()
        gpuDelegate?.close()
        interpreter = null
        gpuDelegate = null

        // Reset state
        isInitialized = false
        modelAvailable = false
        activeBackend = InferenceBackend.FALLBACK
        Log.d(TAG, "FunctionGemma resources released")
    }
}

/**
 * Model status information
 */
data class ModelStatus(
    val isInitialized: Boolean,
    val modelAvailable: Boolean,
    val tokenizerLoaded: Boolean,
    val vocabularySize: Int,
    val activeBackend: InferenceBackend,
    val lastError: String?
)

/**
 * Exception for FunctionGemma errors
 */
class FunctionGemmaException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
