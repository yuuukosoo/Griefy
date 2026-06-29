package com.naufal.griefy.ui.photoalbum
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.memory.memories.GetPhotoAlbumUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class PhotoAlbumViewModel @Inject constructor(
    private val getPhotoAlbumUseCase: GetPhotoAlbumUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoAlbumState())
    val uiState: StateFlow<PhotoAlbumState> = _uiState.asStateFlow()
    init {
        fetchPhotoAlbum()
    }
    private fun fetchPhotoAlbum() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            getPhotoAlbumUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = e.message) }
                }
                .collect { groups ->
                    _uiState.update { it.copy(photoGroups = groups, isLoading = false) }
                }
        }
    }
}
