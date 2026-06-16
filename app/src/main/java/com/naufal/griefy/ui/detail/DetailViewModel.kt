package com.naufal.griefy.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MemoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])

    private val _memory = MutableStateFlow<Memory?>(null)
    val memory = _memory.asStateFlow()

    init {
        loadMemory()
    }

    private fun loadMemory() {
        viewModelScope.launch {
            _memory.value = repository.getMemoryById(memoryId)
        }
    }

    fun moveToTrash(onDeleteSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.moveToTrash(memoryId)
            onDeleteSuccess() // Beritahu UI kalau sudah sukses dihapus
        }
    }
}