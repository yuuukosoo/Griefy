package com.naufal.griefy.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naufal.griefy.util.toImageModel
import com.naufal.griefy.util.adaptiveWidth
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.scaled
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.R
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.ui.navigation.FloatingNavigationDock
import com.naufal.griefy.ui.navigation.Screen
import androidx.compose.ui.text.TextStyle
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val memories by viewModel.memories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val horizontalPadding = getAdaptiveHorizontalPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .adaptiveWidth()
        ) {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 8.dp.scaled(), bottom = 100.dp.scaled()),
                verticalArrangement = Arrangement.spacedBy(16.dp.scaled())
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp.scaled(), bottom = 16.dp.scaled()),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.home_greeting, userProfile?.displayName ?: "User"),
                                fontSize = 24.sp.scaled(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = stringResource(R.string.home_subtitle),
                                fontSize = 14.sp.scaled(),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { navController.navigate(Screen.Settings.route) },
                            modifier = Modifier
                                .size(36.dp.scaled())
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title),
                                tint = Color.White
                            )
                        }
                    }
                }

                item {
                    // Local Search bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp.scaled())
                            .height(44.dp.scaled())
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp.scaled()))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp.scaled()))
                            .padding(horizontal = 12.dp.scaled()),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp.scaled())
                            )
                            Spacer(modifier = Modifier.width(8.dp.scaled()))
                            
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.home_search_placeholder),
                                        color = MaterialTheme.colorScheme.outline,
                                        fontSize = 14.sp.scaled()
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { viewModel.setSearchQuery(it) },
                                    textStyle = TextStyle(fontSize = 14.sp.scaled(), color = MaterialTheme.colorScheme.onBackground),
                                    singleLine = true,
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            
                            if (searchQuery.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp.scaled()))
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .size(20.dp.scaled())
                                        .clickable { viewModel.setSearchQuery("") }
                                )
                            }
                        }
                    }
                }

                if (memories.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp.scaled()),
                            contentAlignment = Alignment.Center
                        ) {
                            val message = if (searchQuery.isEmpty()) {
                                stringResource(R.string.home_empty_memories)
                            } else {
                                stringResource(R.string.home_no_search_results, searchQuery)
                            }
                            Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    }
                } else {
                    items(memories, key = { it.id }) { memory ->
                        MemoryCard(
                            memory = memory,
                            onClick = {
                                navController.navigate(Screen.DetailMemory.createRoute(memory.id))
                            },
                            onProfileClick = {
                                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                val authorId = memory.userId
                                if (authorId.isNullOrEmpty() || authorId == currentUserId) {
                                    navController.navigate(Screen.Profile.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    navController.navigate(Screen.OtherProfile.createRoute(authorId))
                                }
                            },
                            onSaveClick = {
                                viewModel.toggleSaveMemory(memory)
                            }
                        )
                    }
                }
            }
        }


        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingNavigationDock(
                navController = navController,
                currentRoute = Screen.Home.route
            )
        }
    }
}

@Composable
fun MemoryCard(
    memory: Memory,
    onClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")) }
    val dateString = formatter.format(Date(memory.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp.scaled()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp.scaled())) {
            // Profile Header Row with Save button on far right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp.scaled()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onProfileClick() }
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp.scaled())
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatar = memory.userAvatar
                        if (!avatar.isNullOrEmpty()) {
                            AsyncImage(
                                model = avatar.toImageModel(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text("👤", fontSize = 10.sp.scaled())
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp.scaled()))
                    Text(
                        text = memory.userName ?: Memory.DEFAULT_USERNAME,
                        fontSize = 14.sp.scaled(),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Save button
                val isSaved = memory.isSaved
                IconButton(
                    onClick = { onSaveClick() },
                    modifier = Modifier.size(32.dp.scaled())
                ) {
                    if (isSaved) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = stringResource(R.string.nav_saved),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp.scaled())
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = stringResource(R.string.nav_saved),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp.scaled())
                        )
                    }
                }
            }

            if (memory.imageUris.isNotEmpty()) {
                AsyncImage(
                    model = memory.imageUris.first().toImageModel(),
                    contentDescription = stringResource(R.string.home_memory_photo_desc),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp.scaled())
                        .clip(RoundedCornerShape(12.dp.scaled())),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp.scaled()))
            }

            Text(
                text = memory.title,
                fontSize = 18.sp.scaled(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(4.dp.scaled()))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = dateString,
                    fontSize = 12.sp.scaled(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                if (!memory.songTrackId.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(6.dp.scaled()),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = stringResource(R.string.home_pinned_song),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp.scaled())
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp.scaled()))

            Text(
                text = memory.content,
                fontSize = 14.sp.scaled(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (memory.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp.scaled()))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp.scaled()),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    memory.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp.scaled()))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 8.dp.scaled(), vertical = 4.dp.scaled())
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp.scaled(),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}