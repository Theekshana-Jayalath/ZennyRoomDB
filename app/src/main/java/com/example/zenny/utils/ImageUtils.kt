
package com.example.zenny.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageUtils {

    /**
     * Copies an image from a given Uri to the app's internal storage.
     * This provides a permanent and safe file path to access the image later.
     *
     * @param context The context.
     * @param uri The content URI of the image to copy.
     * @return A permanent File object pointing to the copied image, or null on failure.
     */
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
