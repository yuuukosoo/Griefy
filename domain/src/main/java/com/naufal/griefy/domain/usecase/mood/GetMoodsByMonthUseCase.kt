package com.naufal.griefy.domain.usecase.mood

import com.naufal.griefy.domain.model.DailyMood
import com.naufal.griefy.domain.repository.DailyMoodRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetMoodsByMonthUseCase @Inject constructor(
    private val repository: DailyMoodRepository
) {
    operator fun invoke(yearMonth: YearMonth): Flow<List<DailyMood>> {
        val monthString = String.format(java.util.Locale.US, "%04d-%02d", yearMonth.year, yearMonth.monthValue)
        return repository.getMoodsForMonth(monthString)
    }
}
