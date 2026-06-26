package com.naufal.griefy.ui.create

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Application
import com.naufal.griefy.R
import com.naufal.griefy.domain.usecase.memory.memories.AddMemoryUseCase
import com.naufal.griefy.domain.usecase.network.CheckNetworkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMemoryViewModel @Inject constructor(
    private val addMemoryUseCase: AddMemoryUseCase,
    private val checkNetworkUseCase: CheckNetworkUseCase,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateMemoryState())
    val uiState: StateFlow<CreateMemoryState> = _uiState.asStateFlow()

    fun onTitleChange(newTitle: String) {
        _uiState.update { it.copy(titleText = newTitle) }
    }

    fun onContentChange(newContent: String) {
        _uiState.update { it.copy(contentText = newContent) }
    }

    fun onPrivacyChange(newIsPublic: Boolean) {
        _uiState.update { it.copy(isPublic = newIsPublic) }
    }

    fun addTag(tag: String) {
        val clean = tag.trim()
        _uiState.update { state ->
            if (clean.isNotEmpty() && !state.tagsList.contains(clean)) {
                state.copy(tagsList = state.tagsList + clean)
            } else {
                state
            }
        }
    }

    fun removeTag(tag: String) {
        _uiState.update { state ->
            state.copy(tagsList = state.tagsList - tag)
        }
    }

    fun setSelectedSong(trackId: String?, title: String?, artist: String?, imageUrl: String?) {
        _uiState.update {
            it.copy(
                selectedSongTrackId = trackId,
                selectedSongTitle = title,
                selectedSongArtist = artist,
                selectedSongImageUrl = imageUrl
            )
        }
    }

    fun addImages(uris: List<Uri>) {
        _uiState.update { state ->
            val combined = (state.selectedImageUris + uris).distinct().take(5)
            state.copy(selectedImageUris = combined)
        }
    }

    fun removeImage(uri: Uri) {
        _uiState.update { state ->
            state.copy(selectedImageUris = state.selectedImageUris.filter { it != uri })
        }
    }

    fun saveMemory(onSaveSuccess: () -> Unit) {
        val state = _uiState.value
        // Periksa koneksi hanya jika ada foto yang dipilih
        if (state.selectedImageUris.isNotEmpty() && !checkNetworkUseCase()) {
            _uiState.update { it.copy(showOfflineWarningDialog = true) }
            return
        }
        performSave(onSaveSuccess)
    }

    fun dismissOfflineWarningDialog() {
        _uiState.update { it.copy(showOfflineWarningDialog = false) }
    }

    fun saveMemoryAnyway(onSaveSuccess: () -> Unit) {
        _uiState.update { it.copy(showOfflineWarningDialog = false) }
        performSave(onSaveSuccess)
    }

    private fun performSave(onSaveSuccess: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            addMemoryUseCase(
                title = state.titleText,
                content = state.contentText,
                imageUris = state.selectedImageUris.map { it.toString() },
                tags = state.tagsList,
                isPublic = state.isPublic,
                songTrackId = state.selectedSongTrackId,
                songTitle = state.selectedSongTitle,
                defaultTagString = application.getString(R.string.default_memory_tag)
            )
            onSaveSuccess()
        }
    }
}