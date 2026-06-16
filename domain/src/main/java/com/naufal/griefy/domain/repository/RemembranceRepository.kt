package com.naufal.griefy.domain.repository

import com.naufal.griefy.domain.model.RemembranceDay
import kotlinx.coroutines.flow.Flow

interface RemembranceRepository {
    fun getAllRemembranceDays(): Flow<List<RemembranceDay>>
    suspend fun getRemembranceDayById(id: Int): RemembranceDay?
    suspend fun addRemembranceDay(day: RemembranceDay): Long
    suspend fun updateRemembranceDay(day: RemembranceDay)
    suspend fun deleteRemembranceDay(day: RemembranceDay)
}
