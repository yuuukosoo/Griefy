package com.naufal.griefy.util

fun String.toImageModel(): Any {
    return if (this.startsWith("base64:")) {
        try {
            val base64Data = this.substringAfter("base64:")
            android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
        } catch (e: Exception) {
            this
        }
    } else {
        this
    }
}
