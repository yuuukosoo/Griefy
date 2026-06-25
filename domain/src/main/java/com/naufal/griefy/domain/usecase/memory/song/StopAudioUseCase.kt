package com.naufal.griefy.domain.usecase.memory.song

import com.naufal.griefy.domain.repository.AudioPlayer
import javax.inject.Inject

class StopAudioUseCase @Inject constructor(
    private val audioPlayer: AudioPlayer
) {
    operator fun invoke() {
        audioPlayer.stop()
    }
}
