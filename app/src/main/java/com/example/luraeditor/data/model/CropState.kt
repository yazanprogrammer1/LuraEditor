package com.example.luraeditor.data.model

import androidx.compose.ui.geometry.Rect

data class CropState(
    val cropRect: Rect = Rect.Zero,
    val rotation: Float = 0f,
    val aspectRatio: AspectRatio = AspectRatio.FREE
)

enum class AspectRatio(val ratio: Float?, val label: String) {
    FREE(null, "حر"),
    SQUARE(1f, "1:1"),
    PORTRAIT_4_5(4f / 5f, "4:5"),
    PORTRAIT_9_16(9f / 16f, "9:16"),
    LANDSCAPE_16_9(16f / 9f, "16:9"),
    LANDSCAPE_5_4(5f / 4f, "5:4")
}