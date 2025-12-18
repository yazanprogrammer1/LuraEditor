package com.example.luraeditor.domain.processor

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Matrix4f
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicColorMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OptimizedImageProcessor(private val context: Context) {

    private val renderScript: RenderScript by lazy {
        RenderScript.create(context)
    }

    suspend fun applyEditsOptimized(
        bitmap: Bitmap,
        brightness: Float,
        contrast: Float,
        saturation: Float,
        warmth: Float
    ): Bitmap = withContext(Dispatchers.Default) {

        val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

        try {
            val input = Allocation.createFromBitmap(renderScript, bitmap)
            val output = Allocation.createFromBitmap(renderScript, outputBitmap)

            // Create ScriptIntrinsicColorMatrix
            val colorMatrix = ScriptIntrinsicColorMatrix.create(renderScript)

            // Build color matrix
            val matrix = Matrix4f()

            // Apply brightness
            val brightnessValue = (brightness - 1f) * 255f
            matrix.set(0, 4, brightnessValue)
            matrix.set(1, 4, brightnessValue)
            matrix.set(2, 4, brightnessValue)

            // Apply contrast
            val scale = contrast
            val translate = (1f - contrast) / 2f * 255f
            for (i in 0..2) {
                matrix.set(i, i, scale)
                matrix.set(i, 4, translate)
            }

            colorMatrix.setColorMatrix(matrix)
            colorMatrix.forEach(input, output)

            // Apply saturation
            if (saturation != 1f) {
                val satMatrix = android.graphics.ColorMatrix()
                satMatrix.setSaturation(saturation)

                val rsMatrix = Matrix4f(satMatrix.array)
                colorMatrix.setColorMatrix(rsMatrix)
                colorMatrix.forEach(output, output)
            }

            // Apply warmth
            if (warmth != 0f) {
                applyWarmth(output, warmth)
            }

            output.copyTo(outputBitmap)

            input.destroy()
            output.destroy()
            colorMatrix.destroy()

        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to standard processing
            return@withContext ImageProcessor.applyEdits(
                bitmap, brightness, contrast, saturation, warmth, 0f
            )
        }

        outputBitmap
    }

    private fun applyWarmth(allocation: Allocation, warmth: Float) {
        val script = ScriptIntrinsicColorMatrix.create(renderScript)
        val warmthMatrix = Matrix4f()

        val warmthValue = warmth / 100f
        if (warmth > 0) {
            // Warm: increase red, decrease blue
            warmthMatrix.set(0, 0, 1f + warmthValue * 0.3f)
            warmthMatrix.set(2, 2, 1f - warmthValue * 0.3f)
        } else {
            // Cool: decrease red, increase blue
            warmthMatrix.set(0, 0, 1f + warmthValue * 0.3f)
            warmthMatrix.set(2, 2, 1f - warmthValue * 0.3f)
        }

        script.setColorMatrix(warmthMatrix)
        script.forEach(allocation, allocation)
        script.destroy()
    }

    fun cleanup() {
        renderScript.destroy()
    }
}