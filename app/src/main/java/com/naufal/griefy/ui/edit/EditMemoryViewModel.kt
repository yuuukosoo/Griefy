package com.naufal.griefy.ui.edit

import android.net.Uri
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


    var selectedImageUris by mutableStateOf<List<Uri>>(emptyList())
        private set

    init {

        viewModelScope.launch {
            currentMemory = repository.getMemoryById(memoryId)
            currentMemory?.let {
                contentText = it.content
                isPublic = it.isPublic

                selectedImageUris = it.imageUris.map { uriString -> Uri.parse(uriString) }
            }
        }
    }

    fun onContentChange(newText: String) {
        contentText = newText
    }

    fun onPrivacyChange(newStatus: Boolean) {
        isPublic = newStatus
    }


    fun addImages(newUris: List<Uri>) {
        val combinedList = (selectedImageUris + newUris).distinct().take(5)
        selectedImageUris = combinedList
    }


    fun removeImage(uriToRemove: Uri) {
        selectedImageUris = selectedImageUris.filter { it != uriToRemove }
    }

    fun updateMemory(onUpdateSuccess: () -> Unit) {
        viewModelScope.launch {
            currentMemory?.let { oldMemory ->
                val updatedMemory = oldMemory.copy(
                    content = contentText,
                    isPublic = isPublic,

                    imageUris = selectedImageUris.map { it.toString() }
                )
                repository.updateMemory(updatedMemory)
                onUpdateSuccess()
            }
        }
    }
}