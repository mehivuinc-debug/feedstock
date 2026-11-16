package com.example.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PhotoStorageManager(private val context: Context) {

    private val photosDir: File
        get() = File(context.filesDir, "progress_photos").apply {
            if (!exists()) mkdirs()
        }

    private val tempCameraDir: File
        get() = File(context.cacheDir, "camera_temp").apply {
            if (!exists()) mkdirs()
        }

    fun createCameraOutputUri(): Pair<Uri, File> {
        val file = File(tempCameraDir, "cam_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return uri to file
    }

    suspend fun saveCompressedPhotoFromUri(sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOpts)
            }
            val maxDim = 1080
            var sampleSize = 1
            while (boundsOpts.outWidth / sampleSize > maxDim * 2 ||
                boundsOpts.outHeight / sampleSize > maxDim * 2
            ) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOpts)
            } ?: return@withContext null

            val scaled = scaleDownIfNeeded(decoded, maxDim)
            val destFile = File(photosDir, "odot_${UUID.randomUUID()}.jpg")
            FileOutputStream(destFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
            }
            if (scaled != decoded) decoded.recycle()
            scaled.recycle()
            destFile.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    suspend fun saveCompressedPhotoFromFile(tempFile: File): String? = withContext(Dispatchers.IO) {
        if (!tempFile.exists() || tempFile.length() == 0L) return@withContext null
        try {
            val uri = Uri.fromFile(tempFile)
            val saved = saveCompressedPhotoFromUri(uri)
            tempFile.delete()
            saved
        } catch (_: Exception) {
            null
        }
    }

    suspend fun deleteLocalFiles(paths: List<String>) = withContext(Dispatchers.IO) {
        paths.forEach { path ->
            if (!path.startsWith("res://")) {
                runCatching {
                    val f = File(path)
                    if (f.exists() && f.parentFile?.name == "progress_photos") {
                        f.delete()
                    }
                }
            }
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxWidthOrHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxWidthOrHeight && height <= maxWidthOrHeight) return bitmap
        val ratio = minOf(
            maxWidthOrHeight.toFloat() / width.toFloat(),
            maxWidthOrHeight.toFloat() / height.toFloat()
        )
        val newW = (width * ratio).toInt().coerceAtLeast(1)
        val newH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}
