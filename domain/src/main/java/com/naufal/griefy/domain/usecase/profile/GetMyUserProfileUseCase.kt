package com.naufal.griefy.domain.usecase.profile

import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMyUserProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<UserProfile>> {
        val currentUser = repository.getCurrentUser() ?: return flow {
            emit(Resource.Error("ERROR_UNAUTHENTICATED"))
        }
        return repository.getUserProfile(currentUser.uid)
    }
}
