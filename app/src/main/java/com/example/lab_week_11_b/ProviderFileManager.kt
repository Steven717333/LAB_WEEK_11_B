package com.example.lab_week_11_b

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import org.apache.commons.io.IOUtils
import java.io.File
import java.util.concurrent.Executor

class ProviderFileManager(
    private val context: Context,
    private val fileHelper: FileHelper,
    private val contentResolver: ContentResolver,
    private val executor: Executor,
    private val mediaContentHelper: MediaContentHelper
) {

    // Generate FileInfo untuk foto
    fun generatePhotoUri(time: Long): FileInfo {
        val name = "img_$time.jpg"

        val file = File(
            context.getExternalFilesDir(fileHelper.getPicturesFolder()),
            name
        )

        return FileInfo(
            uri = fileHelper.getUriFromFile(file),
            file = file,
            name = name,
            relativePath = fileHelper.getPicturesFolder(),
            mimeType = "image/jpeg"
        )
    }

    // Generate FileInfo untuk video
    fun generateVideoUri(time: Long): FileInfo {
        val name = "video_$time.mp4"

        val file = File(
            context.getExternalFilesDir(fileHelper.getVideosFolder()),
            name
        )

        return FileInfo(
            uri = fileHelper.getUriFromFile(file),
            file = file,
            name = name,
            relativePath = fileHelper.getVideosFolder(),
            mimeType = "video/mp4"
        )
    }

    // Insert IMAGE ke MediaStore
    fun insertImageToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                it,
                mediaContentHelper.getImageContentUri(),
                mediaContentHelper.generateImageContentValues(it)
            )
        }
    }

    // Insert VIDEO ke MediaStore
    fun insertVideoToStore(fileInfo: FileInfo?) {
        fileInfo?.let {
            insertToStore(
                it,
                mediaContentHelper.getVideoContentUri(),
                mediaContentHelper.generateVideoContentValues(it)
            )
        }
    }

    // Copy file ke MediaStore
    private fun insertToStore(
        fileInfo: FileInfo,
        contentUri: Uri,
        contentValues: ContentValues
    ) {
        executor.execute {

            val insertedUri = contentResolver.insert(contentUri, contentValues)

            insertedUri?.let { uri ->

                val inputStream = contentResolver.openInputStream(fileInfo.uri)
                val outputStream = contentResolver.openOutputStream(uri)

                // Pastikan stream ditutup otomatis
                inputStream?.use { input ->
                    outputStream?.use { output ->
                        IOUtils.copy(input, output)
                    }
                }
            }
        }
    }
}
