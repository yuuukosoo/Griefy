package com.naufal.griefy.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import com.naufal.griefy.domain.usecase.auth.LoginUseCase
import com.naufal.griefy.domain.usecase.auth.SendPasswordResetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase
) : ViewModel() {

    private val _loginState = MutableStateFlow<Resource<User>?>(null)
    val loginState: StateFlow<Resource<User>?> = _loginState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<Resource<Unit>?>(null)
    val forgotPasswordState: StateFlow<Resource<Unit>?> = _forgotPasswordState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loginUseCase(email, password).collect { result ->
                _loginState.value = result
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            sendPasswordResetUseCase(email).collect { result ->
                _forgotPasswordState.value = result
            }
        }
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = null
    }

    fun resetState() {
        _loginState.value = null
    }
}
