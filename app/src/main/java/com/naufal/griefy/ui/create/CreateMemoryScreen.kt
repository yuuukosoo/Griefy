package com.naufal.griefy.ui.create

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.naufal.griefy.util.toImageModel
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.util.adaptiveWidth
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.scaled
import com.naufal.griefy.R
import com.naufal.griefy.ui.navigation.Screen
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMemoryScreen(
    navController: NavController,
    viewModel: CreateMemoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val titleText = state.titleText
    val contentText = state.contentText
    val isPublic = state.isPublic
    val selectedImageUris = state.selectedImageUris
    val tagsList = state.tagsList
    val selectedSongTrackId = state.selectedSongTrackId

    val showAddLabelDialog = remember { mutableStateOf(false) }
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
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (e: SecurityException) {
                    android.util.Log.e("PHOTO_PICKER", "Gagal mengambil izin persisten untuk $uri", e)
                }
            }
            viewModel.addImages(uris)
        }
    }

    val horizontalPadding = getAdaptiveHorizontalPadding()
    val scrollState = rememberScrollState()

    val showExitDialog = remember { mutableStateOf(false) }

    BackHandler(enabled = state.hasChanges) {
        showExitDialog.value = true
    }

    if (showExitDialog.value) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = getAdaptiveHorizontalPadding()),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showExitDialog.value = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_discard_title),
                    fontSize = 20.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = { Text(stringResource(R.string.dialog_discard_text), color = MaterialTheme.colorScheme.onBackground) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog.value = false
                        navController.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.dialog_discard_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog.value = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFF8C7D73))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        val boxBgColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else Color(0xFFC4D8BF)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveWidth()
                .padding(start = horizontalPadding, end = horizontalPadding, top = 48.dp.scaled())
                .imePadding()
                .verticalScroll(scrollState)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp.scaled()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { 
                        if (state.hasChanges) {
                            showExitDialog.value = true
                        } else {
                            navController.navigateUp()
                        }
                    },
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
                    text = stringResource(R.string.create_title),
                    fontSize = 20.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = {
                        if (titleText.isNotBlank() || contentText.isNotBlank() || selectedImageUris.isNotEmpty()) {
                            viewModel.saveMemory(
                                onSaveSuccess = { navController.navigateUp() }
                            )
                        }
                    },
                    enabled = titleText.isNotBlank() || contentText.isNotBlank() || selectedImageUris.isNotEmpty(),
                    modifier = Modifier.size(36.dp.scaled())
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.create_save_button),
                        tint = if (titleText.isNotBlank() || contentText.isNotBlank() || selectedImageUris.isNotEmpty())
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp.scaled())
                    )
                }
            }

            val configuration = LocalConfiguration.current
            val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            if (isLandscape) {
                    // Photo Box - smaller height for landscape
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp.scaled())
                            .background(boxBgColor, RoundedCornerShape(16.dp.scaled()))
                            .clickable {
                                multiplePhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageUris.isEmpty()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(R.string.create_select_photo_desc),
                                    tint = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.size(36.dp.scaled())
                                )
                                Spacer(modifier = Modifier.height(8.dp.scaled()))
                                Text(
                                    text = stringResource(R.string.create_select_photo_text),
                                    fontSize = 14.sp.scaled(),
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            LazyRow(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp.scaled()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp.scaled()),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                items(selectedImageUris) { uri ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .aspectRatio(1f)
                                    ) {
                                        AsyncImage(
                                            model = uri.toString().toImageModel(),
                                            contentDescription = stringResource(R.string.create_selected_photo_desc),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(12.dp.scaled())),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp.scaled())
                                                .size(24.dp.scaled())
                                                .clip(CircleShape)
                                                .background(Color.Black.copy(alpha = 0.6f))
                                                .clickable { viewModel.removeImage(uri) },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.delete),
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp.scaled())
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp.scaled()))

                    // Privacy, Music & Add Label Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp.scaled()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp.scaled())
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                                .clickable { showAddLabelDialog.value = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tag,
                                contentDescription = stringResource(R.string.create_add_label),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp.scaled())
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .size(36.dp.scaled())
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                                .clickable { viewModel.onPrivacyChange(!isPublic) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPublic) Icons.Default.Language else Icons.Default.Lock,
                                contentDescription = if (isPublic) stringResource(R.string.public_text) else stringResource(R.string.private_text),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp.scaled())
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp.scaled()))

                        var showMusicMenu by remember { mutableStateOf(false) }

                        Box {
                            Box(
                                modifier = Modifier
                                    .size(36.dp.scaled())
                                    .clip(CircleShape)
                                    .background(
                                        if (selectedSongTrackId != null)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                    )
                                    .clickable {
                                        if (selectedSongTrackId != null) {
                                            showMusicMenu = true
                                        } else {
                                            navController.navigate(Screen.SearchPublic.route)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = stringResource(R.string.create_memory_song),
                                    tint = if (selectedSongTrackId != null)
                                        Color.White
                                    else
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    modifier = Modifier.size(20.dp.scaled())
                                )
                            }

                            DropdownMenu(
                                expanded = showMusicMenu,
                                onDismissRequest = { showMusicMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.create_change_song)) },
                                    onClick = {
                                        showMusicMenu = false
                                        navController.navigate(Screen.SearchPublic.route)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.create_delete_song_desc)) },
                                    onClick = {
                                        showMusicMenu = false
                                        viewModel.setSelectedSong(null, null, null, null)
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp.scaled()))

                    // Title Input Field
                    TextField(
                        value = titleText,
                        onValueChange = { viewModel.onTitleChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.create_title_placeholder),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontSize = 22.sp.scaled(),
                                fontWeight = FontWeight.Bold
                            )
                        },
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 22.sp.scaled(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp.scaled()),
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Content Input Field
                    TextField(
                        value = contentText,
                        onValueChange = { viewModel.onContentChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp.scaled(), max = 400.dp.scaled()),
                        placeholder = {
                            Text(
                                text = stringResource(R.string.create_content_placeholder),
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                fontSize = 16.sp.scaled()
                            )
                        },
                        textStyle = TextStyle(
                            fontSize = 16.sp.scaled(),
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp.scaled()))

                    // Tags Display
                    if (tagsList.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp.scaled()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp.scaled())
                        ) {
                            items(tagsList) { tag ->
                                Box(
                                    modifier = Modifier
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp.scaled()))
                                        .background(boxBgColor, RoundedCornerShape(10.dp.scaled()))
                                        .padding(horizontal = 10.dp.scaled(), vertical = 6.dp.scaled())
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = tag, fontSize = 11.sp.scaled(), color = MaterialTheme.colorScheme.onBackground)
                                        Spacer(modifier = Modifier.width(4.dp.scaled()))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.delete),
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier
                                                .size(12.dp.scaled())
                                                .clickable { viewModel.removeTag(tag) }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp.scaled()))
            } else {
                // Portrait layout (original)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(boxBgColor, RoundedCornerShape(16.dp.scaled()))
                        .clickable {
                            multiplePhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUris.isEmpty()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.create_select_photo_desc),
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(36.dp.scaled())
                            )
                            Spacer(modifier = Modifier.height(8.dp.scaled()))
                            Text(
                                text = stringResource(R.string.create_select_photo_text),
                                fontSize = 14.sp.scaled(),
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp.scaled()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp.scaled()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(selectedImageUris) { uri ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(1f)
                                ) {
                                    AsyncImage(
                                        model = uri.toString().toImageModel(),
                                        contentDescription = stringResource(R.string.create_selected_photo_desc),
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(12.dp.scaled())),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp.scaled())
                                            .size(24.dp.scaled())
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable { viewModel.removeImage(uri) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.delete),
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp.scaled())
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp.scaled()))

                // Privacy, Music & Add Label Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp.scaled()),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Add Label Icon (Align Left)
                    Box(
                        modifier = Modifier
                            .size(36.dp.scaled())
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                            .clickable { showAddLabelDialog.value = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tag,
                            contentDescription = stringResource(R.string.create_add_label),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp.scaled())
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Privacy Toggle Button (Internet/Language icon if public, Lock icon if private)
                    Box(
                        modifier = Modifier
                            .size(36.dp.scaled())
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
                            .clickable { viewModel.onPrivacyChange(!isPublic) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPublic) Icons.Default.Language else Icons.Default.Lock,
                            contentDescription = if (isPublic) stringResource(R.string.public_text) else stringResource(R.string.private_text),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp.scaled())
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp.scaled()))

                    var showMusicMenu by remember { mutableStateOf(false) }

                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp.scaled())
                                .clip(CircleShape)
                                .background(
                                    if (selectedSongTrackId != null)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                                )
                                .clickable {
                                    if (selectedSongTrackId != null) {
                                        showMusicMenu = true
                                    } else {
                                        navController.navigate(Screen.SearchPublic.route)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = stringResource(R.string.create_memory_song),
                                tint = if (selectedSongTrackId != null)
                                    Color.White
                                else
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                modifier = Modifier.size(20.dp.scaled())
                            )
                        }

                        DropdownMenu(
                            expanded = showMusicMenu,
                            onDismissRequest = { showMusicMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.create_change_song)) },
                                onClick = {
                                    showMusicMenu = false
                                    navController.navigate(Screen.SearchPublic.route)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.create_delete_song_desc)) },
                                onClick = {
                                    showMusicMenu = false
                                    viewModel.setSelectedSong(null, null, null, null)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp.scaled()))

                // Title Input Field
                TextField(
                    value = titleText,
                    onValueChange = { viewModel.onTitleChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.create_title_placeholder),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 22.sp.scaled(),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 22.sp.scaled(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp.scaled()),
                    color = MaterialTheme.colorScheme.outline
                )

                // Content Input Field - takes remaining space and internally scrollable
                TextField(
                    value = contentText,
                    onValueChange = { viewModel.onContentChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 400.dp.scaled(), max = 650.dp.scaled()),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.create_content_placeholder),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            fontSize = 16.sp.scaled()
                        )
                    },
                    textStyle = TextStyle(
                        fontSize = 16.sp.scaled(),
                        color = MaterialTheme.colorScheme.onBackground
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp.scaled()))

                // Tags Display (Chips) if any custom tags exist
                if (tagsList.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp.scaled()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp.scaled())
                    ) {
                        items(tagsList) { tag ->
                            Box(
                                modifier = Modifier
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp.scaled()))
                                    .background(boxBgColor, RoundedCornerShape(10.dp.scaled()))
                                    .padding(horizontal = 10.dp.scaled(), vertical = 6.dp.scaled())
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = tag, fontSize = 11.sp.scaled(), color = MaterialTheme.colorScheme.onBackground)
                                    Spacer(modifier = Modifier.width(4.dp.scaled()))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(R.string.delete),
                                        tint = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier
                                            .size(12.dp.scaled())
                                            .clickable { viewModel.removeTag(tag) }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp.scaled()))
            }




            // Removed bottom label button
            Spacer(modifier = Modifier.height(16.dp.scaled()))
        }
    }


    if (showAddLabelDialog.value) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = getAdaptiveHorizontalPadding()),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showAddLabelDialog.value = false },
            title = { 
                Text(
                    text = stringResource(R.string.create_dialog_title), 
                    fontSize = 20.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                ) 
            },
            text = {
                OutlinedTextField(
                    value = newLabelText,
                    onValueChange = { newLabelText = it },
                    placeholder = { Text(stringResource(R.string.create_dialog_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp.scaled())
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLabelText.isNotBlank()) {
                            viewModel.addTag(newLabelText)
                            newLabelText = ""
                            showAddLabelDialog.value = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLabelDialog.value = false }) {
                    Text(stringResource(R.string.cancel), color = Color(0xFF8C7D73))
                }
            }
        )
    }

    if (state.showOfflineWarningDialog) {
        AlertDialog(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = getAdaptiveHorizontalPadding()),
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { viewModel.dismissOfflineWarningDialog() },
            title = {
                Text(
                    text = stringResource(R.string.offline_warning_title),
                    fontSize = 20.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = { Text(stringResource(R.string.offline_warning_message), color = MaterialTheme.colorScheme.onBackground) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveMemoryAnyway {
                            navController.navigateUp()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.offline_warning_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissOfflineWarningDialog() }) {
                    Text(stringResource(R.string.offline_warning_cancel), color = Color(0xFF8C7D73))
                }
            }
        )
    }
}