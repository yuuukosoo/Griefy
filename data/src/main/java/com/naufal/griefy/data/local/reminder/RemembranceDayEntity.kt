package com.naufal.griefy.data.local.reminder

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.naufal.griefy.data.local.memory.MemoryEntity

@Entity(
    tableName = "remembrance_days",
    foreignKeys = [
        ForeignKey(
            entity = MemoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["memoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["memoryId"])]
)
data class RemembranceDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val dateTime: Long,
    val memoryId: Int? = null,
    val userId: String = ""
)
