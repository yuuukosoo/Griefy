package com.naufal.griefy.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OtherProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId: String = checkNotNull(savedStateHandle["userId"])

    var profileState by mutableStateOf<Resource<UserProfile>>(Resource.Loading())
        private set

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            profileState = Resource.Loading()
            authRepository.getUserProfile(userId).collect { result ->
                profileState = result
            }
        }
    }
}
