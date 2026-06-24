package com.naufal.griefy.domain.usecase.reminder

import com.naufal.griefy.domain.model.RemembranceDay
import com.naufal.griefy.domain.repository.RemembranceRepository
import com.naufal.griefy.domain.repository.ReminderScheduler
import javax.inject.Inject

class AddRemembranceDayUseCase @Inject constructor(
    private val repository: RemembranceRepository,
    private val scheduler: ReminderScheduler
) {
    suspend operator fun invoke(day: RemembranceDay): Long {
        val generatedId = repository.addRemembranceDay(day)
        val finalDay = day.copy(id = generatedId.toInt())
        scheduler.schedule(finalDay)
        return generatedId
    }
}
