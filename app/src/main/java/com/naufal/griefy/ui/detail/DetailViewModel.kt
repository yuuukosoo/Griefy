package com.naufal.griefy.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.Song
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MemoryRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val memoryId: Int = checkNotNull(savedStateHandle["memoryId"])

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

    val memory: StateFlow<Memory?> = combine(
        repository.getMemoryByIdAsFlow(memoryId),
        userProfile
    ) { mem, profile ->
        if (mem != null && profile != null && (mem.userName == "Khalish" || mem.userName.isNullOrEmpty() || mem.userName == profile.displayName)) {
            mem.copy(
                userName = profile.displayName,
                userAvatar = profile.avatarBase64
            )
        } else {
            mem
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _songDetails = MutableStateFlow<Song?>(null)
    val songDetails: StateFlow<Song?> = _songDetails.asStateFlow()

    val isOwnMemory: StateFlow<Boolean> = memory.map { mem ->
        val currentUser = authRepository.getCurrentUser()
        mem != null && currentUser != null && mem.userId == currentUser.uid
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    init {
        viewModelScope.launch {
            repository.getMemoryByIdAsFlow(memoryId).collect { memory ->
                memory?.songTrackId?.let { trackId ->
                    val song = repository.getSongDetails(trackId)
                    _songDetails.value = song
                } ?: run {
                    _songDetails.value = null
                }
            }
        }
    }

    fun moveToTrash(onDeleteSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.moveToTrash(memoryId)
            onDeleteSuccess()
        }
    }
}