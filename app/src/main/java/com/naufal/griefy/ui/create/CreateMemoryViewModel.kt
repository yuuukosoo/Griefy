package com.naufal.griefy.ui.create

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMemoryViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {

    var titleText by mutableStateOf("")
        private set

    var contentText by mutableStateOf("")
        private set

    var isPublic by mutableStateOf(false)
        private set

    var selectedImageUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    var tagsList by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedSongTrackId by mutableStateOf<String?>(null)
        private set

    var selectedSongTitle by mutableStateOf<String?>(null)
        private set

    var selectedSongArtist by mutableStateOf<String?>(null)
        private set

    var selectedSongImageUrl by mutableStateOf<String?>(null)
        private set

    fun onTitleChange(newTitle: String) {
        titleText = newTitle
    }

    fun onContentChange(newContent: String) {
        contentText = newContent
    }

    fun onPrivacyChange(newIsPublic: Boolean) {
        isPublic = newIsPublic
    }

    fun addTag(tag: String) {
        val clean = tag.trim()
        if (clean.isNotEmpty() && !tagsList.contains(clean)) {
            tagsList = tagsList + clean
        }
    }

    fun removeTag(tag: String) {
        tagsList = tagsList - tag
    }

    fun setSelectedSong(trackId: String?, title: String?, artist: String?, imageUrl: String?) {
        selectedSongTrackId = trackId
        selectedSongTitle = title
        selectedSongArtist = artist
        selectedSongImageUrl = imageUrl
    }

    fun addImages(uris: List<Uri>) {
        val combined = (selectedImageUris + uris).distinct().take(5)
        selectedImageUris = combined
    }

    fun removeImage(uri: Uri) {
        selectedImageUris = selectedImageUris.filter { it != uri }
    }

    fun saveMemory(onSaveSuccess: () -> Unit) {
        viewModelScope.launch {
            val newMemory = Memory(
                title = titleText,
                content = contentText,
                imageUris = selectedImageUris.map { it.toString() },
                createdAt = System.currentTimeMillis(),
                tags = if (tagsList.isEmpty()) listOf("Kenangan Baru") else tagsList,
                isPublic = isPublic,
                songTrackId = selectedSongTrackId,
                songTitle = selectedSongTitle,
                isTrashed = false
            )

            repository.addMemory(newMemory)
            onSaveSuccess()
        }
    }
}