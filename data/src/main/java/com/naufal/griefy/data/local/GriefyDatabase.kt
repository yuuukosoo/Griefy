package com.naufal.griefy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

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