package com.naufal.griefy.domain.usecase.memory.trash

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.MemoryRepository
import javax.inject.Inject

class EmptyTrashUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(memories: List<Memory>) {
        memories.forEach { memory ->
            memoryRepository.deletePermanently(memory.id)
        }
    }
}
