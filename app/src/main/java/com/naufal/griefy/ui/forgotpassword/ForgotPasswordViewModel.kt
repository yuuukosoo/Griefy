package com.naufal.griefy.ui.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.R
import com.naufal.griefy.domain.usecase.auth.SendPasswordResetUseCase
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val sendPasswordResetUseCase: SendPasswordResetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordState())
    val uiState: StateFlow<ForgotPasswordState> = _uiState.asStateFlow()

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessageRes = null, errorMessage = null) }
            sendPasswordResetUseCase(email).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                    is Resource.Error -> {
                        val msg = result.message
                        if (msg == "ERROR_EMAIL_EMPTY") {
                            _uiState.update { it.copy(isLoading = false, errorMessageRes = R.string.error_email_empty) }
                        } else if (msg != null) {
                            _uiState.update { it.copy(isLoading = false, errorMessage = msg) }
                        } else {
                            _uiState.update { it.copy(isLoading = false, errorMessageRes = R.string.error_forgot_password_failed) }
                        }
                    }
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { ForgotPasswordState() }
    }
}
