package com.naufal.griefy.ui.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import com.naufal.griefy.util.getBase64FromUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val application: Application
) : ViewModel() {
    var isEditing by mutableStateOf(false)
        private set

    var username by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    var gender by mutableStateOf(com.naufal.griefy.util.ProfileUtils.GENDER_MALE_KEY)
        private set

    var profileImageUriString by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var saveSuccess by mutableStateOf(false)
        private set

    private var originalProfile: UserProfile? = null

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            errorMessage = "ERROR_UNAUTHENTICATED"
            return
        }
        viewModelScope.launch {
            authRepository.getUserProfile(currentUser.uid).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        isLoading = true
                        errorMessage = null
                    }
                    is Resource.Success -> {
                        isLoading = false
                        val profile = resource.data
                        if (profile != null) {
                            originalProfile = profile
                            username = profile.displayName
                            email = profile.email
                            gender = profile.gender ?: com.naufal.griefy.util.ProfileUtils.GENDER_MALE_KEY
                            profileImageUriString = profile.avatarBase64
                        }
                    }
                    is Resource.Error -> {
                        isLoading = false
                        errorMessage = resource.message
                    }
                }
            }
        }
    }

    fun setIsEditing(editing: Boolean) {
        if (!editing && isEditing) {
            // Revert changes if we exit editing mode without saving
            originalProfile?.let { profile ->
                username = profile.displayName
                email = profile.email
                gender = profile.gender ?: com.naufal.griefy.util.ProfileUtils.GENDER_MALE_KEY
                profileImageUriString = profile.avatarBase64
            }
        }
        isEditing = editing
    }

    fun onUsernameChange(newUsername: String) {
        username = newUsername
    }

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onGenderChange(newGender: String) {
        gender = newGender
    }

    fun onProfileImagePicked(uriString: String) {
        viewModelScope.launch {
            val base64 = getBase64FromUri(application, uriString)
            if (base64 != null) {
                profileImageUriString = base64
            } else {
                errorMessage = "ERROR_PROCESS_IMAGE_FAILED"
            }
        }
    }

    fun clearSaveSuccess() {
        saveSuccess = false
    }

    fun clearErrorMessage() {
        errorMessage = null
    }

    fun saveUserProfile() {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser == null) {
            errorMessage = "ERROR_UNAUTHENTICATED"
            return
        }

        viewModelScope.launch {
            isSaving = true
            errorMessage = null
            saveSuccess = false

            val updatedProfile = UserProfile(
                uid = currentUser.uid,
                email = email,
                displayName = username,
                gender = gender,
                avatarBase64 = profileImageUriString
            )

            when (val result = authRepository.saveUserProfile(updatedProfile)) {
                is Resource.Success -> {
                    isSaving = false
                    saveSuccess = true
                    isEditing = false
                    originalProfile = updatedProfile
                }
                is Resource.Error -> {
                    isSaving = false
                    errorMessage = result.message ?: "ERROR_SAVE_PROFILE_FAILED"
                }
                is Resource.Loading -> {
                    // Safe fallback
                }
            }
        }
    }
}

