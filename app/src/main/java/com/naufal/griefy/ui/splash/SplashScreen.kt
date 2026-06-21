package com.naufal.griefy.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.*
import com.naufal.griefy.R
import com.naufal.griefy.ui.navigation.Screen
import com.naufal.griefy.util.scaled
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.logo_animation))
    

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )


    LaunchedEffect(Unit) {
        delay(3000)
        val isLoggedIn = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
        val startRoute = if (isLoggedIn) Screen.Home.route else Screen.Login.route
        navController.navigate(startRoute) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(240.dp.scaled())
        )
    }
}
