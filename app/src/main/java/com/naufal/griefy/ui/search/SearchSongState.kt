package com.naufal.griefy.ui.search
import com.naufal.griefy.domain.model.Song
data class SearchSongState(
    val searchQuery: String = "",
    val searchResults: List<Song> = emptyList(),
    val isLoading: Boolean = false
)
