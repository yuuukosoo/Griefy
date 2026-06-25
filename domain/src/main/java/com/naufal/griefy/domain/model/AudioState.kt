package com.naufal.griefy.domain.model

import kotlinx.coroutines.flow.StateFlow

data class AudioState(
    val isPlaying: StateFlow<Boolean>,
    val currentTrackId: StateFlow<String?>,
    val currentPosition: StateFlow<Long>,
    val duration: StateFlow<Long>
)
