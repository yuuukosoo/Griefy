package com.naufal.griefy.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val memories by viewModel.memories.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Griefy ✨") },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profil")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.CreateMemory.route) }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Cari kenangan...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (memories.isEmpty()) {
                    item {
                        val message = if (searchQuery.isEmpty()) {
                            "Belum ada kenangan. Tekan + untuk menambah."
                        } else {
                            "Tidak ditemukan kenangan dengan kata kunci \"$searchQuery\"."
                        }
                        Text(message, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    items(memories) { memory ->
                        MemoryCard(
                            memory = memory,
                            onClick = {
                                navController.navigate(Screen.DetailMemory.createRoute(memory.id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryCard(memory: Memory, onClick: () -> Unit) {
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }
    val dateString = formatter.format(Date(memory.createdAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp), // Rounded corners as shown in the image
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE8E0)) // Soft grey/beige background
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Square Thumbnail Image or Placeholder
            Box(
                modifier = Modifier
                    .size(110.dp) // Large square thumbnail size
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFC5C0B7)), // Placeholder background color
                contentAlignment = Alignment.Center
            ) {
                if (memory.imageUris.isNotEmpty()) {
                    AsyncImage(
                        model = memory.imageUris.first(),
                        contentDescription = "Foto Kenangan",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No Image",
                        tint = Color(0xFF8C8075),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right side: Info Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(110.dp), // Align height with the image
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top row: Music (left) and Date (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Music indicator
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC5C0B7))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (memory.songTrackId != null) "Music" else "No Music",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8C8075)
                        )
                    }

                    // Date
                    Text(
                        text = dateString,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8C8075)
                    )
                }

                // Middle: Title and Truncated Content
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = memory.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4E4640),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = memory.content,
                        fontSize = 12.sp,
                        color = Color(0xFF8C8075),
                        maxLines = 2, // Truncate at 2 lines as shown in the image
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom row: Tags (left) and Privacy Lock Icon (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tags chips list
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val displayTags = memory.tags.take(2) // Take first 2 tags
                        displayTags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFC5C0B7))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = tag,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E4640)
                                )
                            }
                        }
                    }

                    // Privacy Lock Icon
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White), // White background circle for the lock icon as shown in image
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (!memory.isPublic) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Privasi",
                            tint = Color(0xFF5C524A),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}