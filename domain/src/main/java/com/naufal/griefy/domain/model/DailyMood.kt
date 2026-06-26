package com.naufal.griefy.domain.model

data class DailyMood(
    val id: String,
    val dateString: String,
    val moodValue: String,
    val userId: String?
)
