package com.naufal.griefy.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val repository: MemoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = flow {
        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            authRepository.getUserProfile(currentUser.uid).collect { resource ->
                if (resource is Resource.Success) {
                    emit(resource.data)
                }
            }
        } else {
            emit(null)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val savedMemories: StateFlow<List<Memory>> = combine(
        repository.getAllMemories(),
        userProfile
    ) { memoriesList, profile ->
        memoriesList.filter { it.isSaved }.map { memory ->
            if (profile != null && (memory.userName == Memory.DEFAULT_USERNAME || memory.userName.isNullOrEmpty() || memory.userName == profile.displayName)) {
                memory.copy(
                    userName = profile.displayName,
                    userAvatar = profile.avatarBase64
                )
            } else {
                memory
            }
        }.sortedByDescending { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleSaveMemory(memory: Memory) {
        viewModelScope.launch {
            repository.updateMemory(memory.copy(isSaved = !memory.isSaved))
        }
    }
}
