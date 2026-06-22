package com.naufal.griefy.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.naufal.griefy.R


@Composable
fun FloatingNavigationDock(
    navController: NavController,
    currentRoute: String,
    onFabClick: (() -> Unit)? = null
) {
    val isDark = isSystemInDarkTheme()
    val dockBgColor = if (isDark) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.primary
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        color = dockBgColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(72.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Home Tab
            NavigationTabItem(
                selected = currentRoute == Screen.Home.route,
                onClick = {
                    if (currentRoute != Screen.Home.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    }
                },
                icon = Icons.Default.Home,
                label = stringResource(R.string.nav_home),
                contentDescription = stringResource(R.string.nav_home)
            )

            // 2. Search Tab
            NavigationTabItem(
                selected = currentRoute == Screen.SearchMemory.route,
                onClick = {
                    if (currentRoute != Screen.SearchMemory.route) {
                        navController.navigate(Screen.SearchMemory.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = Icons.Default.Search,
                label = stringResource(R.string.nav_search),
                contentDescription = stringResource(R.string.nav_search)
            )

            // 3. Create Button (Center)
            val createBgColor = if (isDark) MaterialTheme.colorScheme.primary else Color.White
            val createIconColor = if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(createBgColor)
                    .clickable {
                        if (onFabClick != null) {
                            onFabClick()
                        } else {
                            navController.navigate(Screen.CreateMemory.route)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.nav_write_memory_desc),
                    tint = createIconColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            // 4. Saved Tab
            NavigationTabItem(
                selected = currentRoute == Screen.Saved.route,
                onClick = {
                    if (currentRoute != Screen.Saved.route) {
                        navController.navigate(Screen.Saved.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = Icons.Default.Bookmark,
                label = stringResource(R.string.nav_saved),
                contentDescription = stringResource(R.string.nav_saved)
            )

            // 5. Profile Tab
            NavigationTabItem(
                selected = currentRoute == Screen.Profile.route,
                onClick = {
                    if (currentRoute != Screen.Profile.route) {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = Icons.Default.Person,
                label = stringResource(R.string.nav_profile),
                contentDescription = stringResource(R.string.nav_profile)
            )
        }
    }
}

@Composable
private fun NavigationTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val backgroundColor = if (selected) {
        if (isDark) MaterialTheme.colorScheme.primary else Color.White
    } else {
        Color.Transparent
    }

    val contentColor = if (selected) {
        if (isDark) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    } else {
        if (isDark) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f)
    }

    val animatedBgColor by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 300),
        label = "tab_bg_color"
    )
    val animatedContentColor by animateColorAsState(
        targetValue = contentColor,
        animationSpec = tween(durationMillis = 300),
        label = "tab_content_color"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.25f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )
    val iconOffset by animateDpAsState(
        targetValue = if (selected) (-3).dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_offset"
    )

    Row(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(animatedBgColor)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = animatedContentColor,
            modifier = Modifier
                .offset(y = iconOffset)
                .graphicsLayer(
                    scaleX = iconScale,
                    scaleY = iconScale
                )
                .size(18.dp)
        )

        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(animationSpec = tween(150, delayMillis = 150)) + expandHorizontally(
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
            ),
            exit = fadeOut(animationSpec = tween(150)) + shrinkHorizontally(
                animationSpec = tween(300, easing = LinearOutSlowInEasing)
            )
        ) {
            Row {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    color = animatedContentColor,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        }
    }
}

