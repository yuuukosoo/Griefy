package com.naufal.griefy.ui.profile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.naufal.griefy.domain.usecase.profile.GetMyUserProfileUseCase
import com.naufal.griefy.domain.usecase.profile.SaveUserProfileUseCase
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getMyUserProfileUseCase: GetMyUserProfileUseCase,
    private val saveUserProfileUseCase: SaveUserProfileUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileState())
    val uiState: StateFlow<ProfileState> = _uiState.asStateFlow()
    private var originalProfile: UserProfile? = null
    init {
        loadUserProfile()
    }
    fun loadUserProfile() {
        viewModelScope.launch {
            getMyUserProfileUseCase().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }
                    is Resource.Success -> {
                        val profile = resource.data
                        if (profile != null) {
                            originalProfile = profile
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    username = profile.displayName,
                                    email = profile.email,
                                    gender = profile.gender ?: com.naufal.griefy.util.ProfileUtils.GENDER_MALE_KEY,
                                    profileImageUriString = profile.avatarBase64
                                )
                            }
                        } else {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoading = false, errorMessage = resource.message) }
                    }
                }
            }
        }
    }
    fun setIsEditing(editing: Boolean) {
        _uiState.update { state ->
            if (!editing && state.isEditing) {
                originalProfile?.let { profile ->
                    state.copy(
                        isEditing = false,
                        username = profile.displayName,
                        email = profile.email,
                        gender = profile.gender ?: com.naufal.griefy.util.ProfileUtils.GENDER_MALE_KEY,
                        profileImageUriString = profile.avatarBase64
                    )
                } ?: state.copy(isEditing = false)
            } else {
                state.copy(isEditing = editing)
            }
        }
    }
    fun onUsernameChange(newUsername: String) {
        _uiState.update { it.copy(username = newUsername) }
    }
    fun onEmailChange(newEmail: String) {
        _uiState.update { it.copy(email = newEmail) }
    }
    fun onGenderChange(newGender: String) {
        _uiState.update { it.copy(gender = newGender) }
    }
    fun onProfileImagePicked(uriString: String) {
        _uiState.update { it.copy(profileImageUriString = uriString) }
    }
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    fun saveUserProfile() {
        val original = originalProfile ?: return
        val currentState = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }
            val updatedProfile = UserProfile(
                uid = original.uid,
                email = currentState.email,
                displayName = currentState.username,
                gender = currentState.gender,
                avatarBase64 = currentState.profileImageUriString
            )
            when (val result = saveUserProfileUseCase(updatedProfile)) {
                is Resource.Success -> {
                    originalProfile = updatedProfile
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveSuccess = true,
                            isEditing = false
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = result.message ?: "ERROR_SAVE_PROFILE_FAILED"
                        )
                    }
                }
                is Resource.Loading -> {
                }
            }
        }
    }
}
