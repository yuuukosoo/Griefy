package com.naufal.griefy.ui.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.auth.IsCurrentUserUseCase
import com.naufal.griefy.domain.usecase.memory.trash.DeletePermanentlyUseCase
import com.naufal.griefy.domain.usecase.memory.trash.EmptyTrashUseCase
import com.naufal.griefy.domain.usecase.memory.trash.GetTrashedMemoriesUseCase
import com.naufal.griefy.domain.usecase.memory.trash.RestoreMemoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    getTrashedMemoriesUseCase: GetTrashedMemoriesUseCase,
    private val restoreMemoryUseCase: RestoreMemoryUseCase,
    private val deletePermanentlyUseCase: DeletePermanentlyUseCase,
    private val emptyTrashUseCase: EmptyTrashUseCase,
    private val isCurrentUserUseCase: IsCurrentUserUseCase
) : ViewModel() {

    val uiState: StateFlow<TrashState> = getTrashedMemoriesUseCase()
        .map { TrashState(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrashState()
        )

    fun restoreMemory(id: Int) {
        viewModelScope.launch {
            restoreMemoryUseCase(id)
        }
    }

    fun deletePermanently(id: Int) {
        viewModelScope.launch {
            deletePermanentlyUseCase(id)
        }
    }

    fun emptyTrash() {
        viewModelScope.launch {
            emptyTrashUseCase(uiState.value.trashedMemories)
        }
    }

    fun isCurrentUser(userId: String?): Boolean {
        return isCurrentUserUseCase(userId)
    }
}