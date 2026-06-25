package com.naufal.griefy.domain.usecase.memory.song

import com.naufal.griefy.domain.repository.AudioPlayer
import javax.inject.Inject

class ManageAudioPlaybackUseCase @Inject constructor(
    private val audioPlayer: AudioPlayer
) {
    operator fun invoke(trackId: String, previewUrl: String) {
        if (audioPlayer.currentTrackId.value == trackId) {
            if (audioPlayer.isPlaying.value) {
                audioPlayer.pause()
            } else {
                audioPlayer.resume()
            }
        } else {
            audioPlayer.play(trackId, previewUrl)
        }
    }
}
