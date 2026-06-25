package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ToggleSaveMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memory: Memory) {
        val localMemories = memoryRepository.getAllMemories().first()
        val localMatch = localMemories.find { it.createdAt == memory.createdAt && it.title == memory.title }
        if (localMatch != null) {
            val newIsSaved = !localMatch.isSaved
            val newSavedAt = if (newIsSaved) System.currentTimeMillis() else 0L
            memoryRepository.updateMemory(
                localMatch.copy(
                    isSaved = newIsSaved,
                    savedAt = newSavedAt
                )
            )
        } else {
            memoryRepository.addMemory(
                memory.copy(
                    isSaved = true,
                    savedAt = System.currentTimeMillis()
                )
            )
        }
    }
}
