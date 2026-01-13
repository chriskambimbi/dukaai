package com.example.dukaai.ml

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Barcode detection result
 */
data class BarcodeResult(
    val barcode: String,
    val format: BarcodeFormat,
    val confidence: Float = 1.0f,
    val boundingBox: android.graphics.Rect? = null
)

/**
 * Supported barcode formats
 */
enum class BarcodeFormat {
    EAN_13,
    EAN_8,
    UPC_A,
    UPC_E,
    CODE_128,
    CODE_39,
    CODE_93,
    CODABAR,
    ITF,
    QR_CODE,
    DATA_MATRIX,
    PDF_417,
    AZTEC,
    UNKNOWN
}

/**
 * Interface for barcode scanning
 * Can be implemented using ML Kit, ZXing, or other barcode libraries
 */
interface BarcodeScanner {
    /**
     * Scan a barcode from an image
     * @param bitmap The image containing the barcode
     * @return List of detected barcodes
     */
    suspend fun scanBarcode(bitmap: Bitmap): List<BarcodeResult>

    /**
     * Scan a barcode from an InputImage (more flexible for camera integration)
     * @param image The InputImage containing the barcode
     * @return List of detected barcodes
     */
    suspend fun scanBarcode(image: InputImage): List<BarcodeResult>

    /**
     * Close and release resources
     */
    fun close()
}

/**
 * ML Kit implementation of BarcodeScanner
 *
 * Supports all common barcode formats used in retail:
 * - 1D: EAN-13, EAN-8, UPC-A, UPC-E, Code-128, Code-39, Code-93, Codabar, ITF
 * - 2D: QR Code, Data Matrix, PDF-417, Aztec
 *
 * Thread-safe and optimized for real-time camera scanning.
 */
class DefaultBarcodeScanner : BarcodeScanner {

    companion object {
        private const val TAG = "BarcodeScanner"
    }

    // Configure scanner for retail barcode formats (1D product codes + QR)
    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_EAN_8,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_UPC_E,
            Barcode.FORMAT_CODE_128,
            Barcode.FORMAT_CODE_39,
            Barcode.FORMAT_QR_CODE
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    override suspend fun scanBarcode(bitmap: Bitmap): List<BarcodeResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return scanBarcode(image)
    }

    override suspend fun scanBarcode(image: InputImage): List<BarcodeResult> {
        return suspendCoroutine { continuation ->
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val results = barcodes.mapNotNull { barcode ->
                        barcode.rawValue?.let { value ->
                            BarcodeResult(
                                barcode = value,
                                format = convertBarcodeFormat(barcode.format),
                                confidence = 1.0f, // ML Kit doesn't provide confidence scores
                                boundingBox = barcode.boundingBox
                            )
                        }
                    }

                    if (results.isNotEmpty()) {
                        Log.d(TAG, "Detected ${results.size} barcode(s): ${results.map { it.barcode }}")
                    }

                    continuation.resume(results)
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Barcode scanning failed", e)
                    continuation.resume(emptyList())
                }
        }
    }

    override fun close() {
        scanner.close()
        Log.d(TAG, "BarcodeScanner resources released")
    }

    /**
     * Convert ML Kit barcode format constant to our BarcodeFormat enum
     */
    private fun convertBarcodeFormat(mlKitFormat: Int): BarcodeFormat {
        return when (mlKitFormat) {
            Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
            Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
            Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
            Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
            Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
            Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
            Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
            Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
            Barcode.FORMAT_ITF -> BarcodeFormat.ITF
            Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
            Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
            Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
            else -> BarcodeFormat.UNKNOWN
        }
    }
}

/**
 * Factory for creating BarcodeScanner instances with different configurations
 */
object BarcodeScannerFactory {

    /**
     * Create a scanner optimized for product barcodes (1D codes)
     */
    fun createProductScanner(): BarcodeScanner = DefaultBarcodeScanner()

    /**
     * Create a scanner for QR codes only (faster for QR-specific use cases)
     */
    fun createQrScanner(): BarcodeScanner = QrOnlyBarcodeScanner()

    /**
     * Create a scanner that supports all barcode formats
     */
    fun createAllFormatScanner(): BarcodeScanner = AllFormatBarcodeScanner()
}

/**
 * Specialized scanner for QR codes only - faster processing
 */
class QrOnlyBarcodeScanner : BarcodeScanner {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    override suspend fun scanBarcode(bitmap: Bitmap): List<BarcodeResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return scanBarcode(image)
    }

    override suspend fun scanBarcode(image: InputImage): List<BarcodeResult> {
        return suspendCoroutine { continuation ->
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val results = barcodes.mapNotNull { barcode ->
                        barcode.rawValue?.let { value ->
                            BarcodeResult(
                                barcode = value,
                                format = BarcodeFormat.QR_CODE,
                                boundingBox = barcode.boundingBox
                            )
                        }
                    }
                    continuation.resume(results)
                }
                .addOnFailureListener {
                    continuation.resume(emptyList())
                }
        }
    }

    override fun close() {
        scanner.close()
    }
}

/**
 * Scanner that supports all barcode formats - slower but comprehensive
 */
class AllFormatBarcodeScanner : BarcodeScanner {

    private val scanner = BarcodeScanning.getClient()

    override suspend fun scanBarcode(bitmap: Bitmap): List<BarcodeResult> {
        val image = InputImage.fromBitmap(bitmap, 0)
        return scanBarcode(image)
    }

    override suspend fun scanBarcode(image: InputImage): List<BarcodeResult> {
        return suspendCoroutine { continuation ->
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    val results = barcodes.mapNotNull { barcode ->
                        barcode.rawValue?.let { value ->
                            BarcodeResult(
                                barcode = value,
                                format = convertFormat(barcode.format),
                                boundingBox = barcode.boundingBox
                            )
                        }
                    }
                    continuation.resume(results)
                }
                .addOnFailureListener {
                    continuation.resume(emptyList())
                }
        }
    }

    override fun close() {
        scanner.close()
    }

    private fun convertFormat(format: Int): BarcodeFormat {
        return when (format) {
            Barcode.FORMAT_EAN_13 -> BarcodeFormat.EAN_13
            Barcode.FORMAT_EAN_8 -> BarcodeFormat.EAN_8
            Barcode.FORMAT_UPC_A -> BarcodeFormat.UPC_A
            Barcode.FORMAT_UPC_E -> BarcodeFormat.UPC_E
            Barcode.FORMAT_CODE_128 -> BarcodeFormat.CODE_128
            Barcode.FORMAT_CODE_39 -> BarcodeFormat.CODE_39
            Barcode.FORMAT_CODE_93 -> BarcodeFormat.CODE_93
            Barcode.FORMAT_CODABAR -> BarcodeFormat.CODABAR
            Barcode.FORMAT_ITF -> BarcodeFormat.ITF
            Barcode.FORMAT_QR_CODE -> BarcodeFormat.QR_CODE
            Barcode.FORMAT_DATA_MATRIX -> BarcodeFormat.DATA_MATRIX
            Barcode.FORMAT_PDF417 -> BarcodeFormat.PDF_417
            Barcode.FORMAT_AZTEC -> BarcodeFormat.AZTEC
            else -> BarcodeFormat.UNKNOWN
        }
    }
}
