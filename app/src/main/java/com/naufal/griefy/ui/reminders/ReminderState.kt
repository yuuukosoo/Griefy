package com.naufal.griefy.ui.reminders

import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.domain.model.RemembranceDay

data class ReminderState(
    val remembranceDays: List<RemembranceDay> = emptyList(),
    val memories: List<Memory> = emptyList()
)
