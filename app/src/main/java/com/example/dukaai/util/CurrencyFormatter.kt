package com.example.dukaai.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Currency formatting utility for consistent display across the app
 * Standard format: "K 1,234.56" (Zambian Kwacha)
 */
object CurrencyFormatter {
    private val symbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    // Format with decimals: K 1,234.56
    private val fullFormat = DecimalFormat("#,##0.00", symbols)

    // Format without decimals: K 1,234
    private val wholeFormat = DecimalFormat("#,##0", symbols)

    // Compact format for large numbers: K 1.2K, K 1.5M
    private val compactFormat = DecimalFormat("#,##0.#", symbols)

    /**
     * Format amount with full precision (2 decimal places)
     * Example: 1234.5 -> "K 1,234.50"
     */
    fun format(amount: Double): String {
        return "K ${fullFormat.format(amount)}"
    }

    /**
     * Format amount as whole number (no decimals)
     * Example: 1234.56 -> "K 1,235"
     */
    fun formatWhole(amount: Double): String {
        return "K ${wholeFormat.format(amount)}"
    }

    /**
     * Format amount with automatic decimal handling
     * Shows decimals only if there are cents
     * Example: 1234.00 -> "K 1,234", 1234.50 -> "K 1,234.50"
     */
    fun formatSmart(amount: Double): String {
        return if (amount % 1.0 == 0.0) {
            formatWhole(amount)
        } else {
            format(amount)
        }
    }

    /**
     * Format large amounts in compact form
     * Example: 1500 -> "K 1.5K", 1500000 -> "K 1.5M"
     */
    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "K ${compactFormat.format(amount / 1_000_000)}M"
            amount >= 1_000 -> "K ${compactFormat.format(amount / 1_000)}K"
            else -> formatSmart(amount)
        }
    }

    /**
     * Format for display in cards/summaries (no decimals, compact for large numbers)
     * Example: 456 -> "K 456", 1234 -> "K 1,234", 15000 -> "K 15K"
     */
    fun formatDisplay(amount: Double): String {
        return when {
            amount >= 10_000 -> formatCompact(amount)
            else -> formatWhole(amount)
        }
    }

    /**
     * Format amount for input fields (raw number with decimals)
     * Example: 1234.50 -> "1234.50"
     */
    fun formatForInput(amount: Double): String {
        return fullFormat.format(amount)
    }

    /**
     * Parse a currency string back to Double
     * Handles: "K 1,234.56", "K1234.56", "1234.56", "1,234.56"
     */
    fun parse(text: String): Double? {
        return try {
            val cleanText = text
                .replace("K", "")
                .replace(" ", "")
                .replace(",", "")
                .trim()
            cleanText.toDoubleOrNull()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Format percentage change
     * Example: 0.18 -> "+18%", -0.05 -> "-5%"
     */
    fun formatPercentage(value: Double, showSign: Boolean = true): String {
        val percent = (value * 100).toInt()
        val sign = when {
            !showSign -> ""
            percent > 0 -> "+"
            else -> ""
        }
        return "$sign$percent%"
    }

    /**
     * Format profit margin
     * Example: 0.25 -> "25% margin"
     */
    fun formatMargin(costPrice: Double, sellingPrice: Double): String {
        if (costPrice <= 0) return "N/A"
        val margin = ((sellingPrice - costPrice) / costPrice * 100).toInt()
        return "+$margin%"
    }
}

/**
 * Extension function for Double to format as currency
 */
fun Double.toCurrency(): String = CurrencyFormatter.format(this)

/**
 * Extension function for Double to format as whole currency
 */
fun Double.toCurrencyWhole(): String = CurrencyFormatter.formatWhole(this)

/**
 * Extension function for Double to format as compact currency
 */
fun Double.toCurrencyCompact(): String = CurrencyFormatter.formatCompact(this)

/**
 * Extension function for Double to format as display currency
 */
fun Double.toCurrencyDisplay(): String = CurrencyFormatter.formatDisplay(this)
