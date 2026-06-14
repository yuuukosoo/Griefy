package com.naufal.griefy.domain.model

data class Memory(
    val id: Int = 0,
    val content: String,
    val imageUris: List<String>,
    val createAt : Long,
    val tags : List<String>,
    val isPublic : Boolean = false,
    val songTracked: String? = null,
    val isTrashed: Boolean = false
)
