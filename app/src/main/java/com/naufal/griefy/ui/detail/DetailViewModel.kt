package com.naufal.griefy.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.Song
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MemoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])

    val memory: StateFlow<Memory?> = repository.getMemoryByIdAsFlow(memoryId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
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