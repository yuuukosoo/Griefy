package com.naufal.griefy.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Song
import com.naufal.griefy.domain.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchSongViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {


    var searchQuery by mutableStateOf("")
        private set

    var searchResults by mutableStateOf<List<Song>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun onQueryChange(query: String) {
        searchQuery = query
    }


    fun searchSongs() {
        if (searchQuery.isBlank()) return

        viewModelScope.launch {
            isLoading = true

            searchResults = repository.searchSongs(searchQuery)
            isLoading = false
        }
    }
}