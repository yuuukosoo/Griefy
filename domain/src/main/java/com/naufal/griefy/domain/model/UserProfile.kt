package com.naufal.griefy.domain.model

data class UserProfile(
    val uid: String,
    val email: String,
    val displayName: String,
    val gender: String?,
    val avatarBase64: String?
)
