package com.naufal.griefy.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.memory.song.SearchSongsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.naufal.griefy.domain.usecase.memory.song.ManageAudioPlaybackUseCase
import com.naufal.griefy.domain.usecase.memory.song.ObserveAudioStateUseCase
import com.naufal.griefy.domain.usecase.memory.song.StopAudioUseCase
import javax.inject.Inject

@HiltViewModel
class SearchSongViewModel @Inject constructor(
    private val searchSongsUseCase: SearchSongsUseCase,
    private val manageAudioPlaybackUseCase: ManageAudioPlaybackUseCase,
    observeAudioStateUseCase: ObserveAudioStateUseCase,
    private val stopAudioUseCase: StopAudioUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchSongState())
    val uiState: StateFlow<SearchSongState> = _uiState.asStateFlow()

    private val audioState = observeAudioStateUseCase()
    val playingTrackId = audioState.currentTrackId
    val isMediaPlaying = audioState.isPlaying

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchSongs() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val results = searchSongsUseCase(query)
            _uiState.update {
                it.copy(
                    searchResults = results,
                    isLoading = false
                )
            }
        }
    }

    fun onSongClick(song: com.naufal.griefy.domain.model.Song) {
        manageAudioPlaybackUseCase(song.trackId, song.previewUrl)
    }

    fun stopPlayback() {
        stopAudioUseCase()
    }

    override fun onCleared() {
        super.onCleared()
        stopPlayback()
    }
}