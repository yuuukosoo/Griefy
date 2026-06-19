package com.naufal.griefy.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName
                )
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("User tidak ditemukan"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan saat masuk"))
        }
    }

    override fun register(name: String, email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                // Update profile display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
                
                val user = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = name
                )
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Registrasi gagal"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan saat mendaftar"))
        }
    }

    override fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return if (firebaseUser != null) {
            User(
                uid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName
            )
        } else null
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}

// Suspend helper to await Google Play Services / Firebase Tasks
private suspend fun <T> Task<T>.await(): T {
    if (isComplete) {
        val e = exception
        return if (e == null) {
            if (isCanceled) {
                throw java.util.concurrent.CancellationException("Task was cancelled.")
            } else {
                result as T
            }
        } else {
            throw e
        }
    }

    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            val e = task.exception
            if (e == null) {
                if (task.isCanceled) {
                    cont.cancel()
                } else {
                    cont.resume(task.result as T)
                }
            } else {
                cont.resumeWithException(e)
            }
        }
    }
}
