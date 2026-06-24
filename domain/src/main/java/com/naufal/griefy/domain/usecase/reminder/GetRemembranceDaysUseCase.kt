package com.naufal.griefy.domain.usecase.reminder

import com.naufal.griefy.domain.model.RemembranceDay
import com.naufal.griefy.domain.repository.RemembranceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRemembranceDaysUseCase @Inject constructor(
    private val repository: RemembranceRepository
) {
    operator fun invoke(): Flow<List<RemembranceDay>> {
        return repository.getAllRemembranceDays()
    }
}
