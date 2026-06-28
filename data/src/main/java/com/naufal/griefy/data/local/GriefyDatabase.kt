package com.naufal.griefy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.naufal.griefy.data.local.memory.MemoryDao
import com.naufal.griefy.data.local.memory.MemoryEntity
import com.naufal.griefy.data.local.mood.DailyMoodDao
import com.naufal.griefy.data.local.mood.DailyMoodEntity
import com.naufal.griefy.data.local.reminder.RemembranceDayDao
import com.naufal.griefy.data.local.reminder.RemembranceDayEntity

@Database(
    entities = [MemoryEntity::class, RemembranceDayEntity::class, DailyMoodEntity::class],
    version = 15,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GriefyDatabase : RoomDatabase() {

    abstract val memoryDao: MemoryDao
    abstract val remembranceDayDao: RemembranceDayDao
    abstract val dailyMoodDao: DailyMoodDao


    companion object {
        const val DATABASE_NAME = "griefy_journal.db"
    }
}