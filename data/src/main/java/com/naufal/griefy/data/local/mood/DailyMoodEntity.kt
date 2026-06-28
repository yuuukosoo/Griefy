package com.naufal.griefy.data.local.mood

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_moods")
data class DailyMoodEntity(
    @PrimaryKey val id: String,
    val dateString: String,
    val moodValue: String,
    val userId: String?
)
