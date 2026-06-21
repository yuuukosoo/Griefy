package com.naufal.griefy.ui.reminders

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.naufal.griefy.R
import com.naufal.griefy.domain.model.RemembranceDay
import com.naufal.griefy.util.adaptiveWidth
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.scaled
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    navController: NavController,
    viewModel: ReminderViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val remembranceDays by viewModel.remembranceDays.collectAsState()

    val alarmManager = remember { context.getSystemService(Context.ALARM_SERVICE) as AlarmManager }
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }

    var hasExactAlarmPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }
        )
    }

    var isBatteryOptimized by remember {
        mutableStateOf(!powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }
                isBatteryOptimized = !powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    val showDialog = remember { mutableStateOf(false) }
    val editingReminder = remember { mutableStateOf<RemembranceDay?>(null) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    val reminderToDelete = remember { mutableStateOf<RemembranceDay?>(null) }

    var titleText by remember { mutableStateOf("") }
    var descText by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var activePicker by remember { mutableStateOf("date") }

    val formatter = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("id", "ID")) }
    val dialogDateFormatter = remember { SimpleDateFormat("MMMM d, yyyy", Locale("id", "ID")) }
    val dialogTimeFormatter = remember { SimpleDateFormat("h:mm a", Locale.US) }

    fun openDialog(reminder: RemembranceDay? = null) {
        editingReminder.value = reminder
        if (reminder != null) {
            titleText = reminder.title
            descText = reminder.description
            selectedDateTime = reminder.dateTime
        } else {
            titleText = ""
            descText = ""
            selectedDateTime = System.currentTimeMillis() + 60000
        }
        activePicker = "date"
        showDialog.value = true
    }

    val horizontalPadding = getAdaptiveHorizontalPadding()

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
                            text = stringResource(R.string.reminder_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp.scaled(),
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .adaptiveWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (!hasExactAlarmPermission) {
                        ExactAlarmWarningCard(context)
                    } else if (isBatteryOptimized) {
                        BatteryOptimizationWarningCard(context)
                    }

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (remembranceDays.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp.scaled()),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.reminder_empty_title),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = horizontalPadding, end = horizontalPadding, top = 16.dp.scaled(), bottom = 100.dp.scaled()),
                                verticalArrangement = Arrangement.spacedBy(12.dp.scaled())
                            ) {
                                items(remembranceDays, key = { it.id }) { reminder ->
                                    ReminderCard(
                                        reminder = reminder,
                                        dateTimeString = formatter.format(Date(reminder.dateTime)),
                                        onEdit = { openDialog(reminder) },
                                        onDelete = {
                                            reminderToDelete.value = reminder
                                            showDeleteDialog.value = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { openDialog() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = horizontalPadding, bottom = 48.dp.scaled())
                        .size(56.dp.scaled())
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.nav_write_memory_desc),
                        tint = Color.White,
                        modifier = Modifier.size(28.dp.scaled())
                    )
                }
            }
        }
    }

        if (showDeleteDialog.value) {
            Dialog(
                onDismissRequest = {
                    showDeleteDialog.value = false
                    reminderToDelete.value = null
                },
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
                            text = stringResource(R.string.dialog_delete_reminder_title),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 20.sp.scaled()
                        )
                        Spacer(modifier = Modifier.height(8.dp.scaled()))
                        Text(
                            text = stringResource(R.string.dialog_delete_reminder_text),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp.scaled()
                        )
                        Spacer(modifier = Modifier.height(16.dp.scaled()))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    showDeleteDialog.value = false
                                    reminderToDelete.value = null
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(R.string.cancel), fontSize = 16.sp.scaled())
                            }
                            Spacer(modifier = Modifier.width(8.dp.scaled()))
                            TextButton(
                                onClick = {
                                    reminderToDelete.value?.let {
                                        viewModel.deleteReminder(it)
                                    }
                                    showDeleteDialog.value = false
                                    reminderToDelete.value = null
                                },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text(stringResource(R.string.delete), fontSize = 16.sp.scaled(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        if (showDialog.value) {
            var isTitleFocused by remember { mutableStateOf(false) }
            var isDescFocused by remember { mutableStateOf(false) }
            val keyboardController = LocalSoftwareKeyboardController.current
            val titleFocusRequester = remember { FocusRequester() }
            val descFocusRequester = remember { FocusRequester() }
            val dummyFocusRequester = remember { FocusRequester() }

            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                properties = DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                containerColor = MaterialTheme.colorScheme.surface,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (editingReminder.value != null) 
                                stringResource(R.string.reminder_dialog_edit) 
                            else 
                                stringResource(R.string.reminder_dialog_add),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp.scaled()
                        )
                        
                        val canSave = titleText.isNotBlank()
                        Box(
                            modifier = Modifier
                                .size(32.dp.scaled())
                                .clip(CircleShape)
                                .background(
                                    if (canSave) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                )
                                .clickable(enabled = canSave) {
                                    if (editingReminder.value != null) {
                                        viewModel.updateReminder(
                                            editingReminder.value!!.copy(
                                                title = titleText,
                                                description = descText,
                                                dateTime = selectedDateTime
                                            )
                                        )
                                    } else {
                                        viewModel.addReminder(
                                            title = titleText,
                                            description = descText,
                                            dateTime = selectedDateTime
                                        )
                                    }
                                    showDialog.value = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.save),
                                tint = if (canSave) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp.scaled())
                            )
                        }
                    }
                },
                text = {
                    val configuration = LocalConfiguration.current
                    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                    val calendar = remember { Calendar.getInstance() }.apply {
                        timeInMillis = selectedDateTime
                    }

                    val titleInput = @Composable {
                        val isTitleActive = isTitleFocused || activePicker == "title"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isTitleActive) 2.dp.scaled() else 1.dp.scaled(),
                                    color = if (isTitleActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp.scaled())
                                )
                                .background(
                                    color = if (isTitleActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.02f) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp.scaled())
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        titleFocusRequester.requestFocus()
                                    }
                                }
                                .padding(horizontal = 16.dp.scaled(), vertical = 14.dp.scaled())
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.reminder_name_label),
                                    fontSize = 12.sp.scaled(),
                                    color = if (isTitleActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp.scaled()))
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (titleText.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.reminder_title_placeholder),
                                            fontSize = 16.sp.scaled(),
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    }
                                    BasicTextField(
                                        value = titleText,
                                        onValueChange = { titleText = it },
                                        singleLine = true,
                                        textStyle = TextStyle(
                                            fontSize = 16.sp.scaled(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { 
                                            keyboardController?.hide()
                                            dummyFocusRequester.requestFocus()
                                        }),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(titleFocusRequester)
                                            .onFocusChanged { 
                                                isTitleFocused = it.isFocused 
                                                if (it.isFocused) activePicker = "title"
                                            }
                                    )
                                }
                            }
                        }
                    }

                    val descInput = @Composable {
                        val isDescActive = isDescFocused || activePicker == "desc"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isDescActive) 2.dp.scaled() else 1.dp.scaled(),
                                    color = if (isDescActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp.scaled())
                                )
                                .background(
                                    color = if (isDescActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.02f) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp.scaled())
                                )
                                .pointerInput(Unit) {
                                    detectTapGestures {
                                        descFocusRequester.requestFocus()
                                    }
                                }
                                .padding(horizontal = 16.dp.scaled(), vertical = 14.dp.scaled())
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.reminder_note_label),
                                    fontSize = 12.sp.scaled(),
                                    color = if (isDescActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(2.dp.scaled()))
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    if (descText.isEmpty()) {
                                        Text(
                                            text = stringResource(R.string.reminder_note_placeholder),
                                            fontSize = 16.sp.scaled(),
                                            fontWeight = FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                        )
                                    }
                                    BasicTextField(
                                        value = descText,
                                        onValueChange = { descText = it },
                                        maxLines = 3,
                                        textStyle = TextStyle(
                                            fontSize = 16.sp.scaled(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        ),
                                        cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = { 
                                            keyboardController?.hide()
                                            dummyFocusRequester.requestFocus()
                                        }),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .focusRequester(descFocusRequester)
                                            .onFocusChanged { 
                                                isDescFocused = it.isFocused 
                                                if (it.isFocused) activePicker = "desc"
                                            }
                                    )
                                }
                            }
                        }
                    }

                    val dateInput = @Composable {
                        val isDateActive = activePicker == "date"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isDateActive) 2.dp.scaled() else 1.dp.scaled(),
                                    color = if (isDateActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp.scaled())
                                )
                                .background(
                                    color = if (isDateActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.02f) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp.scaled())
                                )
                                .clickable {
                                    activePicker = "date"
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            calendar.set(Calendar.YEAR, year)
                                            calendar.set(Calendar.MONTH, month)
                                            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                            selectedDateTime = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                }
                                .padding(horizontal = 16.dp.scaled(), vertical = 14.dp.scaled())
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (isDateActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp.scaled())
                                )
                                Spacer(modifier = Modifier.width(16.dp.scaled()))
                                Column {
                                    Text(
                                        text = stringResource(R.string.reminder_date_btn),
                                        fontSize = 12.sp.scaled(),
                                        color = if (isDateActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp.scaled()))
                                    Text(
                                        text = dialogDateFormatter.format(Date(selectedDateTime)),
                                        fontSize = 16.sp.scaled(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    val timeInput = @Composable {
                        val isTimeActive = activePicker == "time"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isTimeActive) 2.dp.scaled() else 1.dp.scaled(),
                                    color = if (isTimeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(16.dp.scaled())
                                )
                                .background(
                                    color = if (isTimeActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.02f) else MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(16.dp.scaled())
                                )
                                .clickable {
                                    activePicker = "time"
                                    TimePickerDialog(
                                        context,
                                        { _, hourOfDay, minute ->
                                            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                                            calendar.set(Calendar.MINUTE, minute)
                                            calendar.set(Calendar.SECOND, 0)
                                            selectedDateTime = calendar.timeInMillis
                                        },
                                        calendar.get(Calendar.HOUR_OF_DAY),
                                        calendar.get(Calendar.MINUTE),
                                        false
                                    ).show()
                                }
                                .padding(horizontal = 16.dp.scaled(), vertical = 14.dp.scaled())
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = if (isTimeActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp.scaled())
                                )
                                Spacer(modifier = Modifier.width(16.dp.scaled()))
                                Column {
                                    Text(
                                        text = stringResource(R.string.reminder_time_btn),
                                        fontSize = 12.sp.scaled(),
                                        color = if (isTimeActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp.scaled()))
                                    Text(
                                        text = dialogTimeFormatter.format(Date(selectedDateTime)),
                                        fontSize = 16.sp.scaled(),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    dummyFocusRequester.requestFocus()
                                    keyboardController?.hide()
                                }
                            }
                    ) {
                        // Dummy focusable component
                        Box(
                            modifier = Modifier
                                .size(1.dp)
                                .focusRequester(dummyFocusRequester)
                                .focusable()
                        )

                        if (isLandscape) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp.scaled()),
                                horizontalArrangement = Arrangement.spacedBy(16.dp.scaled())
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(12.dp.scaled())
                                ) {
                                    titleInput()
                                    descInput()
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp.scaled())
                                ) {
                                    Text(
                                        text = stringResource(R.string.reminder_time_settings_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    dateInput()
                                    timeInput()
                                }
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp.scaled()),
                                verticalArrangement = Arrangement.spacedBy(16.dp.scaled())
                            ) {
                                titleInput()
                                descInput()
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp.scaled())
                                ) {
                                    Text(
                                        text = stringResource(R.string.reminder_time_settings_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp.scaled()))
                                    dateInput()
                                    Spacer(modifier = Modifier.height(12.dp.scaled()))
                                    timeInput()
                                }
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }

@Composable
fun ReminderCard(
    reminder: RemembranceDay,
    dateTimeString: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isPast = reminder.dateTime < System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp.scaled()),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp.scaled())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = reminder.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (reminder.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp.scaled()))
                        Text(
                            text = reminder.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.reminder_dialog_edit),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp.scaled()))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isPast) Icons.Default.NotificationsNone else Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp.scaled())
                )
                Spacer(modifier = Modifier.width(6.dp.scaled()))
                Text(
                    text = dateTimeString,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isPast) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                if (isPast) {
                    Spacer(modifier = Modifier.width(8.dp.scaled()))
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp.scaled()),
                        modifier = Modifier.padding(horizontal = 4.dp.scaled())
                    ) {
                        Text(
                            text = stringResource(R.string.reminder_passed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 6.dp.scaled(), vertical = 2.dp.scaled())
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExactAlarmWarningCard(context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = getAdaptiveHorizontalPadding(), vertical = 8.dp.scaled()),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFDF3E7)
        ),
        shape = RoundedCornerShape(12.dp.scaled()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp.scaled())
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(24.dp.scaled())
                )
                Spacer(modifier = Modifier.width(12.dp.scaled()))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_exact_alarm_warning_title),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF78350F),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp.scaled()))
                    Text(
                        text = stringResource(R.string.reminder_exact_alarm_warning_desc),
                        color = Color(0xFF92400E),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp.scaled()))
            Button(
                onClick = {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = "package:${context.packageName}".toUri()
                            }
                            context.startActivity(intent)
                        }
                    } catch (_: Exception) {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD97706),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp.scaled()),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.reminder_exact_alarm_warning_btn), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@SuppressLint("BatteryLife")
@Composable
fun BatteryOptimizationWarningCard(context: Context) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = getAdaptiveHorizontalPadding(), vertical = 8.dp.scaled()),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF0FDF4)
        ),
        shape = RoundedCornerShape(12.dp.scaled()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp.scaled())
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = Color(0xFF16A34A),
                    modifier = Modifier.size(24.dp.scaled())
                )
                Spacer(modifier = Modifier.width(12.dp.scaled()))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.reminder_battery_warning_title),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14532D),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(4.dp.scaled()))
                    Text(
                        text = stringResource(R.string.reminder_battery_warning_desc),
                        color = Color(0xFF166534),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp.scaled()))
            Button(
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = "package:${context.packageName}".toUri()
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        val intent = Intent(Settings.ACTION_SETTINGS)
                        context.startActivity(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF16A34A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp.scaled()),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(stringResource(R.string.reminder_battery_warning_btn), fontWeight = FontWeight.Bold)
            }
        }
    }
}
