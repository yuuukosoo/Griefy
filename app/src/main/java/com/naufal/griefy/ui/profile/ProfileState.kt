package com.naufal.griefy.ui.profile

data class ProfileState(
    val isEditing: Boolean = false,
    val username: String = "",
    val email: String = "",
    val gender: String = com.naufal.griefy.util.ProfileUtils.GENDER_MALE_KEY,
    val profileImageUriString: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
)
