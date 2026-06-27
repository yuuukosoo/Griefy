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
import androidx.compose.material.icons.filled.PhotoAlbum
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.naufal.griefy.R

@Composable
fun FloatingNavigationDock(
    navController: NavController,
    currentRoute: String,
    onFabClick: (() -> Unit)? = null
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 16.dp,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .height(84.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Grouped Navigation Icons
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Home Tab
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
                    contentDescription = stringResource(R.string.nav_home)
                )

                // Search Tab
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
                    contentDescription = stringResource(R.string.nav_search)
                )

                // Saved Tab
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
                    contentDescription = stringResource(R.string.nav_saved)
                )

                // Photo Album Tab
                NavigationTabItem(
                    selected = currentRoute == Screen.PhotoAlbum.route,
                    onClick = {
                        if (currentRoute != Screen.PhotoAlbum.route) {
                            navController.navigate(Screen.PhotoAlbum.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = Icons.Default.PhotoAlbum,
                    contentDescription = "Album Foto"
                )

                // Profile Tab
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
                    contentDescription = stringResource(R.string.nav_profile)
                )
            }

            // 2. Separate FAB (Add Button)
            val createBgColor = MaterialTheme.colorScheme.surface
            val createIconColor = MaterialTheme.colorScheme.primary

            Surface(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (onFabClick != null) {
                            onFabClick()
                        } else {
                            navController.navigate(Screen.CreateMemory.route)
                        }
                    },
                shape = CircleShape,
                color = createBgColor,
                shadowElevation = 6.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.nav_write_memory_desc),
                        tint = createIconColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) {
        MaterialTheme.colorScheme.surface
    } else {
        Color.Transparent
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
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
        targetValue = if (selected) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "icon_scale"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(animatedBgColor)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = animatedContentColor,
            modifier = Modifier
                .graphicsLayer(
                    scaleX = iconScale,
                    scaleY = iconScale
                )
                .size(24.dp)
        )
    }
}
