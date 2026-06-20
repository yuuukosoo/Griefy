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
        _searchQuery,
        userProfile
    ) { memories, query, profile ->
        val processedList = memories.map { memory ->
            if (profile != null && (memory.userName == "Khalish" || memory.userName.isNullOrEmpty() || memory.userName == profile.displayName)) {
                memory.copy(
                    userName = profile.displayName,
                    userAvatar = profile.avatarBase64
                )
            } else {
                memory
            }
        }
        if (query.isBlank()) {
            processedList
        } else {
            processedList.filter {
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
}
