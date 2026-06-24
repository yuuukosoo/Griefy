package com.naufal.griefy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoriesUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getMemoriesUseCase: GetMemoriesUseCase,
    private val repository: MemoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    val memories: StateFlow<List<Memory>> = _searchQuery
        .flatMapLatest { query ->
            val currentUserId = authRepository.getCurrentUser()?.uid
            getMemoriesUseCase(query, currentUserId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSaveMemory(memory: Memory) {
        viewModelScope.launch {
            repository.updateMemory(memory.copy(isSaved = !memory.isSaved))
        }
    }
}