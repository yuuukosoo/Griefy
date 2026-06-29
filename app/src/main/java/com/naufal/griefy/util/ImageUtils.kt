package com.naufal.griefy.util
import androidx.core.net.toUri
import android.util.Base64


fun String.toImageModel(): Any {
    return if (this.startsWith("base64:")) {
        try {
            val base64Data = this.substringAfter("base64:")
            Base64.decode(base64Data, Base64.DEFAULT)
        } catch (_: Exception) {
            this
        }
    } else {
        try {
            this.toUri()
        } catch (_: Exception) {
            this
        }
    }
}

