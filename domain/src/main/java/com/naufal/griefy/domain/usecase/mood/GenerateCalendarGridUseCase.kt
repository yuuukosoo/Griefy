package com.naufal.griefy.domain.usecase.mood

import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class GenerateCalendarGridUseCase @Inject constructor() {
    operator fun invoke(yearMonth: YearMonth): List<List<LocalDate?>> {
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7
        
        val totalCells = daysInMonth + firstDayOfWeek
        val rows = kotlin.math.ceil(totalCells / 7.0).toInt()
        val gridItems = mutableListOf<LocalDate?>()
        
        repeat(firstDayOfWeek) {
            gridItems.add(null)
        }
        for (i in 1..daysInMonth) {
            gridItems.add(yearMonth.atDay(i))
        }
        
        // Pad the end to ensure full rows
        val remaining = (rows * 7) - gridItems.size
        repeat(remaining) {
            gridItems.add(null)
        }
        
        return gridItems.chunked(7)
    }
}
