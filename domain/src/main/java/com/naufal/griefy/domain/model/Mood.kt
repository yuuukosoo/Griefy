package com.naufal.griefy.domain.model

enum class Mood(val stringValue: String) {
    NEGATIVE("Negative"),
    NEUTRAL("Neutral"),
    POSITIVE("Positive");

    companion object {
        fun fromString(value: String?): Mood? = entries.find { it.stringValue == value }
    }
}
