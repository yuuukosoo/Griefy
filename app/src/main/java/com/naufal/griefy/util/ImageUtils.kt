package com.naufal.griefy.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

fun String.toImageModel(): Any {
    return if (this.startsWith("base64:")) {
        try {
            val base64Data = this.substringAfter("base64:")
            Base64.decode(base64Data, Base64.DEFAULT)
        } catch (e: Exception) {
            this
        }
    } else {
        try {
            Uri.parse(this)
        } catch (e: Exception) {
            this
        }
    }
}

fun getBase64FromUri(context: Context, uriString: String): String? {
    if (uriString.startsWith("base64:") || uriString.isBlank()) return uriString
    return try {
        val uri = Uri.parse(uriString)
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (originalBitmap == null) return null

        val maxDimension = 800
        val width = originalBitmap.width
        val height = originalBitmap.height
        val scaledBitmap = if (width > maxDimension || height > maxDimension) {
            val ratio = width.toFloat() / height.toFloat()
            val newWidth: Int
            val newHeight: Int
            if (ratio > 1) {
                newWidth = maxDimension
                newHeight = (maxDimension / ratio).toInt()
            } else {
                newHeight = maxDimension
                newWidth = (maxDimension * ratio).toInt()
            }
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()
        outputStream.close()

        "base64:" + Base64.encodeToString(imageBytes, Base64.DEFAULT).trim()
    } catch (e: Exception) {
        android.util.Log.e("BASE64_ERROR", "Error converting image to Base64: ${e.message}", e)
        null
    }
}
