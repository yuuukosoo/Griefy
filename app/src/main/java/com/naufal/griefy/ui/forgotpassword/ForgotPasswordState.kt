package com.naufal.griefy.ui.forgotpassword

import androidx.annotation.StringRes

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    @StringRes val errorMessageRes: Int? = null,
    val errorMessage: String? = null
)
