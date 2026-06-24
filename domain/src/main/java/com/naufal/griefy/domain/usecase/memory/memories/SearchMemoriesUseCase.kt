package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SearchMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(searchQuery: Flow<String>): Flow<List<Memory>> {
        val currentUser = authRepository.getCurrentUser()
        val userProfileFlow: Flow<UserProfile?> = flow {
            if (currentUser != null) {
                authRepository.getUserProfile(currentUser.uid).collect { resource ->
                    if (resource is Resource.Success) {
                        emit(resource.data)
                    }
                }
            } else {
                emit(null)
            }
        }

        return combine(
            memoryRepository.getPublicMemories(),
            memoryRepository.getAllMemories(),
            searchQuery,
            userProfileFlow
        ) { remoteMemories, localMemories, query, profile ->
            val filteredRemote = remoteMemories.filter { remote ->
                remote.isPublic && (currentUser == null || remote.userId != currentUser.uid)
            }
            val processedList = filteredRemote.map { remote ->
                val localMatch = localMemories.find { it.createdAt == remote.createdAt && it.title == remote.title }
                val isSaved = localMatch?.isSaved ?: false
                val id = localMatch?.id ?: remote.id

                val memory = remote.copy(
                    id = id,
                    isSaved = isSaved
                )

                if (profile != null && (memory.userName == Memory.DEFAULT_USERNAME || memory.userName.isNullOrEmpty() || memory.userName == profile.displayName)) {
                    memory.copy(
                        userName = profile.displayName,
                        userAvatar = profile.avatarBase64
                    )
                } else {
                    memory
                }
            }
            val sortedList = processedList.sortedByDescending { it.createdAt }
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
