package com.naufal.griefy.ui.search


import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.scaled
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.PlatformTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSongScreen(
    navController: NavController,
    viewModel: SearchSongViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery = uiState.searchQuery
    val searchResults = uiState.searchResults
    val isLoading = uiState.isLoading

    val playingTrackId by viewModel.playingTrackId.collectAsState()
    val isMediaPlaying by viewModel.isMediaPlaying.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPlayback()
        }
    }

    val onCardClick = { song: Song ->
        viewModel.onSongClick(song)
    }

    val horizontalPadding = getAdaptiveHorizontalPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = horizontalPadding, end = horizontalPadding, bottom = 16.dp.scaled(), top = 48.dp.scaled())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.size(36.dp.scaled())
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp.scaled())
                    )
                }

                Spacer(modifier = Modifier.width(16.dp.scaled()))

                Text(
                    text = stringResource(R.string.search_song_title),
                    fontSize = 20.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp.scaled()))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                                text = stringResource(R.string.search_song_placeholder),
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp.scaled()
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onQueryChange(it) },
                            textStyle = TextStyle(fontSize = 14.sp.scaled(), color = MaterialTheme.colorScheme.onBackground),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { viewModel.searchSongs() }),
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
                                .clickable { viewModel.onQueryChange("") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp.scaled()))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp.scaled())
                ) {
                    if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.search_song_empty_hint), 
                                color = MaterialTheme.colorScheme.outline,
                                fontSize = 14.sp.scaled()
                            )
                        }
                    }

                    items(searchResults) { song ->
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
        shape = RoundedCornerShape(16.dp.scaled()),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp.scaled()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = song.imageUrl,
                    contentDescription = stringResource(R.string.search_song_album_cover_desc),
                    modifier = Modifier
                        .size(56.dp.scaled())
                        .clip(RoundedCornerShape(8.dp.scaled())),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .size(56.dp.scaled())
                        .clip(RoundedCornerShape(8.dp.scaled()))
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
                        modifier = Modifier.size(24.dp.scaled())
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp.scaled()))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title, 
                    fontSize = 16.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.height(2.dp.scaled()))
                Text(
                    text = song.artistName, 
                    fontSize = 12.sp.scaled(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = LocalTextStyle.current.copy(
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }

            if (isSelected) {
                Spacer(modifier = Modifier.width(8.dp.scaled()))
                Box(
                    modifier = Modifier
                        .size(36.dp.scaled())
                        .clip(CircleShape)
                        .background(Color(0xFF4CAF50))
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.create_add_song),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp.scaled())
                    )
                }
            }
        }
    }
}