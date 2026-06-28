package com.naufal.griefy.ui.trash
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import com.naufal.griefy.ui.navigation.Screen
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.R
import com.naufal.griefy.domain.model.Memory
import com.naufal.griefy.util.toImageModel
import com.naufal.griefy.util.scaled
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    navController: NavController,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val trashedMemories = uiState.trashedMemories
    val showEmptyTrashDialog = remember { mutableStateOf(false) }
    val showDeleteSingleDialog = remember { mutableStateOf(false) }
    val memoryToDeleteId = remember { mutableStateOf<Int?>(null) }
    val horizontalPadding = getAdaptiveHorizontalPadding()
    if (showEmptyTrashDialog.value) {
        Dialog(
            onDismissRequest = { showEmptyTrashDialog.value = false },
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
                        text = stringResource(R.string.trash_dialog_title), 
                        color = MaterialTheme.colorScheme.onSurface, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(8.dp.scaled()))
                    Text(
                        text = stringResource(R.string.trash_dialog_text), 
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(16.dp.scaled()))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showEmptyTrashDialog.value = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) { 
                            Text(stringResource(R.string.cancel), fontSize = 16.sp.scaled()) 
                        }
                        Spacer(modifier = Modifier.width(8.dp.scaled()))
                        TextButton(
                            onClick = {
                                viewModel.emptyTrash()
                                showEmptyTrashDialog.value = false
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.trash_dialog_confirm), fontSize = 16.sp.scaled(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    if (showDeleteSingleDialog.value) {
        Dialog(
            onDismissRequest = {
                showDeleteSingleDialog.value = false
                memoryToDeleteId.value = null
            },
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
                        text = stringResource(R.string.dialog_delete_permanent_title),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(8.dp.scaled()))
                    Text(
                        text = stringResource(R.string.dialog_delete_permanent_text),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(16.dp.scaled()))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                showDeleteSingleDialog.value = false
                                memoryToDeleteId.value = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.cancel), fontSize = 16.sp.scaled())
                        }
                        Spacer(modifier = Modifier.width(8.dp.scaled()))
                        TextButton(
                            onClick = {
                                memoryToDeleteId.value?.let { id ->
                                    viewModel.deletePermanently(id)
                                }
                                showDeleteSingleDialog.value = false
                                memoryToDeleteId.value = null
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
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                TopAppBar(
                    modifier = Modifier
                        .padding(top = 32.dp.scaled(), start = horizontalPadding - 12.dp.scaled(), end = horizontalPadding)
                        .widthIn(max = 500.dp),
                    title = { Text(stringResource(R.string.trash_title), color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 22.sp.scaled()) },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        if (trashedMemories.isNotEmpty()) {
                            TextButton(
                                onClick = { showEmptyTrashDialog.value = true },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(R.string.clear), fontSize = 16.sp.scaled())
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 500.dp)
            ) {
                Text(
                    text = stringResource(R.string.trash_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp.scaled(),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = horizontalPadding, vertical = 8.dp.scaled())
                )
                if (trashedMemories.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.trash_empty),
                            fontSize = 16.sp.scaled(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 16.dp.scaled()),
                        verticalArrangement = Arrangement.spacedBy(16.dp.scaled())
                    ) {
                        items(trashedMemories) { memory ->
                            TrashedMemoryCard(
                                memory = memory,
                                onRestore = { viewModel.restoreMemory(memory.id) },
                                onDelete = {
                                    memoryToDeleteId.value = memory.id
                                    showDeleteSingleDialog.value = true
                                },
                                onProfileClick = {
                                    val authorId = memory.userId
                                    if (authorId.isNullOrEmpty() || viewModel.isCurrentUser(authorId)) {
                                        navController.navigate(Screen.Profile.route) {
                                            popUpTo(Screen.Home.route) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } else {
                                        navController.navigate(Screen.OtherProfile.createRoute(authorId))
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun TrashedMemoryCard(memory: Memory, onRestore: () -> Unit, onDelete: () -> Unit, onProfileClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp.scaled()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp.scaled())) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onProfileClick() }
                    .padding(bottom = 12.dp.scaled())
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp.scaled())
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    val avatar = memory.userAvatar
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
                    text = memory.userName ?: Memory.DEFAULT_USERNAME,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp.scaled(),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (memory.imageUris.isNotEmpty()) {
                AsyncImage(
                    model = memory.imageUris.first().toImageModel(),
                    contentDescription = stringResource(R.string.trash_thumbnail_desc),
                    modifier = Modifier.fillMaxWidth().height(140.dp.scaled()).clip(RoundedCornerShape(12.dp.scaled())),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp.scaled()))
            }
            Text(text = memory.title, fontSize = 18.sp.scaled(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(6.dp.scaled()))
            Text(text = memory.content, fontSize = 14.sp.scaled(), color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            Spacer(modifier = Modifier.height(20.dp.scaled()))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onRestore,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(10.dp.scaled())
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore, 
                        contentDescription = stringResource(R.string.trash_restore), 
                        modifier = Modifier.size(18.dp.scaled())
                    )
                    Spacer(modifier = Modifier.width(6.dp.scaled()))
                    Text(stringResource(R.string.trash_restore), fontSize = 14.sp.scaled(), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp.scaled()))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp.scaled())
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever, 
                        contentDescription = stringResource(R.string.delete), 
                        tint = Color.White,
                        modifier = Modifier.size(18.dp.scaled())
                    )
                }
            }
        }
    }
}