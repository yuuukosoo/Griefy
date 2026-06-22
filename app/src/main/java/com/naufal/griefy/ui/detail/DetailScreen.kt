package com.naufal.griefy.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.util.toImageModel
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.scaled
import com.naufal.griefy.util.adaptiveWidth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.R
import com.naufal.griefy.ui.navigation.Screen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val memory by viewModel.memory.collectAsState()
    val songDetails by viewModel.songDetails.collectAsState()
    val isOwnMemory by viewModel.isOwnMemory.collectAsState()
    val horizontalPadding = getAdaptiveHorizontalPadding()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val showDeleteDialog = remember { mutableStateOf(false) }
    val selectedImageIndexForFullScreen = remember { mutableStateOf<Int?>(null) }
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }

    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember(songDetails) { android.media.MediaPlayer() }

    var currentPosition by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }

    DisposableEffect(mediaPlayer) {
        val previewUrl = songDetails?.previewUrl
        if (!previewUrl.isNullOrEmpty()) {
            try {
                mediaPlayer.setDataSource(previewUrl)
                mediaPlayer.isLooping = true
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    mediaPlayer.start()
                    isPlaying = true
                }
                mediaPlayer.setOnCompletionListener {
                    try {
                        mediaPlayer.seekTo(0)
                        mediaPlayer.start()
                        isPlaying = true
                    } catch (_: Exception) {
                        try {
                            mediaPlayer.reset()
                            mediaPlayer.setDataSource(previewUrl)
                            mediaPlayer.isLooping = true
                            mediaPlayer.prepareAsync()
                        } catch (_: Exception) {
                            isPlaying = false
                        }
                    }
                }
                mediaPlayer.setOnErrorListener { _, what, extra ->
                    android.util.Log.e("MEDIA_PLAYER", "MediaPlayer error: what=$what, extra=$extra. Menginisialisasi ulang...")
                    try {
                        mediaPlayer.reset()
                        mediaPlayer.setDataSource(previewUrl)
                        mediaPlayer.isLooping = true
                        mediaPlayer.prepareAsync()
                    } catch (_: Exception) {
                        isPlaying = false
                    }
                    true
                }
            } catch (e: Exception) {
                android.util.Log.e("MEDIA_PLAYER", "Gagal inisialisasi MediaPlayer", e)
            }
        }
        onDispose {
            mediaPlayer.release()
            isPlaying = false
            currentPosition = 0f
            duration = 0f
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isPlaying) {
                try {
                    currentPosition = mediaPlayer.currentPosition.toFloat()
                    duration = mediaPlayer.duration.toFloat()
                } catch (_: Exception) {
                    // ignore if media player is released/reset
                }
                delay(300)
            }
        }
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 32.dp.scaled(), start = horizontalPadding, end = horizontalPadding)
                        .widthIn(max = 500.dp)
                        .fillMaxWidth()
                        .height(64.dp.scaled()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier
                                .size(36.dp.scaled())
                                .offset(x = (-8).dp.scaled())
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp.scaled())
                            )
                        }

                        Text(
                            text = stringResource(R.string.detail_title),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp.scaled(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.offset(x = (-4).dp.scaled())
                        )
                    }

                    if (isOwnMemory) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = {
                                    memory?.let { mem ->
                                        navController.navigate(Screen.EditMemory.createRoute(mem.id))
                                    }
                                },
                                modifier = Modifier.size(36.dp.scaled())
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp.scaled())
                                )
                            }

                            IconButton(
                                onClick = {
                                    showDeleteDialog.value = true
                                },
                                modifier = Modifier
                                    .size(36.dp.scaled())
                                    .offset(x = 8.dp.scaled())
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp.scaled())
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (!isLandscape) {
                songDetails?.let { song ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(
                                start = horizontalPadding,
                                end = horizontalPadding,
                                top = 16.dp.scaled(),
                                bottom = 48.dp.scaled()
                            ),
                        shape = RoundedCornerShape(16.dp.scaled()),
                        colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else Color(0xFFC4D8BF)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        val context = LocalContext.current
                        Column(
                            modifier = Modifier.padding(16.dp.scaled())
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (song.imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = song.imageUrl,
                                        contentDescription = stringResource(R.string.search_song_album_cover_desc),
                                        modifier = Modifier
                                            .size(48.dp.scaled())
                                            .clip(RoundedCornerShape(8.dp.scaled())),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp.scaled()))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = song.title,
                                        fontSize = 16.sp.scaled(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = song.artistName,
                                        fontSize = 12.sp.scaled(),
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        val webIntent = Intent(Intent.ACTION_VIEW, "https://www.deezer.com/track/${song.trackId}".toUri())
                                        context.startActivity(webIntent)
                                    },
                                    modifier = Modifier.size(36.dp.scaled())
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = stringResource(R.string.detail_open_deezer_desc),
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp.scaled())
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp.scaled()))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (!song.previewUrl.isNullOrEmpty()) {
                                    IconButton(
                                        onClick = {
                                            if (isPlaying) {
                                                mediaPlayer.pause()
                                                isPlaying = false
                                            } else {
                                                mediaPlayer.start()
                                                isPlaying = true
                                            }
                                        },
                                        modifier = Modifier.size(36.dp.scaled())
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isPlaying) 
                                                stringResource(R.string.detail_pause_desc) 
                                            else 
                                                stringResource(R.string.detail_play_desc),
                                            tint = Color.White,
                                            modifier = Modifier.size(28.dp.scaled())
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(8.dp.scaled()))

                                    val progress = if (duration > 0) currentPosition / duration else 0f
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp.scaled())
                                            .clip(RoundedCornerShape(3.dp.scaled())),
                                        color = Color.White,
                                        trackColor = Color.White.copy(alpha = 0.4f)
                                    )
                                } else {
                                    Text(
                                        text = stringResource(R.string.detail_preview_unavailable),
                                        fontSize = 12.sp.scaled(),
                                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(vertical = 8.dp.scaled())
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        memory?.let { mem ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 8.dp.scaled()
                    ),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .adaptiveWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Profile Header Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .clickable {
                                val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                                val authorId = mem.userId
                                if (authorId.isNullOrEmpty() || authorId == currentUserId) {
                                    navController.navigate(Screen.Profile.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                } else {
                                    navController.navigate(Screen.OtherProfile.createRoute(authorId))
                                }
                            }
                            .padding(bottom = 12.dp.scaled())
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp.scaled())
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val avatar = mem.userAvatar
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
                            text = mem.userName ?: Memory.DEFAULT_USERNAME,
                            fontSize = 14.sp.scaled(),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (mem.imageUris.isNotEmpty()) {
                        val pagerState = rememberPagerState(initialPage = 0) { mem.imageUris.size }
                        
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp.scaled())
                        ) {
                            val pagerModifier = if (isLandscape) {
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                                    .height(220.dp.scaled())
                                    .clip(RoundedCornerShape(16.dp.scaled()))
                            } else {
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = horizontalPadding)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(16.dp.scaled()))
                            }

                            HorizontalPager(
                                state = pagerState,
                                modifier = pagerModifier
                            ) { page ->
                                AsyncImage(
                                    model = mem.imageUris[page].toImageModel(),
                                    contentDescription = stringResource(R.string.home_memory_photo_desc),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp.scaled()))
                                        .clickable { selectedImageIndexForFullScreen.value = page },
                                    contentScale = ContentScale.Crop
                                )
                            }

                            if (mem.imageUris.size > 1) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(top = 10.dp.scaled()),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    repeat(mem.imageUris.size) { iteration ->
                                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                        Box(
                                            modifier = Modifier
                                                .padding(3.dp.scaled())
                                                .clip(CircleShape)
                                                .background(color)
                                                .size(8.dp.scaled())
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp.scaled()))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatter.format(Date(mem.createdAt)),
                            fontSize = 14.sp.scaled(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.width(12.dp.scaled()))

                        Box(
                            modifier = Modifier
                                .size(12.dp.scaled())
                                .clip(CircleShape)
                                .background(if (mem.isPublic) Color(0xFF81C784) else MaterialTheme.colorScheme.outline)
                        )

                        Spacer(modifier = Modifier.width(16.dp.scaled()))

                        if (mem.tags.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp.scaled(), Alignment.End),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(mem.tags) { _, tag ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp.scaled()))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(horizontal = 10.dp.scaled(), vertical = 4.dp.scaled())
                                    ) {
                                        Text(
                                            text = tag,
                                            fontSize = 11.sp.scaled(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp.scaled()))

                    Text(
                        text = mem.title,
                        fontSize = 22.sp.scaled(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = horizontalPadding)
                    )

                    Spacer(modifier = Modifier.height(12.dp.scaled()))

                    Text(
                        text = mem.content,
                        fontSize = 16.sp.scaled(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp.scaled(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                    )
                }

                if (isLandscape && songDetails != null) {
                    val song = songDetails!!
                    if (!song.previewUrl.isNullOrEmpty()) {
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    mediaPlayer.pause()
                                    isPlaying = false
                                } else {
                                    mediaPlayer.start()
                                    isPlaying = true
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = horizontalPadding, bottom = 16.dp.scaled())
                                .size(48.dp.scaled())
                                .background(
                                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else Color(0xFFC4D8BF),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) 
                                    stringResource(R.string.detail_pause_desc) 
                                else 
                                    stringResource(R.string.detail_play_desc),
                                tint = Color.White,
                                modifier = Modifier.size(24.dp.scaled())
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedImageIndexForFullScreen.value != null && memory != null) {
        val pagerState = rememberPagerState(initialPage = selectedImageIndexForFullScreen.value!!) { memory!!.imageUris.size }
        Dialog(
            onDismissRequest = { selectedImageIndexForFullScreen.value = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = memory!!.imageUris[page].toImageModel(),
                        contentDescription = stringResource(R.string.home_memory_photo_desc),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

                IconButton(
                    onClick = { selectedImageIndexForFullScreen.value = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(24.dp)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.clear),
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (showDeleteDialog.value) {
        Dialog(
            onDismissRequest = { showDeleteDialog.value = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                shape = RoundedCornerShape(16.dp.scaled()),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp.scaled())
                ) {
                    Text(
                        text = stringResource(R.string.dialog_delete_memory_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(8.dp.scaled()))
                    Text(
                        text = stringResource(R.string.dialog_delete_memory_text),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(16.dp.scaled()))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDeleteDialog.value = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.cancel), fontSize = 16.sp.scaled())
                        }
                        Spacer(modifier = Modifier.width(8.dp.scaled()))
                        TextButton(
                            onClick = {
                                showDeleteDialog.value = false
                                viewModel.moveToTrash(
                                    onDeleteSuccess = { navController.navigateUp() }
                                )
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.delete), fontSize = 16.sp.scaled(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}