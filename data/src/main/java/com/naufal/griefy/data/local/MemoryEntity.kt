package com.naufal.griefy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memory_table")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val content: String,
    val imageUris: List<String>,
    val createdAt: Long,
    val tags: List<String>,
    val isPublic: Boolean,
    val songTrackId: String?,
    val songTitle: String?,
    val isTrashed: Boolean
)