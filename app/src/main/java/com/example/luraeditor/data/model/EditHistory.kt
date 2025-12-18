package com.example.luraeditor.data.model

data class EditHistory(
    val brightness: Float,
    val contrast: Float,
    val saturation: Float,
    val warmth: Float,
    val sharpen: Float,
    val selectedFilter: Filter,
    val filterIntensity: Float
)