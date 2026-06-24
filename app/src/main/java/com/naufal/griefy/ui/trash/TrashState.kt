package com.naufal.griefy.ui.trash

import com.naufal.griefy.domain.model.Memory

data class TrashState(
    val trashedMemories: List<Memory> = emptyList()
)
