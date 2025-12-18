package com.example.luraeditor.data.model

import android.graphics.Bitmap

data class EditState(
    val originalBitmap: Bitmap? = null,
    val editedBitmap: Bitmap? = null,
    val brightness: Float = 1f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val warmth: Float = 0f,
    val sharpen: Float = 0f,
    val selectedFilter: Filter = Filter.FILTERS[0],
    val filterIntensity: Float = 1f,
    val isProcessing: Boolean = false
)