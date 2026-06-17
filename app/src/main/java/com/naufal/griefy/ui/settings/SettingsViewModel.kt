package com.naufal.griefy.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val sharedPreferences = context.getSharedPreferences("settings_pref", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(sharedPreferences.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentLanguageCode = MutableStateFlow(getCurrentLanguage())
    val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        sharedPreferences.edit {
            putBoolean("dark_mode", enabled)
        }
    }

    fun changeLanguage(langCode: String) {
        _currentLanguageCode.value = langCode
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
    }

    private fun getCurrentLanguage(): String {
        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)
        return currentLocale?.language ?: "en"
    }
}
