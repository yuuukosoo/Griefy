package com.naufal.griefy.domain.usecase.auth

import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<User>> {
        if (email.isBlank() || password.isBlank()) {
            return flow {
                emit(Resource.Error("ERROR_EMAIL_PASSWORD_EMPTY"))
            }
        }
        return repository.login(email, password)
    }
}
