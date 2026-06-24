package com.naufal.griefy.ui.searchmemory

import com.naufal.griefy.domain.model.Memory

data class SearchMemoryState(
    val searchQuery: String = "",
    val publicMemories: List<Memory> = emptyList()
)
