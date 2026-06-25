package com.naufal.griefy.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer {
    val isPlaying: StateFlow<Boolean>
    val currentTrackId: StateFlow<String?>
    val currentPosition: StateFlow<Long>
    val duration: StateFlow<Long>

    fun play(trackId: String, url: String)
    fun pause()
    fun resume()
    fun stop()
    fun release()
}
