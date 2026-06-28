package com.naufal.griefy.ui.login
import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.util.Resource
data class LoginUiState(
    val loginState: Resource<User>? = null
)
