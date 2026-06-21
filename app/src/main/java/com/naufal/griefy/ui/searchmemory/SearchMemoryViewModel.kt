package com.naufal.griefy.ui.searchmemory

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
class SearchMemoryViewModel @Inject constructor(
    private val repository: MemoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

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

    val publicMemories: StateFlow<List<Memory>> = combine(
        repository.getPublicMemories(),
        repository.getAllMemories(),
        _searchQuery,
        userProfile
    ) { remoteMemories, localMemories, query, profile ->
        val processedList = remoteMemories.map { remote ->
            val localMatch = localMemories.find { it.createdAt == remote.createdAt && it.title == remote.title }
            val isSaved = localMatch?.isSaved ?: false
            val id = localMatch?.id ?: remote.id

            val memory = remote.copy(
                id = id,
                isSaved = isSaved
            )

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
            sortedList.filter {
                it.title.contains(query, ignoreCase = true) ||
                it.content.contains(query, ignoreCase = true) ||
                it.tags.any { tag -> tag.contains(query, ignoreCase = true) }
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
            val localMemories = repository.getAllMemories().first()
            val localMatch = localMemories.find { it.createdAt == memory.createdAt && it.title == memory.title }
            if (localMatch != null) {
                repository.updateMemory(localMatch.copy(isSaved = !localMatch.isSaved))
            } else {
                repository.addMemory(memory.copy(isSaved = true))
            }
        }
    }
}
