package com.naufal.griefy.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (memories.isEmpty()) {
                item {
                    Text("Belum ada kenangan. Tekan + untuk menambah.", modifier = Modifier.padding(16.dp))
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

@Composable
fun MemoryCard(memory: Memory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {


            if (memory.imageUris.isNotEmpty()) {
                AsyncImage(
                    model = memory.imageUris.first(),
                    contentDescription = "Foto Kenangan",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }


            Text(text = memory.content, style = MaterialTheme.typography.bodyLarge)

            if (memory.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "🏷️ ${memory.tags.joinToString(", ")}", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}