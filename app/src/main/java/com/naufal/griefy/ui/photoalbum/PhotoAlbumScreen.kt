package com.naufal.griefy.ui.photoalbum

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.scaled
import com.naufal.griefy.util.adaptiveWidth
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoAlbumScreen(
    viewModel: PhotoAlbumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val photoGroups = uiState.photoGroups
    val selectedPhotoUri = remember { mutableStateOf<String?>(null) }

    val horizontalPadding = getAdaptiveHorizontalPadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .adaptiveWidth()
        ) {
            val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
            val headerBgColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
            val headerTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = headerBgColor
                    )
                    .padding(start = horizontalPadding, end = horizontalPadding, top = 24.dp.scaled(), bottom = 24.dp.scaled()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Album Foto",
                    fontSize = 24.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = headerTextColor
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (photoGroups.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada foto yang diupload.")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 24.dp.scaled(), bottom = 100.dp.scaled()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    photoGroups.forEachIndexed { index, group ->
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (index > 0) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(top = 16.dp.scaled()),
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    )
                                }
                                Text(
                                    text = group.date,
                                    fontSize = 14.sp.scaled(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = if (index > 0) 16.dp.scaled() else 8.dp.scaled(), bottom = 8.dp.scaled())
                                )
                            }
                        }
                        items(group.photos) { photoUri ->
                            AsyncImage(
                                model = photoUri,
                                contentDescription = "Foto album",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedPhotoUri.value = photoUri }
                            )
                        }
                    }
                }
            }
        }
    }

    selectedPhotoUri.value?.let { uri ->
        Dialog(
            onDismissRequest = { selectedPhotoUri.value = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { selectedPhotoUri.value = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = uri,
                    contentDescription = "Foto full size",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
