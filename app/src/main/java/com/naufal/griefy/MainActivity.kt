package com.naufal.griefy

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.naufal.griefy.ui.create.CreateMemoryScreen
import com.naufal.griefy.ui.detail.DetailScreen
import com.naufal.griefy.ui.edit.EditMemoryScreen
import com.naufal.griefy.ui.home.HomeScreen
import com.naufal.griefy.ui.login.LoginScreen
import com.naufal.griefy.ui.navigation.Screen
import com.naufal.griefy.ui.profile.ProfileScreen
import com.naufal.griefy.ui.register.RegisterScreen
import com.naufal.griefy.ui.search.SearchSongScreen
import com.naufal.griefy.ui.settings.SettingsScreen
import com.naufal.griefy.ui.theme.GriefyTheme
import com.naufal.griefy.ui.trash.TrashScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GriefyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Screen.Login.route
                    ) {

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

                        composable(Screen.Trash.route) {
                            TrashScreen(navController = navController)
                        }


                    }
                }
            }
        }
    }
}