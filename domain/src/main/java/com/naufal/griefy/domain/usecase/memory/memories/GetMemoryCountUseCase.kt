package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMemoryCountUseCase @Inject constructor(
    private val repository: MemoryRepository
) {
    operator fun invoke(userId: String): Flow<Int> {
        return repository.getMemoryCount(userId)
    }
}
