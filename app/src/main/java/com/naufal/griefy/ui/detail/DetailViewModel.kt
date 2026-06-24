package com.naufal.griefy.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.Song
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoryDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MemoryRepository,
    getMemoryDetailUseCase: GetMemoryDetailUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])

    private val memoryDetail: StateFlow<com.naufal.griefy.domain.model.MemoryDetail?> = getMemoryDetailUseCase(memoryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val memory: StateFlow<Memory?> = memoryDetail
        .map { it?.memory }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val isOwnMemory: StateFlow<Boolean> = memoryDetail
        .map { it?.isOwnMemory ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _songDetails = MutableStateFlow<Song?>(null)
    val songDetails: StateFlow<Song?> = _songDetails.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getMemoryByIdAsFlow(memoryId).collect { memory ->
                memory?.songTrackId?.let { trackId ->
                    val song = repository.getSongDetails(trackId)
                    _songDetails.value = song
                } ?: run {
                    _songDetails.value = null
                }
            }
        }
    }

    fun moveToTrash(onDeleteSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.moveToTrash(memoryId)
            onDeleteSuccess()
        }
    }
}