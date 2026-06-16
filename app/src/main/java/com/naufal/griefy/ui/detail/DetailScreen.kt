package com.naufal.griefy.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naufal.griefy.ui.navigation.Screen
import androidx.compose.material.icons.filled.Edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val memory by viewModel.memory.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Kenangan") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
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
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }

                    IconButton(
                        onClick = {
                            viewModel.moveToTrash(
                                onDeleteSuccess = { navController.navigateUp() } // Kembali ke Home setelah dihapus
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Hapus",
                            tint = MaterialTheme.colorScheme.error // Warnanya otomatis jadi merah
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        memory?.let { mem ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text(
                    text = if (mem.isPublic) "Status: Publik 🌐" else "Status: Privat 🔒",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mem.content,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Fitur Foto & Pemutar Lagu Spotify akan muncul di sini nanti.",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } ?: run {

            CircularProgressIndicator(modifier = Modifier.padding(paddingValues).padding(16.dp))
        }
    }
}