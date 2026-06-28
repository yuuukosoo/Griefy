package com.naufal.griefy

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.compose.dialog
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import android.content.SharedPreferences
import androidx.navigation.compose.currentBackStackEntryAsState
import com.naufal.griefy.ui.navigation.GriefyNavHost
import com.naufal.griefy.ui.navigation.FloatingNavigationDock
import com.naufal.griefy.ui.create.CreateMemoryScreen
import com.naufal.griefy.ui.detail.DetailScreen
import com.naufal.griefy.ui.edit.EditMemoryScreen
import com.naufal.griefy.ui.home.HomeScreen
import com.naufal.griefy.ui.login.LoginScreen
import com.naufal.griefy.ui.navigation.Screen
import com.naufal.griefy.ui.profile.ProfileScreen
import com.naufal.griefy.ui.otherprofile.OtherProfileScreen
import com.naufal.griefy.ui.register.RegisterScreen
import com.naufal.griefy.ui.splash.SplashScreen
import com.naufal.griefy.ui.search.SearchSongScreen
import com.naufal.griefy.ui.searchmemory.SearchMemoryScreen
import com.naufal.griefy.ui.reminders.ReminderScreen
import com.naufal.griefy.ui.settings.SettingsScreen
import androidx.appcompat.app.AppCompatDelegate
import com.naufal.griefy.ui.theme.GriefyTheme
import com.naufal.griefy.ui.trash.TrashScreen
import com.naufal.griefy.ui.saved.SavedScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("settings_pref", MODE_PRIVATE)
        if (sharedPreferences.contains("dark_mode")) {
            val isDark = sharedPreferences.getBoolean("dark_mode", false)
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val sharedPreferencesCompose = remember { context.getSharedPreferences("settings_pref", MODE_PRIVATE) }
            val systemDark = isSystemInDarkTheme()
            
            var isDarkTheme by remember {
                mutableStateOf(
                    if (sharedPreferencesCompose.contains("dark_mode")) {
                        sharedPreferencesCompose.getBoolean("dark_mode", false)
                    } else {
                        systemDark
                    }
                )
            }

            DisposableEffect(sharedPreferencesCompose) {
                val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
                    if (key == "dark_mode") {
                        isDarkTheme = prefs.getBoolean("dark_mode", false)
                    }
                }
                sharedPreferencesCompose.registerOnSharedPreferenceChangeListener(listener)
                onDispose {
                    sharedPreferencesCompose.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }

            GriefyTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    GriefyNavHost()

                }
            }
        }
    }
}