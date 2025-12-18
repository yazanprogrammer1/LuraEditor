package com.example.luraeditor.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.luraeditor.data.model.EditHistory
import com.example.luraeditor.data.model.EditState
import com.example.luraeditor.data.model.Filter
import com.example.luraeditor.domain.processor.FilterProcessor
import com.example.luraeditor.domain.processor.ImageProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EditorViewModel : ViewModel() {

    // ═══════════════════════════════════════════════════
    // 📊 STATE MANAGEMENT
    // ═══════════════════════════════════════════════════

    private val _editState = MutableStateFlow(EditState())
    val editState: StateFlow<EditState> = _editState.asStateFlow()

    private val _history = mutableListOf<EditHistory>()

    private val _recentImages = MutableStateFlow<List<Bitmap>>(emptyList())
    val recentImages: StateFlow<List<Bitmap>> = _recentImages.asStateFlow()

    // ═══════════════════════════════════════════════════
    // 🖼️ IMAGE LOADING
    // ═══════════════════════════════════════════════════

    fun loadImage(bitmap: Bitmap) {
        _editState.update {
            it.copy(
                originalBitmap = bitmap,
                editedBitmap = bitmap
            )
        }
        saveToHistory()
    }

    // ═══════════════════════════════════════════════════
    // 🎛️ ADJUSTMENT CONTROLS
    // ═══════════════════════════════════════════════════

    fun updateBrightness(value: Float) {
        _editState.update { it.copy(brightness = value) }
        processImage()
    }

    fun updateContrast(value: Float) {
        _editState.update { it.copy(contrast = value) }
        processImage()
    }

    fun updateSaturation(value: Float) {
        _editState.update { it.copy(saturation = value) }
        processImage()
    }

    fun updateWarmth(value: Float) {
        _editState.update { it.copy(warmth = value) }
        processImage()
    }

    fun updateSharpen(value: Float) {
        _editState.update { it.copy(sharpen = value) }
        processImage()
    }

    // ═══════════════════════════════════════════════════
    // 🎨 FILTER CONTROLS
    // ═══════════════════════════════════════════════════

    fun selectFilter(filter: Filter) {
        _editState.update { it.copy(selectedFilter = filter) }
        saveToHistory()
        processImage()
    }

    fun updateFilterIntensity(intensity: Float) {
        _editState.update { it.copy(filterIntensity = intensity) }
        processImage()
    }

    // ═══════════════════════════════════════════════════
    // 🔄 IMAGE PROCESSING
    // ═══════════════════════════════════════════════════

    private fun processImage() {
        val state = _editState.value
        val original = state.originalBitmap ?: return

        viewModelScope.launch {
            _editState.update { it.copy(isProcessing = true) }

            try {
                // Step 1: Apply filter if selected
                val filteredBitmap = if (state.selectedFilter.id != "none") {
                    FilterProcessor.applyFilter(
                        bitmap = original,
                        filter = state.selectedFilter,
                        intensity = state.filterIntensity
                    )
                } else {
                    original
                }

                // Step 2: Apply manual adjustments
                val finalBitmap = ImageProcessor.applyEdits(
                    bitmap = filteredBitmap,
                    brightness = state.brightness,
                    contrast = state.contrast,
                    saturation = state.saturation,
                    warmth = state.warmth,
                    sharpen = state.sharpen
                )

                _editState.update {
                    it.copy(
                        editedBitmap = finalBitmap,
                        isProcessing = false
                    )
                }
            } catch (e: Exception) {
                _editState.update { it.copy(isProcessing = false) }
                e.printStackTrace()
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // ↩️ UNDO & RESET
    // ═══════════════════════════════════════════════════

    fun reset() {
        _editState.update {
            it.copy(
                brightness = 1f,
                contrast = 1f,
                saturation = 1f,
                warmth = 0f,
                sharpen = 0f,
                selectedFilter = Filter.FILTERS[0],
                filterIntensity = 1f,
                editedBitmap = it.originalBitmap
            )
        }
        _history.clear()
    }

    fun undo() {
        if (_history.isEmpty()) return

        val lastState = _history.removeAt(_history.size - 1)
        _editState.update {
            it.copy(
                brightness = lastState.brightness,
                contrast = lastState.contrast,
                saturation = lastState.saturation,
                warmth = lastState.warmth,
                sharpen = lastState.sharpen,
                selectedFilter = lastState.selectedFilter,
                filterIntensity = lastState.filterIntensity
            )
        }
        processImage()
    }

    private fun saveToHistory() {
        val state = _editState.value
        _history.add(
            EditHistory(
                brightness = state.brightness,
                contrast = state.contrast,
                saturation = state.saturation,
                warmth = state.warmth,
                sharpen = state.sharpen,
                selectedFilter = state.selectedFilter,
                filterIntensity = state.filterIntensity
            )
        )

        // Keep only last 20 states to avoid memory issues
        if (_history.size > 20) {
            _history.removeAt(0)
        }
    }

    fun canUndo(): Boolean = _history.isNotEmpty()

    // ═══════════════════════════════════════════════════
    // 💾 SAVE IMAGE
    // ═══════════════════════════════════════════════════

    fun saveImage(onSaved: (Bitmap) -> Unit) {
        val bitmap = _editState.value.editedBitmap ?: return

        // Add to recent images gallery
        _recentImages.update {
            (listOf(bitmap) + it).take(6)
        }

        onSaved(bitmap)
    }

    // ═══════════════════════════════════════════════════
    // 🔄 ROTATION (Optional - للمستقبل)
    // ═══════════════════════════════════════════════════

    fun rotateImage(degrees: Float) {
        val bitmap = _editState.value.editedBitmap ?: return

        viewModelScope.launch {
            try {
                val matrix = android.graphics.Matrix()
                matrix.postRotate(degrees)

                val rotatedBitmap = Bitmap.createBitmap(
                    bitmap, 0, 0,
                    bitmap.width, bitmap.height,
                    matrix, true
                )

                _editState.update {
                    it.copy(
                        originalBitmap = rotatedBitmap,
                        editedBitmap = rotatedBitmap
                    )
                }

                saveToHistory()
                processImage()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // 🗑️ CLEANUP
    // ═══════════════════════════════════════════════════

    override fun onCleared() {
        super.onCleared()
        // Clean up bitmaps to free memory
        _editState.value.originalBitmap?.recycle()
        _editState.value.editedBitmap?.recycle()
        _recentImages.value.forEach { it.recycle() }
    }
}