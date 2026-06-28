package com.naufal.griefy.ui.profile
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.*
import coil.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.naufal.griefy.R
import com.airbnb.lottie.compose.*
import com.naufal.griefy.util.toImageModel
import com.naufal.griefy.util.scaled
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEditing = uiState.isEditing
    val username = uiState.username
    val email = uiState.email
    val gender = uiState.gender
    val profileImageUriString = uiState.profileImageUriString
    val profileImageModel = profileImageUriString?.toImageModel()
    val context = LocalContext.current
    val isLoading = uiState.isLoading
    val isSaving = uiState.isSaving
    val errorMessage = uiState.errorMessage
    val saveSuccess = uiState.saveSuccess
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.logo_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            Toast.makeText(context, context.getString(R.string.profile_success_toast), Toast.LENGTH_SHORT).show()
            viewModel.clearSaveSuccess()
        }
    }
    val errorTextAuth = stringResource(R.string.profile_error_auth)
    val errorTextProcessImage = stringResource(R.string.profile_error_image)
    val errorTextSaveProfile = stringResource(R.string.profile_error_save)
    val errorTextLoadProfile = stringResource(R.string.profile_load_failed)
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            val displayMsg = when (msg) {
                "ERROR_UNAUTHENTICATED" -> errorTextAuth
                "ERROR_PROCESS_IMAGE_FAILED" -> errorTextProcessImage
                "ERROR_SAVE_PROFILE_FAILED" -> errorTextSaveProfile
                "ERROR_LOAD_PROFILE_FAILED" -> errorTextLoadProfile
                else -> msg
            }
            Toast.makeText(context, displayMsg, Toast.LENGTH_LONG).show()
            viewModel.clearErrorMessage()
        }
    }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onProfileImagePicked(uri.toString())
        }
    }
    val horizontalPadding = getAdaptiveHorizontalPadding()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(120.dp.scaled())
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 500.dp)
                ) {
                    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
                    val headerBgColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                    val headerTextColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = headerBgColor
                            )
                            .padding(start = horizontalPadding, end = horizontalPadding, top = 24.dp.scaled(), bottom = 24.dp.scaled()),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.profile_title),
                            fontSize = 24.sp.scaled(),
                            fontWeight = FontWeight.Bold,
                            color = headerTextColor
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp.scaled()))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = horizontalPadding, vertical = 16.dp.scaled()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                            if (isEditing) {
                                IconButton(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    modifier = Modifier
                                        .size(32.dp.scaled())
                                        .align(Alignment.BottomEnd)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                        .border(2.dp.scaled(), MaterialTheme.colorScheme.background, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.profile_desc_edit_photo),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(16.dp.scaled())
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp.scaled()))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.profile_personal_info),
                                fontSize = 18.sp.scaled(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            IconButton(onClick = { viewModel.setIsEditing(!isEditing) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.profile_desc_edit_profile),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp.scaled()))
                        ProfileInfoItem(
                            icon = Icons.Default.Person,
                            label = stringResource(R.string.profile_username_label),
                            value = username,
                            isEditing = isEditing,
                            onValueChange = { viewModel.onUsernameChange(it) }
                        )
                        Spacer(modifier = Modifier.height(12.dp.scaled()))
                        ProfileInfoItem(
                            icon = Icons.Default.Email,
                            label = stringResource(R.string.profile_email_label),
                            value = email,
                            isEditing = false,
                            onValueChange = { viewModel.onEmailChange(it) }
                        )
                        Spacer(modifier = Modifier.height(12.dp.scaled()))
                        val maleLabel = stringResource(R.string.profile_gender_male)
                        val femaleLabel = stringResource(R.string.profile_gender_female)
                        ProfileInfoItem(
                            icon = Icons.Default.Face,
                            label = stringResource(R.string.profile_gender_label),
                            value = com.naufal.griefy.util.ProfileUtils.getLocalizedGender(gender),
                            isEditing = isEditing,
                            onValueChange = { selectedLabel ->
                                val stableValue = when (selectedLabel) {
                                    maleLabel -> com.naufal.griefy.util.ProfileUtils.GENDER_MALE_KEY
                                    femaleLabel -> com.naufal.griefy.util.ProfileUtils.GENDER_FEMALE_KEY
                                    else -> selectedLabel
                                }
                                viewModel.onGenderChange(stableValue)
                            },
                            options = listOf(maleLabel, femaleLabel)
                        )
                        if (isEditing) {
                            Spacer(modifier = Modifier.height(24.dp.scaled()))
                            Button(
                                onClick = { viewModel.saveUserProfile() },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp.scaled()),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    text = stringResource(R.string.profile_confirm_button),
                                    fontSize = 16.sp.scaled(),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(vertical = 8.dp.scaled())
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(100.dp.scaled()))
                    }
                }
            }
        }
        if (isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) {}, 
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { progress },
                    modifier = Modifier.size(120.dp.scaled())
                )
            }
        }
    }
}
@Composable
private fun ProfileInfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    onValueChange: (String) -> Unit,
    options: List<String>? = null
) {
    var expanded by remember { mutableStateOf(false) }
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
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp.scaled())
                )
            }
            Spacer(modifier = Modifier.width(16.dp.scaled()))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 12.sp.scaled(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(2.dp.scaled()))
                if (isEditing) {
                    if (options != null) {
                        Box {
                            Text(
                                text = value,
                                fontSize = 16.sp.scaled(),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option, fontSize = 16.sp.scaled()) },
                                        onClick = {
                                            onValueChange(option)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    } else {
                        BasicTextField(
                            value = value,
                            onValueChange = onValueChange,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 16.sp.scaled(),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                } else {
                    Text(
                        text = value,
                        fontSize = 16.sp.scaled(),
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}