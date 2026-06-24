package com.naufal.griefy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.usecase.auth.GetMyUserIdUseCase
import com.naufal.griefy.domain.usecase.profile.GetMyUserProfileUseCase
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoriesUseCase
import com.naufal.griefy.domain.usecase.memory.memories.ToggleSaveLocalMemoryUseCase
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    getMemoriesUseCase: GetMemoriesUseCase,
    getMyUserProfileUseCase: GetMyUserProfileUseCase,
    private val toggleSaveLocalMemoryUseCase: ToggleSaveLocalMemoryUseCase,
    getMyUserIdUseCase: GetMyUserIdUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val userProfileFlow: Flow<UserProfile?> = getMyUserProfileUseCase().map { resource ->
        if (resource is Resource.Success) resource.data else null
    }

    private val memoriesFlow: Flow<List<Memory>> = _searchQuery
        .flatMapLatest { query ->
            val currentUserId = getMyUserIdUseCase()
            getMemoriesUseCase(query, currentUserId)
        }

    val uiState: StateFlow<HomeState> = combine(
        _searchQuery,
        userProfileFlow,
        memoriesFlow
    ) { query, profile, memoriesList ->
        HomeState(
            searchQuery = query,
            userProfile = profile,
            memories = memoriesList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleSaveMemory(memory: Memory) {
        viewModelScope.launch {
            toggleSaveLocalMemoryUseCase(memory)
        }
    }
}