package com.naufal.griefy.domain.usecase.memory.song

import com.naufal.griefy.domain.model.LoadSongResult
import com.naufal.griefy.domain.model.Song
import javax.inject.Inject

class LoadMemorySongUseCase @Inject constructor(
    private val getSongDetailsUseCase: GetSongDetailsUseCase,
    private val manageAudioPlaybackUseCase: ManageAudioPlaybackUseCase
) {
    suspend operator fun invoke(
        trackId: String?,
        loadedTrackId: String?,
        currentSong: Song?,
        hasAutoPlayed: Boolean,
        onAutoPlayed: () -> Unit
    ): LoadSongResult {
        if (trackId == null) {
            return LoadSongResult.NoSong
        }

        if (trackId == loadedTrackId && currentSong != null) {
            return LoadSongResult.Unchanged
        }

        val song = getSongDetailsUseCase(trackId) ?: return LoadSongResult.FetchFailed

        if (!hasAutoPlayed) {
            manageAudioPlaybackUseCase(song.trackId, song.previewUrl)
            onAutoPlayed()
        }

        return LoadSongResult.Success(song)
    }
}


