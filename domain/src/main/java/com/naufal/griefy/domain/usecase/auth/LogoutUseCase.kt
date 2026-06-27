package com.naufal.griefy.domain.usecase.auth

import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.repository.DailyMoodRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val memoryRepository: MemoryRepository,
    private val dailyMoodRepository: DailyMoodRepository
) {
    suspend operator fun invoke() {
        try {
            memoryRepository.clearAllLocalMemories()
            dailyMoodRepository.clearLocalMoods()
        } catch (_: Exception) {

        }
        authRepository.logout()
    }
}
