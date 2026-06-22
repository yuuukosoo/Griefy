package com.naufal.griefy.ui.home

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
class HomeViewModel @Inject constructor(
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

    val memories: StateFlow<List<Memory>> = combine(
        repository.getAllMemories(),
        _searchQuery,
        userProfile
    ) { memoriesList, query, profile ->
        val currentUserId = authRepository.getCurrentUser()?.uid
        val filteredList = memoriesList.filter { memory ->
            !memory.isPublic || 
            (currentUserId != null && memory.userId == currentUserId) || 
            memory.userName == Memory.DEFAULT_USERNAME || 
            memory.userName.isNullOrEmpty()
        }
        val processedList = filteredList.map { memory ->
            if (profile != null && (memory.userName == Memory.DEFAULT_USERNAME || memory.userName.isNullOrEmpty() || memory.userName == profile.displayName)) {
                memory.copy(
                    userName = profile.displayName,
                    userAvatar = profile.avatarBase64
                )
            } else {
                memory
            }
        }
        val sortedList = processedList.sortedByDescending { it.createdAt }
        if (query.isBlank()) {
            sortedList
        } else {
            sortedList.filter { memory ->
                memory.title.contains(query, ignoreCase = true) ||
                memory.content.contains(query, ignoreCase = true) ||
                memory.tags.any { tag -> tag.contains(query, ignoreCase = true) }
            }
        }
    }.stateIn(
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