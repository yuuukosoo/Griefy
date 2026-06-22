package com.naufal.griefy.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPass: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank() || confirmPass.isBlank()) {
            _registerState.value = Resource.Error("ERROR_ALL_FIELDS_REQUIRED")
            return
        }
        if (password != confirmPass) {
            _registerState.value = Resource.Error("ERROR_PASSWORD_MISMATCH")
            return
        }
        if (password.length < 6) {
            _registerState.value = Resource.Error("ERROR_PASSWORD_TOO_SHORT")
            return
        }
        viewModelScope.launch {
            authRepository.register(name, email, password).collect { result ->
                _registerState.value = result
            }
        }
    }

    fun resetState() {
        _registerState.value = null
    }
}
