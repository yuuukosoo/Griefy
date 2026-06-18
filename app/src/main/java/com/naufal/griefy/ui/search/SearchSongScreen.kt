package com.naufal.griefy.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.R
import com.naufal.griefy.domain.model.Song

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSongScreen(
    navController: NavController,
    viewModel: SearchSongViewModel = hiltViewModel()
) {
    var playingTrackId by remember { mutableStateOf<String?>(null) }
    var isMediaPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember { android.media.MediaPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }

    val onCardClick = { song: Song ->
        if (playingTrackId == song.trackId) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
                isMediaPlaying = false
            } else {
                mediaPlayer.start()
                isMediaPlaying = true
            }
        } else {
            mediaPlayer.reset()
            val previewUrl = song.previewUrl
            if (!previewUrl.isNullOrEmpty()) {
                try {
                    mediaPlayer.setDataSource(previewUrl)
                    mediaPlayer.isLooping = true
                    mediaPlayer.prepareAsync()
                    mediaPlayer.setOnPreparedListener {
                        mediaPlayer.start()
                        playingTrackId = song.trackId
                        isMediaPlaying = true
                    }
                    mediaPlayer.setOnCompletionListener {
                        playingTrackId = null
                        isMediaPlaying = false
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SEARCH_PREVIEW", "Gagal memutar lagu pratinjau", e)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                modifier = Modifier.padding(top = 32.dp),
                title = { Text(stringResource(R.string.search_song_title), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 48.dp, vertical = 16.dp)
        ) {

            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_song_placeholder), color = MaterialTheme.colorScheme.outline) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (viewModel.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { 
                            viewModel.onQueryChange("")
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(R.string.clear),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.searchSongs() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
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
                            Text(stringResource(R.string.search_song_empty_hint), color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    items(viewModel.searchResults) { song ->
                        SongCard(
                            song = song,
                            isPlaying = playingTrackId == song.trackId && isMediaPlaying,
                            isSelected = playingTrackId == song.trackId,
                            onCardClick = { onCardClick(song) },
                            onAddClick = {
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_song_track_id", song.trackId)
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_song_title", song.title)
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_song_artist", song.artistName)
                                navController.previousBackStackEntry?.savedStateHandle?.set("selected_song_image_url", song.imageUrl)
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongCard(
    song: Song,
    isPlaying: Boolean,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary) 
        else 
            null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = stringResource(R.string.search_song_album_cover_desc),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = if (isPlaying) 0.4f else 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) 
                            stringResource(R.string.search_song_pause_preview_desc) 
                        else 
                            stringResource(R.string.search_song_play_preview_desc),
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title, 
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artistName, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAddClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.create_add_song),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.search_song_add_button), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}