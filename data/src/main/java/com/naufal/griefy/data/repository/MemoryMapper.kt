package com.naufal.griefy.data.repository

import com.naufal.griefy.data.local.MemoryEntity
import com.naufal.griefy.domain.model.Memory

fun MemoryEntity.toDomain(): Memory {
    return Memory(
        id = id,
        title = title,
        content = content,
        imageUris = imageUris,
        createdAt = createdAt,
        tags = tags,
        isPublic = isPublic,
        songTrackId = songTrackId,
        songTitle = songTitle,
        isTrashed = isTrashed
    )
}

fun Memory.toEntity(): MemoryEntity {
    return MemoryEntity(
        id = id,
        title = title,
        content = content,
        imageUris = imageUris,
        createdAt = createdAt,
        tags = tags,
        isPublic = isPublic,
        songTrackId = songTrackId,
        songTitle = songTitle,
        isTrashed = isTrashed
    )
}