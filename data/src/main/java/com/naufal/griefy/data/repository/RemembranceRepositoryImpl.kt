package com.naufal.griefy.data.repository

import com.naufal.griefy.data.local.reminder.RemembranceDayDao
import com.naufal.griefy.domain.model.RemembranceDay
import com.naufal.griefy.domain.repository.RemembranceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RemembranceRepositoryImpl @Inject constructor(
    private val dao: RemembranceDayDao
) : RemembranceRepository {
    override fun getAllRemembranceDays(): Flow<List<RemembranceDay>> {
        return dao.getAllRemembranceDays().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getRemembranceDayById(id: Int): RemembranceDay? {
        return dao.getRemembranceDayById(id)?.toDomain()
    }

    override suspend fun addRemembranceDay(day: RemembranceDay): Long {
        return dao.insertRemembranceDay(day.toEntity())
    }

    override suspend fun updateRemembranceDay(day: RemembranceDay) {
        dao.updateRemembranceDay(day.toEntity())
    }

    override suspend fun deleteRemembranceDay(day: RemembranceDay) {
        dao.deleteRemembranceDay(day.toEntity())
    }
}
