package com.naufal.griefy.ui.splash

import androidx.lifecycle.ViewModel
import com.naufal.griefy.domain.usecase.auth.GetMyUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    getMyUserIdUseCase: GetMyUserIdUseCase
) : ViewModel() {

    private val currentUserId = getMyUserIdUseCase()

    val isLoggedIn: Boolean = currentUserId.isNotEmpty()
}
