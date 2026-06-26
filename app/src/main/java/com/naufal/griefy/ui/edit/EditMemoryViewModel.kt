package com.naufal.griefy.ui.edit

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.memory.memories.AddImagesUseCase
import com.naufal.griefy.domain.usecase.memory.memories.AddTagUseCase
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoryDetailUseCase
import com.naufal.griefy.domain.usecase.memory.memories.UpdateMemoryUseCase
import com.naufal.griefy.domain.usecase.network.CheckNetworkUseCase
import com.naufal.griefy.domain.usecase.memory.song.GetSongDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMemoryViewModel @Inject constructor(
    private val getMemoryDetailUseCase: GetMemoryDetailUseCase,
    private val updateMemoryUseCase: UpdateMemoryUseCase,
    private val checkNetworkUseCase: CheckNetworkUseCase,
    private val getSongDetailsUseCase: GetSongDetailsUseCase,
    private val addTagUseCase: AddTagUseCase,
    private val addImagesUseCase: AddImagesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])

    private val _uiState = MutableStateFlow(EditMemoryState())
    val uiState: StateFlow<EditMemoryState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getMemoryDetailUseCase(memoryId).collect { detail ->
                val memory = detail.memory
                memory?.let { m ->
                    _uiState.update { state ->
                        state.copy(
                            currentMemory = m,
                            titleText = m.title,
                            contentText = m.content,
                            isPublic = m.isPublic,
                            tagsList = m.tags,
                            selectedImageUris = m.imageUris.map { it.toUri() },
                            selectedSongTrackId = m.songTrackId,
                            selectedSongTitle = m.songTitle
                        )
                    }

                    m.songTrackId?.let { trackId ->
                        val song = getSongDetailsUseCase(trackId)
                        song?.let { s ->
                            _uiState.update { state ->
                                state.copy(
                                    selectedSongTitle = s.title,
                                    selectedSongArtist = s.artistName,
                                    selectedSongImageUrl = s.imageUrl
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun onContentChange(newText: String) {
        _uiState.update { it.copy(contentText = newText) }
    }

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(titleText = newTitle) }
    }

    fun onPrivacyChange(newStatus: Boolean) {
        _uiState.update { it.copy(isPublic = newStatus) }
    }

    fun addTag(tag: String) {
        _uiState.update { state ->
            state.copy(tagsList = addTagUseCase(state.tagsList, tag))
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { state ->
            state.copy(tagsList = state.tagsList - tag)
        }
    }

    fun setSelectedSong(trackId: String?, title: String?, artist: String?, imageUrl: String?) {
        _uiState.update { state ->
            state.copy(
                selectedSongTrackId = trackId,
                selectedSongTitle = title,
                selectedSongArtist = artist,
                selectedSongImageUrl = imageUrl
            )
        }
    }

    fun addImages(newUris: List<Uri>) {
        _uiState.update { state ->
            val current = state.selectedImageUris.map { it.toString() }
            val new = newUris.map { it.toString() }
            val result = addImagesUseCase(current, new)
            state.copy(selectedImageUris = result.map { it.toUri() })
        }
    }

    fun removeImage(uriToRemove: Uri) {
        _uiState.update { state ->
            state.copy(selectedImageUris = state.selectedImageUris.filter { it != uriToRemove })
        }
    }

    fun updateMemory(onUpdateSuccess: () -> Unit) {
        val state = _uiState.value
        if (state.selectedImageUris.isNotEmpty() && !checkNetworkUseCase()) {
            _uiState.update { it.copy(showOfflineWarningDialog = true) }
            return
        }
        performUpdate(onUpdateSuccess)
    }

    fun dismissOfflineWarningDialog() {
        _uiState.update { it.copy(showOfflineWarningDialog = false) }
    }

    fun updateMemoryAnyway(onUpdateSuccess: () -> Unit) {
        _uiState.update { it.copy(showOfflineWarningDialog = false) }
        performUpdate(onUpdateSuccess)
    }

    private fun performUpdate(onUpdateSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            state.currentMemory?.let { oldMemory ->
                updateMemoryUseCase(
                    oldMemory = oldMemory,
                    title = state.titleText,
                    content = state.contentText,
                    tags = state.tagsList,
                    isPublic = state.isPublic,
                    imageUris = state.selectedImageUris.map { it.toString() },
                    songTrackId = state.selectedSongTrackId,
                    songTitle = state.selectedSongTitle
                )
                onUpdateSuccess()
            }
        }
    }
}