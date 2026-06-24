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
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])

    private val _uiState = MutableStateFlow(DetailState())
    val uiState: StateFlow<DetailState> = _uiState.asStateFlow()

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