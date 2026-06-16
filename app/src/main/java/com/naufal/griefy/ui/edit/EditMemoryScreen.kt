package com.naufal.griefy.ui.edit

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
fun EditMemoryScreen(
    navController: NavController,
    viewModel: EditMemoryViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Kenangan") },
                navigationIcon = {
                    Button(onClick = { navController.navigateUp() }, colors = ButtonDefaults.textButtonColors()) {
                        Text("Batal")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (viewModel.contentText.isNotBlank()) {
                                viewModel.updateMemory(
                                    onUpdateSuccess = { navController.navigateUp() } // Kembali ke Detail setelah di-update
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Perbarui")
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
                value = viewModel.contentText,
                onValueChange = { viewModel.onContentChange(it) },
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = if (viewModel.isPublic) "Status: Publik 🌐" else "Status: Privat 🔒")
                Switch(
                    checked = viewModel.isPublic,
                    onCheckedChange = { viewModel.onPrivacyChange(it) }
                )
            }
        }
    }
}