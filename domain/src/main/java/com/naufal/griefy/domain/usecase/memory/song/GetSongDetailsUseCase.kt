package com.naufal.griefy.domain.usecase.memory.song

import com.naufal.griefy.domain.model.Song
import com.naufal.griefy.domain.repository.MemoryRepository
import javax.inject.Inject

class GetSongDetailsUseCase @Inject constructor(
    private val repository: MemoryRepository
) {
    suspend operator fun invoke(trackId: String): Song? {
        return repository.getSongDetails(trackId)
    }
}
