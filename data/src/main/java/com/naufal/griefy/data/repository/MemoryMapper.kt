package com.naufal.griefy.data.repository

import com.naufal.griefy.data.local.MemoryEntity
import com.naufal.griefy.data.remote.TrackDto
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.Song


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
        isTrashed = isTrashed,
    )
}

fun TrackDto.toSong(): Song {
    return Song(
        trackId = id,
        title = name,
        artistName = artists.joinToString(", ") { it.name },
        imageUrl = album.images.firstOrNull()?.url ?: "",
        previewUrl = preview_url
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
        isTrashed = isTrashed,
    )
}