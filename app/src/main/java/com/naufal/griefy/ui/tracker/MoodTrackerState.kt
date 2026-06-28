package com.naufal.griefy.ui.tracker
import com.naufal.griefy.domain.model.DailyMood
import java.time.LocalDate
import java.time.YearMonth
data class MoodTrackerState(
    val currentMonth: YearMonth = YearMonth.now(),
    val moodsForMonth: List<DailyMood> = emptyList(),
    val selectedDate: LocalDate? = null,
    val showMoodSelector: Boolean = false,
    val calendarGrid: List<List<LocalDate?>> = emptyList()
)
