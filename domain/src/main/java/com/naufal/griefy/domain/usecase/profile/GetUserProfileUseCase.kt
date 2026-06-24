package com.naufal.griefy.domain.usecase.profile

import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(userId: String): Flow<Resource<UserProfile>> {
        return repository.getUserProfile(userId)
    }
}
