package com.naufal.griefy.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.User
import com.naufal.griefy.domain.usecase.auth.RegisterUseCase
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _registerState = MutableStateFlow<Resource<User>?>(null)
    val registerState: StateFlow<Resource<User>?> = _registerState.asStateFlow()

    fun register(name: String, email: String, password: String, confirmPass: String) {
        viewModelScope.launch {
            registerUseCase(name, email, password, confirmPass).collect { result ->
                _registerState.value = result
            }
        }
    }

    fun resetState() {
        _registerState.value = null
    }
}
