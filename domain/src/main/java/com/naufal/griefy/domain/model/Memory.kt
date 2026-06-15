package com.naufal.griefy.domain.model

data class Memory(
    val id: Int = 0,
    val content: String,
    val imageUris: List<String>,
    val createdAt : Long,
    val tags : List<String>,
    val isPublic : Boolean = false,
    val songTrackId: String? = null,
    val isTrashed: Boolean = false
)
