package com.naufal.griefy.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.graphics.scale
import androidx.core.net.toUri
import com.naufal.griefy.data.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudinaryUploader @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @param:dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {

    suspend fun uploadImage(uriString: String): String? {
        if (uriString.startsWith("https://") || uriString.startsWith("http://")) return uriString
        if (uriString.isBlank()) return null

        return withContext(Dispatchers.IO) {
            try {
                val uri = uriString.toUri()
                val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (originalBitmap == null) return@withContext null

                // Resize to max 1080px before upload
                val maxDimension = 1080
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scaledBitmap = if (width > maxDimension || height > maxDimension) {
                    val ratio = width.toFloat() / height.toFloat()
                    if (ratio > 1) originalBitmap.scale(maxDimension, (maxDimension / ratio).toInt(), true)
                    else originalBitmap.scale((maxDimension * ratio).toInt(), maxDimension, true)
                } else originalBitmap

                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val imageBytes = outputStream.toByteArray()
                outputStream.close()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("upload_preset", BuildConfig.CLOUDINARY_UPLOAD_PRESET)
                    .addFormDataPart("file", "image.jpg", imageBytes.toRequestBody("image/jpeg".toMediaType()))
                    .build()

                val request = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/${BuildConfig.CLOUDINARY_CLOUD_NAME}/image/upload")
                    .post(requestBody)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    JSONObject(body).getString("secure_url")
                } else {
                    android.util.Log.e("CLOUDINARY_UPLOAD", "Upload failed: ${response.code} - ${response.body?.string()}")
                    null
                }
            } catch (e: Exception) {
                android.util.Log.e("CLOUDINARY_UPLOAD", "Error uploading to Cloudinary: ${e.message}", e)
                null
            }
        }
    }
}
