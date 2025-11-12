package com.example.dukaai.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.IOException

/**
 * Utility functions for image processing and manipulation
 * Used for preparing images for ML classification
 */
object ImageUtils {

    /**
     * Load bitmap from URI with proper orientation
     */
    fun loadBitmapFromUri(context: Context, uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024): Bitmap? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            val newInputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
            newInputStream.close()

            // Fix orientation
            return bitmap?.let { fixOrientation(context, uri, it) }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Resize bitmap to specified dimensions
     */
    fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        if (bitmap.width <= maxWidth && bitmap.height <= maxHeight) {
            return bitmap
        }

        val ratio = (bitmap.width.toFloat() / bitmap.height.toFloat())
        val (newWidth, newHeight) = if (ratio > 1) {
            // Landscape
            maxWidth to (maxWidth / ratio).toInt()
        } else {
            // Portrait
            (maxHeight * ratio).toInt() to maxHeight
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    /**
     * Crop bitmap to center square
     */
    fun cropToSquare(bitmap: Bitmap): Bitmap {
        val size = kotlin.math.min(bitmap.width, bitmap.height)
        val x = (bitmap.width - size) / 2
        val y = (bitmap.height - size) / 2
        return Bitmap.createBitmap(bitmap, x, y, size, size)
    }

    /**
     * Fix image orientation based on EXIF data
     */
    private fun fixOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
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
            }

            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: IOException) {
            e.printStackTrace()
            return bitmap
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
     */
    fun toGrayscale(bitmap: Bitmap): Bitmap {
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

        return grayscaleBitmap
    }

    /**
     * Enhance bitmap contrast for better recognition
     */
    fun enhanceContrast(bitmap: Bitmap, factor: Float = 1.2f): Bitmap {
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

        return enhancedBitmap
    }
}
