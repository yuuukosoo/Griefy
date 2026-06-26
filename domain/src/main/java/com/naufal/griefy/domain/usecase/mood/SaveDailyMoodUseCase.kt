package com.naufal.griefy.domain.usecase.mood

import com.naufal.griefy.domain.model.DailyMood
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.DailyMoodRepository
import javax.inject.Inject

class SaveDailyMoodUseCase @Inject constructor(
    private val repository: DailyMoodRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(dateString: String, moodValue: String) {
        val currentUser = authRepository.getCurrentUser()
        val userId = currentUser?.uid

        val id = "${dateString}_${userId ?: "guest"}"
        
        val dailyMood = DailyMood(
            id = id,
            dateString = dateString,
            moodValue = moodValue,
            userId = userId
        )
        repository.saveMood(dailyMood)
    }
}
