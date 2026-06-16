package com.naufal.griefy.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController, // <--- TAMBAHAN BARU
    viewModel: HomeViewModel = hiltViewModel()
) {
    val memories by viewModel.memories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Griefy ✨") },
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (memories.isEmpty()) {
                item { Text("Belum ada kenangan. Tekan + untuk menambah.", modifier = Modifier.padding(16.dp)) }
            } else {
                items(memories) { memory -> MemoryCard(memory = memory) }
            }
        }
    }
}

@Composable
fun MemoryCard(memory: Memory) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = memory.content, style = MaterialTheme.typography.bodyLarge)
            if (memory.tags.isNotEmpty()) {
                Text(text = "🏷️ ${memory.tags.joinToString(", ")}", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}


@Composable
fun CreateMemoryScreen(navController: NavController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ini Halaman Tambah Kenangan Baru")
            Button(onClick = { navController.navigateUp() }) { Text("Kembali") }
        }
    }
}