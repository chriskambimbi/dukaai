package com.example.dukaai.voice

import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses voice input text and extracts command intent and parameters
 * Supports English, Nyanja, and Bemba languages
 */
@Singleton
class VoiceIntentParser @Inject constructor() {

    companion object {
        private const val TAG = "VoiceIntentParser"

        // Command patterns for English
        private val ENGLISH_PATTERNS = mapOf(
            VoiceCommandType.RECORD_SALE to listOf(
                "record sale", "sell", "quick sale", "sale of", "sold",
                "customer bought", "customer buy"
            ),
            VoiceCommandType.ADD_PRODUCT to listOf(
                "add product", "new product", "create product", "register product",
                "add stock", "new stock"
            ),
            VoiceCommandType.CHECK_STOCK to listOf(
                "check stock", "stock level", "how many", "how much stock",
                "stock for", "remaining stock"
            ),
            VoiceCommandType.RECORD_PAYMENT to listOf(
                "record payment", "customer paid", "payment received",
                "received payment", "payment of"
            ),
            VoiceCommandType.ADD_CUSTOMER to listOf(
                "add customer", "new customer", "register customer",
                "create customer"
            ),
            VoiceCommandType.VIEW_ANALYTICS to listOf(
                "show analytics", "view analytics", "sales report",
                "show report", "statistics"
            ),
            VoiceCommandType.LOW_STOCK_ALERT to listOf(
                "low stock", "out of stock", "stock alert",
                "running low"
            ),
            VoiceCommandType.SEARCH to listOf(
                "search", "find", "look for", "show me"
            )
        )

        // Command patterns for Nyanja (Chinyanja)
        private val NYANJA_PATTERNS = mapOf(
            VoiceCommandType.RECORD_SALE to listOf(
                "lemba kugulitsa", "gulitsa", "wogula", "wagula",
                "kasitomala wagula", "lembani kugulitsa"
            ),
            VoiceCommandType.ADD_PRODUCT to listOf(
                "onjezani katundu", "katundu katsopano", "lembani katundu",
                "onjezani"
            ),
            VoiceCommandType.CHECK_STOCK to listOf(
                "onani katundu", "kuli kangati", "katundu kotsala",
                "onani kotsala"
            ),
            VoiceCommandType.RECORD_PAYMENT to listOf(
                "lemba ndalama", "walipira", "ndalama zilowera",
                "lembani malipiro"
            ),
            VoiceCommandType.ADD_CUSTOMER to listOf(
                "onjezani kasitomala", "kasitomala watsopano",
                "lembani kasitomala"
            ),
            VoiceCommandType.VIEW_ANALYTICS to listOf(
                "onani lipoti", "onani kugulitsa", "lipoti",
                "onani masamu"
            ),
            VoiceCommandType.LOW_STOCK_ALERT to listOf(
                "katundu kachepa", "palibe katundu", "kotsala kochepa"
            ),
            VoiceCommandType.SEARCH to listOf(
                "funani", "onani", "ndifunirani"
            )
        )

        // Command patterns for Bemba (Chibemba)
        private val BEMBA_PATTERNS = mapOf(
            VoiceCommandType.RECORD_SALE to listOf(
                "lemba ukusula", "sula", "walishita", "balishita",
                "kasitomal walishita"
            ),
            VoiceCommandType.ADD_PRODUCT to listOf(
                "onjezeko ifintu", "ifintu ifipya", "lembapo ifintu",
                "onjezeko"
            ),
            VoiceCommandType.CHECK_STOCK to listOf(
                "monako ifintu", "filinga", "ifintu ifishalafye",
                "monako ifishalafye"
            ),
            VoiceCommandType.RECORD_PAYMENT to listOf(
                "lemba indalama", "walipishishe", "indalama shaingilile",
                "lembapo ukulipisha"
            ),
            VoiceCommandType.ADD_CUSTOMER to listOf(
                "onjezeko kasitoma", "kasitoma umupya",
                "lembapo kasitoma"
            ),
            VoiceCommandType.VIEW_ANALYTICS to listOf(
                "monako ilyashi", "monako ukusula", "ilyashi",
                "monako ifyashi"
            ),
            VoiceCommandType.LOW_STOCK_ALERT to listOf(
                "ifintu fipwa", "tafyo ifintu", "ifishalafye fipwa"
            ),
            VoiceCommandType.SEARCH to listOf(
                "fwailapo", "monako", "ndifwaileko"
            )
        )

        // Number words for parsing quantities/amounts
        private val NUMBER_WORDS_EN = mapOf(
            "one" to 1, "two" to 2, "three" to 3, "four" to 4, "five" to 5,
            "six" to 6, "seven" to 7, "eight" to 8, "nine" to 9, "ten" to 10,
            "twenty" to 20, "thirty" to 30, "forty" to 40, "fifty" to 50,
            "hundred" to 100, "thousand" to 1000
        )

        private val NUMBER_WORDS_NY = mapOf(
            "imodzi" to 1, "ziwiri" to 2, "zitatu" to 3, "zinayi" to 4, "zisanu" to 5,
            "zisanu ndi chimodzi" to 6, "zisanu ndi ziwiri" to 7, "zisanu ndi zitatu" to 8,
            "zisanu ndi zinayi" to 9, "khumi" to 10, "makumi awiri" to 20,
            "makumi atatu" to 30, "zana" to 100, "chikwi" to 1000
        )

        private val NUMBER_WORDS_BEM = mapOf(
            "imo" to 1, "fibili" to 2, "fitatu" to 3, "fine" to 4, "fisano" to 5,
            "mutanda" to 6, "konse lubale" to 7, "cinane" to 8, "cipunga" to 9,
            "ikumi" to 10, "amakumi yabili" to 20, "amakumi yatatu" to 30,
            "ñama" to 100, "ulwashi" to 1000
        )
    }

    /**
     * Parse voice input and extract command intent
     */
    fun parse(text: String, language: VoiceLanguage, confidence: Float): VoiceCommand {
        val normalizedText = text.lowercase().trim()

        Log.d(TAG, "Parsing: '$normalizedText' (language: ${language.displayName}, confidence: $confidence)")

        // Get patterns for current language
        val patterns = when (language) {
            VoiceLanguage.ENGLISH -> ENGLISH_PATTERNS
            VoiceLanguage.NYANJA -> NYANJA_PATTERNS
            VoiceLanguage.BEMBA -> BEMBA_PATTERNS
        }

        // Find matching command type
        var commandType = VoiceCommandType.UNKNOWN
        var matchedPattern: String? = null

        for ((type, keywords) in patterns) {
            for (keyword in keywords) {
                if (normalizedText.contains(keyword)) {
                    commandType = type
                    matchedPattern = keyword
                    break
                }
            }
            if (commandType != VoiceCommandType.UNKNOWN) break
        }

        // Extract parameters based on command type
        val parameters = extractParameters(normalizedText, commandType, language)

        Log.d(TAG, "Parsed command: type=$commandType, pattern='$matchedPattern', params=$parameters")

        return VoiceCommand(
            type = commandType,
            originalText = text,
            language = language.code,
            confidence = confidence,
            parameters = parameters
        )
    }

    /**
     * Extract parameters from command text
     */
    private fun extractParameters(text: String, type: VoiceCommandType, language: VoiceLanguage): Map<String, Any> {
        val params = mutableMapOf<String, Any>()

        when (type) {
            VoiceCommandType.RECORD_SALE -> {
                // Extract product name and quantity
                extractProductAndQuantity(text, language)?.let { (product, quantity) ->
                    params["product"] = product
                    if (quantity > 0) params["quantity"] = quantity
                }
                // Extract price/amount
                extractAmount(text, language)?.let { amount ->
                    params["amount"] = amount
                }
            }

            VoiceCommandType.ADD_PRODUCT -> {
                // Extract product name
                extractProductName(text, language)?.let { product ->
                    params["product"] = product
                }
                // Extract price
                extractAmount(text, language)?.let { amount ->
                    params["price"] = amount
                }
                // Extract quantity
                extractQuantity(text, language)?.let { quantity ->
                    params["quantity"] = quantity
                }
            }

            VoiceCommandType.CHECK_STOCK -> {
                // Extract product name to check
                extractProductName(text, language)?.let { product ->
                    params["product"] = product
                }
            }

            VoiceCommandType.RECORD_PAYMENT -> {
                // Extract customer name
                extractCustomerName(text, language)?.let { customer ->
                    params["customer"] = customer
                }
                // Extract payment amount
                extractAmount(text, language)?.let { amount ->
                    params["amount"] = amount
                }
            }

            VoiceCommandType.ADD_CUSTOMER -> {
                // Extract customer name
                extractCustomerName(text, language)?.let { customer ->
                    params["name"] = customer
                }
                // Extract phone number
                extractPhoneNumber(text)?.let { phone ->
                    params["phone"] = phone
                }
            }

            VoiceCommandType.SEARCH -> {
                // Extract search query (everything after "search for" / "find")
                extractSearchQuery(text, language)?.let { query ->
                    params["query"] = query
                }
            }

            else -> {
                // No parameters needed
            }
        }

        return params
    }

    /**
     * Extract product name and quantity from text
     */
    private fun extractProductAndQuantity(text: String, language: VoiceLanguage): Pair<String, Int>? {
        // Pattern: "sell 5 coca cola" or "gulitsa ziwiri fanta"
        val quantity = extractQuantity(text, language) ?: 1
        val product = extractProductName(text, language)

        return if (product != null) {
            Pair(product, quantity)
        } else {
            null
        }
    }

    /**
     * Extract product name from text
     */
    private fun extractProductName(text: String, language: VoiceLanguage): String? {
        // Remove command keywords first
        var cleanText = text
        when (language) {
            VoiceLanguage.ENGLISH -> {
                cleanText = cleanText
                    .replace(Regex("add product|new product|sell|sale of|check stock|search|find"), "")
                    .trim()
            }
            VoiceLanguage.NYANJA -> {
                cleanText = cleanText
                    .replace(Regex("onjezani|lemba|gulitsa|onani|funani"), "")
                    .replace("katundu", "")
                    .trim()
            }
            VoiceLanguage.BEMBA -> {
                cleanText = cleanText
                    .replace(Regex("onjezeko|lemba|sula|monako|fwailapo"), "")
                    .replace("ifintu", "")
                    .trim()
            }
        }

        // Remove numbers and quantity words
        cleanText = cleanText.replace(Regex("\\d+"), "").trim()

        return if (cleanText.isNotEmpty() && cleanText.length > 2) {
            cleanText
        } else {
            null
        }
    }

    /**
     * Extract quantity from text
     */
    private fun extractQuantity(text: String, language: VoiceLanguage): Int? {
        // Try to find numeric digits first
        val digitMatch = Regex("\\d+").find(text)
        if (digitMatch != null) {
            return digitMatch.value.toIntOrNull()
        }

        // Try to find number words
        val numberWords = when (language) {
            VoiceLanguage.ENGLISH -> NUMBER_WORDS_EN
            VoiceLanguage.NYANJA -> NUMBER_WORDS_NY
            VoiceLanguage.BEMBA -> NUMBER_WORDS_BEM
        }

        for ((word, value) in numberWords) {
            if (text.contains(word)) {
                return value
            }
        }

        return null
    }

    /**
     * Extract amount (money) from text
     */
    private fun extractAmount(text: String, language: VoiceLanguage): Double? {
        // Look for currency patterns: "K50", "50 kwacha", "50", "makumi asanu"
        // Try numeric with currency
        val patterns = listOf(
            Regex("k\\s*(\\d+(?:\\.\\d{1,2})?)"),  // K50 or K 50.50
            Regex("(\\d+(?:\\.\\d{1,2})?)\\s*kwacha"),  // 50 kwacha
            Regex("(\\d+(?:\\.\\d{1,2})?)")  // Just 50
        )

        for (pattern in patterns) {
            val match = pattern.find(text.lowercase())
            if (match != null) {
                return match.groupValues[1].toDoubleOrNull()
            }
        }

        // Try to extract number words as amount
        extractQuantity(text, language)?.let { return it.toDouble() }

        return null
    }

    /**
     * Extract customer name from text
     */
    private fun extractCustomerName(text: String, language: VoiceLanguage): String? {
        // Pattern: "payment from John" or "kasitomala John"
        val patterns = when (language) {
            VoiceLanguage.ENGLISH -> listOf("from", "for", "by", "customer")
            VoiceLanguage.NYANJA -> listOf("kwa", "kasitomala")
            VoiceLanguage.BEMBA -> listOf("kuli", "kasitoma")
        }

        for (pattern in patterns) {
            val index = text.indexOf(pattern)
            if (index != -1) {
                val afterPattern = text.substring(index + pattern.length).trim()
                val name = afterPattern.split(" ").take(2).joinToString(" ")
                if (name.isNotEmpty()) {
                    return name
                }
            }
        }

        return null
    }

    /**
     * Extract phone number from text
     */
    private fun extractPhoneNumber(text: String): String? {
        // Match Zambian phone numbers: 0977123456, 0966123456, etc.
        val phonePattern = Regex("0[97]\\d{8}")
        val match = phonePattern.find(text.replace("\\s".toRegex(), ""))
        return match?.value
    }

    /**
     * Extract search query from text
     */
    private fun extractSearchQuery(text: String, language: VoiceLanguage): String? {
        val searchKeywords = when (language) {
            VoiceLanguage.ENGLISH -> listOf("search for", "find", "look for", "show me")
            VoiceLanguage.NYANJA -> listOf("funani", "onani", "ndifunirani")
            VoiceLanguage.BEMBA -> listOf("fwailapo", "monako", "ndifwaileko")
        }

        for (keyword in searchKeywords) {
            val index = text.indexOf(keyword)
            if (index != -1) {
                val query = text.substring(index + keyword.length).trim()
                if (query.isNotEmpty()) {
                    return query
                }
            }
        }

        return null
    }
}
