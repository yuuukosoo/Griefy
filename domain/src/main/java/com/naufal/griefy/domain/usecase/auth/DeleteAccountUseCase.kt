package com.naufal.griefy.domain.usecase.auth

import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import javax.inject.Inject

class DeleteAccountUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val memoryRepository: MemoryRepository
) {
    suspend operator fun invoke(): Resource<Unit> {
        try {
            memoryRepository.clearAllLocalMemories()
        } catch (_: Exception) {

        }
        return authRepository.deleteAccount()
    }
}
