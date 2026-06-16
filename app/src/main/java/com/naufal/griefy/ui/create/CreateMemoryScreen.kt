package com.naufal.griefy.ui.create

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.naufal.griefy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMemoryScreen(
    navController: NavController,
    viewModel: CreateMemoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current


    var titleText by remember { mutableStateOf("") } // <-- TAMBAHAN: State Judul
    var tagsText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val multiplePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImageUris = uris
            uris.forEach { uri ->
                val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(uri, flag)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kenangan Baru") },
                navigationIcon = {
                    Button(onClick = { navController.navigateUp() }, colors = ButtonDefaults.textButtonColors()) { Text("Batal") }
                },
                actions = {
                    Button(
                        onClick = {

                            if (titleText.isNotBlank() || contentText.isNotBlank() || selectedImageUris.isNotEmpty()) {
                                val uriStrings = selectedImageUris.map { it.toString() }
                                val tagsList = tagsText.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                viewModel.saveMemory(
                                    title = titleText, // <-- Mengirim Judul ke ViewModel
                                    content = contentText,
                                    tags = tagsList,
                                    isPublic = isPublic,
                                    imageUris = uriStrings,
                                    onSaveSuccess = { navController.navigateUp() }
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Simpan")
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

            Button(
                onClick = {
                    multiplePhotoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text("🖼️ Pilih Foto Kenangan (Maks 5)")
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedImageUris.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedImageUris) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = "Foto Pilihan",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            OutlinedTextField(
                value = titleText,
                onValueChange = { titleText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Judul Kenangan...") },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = tagsText,
                onValueChange = { tagsText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Kategori / Tag (pisahkan dengan koma)...") },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(8.dp))


            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                placeholder = { Text("Apa yang ingin kamu kenang hari ini?...") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = if (isPublic) "Status: Publik 🌐" else "Status: Privat 🔒")
                Switch(checked = isPublic, onCheckedChange = { isPublic = it })
            }

            Button(
                onClick = { navController.navigate(Screen.SearchPublic.route) },
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text("🎵 Cari Lagu di Spotify")
            }
        }
    }
}