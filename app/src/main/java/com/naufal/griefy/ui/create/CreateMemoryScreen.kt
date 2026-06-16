package com.naufal.griefy.ui.create

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMemoryScreen(
    navController: NavController,
    viewModel: CreateMemoryViewModel = hiltViewModel()
) {
  
    var contentText by remember { mutableStateOf("") }
    var isPublic by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kenangan Baru") },
                navigationIcon = {
                    Button(onClick = { navController.navigateUp() }, colors = ButtonDefaults.textButtonColors()) {
                        Text("Batal")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (contentText.isNotBlank()) {
                              
                                viewModel.saveMemory(
                                    content = contentText,
                                    isPublic = isPublic,
                                    onSaveSuccess = {
                                        navController.navigateUp() // Kembali ke Home kalau sukses
                                    }
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

            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Apa yang ingin kamu kenang hari ini?...") },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant, // Biar kotaknya tidak kaku (menyatu)
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = if (isPublic) "Status: Publik 🌐" else "Status: Privat 🔒")
                Switch(
                    checked = isPublic,
                    onCheckedChange = { isPublic = it }
                )
            }

            Text(
                text = "Catatan: Fitur Upload Foto dan Lagu akan segera hadir!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}