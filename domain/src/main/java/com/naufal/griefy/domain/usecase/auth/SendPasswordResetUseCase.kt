package com.naufal.griefy.domain.usecase.auth

import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SendPasswordResetUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String): Flow<Resource<Unit>> {
        if (email.isBlank()) {
            return flow {
                emit(Resource.Error("ERROR_EMAIL_EMPTY"))
            }
        }
        return repository.sendPasswordResetEmail(email)
    }
}
