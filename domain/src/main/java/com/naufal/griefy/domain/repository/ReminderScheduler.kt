package com.naufal.griefy.domain.repository

import com.naufal.griefy.domain.model.RemembranceDay

interface ReminderScheduler {
    fun schedule(day: RemembranceDay)
    fun cancel(day: RemembranceDay)
}
