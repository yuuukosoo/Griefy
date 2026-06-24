package com.naufal.griefy.domain.usecase.memory.memories

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class AddMemoryUseCase @Inject constructor(
    private val memoryRepository: MemoryRepository,
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        title: String,
        content: String,
        imageUris: List<String>,
        tags: List<String>,
        isPublic: Boolean,
        songTrackId: String?,
        songTitle: String?,
        defaultTagString: String
    ) {
        val currentUser = authRepository.getCurrentUser()
        var profileName = currentUser?.displayName ?: Memory.DEFAULT_USERNAME
        var profileAvatar: String? = null

        if (currentUser != null) {
            authRepository.getUserProfile(currentUser.uid).firstOrNull()?.let { resource ->
                if (resource is Resource.Success) {
                    resource.data?.let {
                        profileName = it.displayName
                        profileAvatar = it.avatarBase64
                    }
                }
            }
        }

        val newMemory = Memory(
            title = title,
            content = content,
            imageUris = imageUris,
            createdAt = System.currentTimeMillis(),
            tags = tags.ifEmpty { listOf(defaultTagString) },
            isPublic = isPublic,
            songTrackId = songTrackId,
            songTitle = songTitle,
            isTrashed = false,
            userName = profileName,
            userAvatar = profileAvatar,
            userId = currentUser?.uid
        )

        memoryRepository.addMemory(newMemory)
    }
}
