package com.naufal.griefy.domain.usecase.auth

import com.naufal.griefy.domain.repository.AuthRepository
import javax.inject.Inject

class IsCurrentUserUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(userId: String?): Boolean {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null || userId.isNullOrEmpty()) {
            return true
        }
        return currentUser.uid == userId
    }
}
