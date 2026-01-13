package com.example.dukaai.util

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Helper object for WhatsApp integration
 * Handles sending messages via WhatsApp
 */
object WhatsAppHelper {

    /**
     * Opens WhatsApp with a pre-filled message to the specified phone number
     *
     * @param context Android context
     * @param phoneNumber Phone number in international format (e.g., +260XXXXXXXXX)
     * @param message Message to send
     * @return true if WhatsApp was opened successfully, false otherwise
     */
    fun sendWhatsAppMessage(context: Context, phoneNumber: String, message: String): Boolean {
        return try {
            // Format phone number - remove spaces and ensure it starts with country code
            val formattedNumber = formatPhoneNumber(phoneNumber)

            // Create WhatsApp URL
            val url = "https://wa.me/$formattedNumber?text=${Uri.encode(message)}"

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Opens WhatsApp with a payment reminder message
     *
     * @param context Android context
     * @param phoneNumber Customer's phone number
     * @param customerName Customer's name
     * @param totalDebt Amount owed
     * @param businessName Name of the business (default: "Duka.AI")
     * @return true if WhatsApp was opened successfully, false otherwise
     */
    fun sendPaymentReminder(
        context: Context,
        phoneNumber: String,
        customerName: String,
        totalDebt: Double,
        businessName: String = "Duka.AI"
    ): Boolean {
        val message = buildPaymentReminderMessage(customerName, totalDebt, businessName)
        return sendWhatsAppMessage(context, phoneNumber, message)
    }

    /**
     * Builds a payment reminder message
     */
    fun buildPaymentReminderMessage(
        customerName: String,
        totalDebt: Double,
        businessName: String = "Duka.AI"
    ): String {
        return "Hello $customerName, friendly reminder that you have a balance of K ${String.format("%.2f", totalDebt)} at $businessName. Thank you for your continued patronage!"
    }

    /**
     * Format phone number for WhatsApp
     * Removes spaces, dashes, and ensures country code
     */
    private fun formatPhoneNumber(phoneNumber: String): String {
        // Remove all non-digit characters except +
        var cleaned = phoneNumber.filter { it.isDigit() || it == '+' }

        // If number starts with +, remove it for wa.me format
        if (cleaned.startsWith("+")) {
            cleaned = cleaned.substring(1)
        }

        // If number starts with 0, assume Zambian number and add country code
        if (cleaned.startsWith("0")) {
            cleaned = "260${cleaned.substring(1)}"
        }

        // If number doesn't have country code (less than 12 digits), assume Zambian
        if (cleaned.length < 12 && !cleaned.startsWith("260")) {
            cleaned = "260$cleaned"
        }

        return cleaned
    }

    /**
     * Check if WhatsApp is installed on the device
     */
    fun isWhatsAppInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.whatsapp", 0)
            true
        } catch (e: Exception) {
            // Try WhatsApp Business
            try {
                context.packageManager.getPackageInfo("com.whatsapp.w4b", 0)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}
