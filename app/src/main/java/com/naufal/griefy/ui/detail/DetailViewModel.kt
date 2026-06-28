package com.naufal.griefy.ui.detail
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.memory.memories.BumpMemoryUseCase
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoryDetailUseCase
import com.naufal.griefy.domain.model.LoadSongResult
import com.naufal.griefy.domain.usecase.memory.song.LoadMemorySongUseCase
import com.naufal.griefy.domain.usecase.memory.song.ManageAudioPlaybackUseCase
import com.naufal.griefy.domain.usecase.memory.song.ObserveAudioStateUseCase
import com.naufal.griefy.domain.usecase.memory.song.StopAudioUseCase
import com.naufal.griefy.domain.usecase.memory.trash.MoveToTrashUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class DetailViewModel @Inject constructor(
    getMemoryDetailUseCase: GetMemoryDetailUseCase,
    private val loadMemorySongUseCase: LoadMemorySongUseCase,
    private val manageAudioPlaybackUseCase: ManageAudioPlaybackUseCase,
    private val stopAudioUseCase: StopAudioUseCase,
    observeAudioStateUseCase: ObserveAudioStateUseCase,
    private val moveToTrashUseCase: MoveToTrashUseCase,
    private val bumpMemoryUseCase: BumpMemoryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])
    private val _uiState = MutableStateFlow(DetailState())
    private val audioState = observeAudioStateUseCase()
    val uiState: StateFlow<DetailState> = combine(
        _uiState,
        audioState.isPlaying,
        audioState.currentTrackId,
        audioState.currentPosition,
        audioState.duration
    ) { state, isPlaying, currentTrackId, position, duration ->
        val isCurrentTrack = state.songDetails?.trackId == currentTrackId
        state.copy(
            isPlaying = isCurrentTrack && isPlaying,
            currentPosition = if (isCurrentTrack) position.toFloat() else 0f,
            duration = if (isCurrentTrack) duration.toFloat() else 0f
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DetailState()
    )
    private var hasAutoPlayed = false
    private var loadedSongTrackId: String? = null
    fun onPlayPauseClick() {
        val song = _uiState.value.songDetails ?: return
        manageAudioPlaybackUseCase(song.trackId, song.previewUrl)
    }
    fun stopAudio() {
        stopAudioUseCase()
    }
    fun bumpMemory() {
        val currentMemory = _uiState.value.memory ?: return
        viewModelScope.launch {
            bumpMemoryUseCase(currentMemory)
        }
    }
    init {
        viewModelScope.launch {
            getMemoryDetailUseCase(memoryId).collect { detail ->
                _uiState.update { state ->
                    state.copy(
                        memory = detail.memory,
                        isOwnMemory = detail.isOwnMemory
                    )
                }
                val result = loadMemorySongUseCase(
                    trackId = detail.memory?.songTrackId,
                    loadedTrackId = loadedSongTrackId,
                    currentSong = _uiState.value.songDetails,
                    hasAutoPlayed = hasAutoPlayed,
                    onAutoPlayed = { hasAutoPlayed = true }
                )
                when (result) {
                    is LoadSongResult.Success -> {
                        loadedSongTrackId = result.song.trackId
                        _uiState.update { it.copy(songDetails = result.song) }
                    }
                    is LoadSongResult.NoSong, is LoadSongResult.FetchFailed -> {
                        loadedSongTrackId = null
                        _uiState.update { it.copy(songDetails = null) }
                    }
                    is LoadSongResult.Unchanged -> Unit
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