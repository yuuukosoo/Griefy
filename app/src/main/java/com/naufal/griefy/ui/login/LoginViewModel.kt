package com.naufal.griefy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.auth.LoginUseCase
import com.naufal.griefy.domain.usecase.auth.SendPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginUseCase(email, password).collect { result ->
                _uiState.update { it.copy(loginState = result) }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            sendPasswordResetUseCase(email).collect { result ->
                _uiState.update { it.copy(forgotPasswordState = result) }
            }
        }
    }

    fun resetForgotPasswordState() {
        _uiState.update { it.copy(forgotPasswordState = null) }
    }

    fun resetState() {
        _uiState.update { it.copy(loginState = null) }
    }
}
