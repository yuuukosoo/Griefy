package com.naufal.griefy.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateMemoryViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {

    fun saveMemory(content: String, isPublic: Boolean, imageUris: List<String>, onSaveSuccess: () -> Unit) {
        viewModelScope.launch {
            val newMemory = Memory(
                content = content,
                imageUris = imageUris,
                createdAt = System.currentTimeMillis(), 
                tags = listOf("Kenangan Baru"),
                isPublic = isPublic,
                songTrackId = null,
                isTrashed = false
            )

            repository.addMemory(newMemory) 
            onSaveSuccess()
        }
    }
}