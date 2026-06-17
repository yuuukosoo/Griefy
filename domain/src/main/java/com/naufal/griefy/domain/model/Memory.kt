package com.naufal.griefy.domain.model

data class Memory(
    val id: Int = 0,
    val title: String,
    val content: String,
    val imageUris: List<String>,
    val createdAt : Long,
    val tags : List<String>,
    val isPublic : Boolean = false,
    val songTrackId: String? = null,
    val songTitle: String? = null,
    val isTrashed: Boolean = false
)
