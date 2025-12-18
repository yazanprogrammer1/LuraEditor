package com.example.luraeditor.domain.processor

import android.graphics.Bitmap
import com.example.luraeditor.data.model.Filter

object FilterProcessor {

    suspend fun applyFilter(
        bitmap: Bitmap,
        filter: Filter,
        intensity: Float = 1f
    ): Bitmap {
        if (filter.id == "none") return bitmap

        // Interpolate filter values based on intensity
        val brightness = 1f + (filter.brightness - 1f) * intensity
        val contrast = 1f + (filter.contrast - 1f) * intensity
        val saturation = 1f + (filter.saturation - 1f) * intensity
        val warmth = filter.warmth * intensity

        return ImageProcessor.applyEdits(
            bitmap = bitmap,
            brightness = brightness,
            contrast = contrast,
            saturation = saturation,
            warmth = warmth,
            sharpen = 0f
        )
    }
}