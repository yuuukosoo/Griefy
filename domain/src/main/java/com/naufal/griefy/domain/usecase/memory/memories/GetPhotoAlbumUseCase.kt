package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.PhotoAlbumGroup
import com.naufal.griefy.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.naufal.griefy.domain.repository.AuthRepository
import javax.inject.Inject

class GetPhotoAlbumUseCase @Inject constructor(
    private val repository: MemoryRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<List<PhotoAlbumGroup>> {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.forLanguageTag("id-ID"))
        val currentUserId = authRepository.getCurrentUser()?.uid
        
        return repository.getAllMemories().map { memories ->
            val memoriesWithImages = memories.filter { 
                it.imageUris.isNotEmpty() && !it.isTrashed && (currentUserId == null || it.userId == currentUserId)
            }
            
            val grouped = memoriesWithImages.groupBy { memory ->
                dateFormat.format(Date(memory.createdAt))
            }
            
            grouped.map { (date, mems) ->
                PhotoAlbumGroup(
                    date = date,
                    photos = mems.flatMap { it.imageUris }
                )
            }.sortedByDescending { group ->
                try {
                    dateFormat.parse(group.date)?.time ?: 0L
                } catch(_: Exception) {
                    0L
                }
            }
        }
    }
}
