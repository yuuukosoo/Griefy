package com.naufal.griefy.domain.repository

import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<Resource<User>>
    fun register(name: String, email: String, password: String): Flow<Resource<User>>
    fun getCurrentUser(): User?
    fun logout()
    
    fun getUserProfile(uid: String): Flow<Resource<UserProfile>>
    suspend fun saveUserProfile(profile: UserProfile): Resource<Unit>
}
