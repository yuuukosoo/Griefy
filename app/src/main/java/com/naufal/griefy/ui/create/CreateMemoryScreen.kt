package com.naufal.griefy.ui.create

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var titleText by remember { mutableStateOf("") }
    var contentText by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var tagsList by remember { mutableStateOf<List<String>>(emptyList()) }

    var showAddLabelDialog by remember { mutableStateOf(false) }
    var newLabelText by remember { mutableStateOf("") }

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
            // Header Row (Cozy minimal back button & title)
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
                        contentDescription = "Kembali",
                        tint = Color(0xFF5C524A),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Tulis Kenangan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E4640)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))


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
                if (selectedImageUris.isEmpty()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Pilih Foto",
                            tint = Color(0xFF8C8075),
                            modifier = Modifier.size(36.dp) // Larger plus icon for square box
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Pilih Foto Kenangan",
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
                        items(selectedImageUris) { uri ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f) // Square image item
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Foto Pilihan",
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
                                        .clickable { selectedImageUris = selectedImageUris - uri },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Hapus",
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
                value = titleText,
                onValueChange = { titleText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Judul kenangan...",
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
                value = contentText,
                onValueChange = { contentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                placeholder = {
                    Text(
                        text = "Tuliskan ceritamu di sini...",
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
            if (tagsList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tagsList) { tag ->
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
                                    contentDescription = "Hapus",
                                    tint = Color(0xFF8C8075),
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { tagsList = tagsList - tag }
                                )
                            }
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
                    Text(text = "Add Song", fontSize = 12.sp, color = Color(0xFF5C524A), fontWeight = FontWeight.Bold)
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
                    Text(text = "Add Label", fontSize = 12.sp, color = Color(0xFF5C524A), fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Minimal Privacy Toggle Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEDE8E0))
                    .padding(4.dp)
            ) {
                // Privat Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isPublic) Color(0xFF8C7D73) else Color.Transparent)
                        .clickable { isPublic = false },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Privat",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (!isPublic) Color.White else Color(0xFF5C524A)
                    )
                }

                // Public Tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPublic) Color(0xFF8C7D73) else Color.Transparent)
                        .clickable { isPublic = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Publik",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPublic) Color.White else Color(0xFF5C524A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (titleText.isNotBlank() || contentText.isNotBlank() || selectedImageUris.isNotEmpty()) {
                        val uriStrings = selectedImageUris.map { it.toString() }
                        viewModel.saveMemory(
                            title = titleText,
                            content = contentText,
                            tags = tagsList,
                            isPublic = isPublic,
                            imageUris = uriStrings,
                            onSaveSuccess = { navController.navigateUp() }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF75685F)), // Cozy warm clay
                enabled = titleText.isNotBlank() || contentText.isNotBlank() || selectedImageUris.isNotEmpty()
            ) {
                Text(
                    text = "Simpan Kenangan Indah",
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
            title = { Text("Tambah Label / Kategori", color = Color(0xFF4E4640)) },
            text = {
                OutlinedTextField(
                    value = newLabelText,
                    onValueChange = { newLabelText = it },
                    placeholder = { Text("Nama label...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLabelText.isNotBlank()) {
                            val cleanTag = newLabelText.trim()
                            if (!tagsList.contains(cleanTag)) {
                                tagsList = tagsList + cleanTag
                            }
                            newLabelText = ""
                            showAddLabelDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF75685F))
                ) {
                    Text("Tambah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLabelDialog = false }) {
                    Text("Batal", color = Color(0xFF8C7D73))
                }
            }
        )
    }
}