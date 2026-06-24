package com.naufal.griefy.ui.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.RemembranceDay
import com.naufal.griefy.domain.usecase.reminder.AddRemembranceDayUseCase
import com.naufal.griefy.domain.usecase.reminder.DeleteRemembranceDayUseCase
import com.naufal.griefy.domain.usecase.memory.memories.GetMemoriesUseCase
import com.naufal.griefy.domain.usecase.auth.GetMyUserIdUseCase
import com.naufal.griefy.domain.usecase.reminder.GetRemembranceDaysUseCase
import com.naufal.griefy.domain.usecase.reminder.UpdateRemembranceDayUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    getRemembranceDaysUseCase: GetRemembranceDaysUseCase,
    private val addRemembranceDayUseCase: AddRemembranceDayUseCase,
    private val updateRemembranceDayUseCase: UpdateRemembranceDayUseCase,
    private val deleteRemembranceDayUseCase: DeleteRemembranceDayUseCase,
    getMyUserIdUseCase: GetMyUserIdUseCase,
    getMemoriesUseCase: GetMemoriesUseCase
) : ViewModel() {

    private val currentUserId = getMyUserIdUseCase()

    val remembranceDays: StateFlow<List<RemembranceDay>> = getRemembranceDaysUseCase(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val memories: StateFlow<List<Memory>> = getMemoriesUseCase("", currentUserId)
        .map { list -> list.filter { it.userId == currentUserId } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addReminder(title: String, description: String, dateTime: Long, memoryId: Int?) {
        viewModelScope.launch {
            val newDay = RemembranceDay(
                title = title,
                description = description,
                dateTime = dateTime,
                memoryId = memoryId
            )
            addRemembranceDayUseCase(newDay)
        }
    }

    fun updateReminder(day: RemembranceDay) {
        viewModelScope.launch {
            updateRemembranceDayUseCase(day)
        }
    }

    fun deleteReminder(day: RemembranceDay) {
        viewModelScope.launch {
            deleteRemembranceDayUseCase(day)
        }
    }
}
