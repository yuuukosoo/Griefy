package com.naufal.griefy.domain.model

data class RemembranceDay(
    val id: Int = 0,
    val title: String,
    val description: String = "",
    val dateTime: Long
)
