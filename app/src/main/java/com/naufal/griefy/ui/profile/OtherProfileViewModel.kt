package com.naufal.griefy.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId: String = checkNotNull(savedStateHandle["userId"])

    var profileState by mutableStateOf<Resource<UserProfile>>(Resource.Loading())
        private set

    var memoryCount by mutableIntStateOf(0)
        private set

    init {
        loadUserProfile()
        loadMemoryCount()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            profileState = Resource.Loading()
            authRepository.getUserProfile(userId).collect { result ->
                profileState = result
            }
        }
    }

    private fun loadMemoryCount() {
        firestore.collection("public_memories")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    memoryCount = snapshot.size()
                }
            }
    }
}
