package com.naufal.griefy.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.naufal.griefy.R
import com.naufal.griefy.ui.navigation.FloatingNavigationDock
import com.naufal.griefy.ui.navigation.Screen

@Composable
fun ProfileScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAF7F2))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header Row (Left: Screen Title, Right: Settings Gear)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E4640)
                )

                IconButton(
                    onClick = { navController.navigate(Screen.Settings.route) },
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEDE8E0), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        tint = Color(0xFF5C524A)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDE8E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👤", fontSize = 48.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Khalish",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4E4640)
                )
                Text(
                    text = "khalish@example.com",
                    color = Color(0xFF8C8075),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.profile_statistics),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4E4640)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("24", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF75685F))
                                Text(stringResource(R.string.profile_total_memories), fontSize = 12.sp, color = Color(0xFF8C8075))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("5", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF75685F))
                                Text(stringResource(R.string.profile_public_albums), fontSize = 12.sp, color = Color(0xFF8C8075))
                            }
                        }
                    }
                }
            }
        }

        // Shared Custom Navigation Dock overlay
        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            FloatingNavigationDock(
                navController = navController,
                currentRoute = Screen.Profile.route
            )
        }
    }
}