package com.naufal.griefy.ui.searchmemory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.usecase.memory.memories.SearchMemoriesUseCase
import com.naufal.griefy.domain.usecase.memory.memories.ToggleSaveMemoryUseCase
import com.naufal.griefy.domain.usecase.auth.IsCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SearchMemoryViewModel @Inject constructor(
    searchMemoriesUseCase: SearchMemoriesUseCase,
    private val toggleSaveMemoryUseCase: ToggleSaveMemoryUseCase,
    private val isCurrentUserUseCase: IsCurrentUserUseCase
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val uiState: StateFlow<SearchMemoryState> = combine(
        _searchQuery,
        searchMemoriesUseCase(_searchQuery)
    ) { query, memoriesList ->
        SearchMemoryState(
            searchQuery = query,
            publicMemories = memoriesList
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SearchMemoryState()
    )
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    fun isCurrentUser(userId: String?): Boolean {
        return isCurrentUserUseCase(userId)
    }
    fun toggleSaveMemory(memory: Memory) {
        viewModelScope.launch {
            toggleSaveMemoryUseCase(memory)
        }
    }
}
