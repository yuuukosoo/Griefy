package com.naufal.griefy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyMoodDao {
    @Query("SELECT * FROM daily_moods WHERE dateString LIKE :yearMonth || '%' AND userId = :userId")
    fun getMoodsForMonth(yearMonth: String, userId: String): Flow<List<DailyMoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(dailyMood: DailyMoodEntity)

    @Query("DELETE FROM daily_moods")
    suspend fun clearAll()

    @Query("DELETE FROM daily_moods WHERE id = :id")
    suspend fun deleteMoodById(id: String)
}
