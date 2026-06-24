package com.naufal.griefy.domain.usecase.reminder

import com.naufal.griefy.domain.model.RemembranceDay
import com.naufal.griefy.domain.repository.RemembranceRepository
import com.naufal.griefy.domain.repository.ReminderScheduler
import javax.inject.Inject

class DeleteRemembranceDayUseCase @Inject constructor(
    private val repository: RemembranceRepository,
    private val scheduler: ReminderScheduler
) {
    suspend operator fun invoke(day: RemembranceDay) {
        repository.deleteRemembranceDay(day)
        scheduler.cancel(day)
    }
}
