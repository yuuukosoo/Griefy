package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.MemoryDetail
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMemoryDetailUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val authRepository: AuthRepository
) {
    operator fun invoke(memoryId: Int): Flow<MemoryDetail> {
        val userProfileFlow = flow {
            val currentUser = authRepository.getCurrentUser()
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
            memoryRepository.getMemoryByIdAsFlow(memoryId),
            userProfileFlow
        ) { mem, profile ->
            val processedMemory = if (mem != null && profile != null && (mem.userName == Memory.DEFAULT_USERNAME || mem.userName.isNullOrEmpty() || mem.userName == profile.displayName)) {
                mem.copy(
                    userName = profile.displayName,
                    userAvatar = profile.avatarBase64
                )
            } else {
                mem
            }

            val currentUser = authRepository.getCurrentUser()
            val isOwn = processedMemory != null && currentUser != null && processedMemory.userId == currentUser.uid

            MemoryDetail(
                memory = processedMemory,
                isOwnMemory = isOwn
            )
        }
    }
}
