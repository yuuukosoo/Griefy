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
import com.naufal.griefy.domain.model.Memory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
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
            FloatingActionButton(onClick = { /* Nanti navigasi ke halaman Create */ }) {
                Text("+")
            }
        }
    ) { paddingValues ->
        // LazyColumn adalah List Recycle-able di Jetpack Compose (Poin 1.d)
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (memories.isEmpty()) {
                item {
                    Text(
                        text = "Belum ada kenangan. Tekan + untuk menambah.",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                items(memories) { memory ->
                    MemoryCard(memory = memory)
                }
            }
        }
    }
}


@Composable
fun MemoryCard(memory: Memory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = memory.content, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))


            if (memory.tags.isNotEmpty()) {
                Text(
                    text = "🏷️ ${memory.tags.joinToString(", ")}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}