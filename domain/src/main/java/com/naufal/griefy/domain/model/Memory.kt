package com.naufal.griefy.domain.model

data class Memory(
    val id: Int = 0,
    val title: String,
    val content: String,
    val imageUris: List<String>,
    val createdAt : Long,
    val updatedAt : Long = createdAt,
    val tags : List<String>,
    val isPublic : Boolean = false,
    val songTrackId: String? = null,
    val songTitle: String? = null,
    val isTrashed: Boolean = false,
    val userName: String? = null,
    val userAvatar: String? = null,
    val userId: String? = null,
    val isSaved: Boolean = false
) {
    companion object {
        const val DEFAULT_USERNAME = "Guest"
    }
}
