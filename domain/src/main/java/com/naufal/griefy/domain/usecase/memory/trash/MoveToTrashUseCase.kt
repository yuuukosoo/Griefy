package com.naufal.griefy.domain.usecase.memory.trash

import com.naufal.griefy.domain.repository.MemoryRepository
import javax.inject.Inject

class MoveToTrashUseCase @Inject constructor(
    private val repository: MemoryRepository
) {
    suspend operator fun invoke(memoryId: Int) {
        repository.moveToTrash(memoryId)
    }
}
