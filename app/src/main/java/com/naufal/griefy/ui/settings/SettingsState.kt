package com.naufal.griefy.ui.settings
import com.naufal.griefy.domain.util.Resource
data class SettingsState(
    val isDarkMode: Boolean = false,
    val currentLanguageCode: String = "en",
    val deleteAccountResult: Resource<Unit>? = null
)
