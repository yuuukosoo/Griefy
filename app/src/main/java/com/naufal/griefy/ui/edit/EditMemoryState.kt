package com.naufal.griefy.ui.edit

import android.net.Uri
import com.naufal.griefy.domain.model.Memory

data class EditMemoryState(
    val titleText: String = "",
    val contentText: String = "",
    val isPublic: Boolean = false,
    val tagsList: List<String> = emptyList(),
    val selectedImageUris: List<Uri> = emptyList(),
    val selectedSongTrackId: String? = null,
    val selectedSongTitle: String? = null,
    val selectedSongArtist: String? = null,
    val selectedSongImageUrl: String? = null,
    val currentMemory: Memory? = null,
    val showOfflineWarningDialog: Boolean = false
)
