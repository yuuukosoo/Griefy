package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import javax.inject.Inject

class UpdateMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        oldMemory: Memory,
        title: String,
        content: String,
        tags: List<String>,
        isPublic: Boolean,
        imageUris: List<String>,
        songTrackId: String?,
        songTitle: String?
    ) {
        val currentUser = authRepository.getCurrentUser()
        var profileName = currentUser?.displayName ?: Memory.DEFAULT_USERNAME
        var profileAvatar: String? = null

        if (currentUser != null) {
            authRepository.getUserProfile(currentUser.uid).collect { resource ->
                if (resource is Resource.Success) {
                    resource.data?.let {
                        profileName = it.displayName
                        profileAvatar = it.avatarBase64
                    }
                }
            }
        }

        val updatedMemory = oldMemory.copy(
            title = title,
            content = content,
            tags = tags,
            isPublic = isPublic,
            imageUris = imageUris,
            songTrackId = songTrackId,
            songTitle = songTitle,
            userName = profileName,
            userAvatar = profileAvatar,
            userId = oldMemory.userId ?: currentUser?.uid
        )
        memoryRepository.updateMemory(updatedMemory)
    }
}
