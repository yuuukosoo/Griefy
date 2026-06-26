package com.naufal.griefy.domain.usecase.mood

import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.DailyMoodRepository
import javax.inject.Inject

class DeleteDailyMoodUseCase @Inject constructor(
    private val repository: DailyMoodRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(dateString: String) {
        val currentUser = authRepository.getCurrentUser()
        val userId = currentUser?.uid

        val id = "${dateString}_${userId ?: "guest"}"
        repository.deleteMood(id)
    }
}
