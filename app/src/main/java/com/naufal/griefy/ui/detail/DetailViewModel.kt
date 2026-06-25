package com.naufal.griefy.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoryDetailUseCase
import com.naufal.griefy.domain.usecase.memory.song.GetSongDetailsUseCase
import com.naufal.griefy.domain.usecase.memory.trash.MoveToTrashUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    getMemoryDetailUseCase: GetMemoryDetailUseCase,
    private val getSongDetailsUseCase: GetSongDetailsUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val manageAudioPlaybackUseCase: com.naufal.griefy.domain.usecase.memory.song.ManageAudioPlaybackUseCase,
    private val audioPlayer: com.naufal.griefy.domain.repository.AudioPlayer,
    private val bumpMemoryUseCase: com.naufal.griefy.domain.usecase.memory.memories.BumpMemoryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])

    private val _uiState = MutableStateFlow(DetailState())
    val uiState: StateFlow<DetailState> = _uiState.asStateFlow()

    val playingTrackId = audioPlayer.currentTrackId
    val isMediaPlaying = audioPlayer.isPlaying
    val currentPosition = audioPlayer.currentPosition
    val duration = audioPlayer.duration

    fun onPlayPauseClick() {
        val song = _uiState.value.songDetails ?: return
        val url = song.previewUrl
        if (!url.isNullOrEmpty()) {
            manageAudioPlaybackUseCase(song.trackId, url)
        }
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    fun bumpMemory() {
        val currentMemory = _uiState.value.memory ?: return
        viewModelScope.launch {
            bumpMemoryUseCase(currentMemory)
        }
    }

    private var hasAutoPlayed = false

    init {
        viewModelScope.launch {
            getMemoryDetailUseCase(memoryId).collect { detail ->
                _uiState.update { state ->
                    state.copy(
                        memory = detail.memory,
                        isOwnMemory = detail.isOwnMemory
                    )
                }

                detail.memory?.songTrackId?.let { trackId ->
                    val song = getSongDetailsUseCase(trackId)
                    _uiState.update { state ->
                        state.copy(songDetails = song)
                    }
                    val url = song?.previewUrl
                    if (!hasAutoPlayed && song != null && !url.isNullOrEmpty()) {
                        hasAutoPlayed = true
                        manageAudioPlaybackUseCase(song.trackId, url)
                    }
                } ?: run {
                    _uiState.update { state ->
                        state.copy(songDetails = null)
                    }
                }
            }
        }
    }

    fun moveToTrash(onDeleteSuccess: () -> Unit) {
        viewModelScope.launch {
            moveToTrashUseCase(memoryId)
            onDeleteSuccess()
        }
    }
}