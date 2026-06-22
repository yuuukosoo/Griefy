package com.naufal.griefy.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Face
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naufal.griefy.R
import com.naufal.griefy.domain.model.UserProfile
import com.naufal.griefy.domain.util.Resource
import com.naufal.griefy.util.scaled
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.toImageModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherProfileScreen(
    navController: NavController,
    viewModel: OtherProfileViewModel = hiltViewModel()
) {
    val profileState = viewModel.profileState
    val displayName = (profileState as? Resource.Success)?.data?.displayName

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = displayName ?: stringResource(R.string.profile_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp.scaled()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.profile_desc_back)
                            )
                        }
                    },
                    modifier = Modifier.widthIn(max = 500.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 500.dp)
            ) {
                when (profileState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    is Resource.Success -> {
                        val profile = profileState.data
                        if (profile != null) {
                            OtherProfileContent(profile = profile, memoryCount = viewModel.memoryCount)
                        } else {
                            Text(
                                text = stringResource(R.string.profile_not_found),
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp.scaled()
                            )
                        }
                    }
                    is Resource.Error -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = profileState.message ?: stringResource(R.string.profile_load_failed),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 16.sp.scaled(),
                                modifier = Modifier.padding(bottom = 16.dp.scaled())
                            )
                            Button(onClick = { viewModel.loadUserProfile() }) {
                                Text(stringResource(R.string.profile_try_again), fontSize = 14.sp.scaled())
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtherProfileContent(profile: UserProfile, memoryCount: Int) {
    val profileImageModel = profile.avatarBase64?.toImageModel()
    val horizontalPadding = getAdaptiveHorizontalPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = horizontalPadding, vertical = 24.dp.scaled()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Box
        Box(
            modifier = Modifier.size(100.dp.scaled())
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp.scaled())
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (profileImageModel != null) {
                    AsyncImage(
                        model = profileImageModel,
                        contentDescription = stringResource(R.string.profile_desc_photo),
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("👤", fontSize = 48.sp.scaled())
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp.scaled()))

        if (profile.email == "deleted") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp.scaled()),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp.scaled()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.profile_deleted_account_msg),
                        fontSize = 16.sp.scaled(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = stringResource(R.string.profile_other_user_info),
                    fontSize = 18.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp.scaled()))

            OtherProfileInfoItem(
                icon = Icons.Default.Person,
                label = stringResource(R.string.profile_username_label),
                value = profile.displayName
            )
            Spacer(modifier = Modifier.height(12.dp.scaled()))
            OtherProfileInfoItem(
                icon = Icons.Default.Face,
                label = stringResource(R.string.profile_gender_label),
                value = com.naufal.griefy.util.ProfileUtils.getLocalizedGender(profile.gender)
            )
            Spacer(modifier = Modifier.height(12.dp.scaled()))
            OtherProfileInfoItem(
                icon = Icons.Default.Book,
                label = stringResource(R.string.profile_other_total_memories),
                value = stringResource(R.string.profile_memories_count, memoryCount)
            )
        }
    }
}

@Composable
private fun OtherProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp.scaled()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp.scaled()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp.scaled())
                    .clip(RoundedCornerShape(12.dp.scaled()))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp.scaled())
                )
            }
            Spacer(modifier = Modifier.width(16.dp.scaled()))
            Column {
                Text(
                    text = label,
                    fontSize = 12.sp.scaled(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    fontSize = 15.sp.scaled(),
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
