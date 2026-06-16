package com.naufal.griefy.ui.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditMemoryViewModel @Inject constructor(
    private val repository: MemoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])
    private var currentMemory: Memory? = null


    var contentText by mutableStateOf("")
        private set
    var isPublic by mutableStateOf(false)
        private set

    init {

        viewModelScope.launch {
            currentMemory = repository.getMemoryById(memoryId)
            currentMemory?.let {
                contentText = it.content
                isPublic = it.isPublic
            }
        }
    }

    fun onContentChange(newText: String) {
        contentText = newText
    }

    fun onPrivacyChange(newStatus: Boolean) {
        isPublic = newStatus
    }

    fun updateMemory(onUpdateSuccess: () -> Unit) {
        viewModelScope.launch {
            currentMemory?.let { oldMemory ->

                val updatedMemory = oldMemory.copy(
                    content = contentText,
                    isPublic = isPublic
                )
                repository.updateMemory(updatedMemory)
                onUpdateSuccess()
            }
        }
    }
}