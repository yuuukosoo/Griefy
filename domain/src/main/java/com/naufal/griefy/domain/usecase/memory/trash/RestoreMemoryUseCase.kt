package com.naufal.griefy.domain.usecase.memory.trash

import com.naufal.griefy.domain.repository.MemoryRepository
import javax.inject.Inject

class RestoreMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(id: Int) {
        memoryRepository.restoreFromTrash(id)
    }
}
