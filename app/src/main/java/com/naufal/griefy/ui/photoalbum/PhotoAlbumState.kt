package com.naufal.griefy.ui.photoalbum

import com.naufal.griefy.domain.model.PhotoAlbumGroup

data class PhotoAlbumState(
    val photoGroups: List<PhotoAlbumGroup> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
