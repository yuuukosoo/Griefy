package com.naufal.griefy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import com.naufal.griefy.R

@Composable
fun GriefyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor to false by default to enforce our custom theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val customColorScheme = lightColorScheme(
        primary = colorResource(id = R.color.brand_primary),
        background = colorResource(id = R.color.brand_background),
        surface = colorResource(id = R.color.neutral_surface),
        surfaceVariant = colorResource(id = R.color.neutral_outline),
        onSurfaceVariant = colorResource(id = R.color.neutral_text),
        outline = colorResource(id = R.color.neutral_outline),
        onBackground = colorResource(id = R.color.neutral_text),
        onSurface = colorResource(id = R.color.neutral_text),
        onPrimary = colorResource(id = R.color.neutral_surface),
        secondary = colorResource(id = R.color.accent_save),
        error = colorResource(id = R.color.accent_error),
        onError = colorResource(id = R.color.neutral_surface)
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> customColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}