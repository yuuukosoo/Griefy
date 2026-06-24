package com.naufal.griefy.domain.usecase.auth

import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke() {
        try {
            memoryRepository.clearAllLocalMemories()
        } catch (_: Exception) {
            // Ignore Room delete failure
        }
        authRepository.logout()
    }
}
