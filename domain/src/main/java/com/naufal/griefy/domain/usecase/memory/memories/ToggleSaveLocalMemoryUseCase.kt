package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import javax.inject.Inject

class ToggleSaveLocalMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memory: Memory) {
        val newIsSaved = !memory.isSaved
        val newSavedAt = if (newIsSaved) System.currentTimeMillis() else 0L
        memoryRepository.updateMemory(
            memory.copy(
                isSaved = newIsSaved,
                savedAt = newSavedAt
            )
        )
    }
}
