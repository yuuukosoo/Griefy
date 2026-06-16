package com.naufal.griefy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remembrance_days")
data class RemembranceDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val dateTime: Long
)
