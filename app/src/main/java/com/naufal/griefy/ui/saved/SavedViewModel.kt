package com.naufal.griefy.ui.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.usecase.memory.memories.GetSavedMemoriesUseCase
import com.naufal.griefy.domain.usecase.auth.IsCurrentUserUseCase
import com.naufal.griefy.domain.usecase.memory.memories.ToggleSaveLocalMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavedViewModel @Inject constructor(
    getSavedMemoriesUseCase: GetSavedMemoriesUseCase,
    private val toggleSaveLocalMemoryUseCase: ToggleSaveLocalMemoryUseCase,
    private val isCurrentUserUseCase: IsCurrentUserUseCase
) : ViewModel() {

    val uiState: StateFlow<SavedState> = getSavedMemoriesUseCase()
        .map { SavedState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SavedState()
        )

    fun isCurrentUser(userId: String?): Boolean {
        return isCurrentUserUseCase(userId)
    }

    fun toggleSaveMemory(memory: Memory) {
        viewModelScope.launch {
            toggleSaveLocalMemoryUseCase(memory)
        }
    }
}
