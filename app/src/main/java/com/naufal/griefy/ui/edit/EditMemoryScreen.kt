package com.naufal.griefy.ui.edit

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.R
import com.naufal.griefy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemoryScreen(
    navController: NavController,
    viewModel: EditMemoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showAddLabelDialog by remember { mutableStateOf(false) }
    var newLabelText by remember { mutableStateOf("") }

    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val returnedTrackId by savedStateHandle?.getStateFlow<String?>("selected_song_track_id", null)?.collectAsState() ?: remember { mutableStateOf(null) }
    val returnedTitle by savedStateHandle?.getStateFlow<String?>("selected_song_title", null)?.collectAsState() ?: remember { mutableStateOf(null) }
    val returnedArtist by savedStateHandle?.getStateFlow<String?>("selected_song_artist", null)?.collectAsState() ?: remember { mutableStateOf(null) }
    val returnedImageUrl by savedStateHandle?.getStateFlow<String?>("selected_song_image_url", null)?.collectAsState() ?: remember { mutableStateOf(null) }

    LaunchedEffect(returnedTrackId) {
        returnedTrackId?.let { trackId ->
            viewModel.setSelectedSong(
                trackId = trackId,
                title = returnedTitle,
                artist = returnedArtist,
                imageUrl = returnedImageUrl
            )
            savedStateHandle?.set("selected_song_track_id", null)
            savedStateHandle?.set("selected_song_title", null)
            savedStateHandle?.set("selected_song_artist", null)
            savedStateHandle?.set("selected_song_image_url", null)
        }
    }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
            }
            viewModel.addImages(uris)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F2)) // Cozy warm paper background (Mymory style)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEDE8E0), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = Color(0xFF5C524A),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = stringResource(R.string.edit_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E4640)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Photo Picker Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // 1:1 Square ratio
                    .border(1.dp, Color(0xFFEDE6DC), RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDE8E0), RoundedCornerShape(16.dp))
                    .clickable {
                        multiplePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (viewModel.selectedImageUris.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.create_select_photo_desc),
                            tint = Color(0xFF8C8075),
                            modifier = Modifier.size(36.dp) // Larger plus icon for square box
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.create_select_photo_text),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8C8075)
                        )
                    }
                } else {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(viewModel.selectedImageUris) { uri ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f) // Square image item
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = stringResource(R.string.create_selected_photo_desc),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .clickable { viewModel.removeImage(uri) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.delete),
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title Input Field
            TextField(
                value = viewModel.titleText,
                onValueChange = { viewModel.onTitleChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = stringResource(R.string.create_title_placeholder),
                        color = Color(0xFFB0A59A),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E4640)
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = Color(0xFFEDE6DC)
            )

            // Content Input Field - limited height and internally scrollable
            TextField(
                value = viewModel.contentText,
                onValueChange = { viewModel.onContentChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = {
                    Text(
                        text = stringResource(R.string.create_content_placeholder),
                        color = Color(0xFFB0A59A),
                        fontSize = 16.sp
                    )
                },
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color(0xFF5C524A)
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tags Display (Chips) if any custom tags exist
            if (viewModel.tagsList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.tagsList) { tag ->
                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFFEDE6DC), RoundedCornerShape(10.dp))
                                .background(Color(0xFFEDE8E0), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = tag, fontSize = 11.sp, color = Color(0xFF5C524A))
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.delete),
                                    tint = Color(0xFF8C8075),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { viewModel.removeTag(tag) }
                                )
                            }
                        }
                    }
                }
            }


            viewModel.selectedSongTrackId?.let { trackId ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.dp, Color(0xFFEDE6DC), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE8E0))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!viewModel.selectedSongImageUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = viewModel.selectedSongImageUrl,
                                contentDescription = stringResource(R.string.search_song_album_cover_desc),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(0xFF8C8075), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = viewModel.selectedSongTitle ?: stringResource(R.string.create_memory_song),
                                style = MaterialTheme.typography.titleSmall,
                                color = Color(0xFF4E4640),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = viewModel.selectedSongArtist ?: stringResource(R.string.create_artist),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF8C8075)
                            )
                        }

                        IconButton(onClick = { viewModel.setSelectedSong(null, null, null, null) }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.create_delete_song_desc),
                                tint = Color(0xFF8C8075)
                            )
                        }
                    }
                }
            }

            // Flat Elegant Add Song & Add Label Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Add Song Button
                Button(
                    onClick = { navController.navigate(Screen.SearchPublic.route) },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, Color(0xFFEDE6DC), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDE8E0))
                ) {
                    Text(text = stringResource(R.string.create_add_song), fontSize = 12.sp, color = Color(0xFF5C524A), fontWeight = FontWeight.Bold)
                }

                // Add Label Button
                Button(
                    onClick = { showAddLabelDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .border(1.dp, Color(0xFFEDE6DC), RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDE8E0))
                ) {
                    Text(text = stringResource(R.string.create_add_label), fontSize = 12.sp, color = Color(0xFF5C524A), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEDE8E0))
                    .padding(4.dp)
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!viewModel.isPublic) Color(0xFF8C7D73) else Color.Transparent)
                        .clickable { viewModel.onPrivacyChange(false) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.private_text),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!viewModel.isPublic) Color.White else Color(0xFF5C524A)
                    )
                }


                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewModel.isPublic) Color(0xFF8C7D73) else Color.Transparent)
                        .clickable { viewModel.onPrivacyChange(true) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.public_text),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.isPublic) Color.White else Color(0xFF5C524A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (viewModel.titleText.isNotBlank() || viewModel.contentText.isNotBlank() || viewModel.selectedImageUris.isNotEmpty()) {
                        viewModel.updateMemory(
                            onUpdateSuccess = { navController.navigateUp() }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF75685F)), // Cozy warm clay
                enabled = viewModel.titleText.isNotBlank() || viewModel.contentText.isNotBlank() || viewModel.selectedImageUris.isNotEmpty()
            ) {
                Text(
                    text = stringResource(R.string.edit_save_button),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    // Add Label Dialog
    if (showAddLabelDialog) {
        AlertDialog(
            onDismissRequest = { showAddLabelDialog = false },
            title = { Text(stringResource(R.string.create_dialog_title), color = Color(0xFF4E4640)) },
            text = {
                OutlinedTextField(
                    value = newLabelText,
                    onValueChange = { newLabelText = it },
                    placeholder = { Text(stringResource(R.string.create_dialog_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLabelText.isNotBlank()) {
                            viewModel.addTag(newLabelText)
                            newLabelText = ""
                            showAddLabelDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF75685F))
                ) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLabelDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFF8C7D73))
                }
            }
        )
    }
}