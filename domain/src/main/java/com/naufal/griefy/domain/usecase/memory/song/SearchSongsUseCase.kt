package com.naufal.griefy.domain.usecase.memory.song

import com.naufal.griefy.domain.model.Song
import com.naufal.griefy.domain.repository.MemoryRepository
import javax.inject.Inject

class SearchSongsUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(query: String): List<Song> {
        if (query.isBlank()) {
            return emptyList()
        }
        return memoryRepository.searchSongs(query)
    }
}
