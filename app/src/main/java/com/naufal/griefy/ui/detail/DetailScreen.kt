package com.naufal.griefy.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
    var selectedImageIndexForFullScreen by remember { mutableStateOf<Int?>(null) }
    val formatter = remember { SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")) }

    var isPlaying by remember { mutableStateOf(false) }
    val mediaPlayer = remember(songDetails) { android.media.MediaPlayer() }

    var currentPosition by remember { mutableStateOf(0f) }
    var duration by remember { mutableStateOf(0f) }

    DisposableEffect(mediaPlayer) {
        val previewUrl = songDetails?.previewUrl
        if (!previewUrl.isNullOrEmpty()) {
            try {
                mediaPlayer.setDataSource(previewUrl)
                mediaPlayer.prepareAsync()
                mediaPlayer.setOnPreparedListener {
                    mediaPlayer.start()
                    isPlaying = true
                }
                mediaPlayer.setOnCompletionListener {
                    isPlaying = false
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
                } catch (e: Exception) {
                    // ignore if media player is released/reset
                }
                delay(300)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Kenangan", color = Color(0xFF4E4640), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(36.dp)

                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color(0xFF5C524A)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            memory?.let { mem ->
                                navController.navigate(Screen.EditMemory.createRoute(mem.id))
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF5C524A))
                    }

                    IconButton(
                        onClick = {
                            viewModel.moveToTrash(
                                onDeleteSuccess = { navController.navigateUp() }
                            )
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            songDetails?.let { song ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE8E0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    val context = LocalContext.current
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = song.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4E4640),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = song.artistName,
                                    fontSize = 12.sp,
                                    color = Color(0xFF8C8075),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(
                                onClick = {
                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.deezer.com/track/${song.trackId}"))
                                    context.startActivity(webIntent)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Buka di Deezer",
                                    tint = Color(0xFF75685F),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Controls & Progress bar row
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
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color(0xFF5C524A),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                val progress = if (duration > 0) currentPosition / duration else 0f
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = Color(0xFF75685F),
                                    trackColor = Color(0xFFFAF7F2)
                                )
                            } else {
                                Text(
                                    text = "Preview audio tidak tersedia.",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8C8075),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        memory?.let { mem ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAF7F2))
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 8.dp
                    )
            ) {
                // Horizontal Image Pager with reduced height for better screen space
                if (mem.imageUris.isNotEmpty()) {
                    val pagerState = rememberPagerState(initialPage = 0) { mem.imageUris.size }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) { page ->
                            AsyncImage(
                                model = mem.imageUris[page],
                                contentDescription = "Foto Kenangan",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { selectedImageIndexForFullScreen = page },
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Page indicators (dots)
                        if (mem.imageUris.size > 1) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                repeat(mem.imageUris.size) { iteration ->
                                    val color = if (pagerState.currentPage == iteration) Color(0xFF75685F) else Color(0xFFEDE6DC)
                                    Box(
                                        modifier = Modifier
                                            .padding(3.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .size(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metadata Row (Date, Privacy circle indicator, Tags)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date
                    Text(
                        text = formatter.format(Date(mem.createdAt)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8C8075)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Privacy status circle
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (mem.isPublic) Color(0xFF81C784) else Color(0xFFB0A59A))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Tags
                    if (mem.tags.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            itemsIndexed(mem.tags) { _, tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFEDE8E0))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF5C524A)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = mem.title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E4640),
                    modifier = Modifier.padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable content area with fixed size and proper breathing padding
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFEDE6DC), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = mem.content,
                            fontSize = 15.sp,
                            color = Color(0xFF5C524A),
                            lineHeight = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }

    selectedImageIndexForFullScreen?.let { initialIndex ->
        val imageUris = memory?.imageUris ?: emptyList()
        if (imageUris.isNotEmpty()) {
            val pagerState = rememberPagerState(initialPage = initialIndex) {
                imageUris.size
            }

            Dialog(
                onDismissRequest = { selectedImageIndexForFullScreen = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.95f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val uri = imageUris.getOrNull(page)
                            if (uri != null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Foto Penuh - Halaman ${page + 1}",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { selectedImageIndexForFullScreen = null },
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { selectedImageIndexForFullScreen = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(16.dp)
                                .statusBarsPadding()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        if (imageUris.size > 1) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${imageUris.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 24.dp)
                                    .statusBarsPadding()
                            )
                        }
                    }
                }
            }
        }
    }
}