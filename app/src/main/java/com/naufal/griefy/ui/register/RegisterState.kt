package com.naufal.griefy.ui.register

import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.util.Resource

data class RegisterState(
    val registerState: Resource<User>? = null
)
