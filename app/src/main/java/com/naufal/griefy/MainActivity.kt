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
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.naufal.griefy.ui.create.CreateMemoryScreen
import com.naufal.griefy.ui.detail.DetailScreen
import com.naufal.griefy.ui.edit.EditMemoryScreen
import com.naufal.griefy.ui.home.HomeScreen
import com.naufal.griefy.ui.login.LoginScreen
import com.naufal.griefy.ui.navigation.Screen
import com.naufal.griefy.ui.profile.ProfileScreen
import com.naufal.griefy.ui.register.RegisterScreen
import com.naufal.griefy.ui.splash.SplashScreen
import com.naufal.griefy.ui.search.SearchSongScreen
import com.naufal.griefy.ui.searchmemory.SearchMemoryScreen
import com.naufal.griefy.ui.reminders.ReminderScreen
import com.naufal.griefy.ui.settings.SettingsScreen
import androidx.appcompat.app.AppCompatDelegate
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import com.naufal.griefy.ui.theme.GriefyTheme
import com.naufal.griefy.ui.trash.TrashScreen
import com.naufal.griefy.worker.TrashCleanupWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("settings_pref", Context.MODE_PRIVATE)
        if (sharedPreferences.contains("dark_mode")) {
            val isDark = sharedPreferences.getBoolean("dark_mode", false)
            AppCompatDelegate.setDefaultNightMode(
                if (isDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }
        super.onCreate(savedInstanceState)

        val trashCleanupWorkRequest = PeriodicWorkRequestBuilder<TrashCleanupWorker>(
            1, TimeUnit.DAYS 
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "TrashCleanupWork",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            trashCleanupWorkRequest
        )
        setContent {
            GriefyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Splash.route
                    ) {

                        composable(Screen.Splash.route) {
                            SplashScreen(navController = navController)
                        }

                        composable(Screen.Login.route) {
                            LoginScreen(navController = navController)
                        }

                        composable(Screen.Register.route) {
                            RegisterScreen(navController = navController)
                        }

                        composable(Screen.Home.route) {
                            HomeScreen(navController = navController)
                        }

                        composable(Screen.Profile.route) {
                            ProfileScreen(navController = navController)
                        }

                        composable(Screen.Settings.route) {
                            SettingsScreen(navController = navController)
                        }
                        
                        composable(Screen.CreateMemory.route) {
                            CreateMemoryScreen(navController = navController)
                        }

                        composable(
                            route = Screen.DetailMemory.route,
                            arguments = listOf(navArgument("memoryId") { type = NavType.IntType })
                        ) {
                            DetailScreen(navController = navController)
                        }

                        composable(
                            route = Screen.EditMemory.route,
                            arguments = listOf(navArgument("memoryId") { type = NavType.IntType })
                        ) {
                            EditMemoryScreen(navController = navController)
                        }

                        composable(Screen.SearchPublic.route) {
                            SearchSongScreen(navController = navController)
                        }

                        composable(Screen.SearchMemory.route) {
                            SearchMemoryScreen(navController = navController)
                        }

                        composable(Screen.Trash.route) {
                            TrashScreen(navController = navController)
                        }

                        composable(Screen.Reminders.route) {
                            ReminderScreen(navController = navController)
                        }


                    }
                }
            }
        }
    }
}