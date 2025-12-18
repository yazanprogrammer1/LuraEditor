package com.example.luraeditor.data.model

data class Filter(
    val id: String,
    val name: String,
    val brightness: Float = 1f,      // 0.0 - 2.0
    val contrast: Float = 1f,        // 0.0 - 2.0
    val saturation: Float = 1f,      // 0.0 - 2.0
    val warmth: Float = 0f,          // -50 to 50
    val hue: Float = 0f              // -180 to 180
) {
    companion object {
        val FILTERS = listOf(
            Filter(
                id = "none",
                name = "Original"
            ),
            Filter(
                id = "clean",
                name = "Clean",
                brightness = 1.05f,
                contrast = 1.05f,
                saturation = 0.95f
            ),
            Filter(
                id = "soft",
                name = "Soft",
                brightness = 1.1f,
                contrast = 0.9f,
                saturation = 0.85f
            ),
            Filter(
                id = "dark",
                name = "Dark",
                brightness = 0.7f,
                contrast = 1.2f,
                saturation = 1.1f
            ),
            Filter(
                id = "vintage",
                name = "Vintage",
                brightness = 0.95f,
                contrast = 1.1f,
                saturation = 0.8f,
                warmth = 30f
            ),
            Filter(
                id = "film",
                name = "Film",
                brightness = 1.05f,
                contrast = 1.15f,
                saturation = 1.2f
            ),
            Filter(
                id = "warm",
                name = "Warm",
                brightness = 1.05f,
                contrast = 1f,
                saturation = 1.1f,
                warmth = 40f
            ),
            Filter(
                id = "cool",
                name = "Cool",
                brightness = 1f,
                contrast = 1.05f,
                saturation = 1.05f,
                warmth = -30f
            )
        )
    }
}