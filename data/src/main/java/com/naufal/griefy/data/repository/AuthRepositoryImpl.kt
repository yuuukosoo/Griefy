package com.naufal.griefy.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
                
                // Create user document in Firestore users collection
                val userMap = mapOf(
                    "uid" to firebaseUser.uid,
                    "email" to (firebaseUser.email ?: ""),
                    "displayName" to name,
                    "gender" to null,
                    "avatarBase64" to null
                )
                firestore.collection("users").document(firebaseUser.uid).set(userMap).await()
                
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

    override fun getUserProfile(uid: String): Flow<Resource<UserProfile>> = flow {
        emit(Resource.Loading())
        try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val email = doc.getString("email") ?: ""
                val displayName = doc.getString("displayName") ?: ""
                val gender = doc.getString("gender")
                val avatarBase64 = doc.getString("avatarBase64")
                
                emit(Resource.Success(UserProfile(uid, email, displayName, gender, avatarBase64)))
            } else {
                // Fallback to FirebaseAuth details
                val firebaseUser = firebaseAuth.currentUser
                if (firebaseUser != null && firebaseUser.uid == uid) {
                    emit(Resource.Success(
                        UserProfile(
                            uid = uid,
                            email = firebaseUser.email ?: "",
                            displayName = firebaseUser.displayName ?: "",
                            gender = null,
                            avatarBase64 = null
                        )
                    ))
                } else {
                    emit(Resource.Success(
                        UserProfile(
                            uid = uid,
                            email = "deleted",
                            displayName = "Akun Dihapus",
                            gender = null,
                            avatarBase64 = null
                        )
                    ))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat profil"))
        }
    }

    override suspend fun saveUserProfile(profile: UserProfile): Resource<Unit> {
        return try {
            // 1. Update display name in FirebaseAuth
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(profile.displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()
            }
            
            // 2. Save profile details to Firestore
            val userMap = mapOf(
                "uid" to profile.uid,
                "email" to profile.email,
                "displayName" to profile.displayName,
                "gender" to profile.gender,
                "avatarBase64" to profile.avatarBase64
            )
            firestore.collection("users").document(profile.uid).set(userMap).await()
            
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menyimpan profil")
        }
    }

    override suspend fun deleteAccount(): Resource<Unit> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val uid = currentUser.uid
                // 1. Delete user profile from Firestore
                firestore.collection("users").document(uid).delete().await()
                
                // 2. Delete user from FirebaseAuth
                currentUser.delete().await()
                
                Resource.Success(Unit)
            } else {
                Resource.Error("User tidak terautentikasi")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Gagal menghapus akun")
        }
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
