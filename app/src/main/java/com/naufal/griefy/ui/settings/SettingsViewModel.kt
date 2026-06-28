package com.naufal.griefy.ui.settings
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naufal.griefy.domain.usecase.auth.DeleteAccountUseCase
import com.naufal.griefy.domain.usecase.auth.LogoutUseCase
import com.naufal.griefy.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val deleteAccountUseCase: DeleteAccountUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(
        SettingsState(
            isDarkMode = sharedPreferences.getBoolean("dark_mode", false),
            currentLanguageCode = getCurrentLanguage()
        )
    )
    val uiState: StateFlow<SettingsState> = _uiState.asStateFlow()
    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(deleteAccountResult = Resource.Loading()) }
            val result = deleteAccountUseCase()
            _uiState.update { it.copy(deleteAccountResult = result) }
        }
    }
    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            onComplete()
        }
    }
    fun setDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
        sharedPreferences.edit {
            putBoolean("dark_mode", enabled)
        }
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
    fun changeLanguage(langCode: String) {
        _uiState.update { it.copy(currentLanguageCode = langCode) }
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(langCode))
    }
    private fun getCurrentLanguage(): String {
        val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)
        return currentLocale?.language ?: "en"
    }
    fun resetDeleteAccountResult() {
        _uiState.update { it.copy(deleteAccountResult = null) }
    }
}
