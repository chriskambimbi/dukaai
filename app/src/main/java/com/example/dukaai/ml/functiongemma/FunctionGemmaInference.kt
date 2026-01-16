package com.example.dukaai.ml.functiongemma

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
 */
@Singleton
class FunctionGemmaInference @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "FunctionGemmaInference"
        private const val MODEL_FILE = "function_gemma.tflite"
        private const val VOCAB_FILE = "function_gemma_vocab.txt"

        // Model configuration
        private const val MAX_INPUT_LENGTH = 512
        private const val MAX_OUTPUT_LENGTH = 256
        private const val VOCAB_SIZE = 256000  // Gemma vocabulary size

        // Special tokens
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
    }

    private var interpreter: Interpreter? = null
    private var gpuDelegate: GpuDelegate? = null
    private var isInitialized = false
    private var vocabulary: Map<String, Int> = emptyMap()
    private var reverseVocabulary: Map<Int, String> = emptyMap()

    // Special token IDs
    private var bosTokenId: Int = 2
    private var eosTokenId: Int = 1
    private var padTokenId: Int = 0
    private var startFunctionCallId: Int = 0
    private var endFunctionCallId: Int = 0
    private var escapeTokenId: Int = 0

    /**
     * Initialize the FunctionGemma model
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        if (isInitialized) {
            return@withContext Result.success(Unit)
        }

        try {
            // Load vocabulary
            loadVocabulary()

            // Load model
            val modelBuffer = loadModelFile()
            val options = createInterpreterOptions()
            interpreter = Interpreter(modelBuffer, options)

            isInitialized = true
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(FunctionGemmaException("Failed to initialize FunctionGemma: ${e.message}", e))
        }
    }

    /**
     * Load vocabulary from assets
     */
    private fun loadVocabulary() {
        try {
            context.assets.open(VOCAB_FILE).bufferedReader().useLines { lines ->
                vocabulary = lines.mapIndexed { index, token ->
                    token to index
                }.toMap()
            }
            reverseVocabulary = vocabulary.entries.associate { it.value to it.key }

            // Get special token IDs
            vocabulary[BOS_TOKEN]?.let { bosTokenId = it }
            vocabulary[EOS_TOKEN]?.let { eosTokenId = it }
            vocabulary[PAD_TOKEN]?.let { padTokenId = it }
            vocabulary[START_FUNCTION_CALL]?.let { startFunctionCallId = it }
            vocabulary[END_FUNCTION_CALL]?.let { endFunctionCallId = it }
            vocabulary[ESCAPE_TOKEN]?.let { escapeTokenId = it }
        } catch (e: Exception) {
            // Use default vocabulary mapping if file not found
            // This allows the system to work with a placeholder until the actual model is deployed
            vocabulary = createDefaultVocabulary()
            reverseVocabulary = vocabulary.entries.associate { it.value to it.key }
        }
    }

    /**
     * Create default vocabulary for testing/development
     */
    private fun createDefaultVocabulary(): Map<String, Int> {
        return mapOf(
            PAD_TOKEN to 0,
            EOS_TOKEN to 1,
            BOS_TOKEN to 2,
            START_FUNCTION_DECLARATION to 3,
            END_FUNCTION_DECLARATION to 4,
            START_FUNCTION_CALL to 5,
            END_FUNCTION_CALL to 6,
            START_FUNCTION_RESPONSE to 7,
            END_FUNCTION_RESPONSE to 8,
            ESCAPE_TOKEN to 9
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
     * Run inference to generate function calls from user input
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
            // Build the prompt with tool declarations and user input
            val prompt = buildPrompt(userInput, toolDeclarations)

            // Tokenize input
            val inputIds = tokenize(prompt)

            // Run inference
            val outputIds = runInference(inputIds)

            // Decode output
            val output = decode(outputIds)

            Result.success(output)
        } catch (e: Exception) {
            Result.failure(FunctionGemmaException("Inference failed: ${e.message}", e))
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
     * Tokenize text to token IDs
     */
    private fun tokenize(text: String): IntArray {
        // Simple character-level tokenization for development
        // In production, this should use the actual Gemma tokenizer (SentencePiece)
        val tokens = mutableListOf<Int>()

        // Add BOS token
        tokens.add(bosTokenId)

        // Tokenize text (simplified - should use proper subword tokenization)
        text.forEach { char ->
            val charStr = char.toString()
            vocabulary[charStr]?.let { tokens.add(it) }
                ?: tokens.add(vocabulary["<unk>"] ?: 0)
        }

        // Pad or truncate to max length
        while (tokens.size < MAX_INPUT_LENGTH) {
            tokens.add(padTokenId)
        }

        return tokens.take(MAX_INPUT_LENGTH).toIntArray()
    }

    /**
     * Run model inference
     */
    private fun runInference(inputIds: IntArray): IntArray {
        val interpreter = this.interpreter
            ?: throw FunctionGemmaException("Model not initialized")

        // Prepare input buffer
        val inputBuffer = ByteBuffer.allocateDirect(MAX_INPUT_LENGTH * 4)
            .order(ByteOrder.nativeOrder())
        inputIds.forEach { inputBuffer.putInt(it) }
        inputBuffer.rewind()

        // Prepare output buffer
        val outputBuffer = ByteBuffer.allocateDirect(MAX_OUTPUT_LENGTH * 4)
            .order(ByteOrder.nativeOrder())

        // Run inference
        interpreter.run(inputBuffer, outputBuffer)

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
                if (id == eosTokenId || id == padTokenId) break
                reverseVocabulary[id]?.let { append(it) }
            }
        }
    }

    /**
     * Check if the model is initialized
     */
    fun isReady(): Boolean = isInitialized

    /**
     * Release resources
     */
    fun close() {
        interpreter?.close()
        gpuDelegate?.close()
        interpreter = null
        gpuDelegate = null
        isInitialized = false
    }
}

/**
 * Exception for FunctionGemma errors
 */
class FunctionGemmaException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
