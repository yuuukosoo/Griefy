package com.naufal.griefy.domain.usecase.auth

import com.naufal.griefy.domain.repository.AuthRepository
import javax.inject.Inject

class GetMyUserIdUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(): String {
        return authRepository.getCurrentUser()?.uid ?: ""
    }
}
