package com.example.dukaai.ml.functiongemma

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser for FunctionGemma model outputs.
 *
 * Extracts function calls from the model's response using control tokens:
 * - <start_function_call> and <end_function_call> delimit function calls
 * - <escape> is used as delimiter for string parameter values
 * - Supports parallel function calls (multiple calls in one response)
 */
@Singleton
class FunctionGemmaParser @Inject constructor() {

    companion object {
        private val FUNCTION_CALL_PATTERN = Regex(
            """<start_function_call>\s*(\w+)\s*\((.*?)\)\s*<end_function_call>""",
            RegexOption.DOT_MATCHES_ALL
        )

        private val PARAMETER_PATTERN = Regex(
            """(\w+)\s*=\s*(<escape>.*?<escape>|\d+\.?\d*|true|false|null)"""
        )

        private val STRING_VALUE_PATTERN = Regex(
            """<escape>(.*?)<escape>"""
        )
    }

    /**
     * Parse model output to extract function calls
     *
     * @param modelOutput The raw output from FunctionGemma
     * @return List of parsed function calls (supports parallel calls)
     */
    fun parse(modelOutput: String): List<ParsedFunctionCall> {
        val functionCalls = mutableListOf<ParsedFunctionCall>()

        FUNCTION_CALL_PATTERN.findAll(modelOutput).forEach { match ->
            val functionName = match.groupValues[1].trim()
            val paramsString = match.groupValues[2].trim()

            val arguments = parseArguments(paramsString)

            functionCalls.add(ParsedFunctionCall(
                functionName = functionName,
                arguments = arguments
            ))
        }

        return functionCalls
    }

    /**
     * Parse the arguments string into a map
     */
    private fun parseArguments(paramsString: String): Map<String, Any?> {
        val arguments = mutableMapOf<String, Any?>()

        PARAMETER_PATTERN.findAll(paramsString).forEach { match ->
            val paramName = match.groupValues[1].trim()
            val rawValue = match.groupValues[2].trim()

            val parsedValue = parseValue(rawValue)
            arguments[paramName] = parsedValue
        }

        return arguments
    }

    /**
     * Parse a raw value to its proper type
     */
    private fun parseValue(rawValue: String): Any? {
        return when {
            // String values (delimited by <escape>)
            rawValue.startsWith("<escape>") -> {
                STRING_VALUE_PATTERN.find(rawValue)?.groupValues?.get(1) ?: ""
            }

            // Boolean values
            rawValue.equals("true", ignoreCase = true) -> true
            rawValue.equals("false", ignoreCase = true) -> false

            // Null value
            rawValue.equals("null", ignoreCase = true) -> null

            // Numeric values
            rawValue.contains('.') -> rawValue.toDoubleOrNull()
            else -> rawValue.toIntOrNull() ?: rawValue
        }
    }

    /**
     * Validate that a function call has all required parameters
     *
     * @param call The parsed function call
     * @param tool The tool declaration to validate against
     * @return List of missing required parameters, empty if valid
     */
    fun validateFunctionCall(call: ParsedFunctionCall, tool: ToolDeclaration): List<String> {
        val missingParams = mutableListOf<String>()

        tool.parameters.filter { it.required }.forEach { param ->
            if (!call.arguments.containsKey(param.name) || call.arguments[param.name] == null) {
                missingParams.add(param.name)
            }
        }

        return missingParams
    }

    /**
     * Find the matching tool declaration for a function call
     */
    fun findMatchingTool(functionName: String): ToolDeclaration? {
        return DukaToolSchema.allTools.find { it.name == functionName }
    }

    /**
     * Format a function response for feeding back to the model (multi-turn)
     */
    fun formatFunctionResponse(
        functionName: String,
        response: Any?
    ): String {
        return buildString {
            append("<start_function_response>")
            append(functionName)
            append(": ")
            when (response) {
                is String -> {
                    append("<escape>")
                    append(response)
                    append("<escape>")
                }
                is Map<*, *> -> {
                    append(formatMapResponse(response))
                }
                is List<*> -> {
                    append(formatListResponse(response))
                }
                else -> append(response.toString())
            }
            append("<end_function_response>")
        }
    }

    private fun formatMapResponse(map: Map<*, *>): String {
        return buildString {
            append("{")
            map.entries.forEachIndexed { index, entry ->
                if (index > 0) append(", ")
                append(entry.key)
                append(": ")
                when (val value = entry.value) {
                    is String -> {
                        append("<escape>")
                        append(value)
                        append("<escape>")
                    }
                    else -> append(value)
                }
            }
            append("}")
        }
    }

    private fun formatListResponse(list: List<*>): String {
        return buildString {
            append("[")
            list.forEachIndexed { index, item ->
                if (index > 0) append(", ")
                when (item) {
                    is String -> {
                        append("<escape>")
                        append(item)
                        append("<escape>")
                    }
                    is Map<*, *> -> append(formatMapResponse(item))
                    else -> append(item)
                }
            }
            append("]")
        }
    }

    /**
     * Extract any explanatory text from the model output (outside function calls)
     */
    fun extractExplanation(modelOutput: String): String? {
        // Remove all function calls from the output
        val withoutCalls = modelOutput
            .replace(FUNCTION_CALL_PATTERN, "")
            .trim()

        return withoutCalls.ifEmpty { null }
    }
}

/**
 * Result of parsing model output
 */
data class ParseResult(
    val functionCalls: List<ParsedFunctionCall>,
    val explanation: String?,
    val hasErrors: Boolean = false,
    val errorMessage: String? = null
)

/**
 * Extension function to safely get string argument
 */
fun ParsedFunctionCall.getStringArg(name: String, default: String = ""): String {
    return arguments[name]?.toString() ?: default
}

/**
 * Extension function to safely get int argument
 */
fun ParsedFunctionCall.getIntArg(name: String, default: Int = 0): Int {
    return when (val value = arguments[name]) {
        is Int -> value
        is Double -> value.toInt()
        is String -> value.toIntOrNull() ?: default
        else -> default
    }
}

/**
 * Extension function to safely get double argument
 */
fun ParsedFunctionCall.getDoubleArg(name: String, default: Double = 0.0): Double {
    return when (val value = arguments[name]) {
        is Double -> value
        is Int -> value.toDouble()
        is String -> value.toDoubleOrNull() ?: default
        else -> default
    }
}

/**
 * Extension function to safely get boolean argument
 */
fun ParsedFunctionCall.getBooleanArg(name: String, default: Boolean = false): Boolean {
    return when (val value = arguments[name]) {
        is Boolean -> value
        is String -> value.equals("true", ignoreCase = true)
        else -> default
    }
}

/**
 * Extension function to check if argument is present and not null
 */
fun ParsedFunctionCall.hasArg(name: String): Boolean {
    return arguments.containsKey(name) && arguments[name] != null
}
