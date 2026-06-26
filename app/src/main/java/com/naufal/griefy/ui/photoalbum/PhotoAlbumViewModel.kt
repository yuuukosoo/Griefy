package com.naufal.griefy.ui.photoalbum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.PhotoAlbumGroup
import com.naufal.griefy.domain.usecase.memory.memories.GetPhotoAlbumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhotoAlbumViewModel @Inject constructor(
    private val getPhotoAlbumUseCase: GetPhotoAlbumUseCase
) : ViewModel() {

    private val _photoGroups = MutableStateFlow<List<PhotoAlbumGroup>>(emptyList())
    val photoGroups: StateFlow<List<PhotoAlbumGroup>> = _photoGroups.asStateFlow()

    init {
        fetchPhotoAlbum()
    }

    private fun fetchPhotoAlbum() {
        viewModelScope.launch {
            getPhotoAlbumUseCase().collect { groups ->
                _photoGroups.value = groups
            }
        }
    }
}
