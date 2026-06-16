package com.naufal.griefy.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RemembranceDayDao {
    @Query("SELECT * FROM remembrance_days ORDER BY dateTime ASC")
    fun getAllRemembranceDays(): Flow<List<RemembranceDayEntity>>

    @Query("SELECT * FROM remembrance_days WHERE id = :id")
    suspend fun getRemembranceDayById(id: Int): RemembranceDayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRemembranceDay(day: RemembranceDayEntity): Long

    @Update
    suspend fun updateRemembranceDay(day: RemembranceDayEntity)

    @Delete
    suspend fun deleteRemembranceDay(day: RemembranceDayEntity)
}
