package com.naufal.griefy.domain.repository

import com.naufal.griefy.domain.model.DailyMood
import kotlinx.coroutines.flow.Flow

interface DailyMoodRepository {
    fun getMoodsForMonth(yearMonth: String): Flow<List<DailyMood>>
    suspend fun saveMood(dailyMood: DailyMood)
    suspend fun clearLocalMoods()
    suspend fun deleteMood(id: String)
}
