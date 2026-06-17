package com.naufal.griefy.ui.searchmemory

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.ui.navigation.FloatingNavigationDock
import com.naufal.griefy.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SearchMemoryScreen(
    navController: NavController,
    viewModel: SearchMemoryViewModel = hiltViewModel()
) {
    val memories by viewModel.publicMemories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F2))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {



            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                placeholder = { Text("Cari album publik...", color = Color(0xFFB0A59A)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF8C8075)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus", tint = Color(0xFF8C8075))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF75685F),
                    unfocusedBorderColor = Color(0xFFEDE6DC),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Content Feed
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (memories.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isEmpty()) "Belum ada album publik." else "Tidak ada album publik yang cocok.",
                                color = Color(0xFF8C8075),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    items(memories, key = { it.id }) { memory ->
                        PublicMemoryCard(
                            memory = memory,
                            onClick = {
                                navController.navigate(Screen.DetailMemory.createRoute(memory.id))
                            }
                        )
                    }
                }
            }
        }

        // Shared Custom Navigation Dock overlay
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingNavigationDock(
                navController = navController,
                currentRoute = Screen.SearchMemory.route
            )
        }
    }
}

@Composable
fun PublicMemoryCard(memory: Memory, onClick: () -> Unit) {
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }
    val dateString = formatter.format(Date(memory.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (memory.imageUris.isNotEmpty()) {
                AsyncImage(
                    model = memory.imageUris.first(),
                    contentDescription = "Foto Sampul",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = memory.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4E4640)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Oleh Pengguna Lain • $dateString",
                    fontSize = 12.sp,
                    color = Color(0xFF8C8075),
                    modifier = Modifier.weight(1f)
                )

                if (!memory.songTrackId.isNullOrEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFEDE8E0))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Lagu Tersemat",
                            tint = Color(0xFF75685F),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = memory.songTitle ?: "Lagu Tersemat",
                            fontSize = 10.sp,
                            color = Color(0xFF5C524A),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 120.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = memory.content,
                fontSize = 14.sp,
                color = Color(0xFF5C524A),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (memory.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "🏷️ ${memory.tags.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = Color(0xFF75685F),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
