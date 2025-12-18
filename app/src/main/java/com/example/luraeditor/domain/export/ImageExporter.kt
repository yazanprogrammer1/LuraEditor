package com.example.luraeditor.domain.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageExporter {

    enum class ExportFormat(val extension: String, val mimeType: String) {
        JPEG("jpg", "image/jpeg"),
        PNG("png", "image/png"),
        WEBP("webp", "image/webp")
    }

    data class ExportOptions(
        val format: ExportFormat = ExportFormat.JPEG,
        val quality: Int = 95,
        val filename: String? = null
    )

    suspend fun exportToGallery(
        context: Context,
        bitmap: Bitmap,
        options: ExportOptions = ExportOptions()
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val filename = options.filename
                ?: "PhotoEditor_${System.currentTimeMillis()}.${options.format.extension}"

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToMediaStore(context, bitmap, filename, options)
            } else {
                saveToExternalStorage(bitmap, filename, options)
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun saveToMediaStore(
        context: Context,
        bitmap: Bitmap,
        filename: String,
        options: ExportOptions
    ): Uri {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, options.format.mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/PhotoEditor")
        }

        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw Exception("Failed to create MediaStore entry")

        resolver.openOutputStream(uri)?.use { outputStream ->
            compressBitmap(bitmap, outputStream, options)
        } ?: throw Exception("Failed to open output stream")

        return uri
    }

    private fun saveToExternalStorage(
        bitmap: Bitmap,
        filename: String,
        options: ExportOptions
    ): Uri {
        val imagesDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_PICTURES
        ).toString() + "/PhotoEditor"

        val file = File(imagesDir)
        if (!file.exists()) {
            file.mkdirs()
        }

        val imageFile = File(imagesDir, filename)
        FileOutputStream(imageFile).use { outputStream ->
            compressBitmap(bitmap, outputStream, options)
        }

        return Uri.fromFile(imageFile)
    }

    private fun compressBitmap(
        bitmap: Bitmap,
        outputStream: OutputStream,
        options: ExportOptions
    ) {
        val format = when (options.format) {
            ExportFormat.JPEG -> Bitmap.CompressFormat.JPEG
            ExportFormat.PNG -> Bitmap.CompressFormat.PNG
            ExportFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
        }

        bitmap.compress(format, options.quality, outputStream)
    }

    suspend fun shareImage(
        context: Context,
        bitmap: Bitmap,
        options: ExportOptions = ExportOptions()
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val cacheDir = File(context.cacheDir, "shared_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val filename = "share_${System.currentTimeMillis()}.${options.format.extension}"
            val file = File(cacheDir, filename)

            FileOutputStream(file).use { outputStream ->
                compressBitmap(bitmap, outputStream, options)
            }

            val uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}