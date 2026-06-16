package com.naufal.griefy.domain.model

data class Song(
    val trackId: String,
    val title: String,
    val artistName: String,
    val imageUrl: String,
    val previewUrl: String?
)