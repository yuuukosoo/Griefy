package com.naufal.griefy.ui.settings

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.naufal.griefy.R
import com.naufal.griefy.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

    var isDarkMode by remember { mutableStateOf(false) }

    val currentLocale = AppCompatDelegate.getApplicationLocales().get(0)
    val currentLangCode = currentLocale?.language ?: "en"
    val currentLanguageName = if (currentLangCode == "in" || currentLangCode == "id") {
        stringResource(R.string.settings_language_indonesian)
    } else {
        stringResource(R.string.settings_language_english)
    }

    var showLanguageDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, context.getString(R.string.settings_permission_granted), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, context.getString(R.string.settings_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    fun setTestReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + (10 * 1000)

        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            Toast.makeText(context, context.getString(R.string.settings_alarm_set), Toast.LENGTH_LONG).show()
        } catch (_: SecurityException) {
            Toast.makeText(context, context.getString(R.string.settings_alarm_error), Toast.LENGTH_LONG).show()
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = stringResource(R.string.settings_select_language_title)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags("en")
                                )
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentLangCode != "in" && currentLangCode != "id"),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags("en")
                                )
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.settings_language_english))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags("in")
                                )
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (currentLangCode == "in" || currentLangCode == "id"),
                            onClick = {
                                AppCompatDelegate.setApplicationLocales(
                                    LocaleListCompat.forLanguageTags("in")
                                )
                                showLanguageDialog = false
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(R.string.settings_language_indonesian))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = stringResource(R.string.settings_back_description)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            SettingsCategoryTitle(stringResource(R.string.settings_pref_display))
            SettingsItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.settings_change_language),
                subtitle = currentLanguageName,
                onClick = { showLanguageDialog = true }
            )
            SettingsSwitchItem(
                icon = Icons.Default.DarkMode,
                title = stringResource(R.string.settings_dark_mode),
                isChecked = isDarkMode,
                onCheckedChange = { isDarkMode = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsCategoryTitle(stringResource(R.string.settings_pref_memory))
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.settings_reminders_title),
                subtitle = stringResource(R.string.settings_reminders_subtitle),
                onClick = { navController.navigate(Screen.Reminders.route) }
            )
            SettingsItem(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.settings_trash_title),
                subtitle = stringResource(R.string.settings_trash_subtitle),
                onClick = { navController.navigate(Screen.Trash.route) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            SettingsCategoryTitle(stringResource(R.string.settings_pref_account))
            SettingsItem(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                title = stringResource(R.string.settings_logout),
                titleColor = MaterialTheme.colorScheme.error,
                onClick = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = titleColor)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = titleColor)
            if (subtitle != null) {
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
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
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}