package com.naufal.griefy.ui.otherprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoryCountUseCase
import com.naufal.griefy.domain.usecase.profile.GetUserProfileUseCase
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class   OtherProfileViewModel @Inject constructor(
    private val getUserProfileUseCase: GetUserProfileUseCase,
    private val getMemoryCountUseCase: GetMemoryCountUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(OtherProfileState())
    val uiState: StateFlow<OtherProfileState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadMemoryCount()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(profileState = Resource.Loading()) }
            getUserProfileUseCase(userId).collect { result ->
                _uiState.update { it.copy(profileState = result) }
            }
        }
    }

    private fun loadMemoryCount() {
        viewModelScope.launch {
            getMemoryCountUseCase(userId).collect { count ->
                _uiState.update { it.copy(memoryCount = count) }
            }
        }
    }
}
