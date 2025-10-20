
package com.example.zenny.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    fun copyImageToInternalStorage(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                // Log.e("ImageUtils", "Failed to get input stream from URI.")
                return null
            }

            // Create a file in the app's private directory
            val file = File(context.filesDir, "profile_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)

            // Copy the bits from instream to outstream
            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            file
        } catch (e: Exception) {
            // Log.e("ImageUtils", "Error copying image to internal storage", e)
            null
        }
    }
}
