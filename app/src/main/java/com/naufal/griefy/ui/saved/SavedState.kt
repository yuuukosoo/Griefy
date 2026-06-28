package com.naufal.griefy.ui.saved
import com.naufal.griefy.domain.model.Memory
data class SavedState(
    val savedMemories: List<Memory> = emptyList()
)
