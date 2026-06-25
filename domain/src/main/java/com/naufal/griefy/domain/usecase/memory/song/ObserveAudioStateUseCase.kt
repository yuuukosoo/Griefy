package com.naufal.griefy.domain.usecase.memory.song

import com.naufal.griefy.domain.model.AudioState
import com.naufal.griefy.domain.repository.AudioPlayer
import javax.inject.Inject

class ObserveAudioStateUseCase @Inject constructor(
    private val audioPlayer: AudioPlayer
) {
    operator fun invoke(): AudioState = AudioState(
        isPlaying = audioPlayer.isPlaying,
        currentTrackId = audioPlayer.currentTrackId,
        currentPosition = audioPlayer.currentPosition,
        duration = audioPlayer.duration
    )
}
