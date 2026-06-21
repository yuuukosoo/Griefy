package com.naufal.griefy.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.repository.AuthRepository
import com.naufal.griefy.domain.repository.MemoryRepository
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val authRepository: AuthRepository,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _deleteAccountResult = MutableSharedFlow<Resource<Unit>>()
    val deleteAccountResult: SharedFlow<Resource<Unit>> = _deleteAccountResult.asSharedFlow()

    fun deleteAccount() {
        viewModelScope.launch {
            _deleteAccountResult.emit(Resource.Loading())
            try {
                memoryRepository.clearAllLocalMemories()
            } catch (_: Exception) {
                // Ignore Room delete failure
            }
            val result = authRepository.deleteAccount()
            _deleteAccountResult.emit(result)
        }
    }

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
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
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
