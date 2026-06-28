package com.naufal.griefy.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naufal.griefy.ui.create.CreateMemoryScreen
import com.naufal.griefy.ui.detail.DetailScreen
import com.naufal.griefy.ui.edit.EditMemoryScreen
import com.naufal.griefy.ui.home.HomeScreen
import com.naufal.griefy.ui.login.LoginScreen
import com.naufal.griefy.ui.otherprofile.OtherProfileScreen
import com.naufal.griefy.ui.profile.ProfileScreen
import com.naufal.griefy.ui.register.RegisterScreen
import com.naufal.griefy.ui.reminders.ReminderScreen
import com.naufal.griefy.ui.saved.SavedScreen
import com.naufal.griefy.ui.search.SearchSongScreen
import com.naufal.griefy.ui.searchmemory.SearchMemoryScreen
import com.naufal.griefy.ui.settings.SettingsScreen
import com.naufal.griefy.ui.splash.SplashScreen
import com.naufal.griefy.ui.trash.TrashScreen

@Composable
fun GriefyNavHost() {
val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize()) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showDock = currentRoute in listOf(
            Screen.Home.route,
            Screen.SearchMemory.route,
            Screen.Saved.route,
            Screen.Profile.route,
            Screen.PhotoAlbum.route
        )

        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(durationMillis = 350)
                ) + fadeIn(animationSpec = tween(350))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(durationMillis = 350)
                ) + fadeOut(animationSpec = tween(350))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(durationMillis = 350)
                ) + fadeIn(animationSpec = tween(350))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(durationMillis = 350)
                ) + fadeOut(animationSpec = tween(350))
            },
            modifier = Modifier.fillMaxSize()
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
                ProfileScreen()
            }

            composable(
                route = Screen.OtherProfile.route,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) {
                OtherProfileScreen(navController = navController)
            }

            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            
            composable(Screen.CreateMemory.route) {
                CreateMemoryScreen(navController = navController)
            }

            dialog(
                route = Screen.DetailMemory.route,
                arguments = listOf(navArgument("memoryId") { type = NavType.IntType }),
                dialogProperties = androidx.compose.ui.window.DialogProperties(
                    usePlatformDefaultWidth = false
                )
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

            composable(Screen.Saved.route) {
                SavedScreen(navController = navController)
            }

            composable(Screen.PhotoAlbum.route) {
                com.naufal.griefy.ui.photoalbum.PhotoAlbumScreen()
            }

        }

        AnimatedVisibility(
            visible = showDock,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingNavigationDock(
                navController = navController,
                currentRoute = currentRoute ?: Screen.Home.route
            )
        }
    }
}
