package com.naufal.griefy.ui.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.model.DailyMood
import com.naufal.griefy.domain.model.Mood
import com.naufal.griefy.domain.usecase.mood.GetMoodsByMonthUseCase
import com.naufal.griefy.domain.usecase.mood.SaveDailyMoodUseCase
import com.naufal.griefy.domain.usecase.mood.DeleteDailyMoodUseCase
import com.naufal.griefy.domain.usecase.mood.GenerateCalendarGridUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class MoodTrackerState(
    val currentMonth: YearMonth = YearMonth.now(),
    val moodsForMonth: List<DailyMood> = emptyList(),
    val selectedDate: LocalDate? = null,
    val showMoodSelector: Boolean = false,
    val calendarGrid: List<List<LocalDate?>> = emptyList()
)

@HiltViewModel
class MoodTrackerViewModel @Inject constructor(
    private val getMoodsByMonthUseCase: GetMoodsByMonthUseCase,
    private val saveDailyMoodUseCase: SaveDailyMoodUseCase,
    private val deleteDailyMoodUseCase: DeleteDailyMoodUseCase,
    private val generateCalendarGridUseCase: GenerateCalendarGridUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodTrackerState())
    val uiState: StateFlow<MoodTrackerState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(calendarGrid = generateCalendarGridUseCase(it.currentMonth)) }
        loadMoodsForMonth(_uiState.value.currentMonth)
    }

    fun onPreviousMonth() {
        val newMonth = _uiState.value.currentMonth.minusMonths(1)
        _uiState.update { 
            it.copy(
                currentMonth = newMonth,
                calendarGrid = generateCalendarGridUseCase(newMonth)
            ) 
        }
        loadMoodsForMonth(newMonth)
    }

    fun onNextMonth() {
        val newMonth = _uiState.value.currentMonth.plusMonths(1)
        _uiState.update { 
            it.copy(
                currentMonth = newMonth,
                calendarGrid = generateCalendarGridUseCase(newMonth)
            ) 
        }
        loadMoodsForMonth(newMonth)
    }

    fun onDateClicked(date: LocalDate) {
        _uiState.update { 
            it.copy(
                selectedDate = date,
                showMoodSelector = true
            ) 
        }
    }

    fun onDismissMoodSelector() {
        _uiState.update { it.copy(showMoodSelector = false, selectedDate = null) }
    }

    fun onMoodSelected(mood: Mood) {
        val date = _uiState.value.selectedDate ?: return
        val dateString = date.toString()
        val currentMonth = _uiState.value.currentMonth
        
        viewModelScope.launch {
            saveDailyMoodUseCase(dateString, mood.stringValue)
            loadMoodsForMonth(currentMonth)
        }
        
        onDismissMoodSelector()
    }

    fun onMoodDeleted() {
        val date = _uiState.value.selectedDate ?: return
        val dateString = date.toString()
        val currentMonth = _uiState.value.currentMonth
        
        viewModelScope.launch {
            deleteDailyMoodUseCase(dateString)
            loadMoodsForMonth(currentMonth)
        }
        
        onDismissMoodSelector()
    }

    private fun loadMoodsForMonth(yearMonth: YearMonth) {
        val monthString = String.format(java.util.Locale.US, "%04d-%02d", yearMonth.year, yearMonth.monthValue)
        viewModelScope.launch {
            getMoodsByMonthUseCase(monthString).collect { moods ->
                _uiState.update { it.copy(moodsForMonth = moods) }
            }
        }
    }
}
