package com.naufal.griefy.ui.edit

import android.net.Uri
import androidx.core.net.toUri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoryDetailUseCase
import com.naufal.griefy.domain.usecase.memory.memories.UpdateMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMemoryViewModel @Inject constructor(
    private val getMemoryDetailUseCase: GetMemoryDetailUseCase,
    private val updateMemoryUseCase: UpdateMemoryUseCase,
    private val repository: MemoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])
    private var currentMemory: Memory? = null

    var titleText by mutableStateOf("")
        private set

    var contentText by mutableStateOf("")
        private set
    var isPublic by mutableStateOf(false)
        private set

    var tagsList by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedImageUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    var selectedSongTrackId by mutableStateOf<String?>(null)
        private set

    var selectedSongTitle by mutableStateOf<String?>(null)
        private set

    var selectedSongArtist by mutableStateOf<String?>(null)
        private set

    var selectedSongImageUrl by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            getMemoryDetailUseCase(memoryId).collect { detail ->
                val memory = detail.memory
                currentMemory = memory
                memory?.let { m ->
                    titleText = m.title
                    contentText = m.content
                    isPublic = m.isPublic
                    tagsList = m.tags
                    selectedImageUris = m.imageUris.map { uriString -> uriString.toUri() }
                    selectedSongTrackId = m.songTrackId
                    selectedSongTitle = m.songTitle

                    m.songTrackId?.let { trackId ->
                        val song = repository.getSongDetails(trackId)
                        song?.let { s ->
                            selectedSongTitle = s.title
                            selectedSongArtist = s.artistName
                            selectedSongImageUrl = s.imageUrl
                        }
                    }
                }
            }
        }
    }

    fun onContentChange(newText: String) {
        contentText = newText
    }

    fun onTitleChange(newTitle: String) {
        titleText = newTitle
    }

    fun onPrivacyChange(newStatus: Boolean) {
        isPublic = newStatus
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

    fun addImages(newUris: List<Uri>) {
        val combinedList = (selectedImageUris + newUris).distinct().take(5)
        selectedImageUris = combinedList
    }

    fun removeImage(uriToRemove: Uri) {
        selectedImageUris = selectedImageUris.filter { it != uriToRemove }
    }

    fun updateMemory(onUpdateSuccess: () -> Unit) {
        viewModelScope.launch {
            currentMemory?.let { oldMemory ->
                updateMemoryUseCase(
                    oldMemory = oldMemory,
                    title = titleText,
                    content = contentText,
                    tags = tagsList,
                    isPublic = isPublic,
                    imageUris = selectedImageUris.map { it.toString() },
                    songTrackId = selectedSongTrackId,
                    songTitle = selectedSongTitle
                )
                onUpdateSuccess()
            }
        }
    }
}