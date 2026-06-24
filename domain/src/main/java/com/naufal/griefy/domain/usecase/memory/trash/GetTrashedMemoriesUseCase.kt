package com.naufal.griefy.domain.usecase.memory.trash

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetTrashedMemoriesUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(): Flow<List<Memory>> {
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
            memoryRepository.getTrashedMemories(),
            userProfileFlow
        ) { memoriesList, profile ->
            val processedList = memoriesList.map { memory ->
                if (profile != null && (memory.userName == Memory.DEFAULT_USERNAME || memory.userName.isNullOrEmpty() || memory.userName == profile.displayName)) {
                    memory.copy(
                        userName = profile.displayName,
                        userAvatar = profile.avatarBase64
                    )
                } else {
                    memory
                }
            }
            processedList.sortedByDescending { it.createdAt }
        }
    }
}
