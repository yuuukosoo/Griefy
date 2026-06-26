package com.naufal.griefy.domain.usecase.mood

import com.naufal.griefy.domain.model.DailyMood
import com.naufal.griefy.domain.repository.DailyMoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoodsByMonthUseCase @Inject constructor(
    private val repository: DailyMoodRepository
) {
    operator fun invoke(yearMonth: String): Flow<List<DailyMood>> {
        return repository.getMoodsForMonth(yearMonth)
    }
}
