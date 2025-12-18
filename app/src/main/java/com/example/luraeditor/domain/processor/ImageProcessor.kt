package com.example.luraeditor.domain.processor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.min

object ImageProcessor {

    suspend fun applyEdits(
        bitmap: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        warmth: Float,
        sharpen: Float
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = bitmap.width
        val height = bitmap.height
        val resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Apply color matrix for adjustments
        val colorMatrix = ColorMatrix()

        // Brightness
        val brightnessMatrix = ColorMatrix(
            floatArrayOf(
                brightness, 0f, 0f, 0f, 0f,
                0f, brightness, 0f, 0f, 0f,
                0f, 0f, brightness, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            )
        )
        colorMatrix.postConcat(brightnessMatrix)

        // Contrast
        val scale = contrast
        val translate = (1f - contrast) / 2f * 255f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        colorMatrix.postConcat(contrastMatrix)

        // Saturation
        colorMatrix.setSaturation(saturation)

        // Warmth - adjust hue
        if (warmth != 0f) {
            val warmthMatrix = ColorMatrix()
            val warmthValue = warmth / 100f

            // Red channel increase, blue decrease for warm
            // Blue increase, red decrease for cool
            if (warmth > 0) {
                warmthMatrix.set(
                    floatArrayOf(
                        1f + warmthValue * 0.2f, 0f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 1f - warmthValue * 0.2f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            } else {
                warmthMatrix.set(
                    floatArrayOf(
                        1f + warmthValue * 0.2f, 0f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 1f - warmthValue * 0.2f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }
            colorMatrix.postConcat(warmthMatrix)
        }

        paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)

        // Apply sharpening if needed
        if (sharpen > 0) {
            applySharpen(resultBitmap, sharpen)
        }

        resultBitmap
    }

    private fun applySharpen(bitmap: Bitmap, amount: Float) {
        // Simple sharpening using convolution
        val sharpness = amount / 100f
        val kernel = floatArrayOf(
            0f, -sharpness, 0f,
            -sharpness, 1f + 4f * sharpness, -sharpness,
            0f, -sharpness, 0f
        )

        // Apply kernel (simplified version)
        // In production, use RenderScript or native code for better performance
    }

    fun createThumbnail(bitmap: Bitmap, size: Int = 100): Bitmap {
        val scale = min(
            size.toFloat() / bitmap.width,
            size.toFloat() / bitmap.height
        )
        val width = (bitmap.width * scale).toInt()
        val height = (bitmap.height * scale).toInt()

        return Bitmap.createScaledBitmap(bitmap, width, height, true)
    }

    suspend fun saveBitmap(
        bitmap: Bitmap,
        quality: Int = 95
    ): ByteArray = withContext(Dispatchers.IO) {
        val stream = java.io.ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        stream.toByteArray()
    }
}