package com.example.dukaai.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Utility functions for image processing and manipulation
 * Used for preparing images for ML classification
 *
 * All heavy operations use Dispatchers.IO to avoid blocking the main thread.
 * Bitmap recycling is handled to prevent OutOfMemoryError.
 */
object ImageUtils {

    /**
     * Load bitmap from URI with proper orientation
     * Performs I/O on background thread
     */
    suspend fun loadBitmapFromUri(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            val newInputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
            newInputStream.close()

            // Fix orientation (may create a new bitmap)
            bitmap?.let { originalBitmap ->
                val fixedBitmap = fixOrientation(context, uri, originalBitmap)
                // Recycle the original if a new bitmap was created
                if (fixedBitmap !== originalBitmap && !originalBitmap.isRecycled) {
                    originalBitmap.recycle()
                }
                fixedBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Resize bitmap to specified dimensions
     * Performs computation on background thread and recycles source bitmap if different
     */
    suspend fun resizeBitmap(
        bitmap: Bitmap,
        maxWidth: Int,
        maxHeight: Int,
        recycleSource: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) {
            return@withContext bitmap
        }

        val ratio = (bitmap.width.toFloat() / bitmap.height.toFloat())
        val (newWidth, newHeight) = if (ratio > 1) {
            // Landscape
            maxWidth to (maxWidth / ratio).toInt()
        } else {
            // Portrait
            (maxHeight * ratio).toInt() to maxHeight
        }

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)

        // Recycle source bitmap if requested and a new bitmap was created
        if (recycleSource && resizedBitmap !== bitmap && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        resizedBitmap
    }

    /**
     * Crop bitmap to center square
     * Performs computation on background thread
     */
    suspend fun cropToSquare(
        bitmap: Bitmap,
        recycleSource: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        val size = kotlin.math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        val croppedBitmap = Bitmap.createBitmap(bitmap, x, y, size, size)

        // Recycle source bitmap if requested and a new bitmap was created
        if (recycleSource && croppedBitmap !== bitmap && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        croppedBitmap
    }

    /**
     * Fix image orientation based on EXIF data
     * Performs I/O on background thread
     */
    private suspend fun fixOrientation(
        context: Context,
        uri: Uri,
        bitmap: Bitmap
    ): Bitmap = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext bitmap
            val exif = ExifInterface(inputStream)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                else -> return@withContext bitmap // No rotation needed
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
            bitmap
        }
    }

    /**
     * Calculate sample size for efficient bitmap loading
     */
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    /**
     * Convert bitmap to grayscale
     * Performs computation on background thread
     */
    suspend fun toGrayscale(
        bitmap: Bitmap,
        recycleSource: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height
        val grayscaleBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                val red = android.graphics.Color.red(pixel)
                val green = android.graphics.Color.green(pixel)
                val blue = android.graphics.Color.blue(pixel)
                val gray = (0.299 * red + 0.587 * green + 0.114 * blue).toInt()
                val grayPixel = android.graphics.Color.rgb(gray, gray, gray)
                grayscaleBitmap.setPixel(x, y, grayPixel)
            }
        }

        // Recycle source bitmap if requested
        if (recycleSource && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        grayscaleBitmap
    }

    /**
     * Enhance bitmap contrast for better recognition
     * Performs computation on background thread
     */
    suspend fun enhanceContrast(
        bitmap: Bitmap,
        factor: Float = 1.2f,
        recycleSource: Boolean = false
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height
        val enhancedBitmap = Bitmap.createBitmap(width, height, bitmap.config ?: Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)
                var red = android.graphics.Color.red(pixel)
                var green = android.graphics.Color.green(pixel)
                var blue = android.graphics.Color.blue(pixel)

                red = ((red - 128) * factor + 128).toInt().coerceIn(0, 255)
                green = ((green - 128) * factor + 128).toInt().coerceIn(0, 255)
                blue = ((blue - 128) * factor + 128).toInt().coerceIn(0, 255)

                enhancedBitmap.setPixel(x, y, android.graphics.Color.rgb(red, green, blue))
            }
        }

        // Recycle source bitmap if requested
        if (recycleSource && !bitmap.isRecycled) {
            bitmap.recycle()
        }

        enhancedBitmap
    }

    /**
     * Safely recycle a bitmap if it's not null and not already recycled
     */
    fun safeRecycle(bitmap: Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}
