package com.naufal.griefy.util

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@ReadOnlyComposable
fun Dp.scaled(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val scale = minOf(screenWidth, 500) / 500f
    return (this.value * scale).dp
}

@Composable
@ReadOnlyComposable
fun TextUnit.scaled(): TextUnit {
    if (!this.isSp) return this
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val scale = minOf(screenWidth, 500) / 500f
    return (this.value * scale).sp
}

@Composable
@ReadOnlyComposable
fun getAdaptiveHorizontalPadding(): Dp {
    return 48.dp.scaled()
}

fun Modifier.adaptiveWidth(): Modifier {
    return this.widthIn(max = 500.dp)
}
