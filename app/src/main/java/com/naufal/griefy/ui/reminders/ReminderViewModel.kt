package com.naufal.griefy.ui.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.RemembranceDay
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.repository.RemembranceRepository
import com.naufal.griefy.ui.settings.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReminderViewModel @Inject constructor(
    private val repository: RemembranceRepository,
    memoryRepository: MemoryRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val scheduler = ReminderScheduler(context)

    val remembranceDays: StateFlow<List<RemembranceDay>> = repository.getAllRemembranceDays()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val memories: StateFlow<List<Memory>> = memoryRepository.getAllMemories()
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
            val generatedId = repository.addRemembranceDay(newDay)
            scheduler.schedule(newDay.copy(id = generatedId.toInt()))
        }
    }

    fun updateReminder(day: RemembranceDay) {
        viewModelScope.launch {
            repository.updateRemembranceDay(day)
            scheduler.schedule(day)
        }
    }

    fun deleteReminder(day: RemembranceDay) {
        viewModelScope.launch {
            repository.deleteRemembranceDay(day)
            scheduler.cancel(day)
        }
    }
}
