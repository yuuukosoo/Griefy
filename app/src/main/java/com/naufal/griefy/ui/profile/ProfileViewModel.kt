package com.naufal.griefy.ui.profile

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    var isEditing by mutableStateOf(false)
        private set

    var username by mutableStateOf("Khalish")
        private set

    var email by mutableStateOf("khalish@example.com")
        private set

    var gender by mutableStateOf("Laki-laki")
        private set

    var profileImageUriString by mutableStateOf<String?>(null)
        private set

    fun setIsEditing(editing: Boolean) {
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

    fun setProfileImageUri(uriString: String?) {
        profileImageUriString = uriString
    }
}
