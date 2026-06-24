package com.naufal.griefy.domain.usecase.auth

import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    operator fun invoke(name: String, email: String, password: String, confirmPass: String): Flow<Resource<User>> = flow {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPass.isBlank()) {
            emit(Resource.Error("ERROR_ALL_FIELDS_REQUIRED"))
            return@flow
        }
        if (password != confirmPass) {
            emit(Resource.Error("ERROR_PASSWORD_MISMATCH"))
            return@flow
        }
        if (password.length < 6) {
            emit(Resource.Error("ERROR_PASSWORD_TOO_SHORT"))
            return@flow
        }
        emit(Resource.Loading())
        try {
            authRepository.register(name, email, password).collect { result ->
                emit(result)
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "ERROR_REGISTRATION_FAILED"))
        }
    }
}
