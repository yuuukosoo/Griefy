package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMemoriesUseCase @Inject constructor(
    private val repository: MemoryRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(query: String, currentUserId: String?): Flow<List<Memory>> {
        val userProfileFlow = flow {
            if (currentUserId != null) {
                authRepository.getUserProfile(currentUserId).collect { resource ->
                    if (resource is Resource.Success) {
                        emit(resource.data)
                    }
                }
            } else {
                emit(null)
            }
        }

        return combine(
            repository.getAllMemories(),
            userProfileFlow
        ) { memories, profile ->

            val filteredList = memories.filter { memory ->
                currentUserId != null && memory.userId == currentUserId
            }


            val processedList = filteredList.map { memory ->
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
                sortedList.filter { memory ->
                    memory.title.contains(query, ignoreCase = true) ||
                    memory.content.contains(query, ignoreCase = true) ||
                    memory.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                }
            }
        }
    }
}
