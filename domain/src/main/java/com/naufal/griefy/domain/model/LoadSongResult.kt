package com.naufal.griefy.domain.model

sealed class LoadSongResult {
    data class Success(val song: Song) : LoadSongResult()
    object NoSong : LoadSongResult()
    object Unchanged : LoadSongResult()
    object FetchFailed : LoadSongResult()
}
