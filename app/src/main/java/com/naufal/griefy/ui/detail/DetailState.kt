package com.naufal.griefy.ui.detail

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.Song

data class DetailState(
    val memory: Memory? = null,
    val isOwnMemory: Boolean = false,
    val songDetails: Song? = null
)
