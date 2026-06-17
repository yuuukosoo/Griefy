package com.naufal.griefy.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSongScreen(
    navController: NavController,
    viewModel: SearchSongViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Lagu Kenangan") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Cari judul lagu atau artis...") },
                trailingIcon = {
                    IconButton(onClick = { viewModel.searchSongs() }) {
                        Icon(Icons.Default.Search, contentDescription = "Cari")
                    }
                },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))


            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (viewModel.searchResults.isEmpty() && viewModel.searchQuery.isNotEmpty()) {
                        item {
                            Text("Ketik judul lagu dan tekan tombol cari.", color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    items(viewModel.searchResults) { song ->
                        SongCard(song = song) {
                            navController.previousBackStackEntry?.savedStateHandle?.set("selected_song_track_id", song.trackId)
                            navController.previousBackStackEntry?.savedStateHandle?.set("selected_song_title", song.title)
                            navController.previousBackStackEntry?.savedStateHandle?.set("selected_song_artist", song.artistName)
                            navController.previousBackStackEntry?.savedStateHandle?.set("selected_song_image_url", song.imageUrl)
                            navController.popBackStack()
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun SongCard(song: Song, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            AsyncImage(
                model = song.imageUrl,
                contentDescription = "Cover Album",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = song.title, style = MaterialTheme.typography.titleMedium)
                Text(text = song.artistName, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}