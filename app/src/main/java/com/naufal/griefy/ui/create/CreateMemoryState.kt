package com.naufal.griefy.ui.create
import android.net.Uri
data class CreateMemoryState(
    val titleText: String = "",
    val contentText: String = "",
    val isPublic: Boolean = false,
    val selectedImageUris: List<Uri> = emptyList(),
    val tagsList: List<String> = emptyList(),
    val selectedSongTrackId: String? = null,
    val selectedSongTitle: String? = null,
    val selectedSongArtist: String? = null,
    val selectedSongImageUrl: String? = null,
    val showOfflineWarningDialog: Boolean = false
) {
    val hasChanges: Boolean
        get() = titleText.isNotBlank() || contentText.isNotBlank() || selectedImageUris.isNotEmpty() || tagsList.isNotEmpty() || selectedSongTrackId != null
}
