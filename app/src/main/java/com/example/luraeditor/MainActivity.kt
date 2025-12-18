package com.example.luraeditor

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.luraeditor.ui.screens.EditorScreen
import com.example.luraeditor.ui.screens.HomeScreen
import com.example.luraeditor.ui.theme.PhotoEditorTheme
import com.example.luraeditor.viewmodel.EditorViewModel
import com.example.luraeditor.viewmodel.EditorViewModelFactory
import java.io.OutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PhotoEditorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PhotoEditorApp()
                }
            }
        }
    }

    @Composable
    fun PhotoEditorApp() {
        // ✅ الحل: استخدام Factory مع Context
        val context = LocalContext.current
        val viewModel: EditorViewModel = viewModel(
            factory = EditorViewModelFactory()
        )

        var currentScreen by remember { mutableStateOf("home") }

        when (currentScreen) {
            "home" -> {
                HomeScreen(
                    viewModel = viewModel,
                    onImageSelected = { currentScreen = "editor" }
                )
            }

            "editor" -> {
                EditorScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = "home" },
                    onSave = { bitmap ->
                        saveBitmapToGallery(bitmap)
                    }
                )
            }
        }
    }

    private fun saveBitmapToGallery(bitmap: Bitmap) {
        val filename = "PhotoEditor_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PhotoEditor")
                }

                val imageUri = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                )

                fos = imageUri?.let { resolver.openOutputStream(it) }
            } else {
                @Suppress("DEPRECATION")
                val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(
                    android.os.Environment.DIRECTORY_PICTURES
                ).toString() + "/PhotoEditor"

                val file = java.io.File(imagesDir)
                if (!file.exists()) {
                    file.mkdirs()
                }

                val image = java.io.File(imagesDir, filename)
                fos = java.io.FileOutputStream(image)
            }

            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos!!)
            fos.close()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}