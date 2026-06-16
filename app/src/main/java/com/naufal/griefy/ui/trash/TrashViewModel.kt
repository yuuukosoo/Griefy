package com.naufal.griefy.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {


    val trashedMemories: StateFlow<List<Memory>> = repository.getTrashedMemories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun restoreMemory(id: Int) {
        viewModelScope.launch {
            repository.restoreFromTrash(id)
        }
    }

    fun deletePermanently(id: Int) {
        viewModelScope.launch {
            repository.deletePermanently(id)
        }
    }


    fun emptyTrash() {
        viewModelScope.launch {
            trashedMemories.value.forEach { memory ->
                repository.deletePermanently(memory.id)
            }
        }
    }
}