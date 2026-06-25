package com.naufal.griefy.data.local

data class MemoryKeys(
    val id: Int,
    val title: String,
    val createdAt: Long,
    val isSaved: Boolean,
    val isTrashed: Boolean,
    val userId: String?
)
