package com.naufal.griefy.ui.navigation
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object CreateMemory : Screen("create_memory")
   object DetailMemory : Screen("detail_memory/{memoryId}") {
        fun createRoute(id: Int) = "detail_memory/$id"
    }
    object EditMemory : Screen("edit_memory/{memoryId}") {
        fun createRoute(id: Int) = "edit_memory/$id"
    }
    object SearchPublic : Screen("search_public")
    object SearchMemory : Screen("search_memory")
    object Profile : Screen("profile")
    object OtherProfile : Screen("other_profile/{userId}") {
        fun createRoute(userId: String) = "other_profile/$userId"
    }
    object Settings : Screen("settings")
    object Trash : Screen("trash")
    object Reminders : Screen("reminders")
    object Saved : Screen("saved")
    object PhotoAlbum : Screen("photo_album")
    object ForgotPassword : Screen("forgot_password")
}