package com.naufal.griefy.ui.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.RemembranceDay
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
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val scheduler = ReminderScheduler(context)

    val remembranceDays: StateFlow<List<RemembranceDay>> = repository.getAllRemembranceDays()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addReminder(title: String, description: String, dateTime: Long) {
        viewModelScope.launch {
            val newDay = RemembranceDay(title = title, description = description, dateTime = dateTime)
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
