package com.naufal.griefy.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.naufal.griefy.domain.util.Resource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.naufal.griefy.R
import com.naufal.griefy.ui.navigation.Screen
import androidx.compose.ui.platform.LocalContext

import com.naufal.griefy.util.scaled
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.widthIn

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentLangCode by viewModel.currentLanguageCode.collectAsState()
    val showLogoutDialog = remember { mutableStateOf(false) }
    val showDeleteAccountDialog = remember { mutableStateOf(false) }
    val showDeleteLoading = remember { mutableStateOf(false) }
    val deleteErrorMessage = remember { mutableStateOf<String?>(null) }
    val horizontalPadding = getAdaptiveHorizontalPadding()

    LaunchedEffect(key1 = true) {
        viewModel.deleteAccountResult.collect { result ->
            when (result) {
                is Resource.Loading -> {
                    showDeleteLoading.value = true
                    deleteErrorMessage.value = null
                }
                is Resource.Success -> {
                    showDeleteLoading.value = false
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
                is Resource.Error -> {
                    showDeleteLoading.value = false
                    deleteErrorMessage.value = result.message ?: context.getString(R.string.settings_delete_account_error)
                }
            }
        }
    }

    val currentLanguageName = if (currentLangCode == "in" || currentLangCode == "id") {
        stringResource(R.string.settings_language_indonesian)
    } else {
        stringResource(R.string.settings_language_english)
    }

    val showLanguageDialog = remember { mutableStateOf(false) }

    if (showLanguageDialog.value) {
        Dialog(
            onDismissRequest = { showLanguageDialog.value = false },
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
                        text = stringResource(R.string.settings_select_language_title),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(12.dp.scaled()))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.changeLanguage("en")
                                showLanguageDialog.value = false
                            }
                            .padding(vertical = 6.dp.scaled()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentLangCode != "in" && currentLangCode != "id"),
                            onClick = {
                                viewModel.changeLanguage("en")
                                showLanguageDialog.value = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp.scaled()))
                        Text(
                            text = stringResource(R.string.settings_language_english),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp.scaled()
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.changeLanguage("in")
                                showLanguageDialog.value = false
                            }
                            .padding(vertical = 6.dp.scaled()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentLangCode == "in" || currentLangCode == "id"),
                            onClick = {
                                viewModel.changeLanguage("in")
                                showLanguageDialog.value = false
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                                unselectedColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp.scaled()))
                        Text(
                            text = stringResource(R.string.settings_language_indonesian),
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp.scaled()
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp.scaled()))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showLanguageDialog.value = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                fontSize = 16.sp.scaled()
                            )
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
                    title = {
                        Text(
                            stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp.scaled(),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = stringResource(R.string.settings_back_description),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
                    .padding(horizontal = horizontalPadding)
                    .verticalScroll(rememberScrollState())
            ) {

                SettingsCategoryTitle(stringResource(R.string.settings_pref_display))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp.scaled()),
                    shape = RoundedCornerShape(16.dp.scaled()),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Language,
                            title = stringResource(R.string.settings_change_language),
                            subtitle = currentLanguageName,
                            onClick = { showLanguageDialog.value = true }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp.scaled()))
                        SettingsSwitchItem(
                            icon = Icons.Default.DarkMode,
                            title = stringResource(R.string.settings_dark_mode),
                            isChecked = isDarkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp.scaled()))

                SettingsCategoryTitle(stringResource(R.string.settings_pref_memory))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp.scaled()),
                    shape = RoundedCornerShape(16.dp.scaled()),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.Default.Notifications,
                            title = stringResource(R.string.settings_reminders_title),
                            subtitle = stringResource(R.string.settings_reminders_subtitle),
                            onClick = { navController.navigate(Screen.Reminders.route) }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp.scaled()))
                        SettingsItem(
                            icon = Icons.Default.Delete,
                            title = stringResource(R.string.settings_trash_title),
                            subtitle = stringResource(R.string.settings_trash_subtitle),
                            onClick = { navController.navigate(Screen.Trash.route) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp.scaled()))

                SettingsCategoryTitle(stringResource(R.string.settings_pref_account))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp.scaled()),
                    shape = RoundedCornerShape(16.dp.scaled()),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column {
                        SettingsItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = stringResource(R.string.settings_logout),
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = {
                                showLogoutDialog.value = true
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp.scaled()))
                        SettingsItem(
                            icon = Icons.Default.Delete,
                            title = stringResource(R.string.settings_delete_account),
                            titleColor = MaterialTheme.colorScheme.error,
                            onClick = {
                                showDeleteAccountDialog.value = true
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(100.dp.scaled()))
            }
        }
    }

    if (showLogoutDialog.value) {
        Dialog(
            onDismissRequest = { showLogoutDialog.value = false },
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
                        text = stringResource(R.string.dialog_logout_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(8.dp.scaled()))
                    Text(
                        text = stringResource(R.string.dialog_logout_text),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(16.dp.scaled()))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showLogoutDialog.value = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.cancel), fontSize = 16.sp.scaled())
                        }
                        Spacer(modifier = Modifier.width(8.dp.scaled()))
                        TextButton(
                            onClick = {
                                showLogoutDialog.value = false
                                viewModel.logout {
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(0)
                                    }
                                }
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.settings_logout), fontSize = 16.sp.scaled(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteAccountDialog.value) {
        Dialog(
            onDismissRequest = { showDeleteAccountDialog.value = false },
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
                        text = stringResource(R.string.dialog_delete_account_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(8.dp.scaled()))
                    Text(
                        text = stringResource(R.string.dialog_delete_account_text),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp.scaled()
                    )
                    Spacer(modifier = Modifier.height(16.dp.scaled()))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { showDeleteAccountDialog.value = false },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.cancel), fontSize = 16.sp.scaled())
                        }
                        Spacer(modifier = Modifier.width(8.dp.scaled()))
                        TextButton(
                            onClick = {
                                showDeleteAccountDialog.value = false
                                viewModel.deleteAccount()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(stringResource(R.string.settings_delete_account), fontSize = 16.sp.scaled(), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteLoading.value) {
        AlertDialog(
            onDismissRequest = { },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(text = stringResource(R.string.settings_deleting_account), fontSize = 20.sp.scaled(), fontWeight = FontWeight.Bold)
            },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp.scaled()),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            },
            confirmButton = { }
        )
    }

    if (deleteErrorMessage.value != null) {
        AlertDialog(
            onDismissRequest = { deleteErrorMessage.value = null },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(text = stringResource(R.string.settings_delete_account_error), fontSize = 20.sp.scaled(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            },
            text = {
                Text(text = deleteErrorMessage.value ?: "", fontSize = 16.sp.scaled(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                TextButton(
                    onClick = { deleteErrorMessage.value = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.ok), fontSize = 16.sp.scaled())
                }
            }
        )
    }
}

@Composable
fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontSize = 12.sp.scaled(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 8.dp.scaled(), end = 8.dp.scaled(), top = 8.dp.scaled(), bottom = 4.dp.scaled())
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onBackground,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp.scaled(), vertical = 14.dp.scaled()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = title, 
            tint = if (titleColor == MaterialTheme.colorScheme.error) titleColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(16.dp.scaled()))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title, 
                fontSize = 16.sp.scaled(),
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp.scaled()))
                Text(
                    text = subtitle, 
                    fontSize = 13.sp.scaled(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight, 
            contentDescription = stringResource(R.string.settings_detail_description), 
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp.scaled(), vertical = 10.dp.scaled()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp.scaled()))
        Text(
            text = title, 
            fontSize = 16.sp.scaled(),
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isChecked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}