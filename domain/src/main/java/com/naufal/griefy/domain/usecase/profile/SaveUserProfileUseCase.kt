package com.naufal.griefy.domain.usecase.profile

import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import javax.inject.Inject

class SaveUserProfileUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(profile: UserProfile): Resource<Unit> {
        if (repository.getCurrentUser() == null) return Resource.Error("ERROR_UNAUTHENTICATED")
        return repository.saveUserProfile(profile)
    }
}
