package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class SearchMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(searchQuery: Flow<String>): Flow<List<Memory>> {
        val currentUser = authRepository.getCurrentUser()
        return combine(
            memoryRepository.getPublicMemories(),
            memoryRepository.getAllMemories(),
            searchQuery
        ) { remoteMemories, localMemories, query ->
            val filteredRemote = remoteMemories.filter { remote ->
                remote.isPublic && (currentUser == null || remote.userId != currentUser.uid)
            }
            val processedList = filteredRemote.map { remote ->
                val localMatch = localMemories.find { it.createdAt == remote.createdAt && it.title == remote.title }
                val isSaved = localMatch?.isSaved ?: false
                val id = localMatch?.id ?: remote.id

                remote.copy(
                    id = id,
                    isSaved = isSaved
                )
            }
            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000L
            val filteredByTime = processedList.filter {
                (currentTime - it.updatedAt) < oneDayInMillis
            }
            val sortedList = filteredByTime.sortedByDescending { it.createdAt }
            if (query.isBlank()) {
                sortedList
            } else {
                sortedList.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.content.contains(query, ignoreCase = true) ||
                    it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                }
            }
        }
    }
}
