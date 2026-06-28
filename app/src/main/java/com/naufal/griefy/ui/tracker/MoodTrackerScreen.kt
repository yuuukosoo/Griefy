package com.naufal.griefy.ui.tracker
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naufal.griefy.domain.model.Mood
import com.naufal.griefy.util.adaptiveWidth
import com.naufal.griefy.util.getAdaptiveHorizontalPadding
import com.naufal.griefy.util.scaled
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.graphics.luminance
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
val Mood.icon: ImageVector
    get() = when (this) {
        Mood.POSITIVE -> Icons.Outlined.SentimentSatisfied
        Mood.NEUTRAL -> Icons.Outlined.SentimentNeutral
        Mood.NEGATIVE -> Icons.Outlined.SentimentDissatisfied
    }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodTrackerScreen(
    viewModel: MoodTrackerViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val horizontalPadding = getAdaptiveHorizontalPadding()
    if (state.showMoodSelector && state.selectedDate != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissMoodSelector() },
            title = {
                Text(
                    text = "How are you feeling?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp.scaled()
                )
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Mood.entries.forEach { mood ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { viewModel.onMoodSelected(mood) }
                                .padding(16.dp.scaled())
                        ) {
                            Icon(
                                imageVector = mood.icon, 
                                contentDescription = mood.stringValue,
                                modifier = Modifier.size(40.dp.scaled()),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp.scaled()))
                            Text(text = mood.stringValue, fontSize = 12.sp.scaled())
                        }
                    }
                }
            },
            confirmButton = {
                val dateString = state.selectedDate?.toString()
                val existingMood = state.moodsForMonth.find { it.dateString == dateString }
                if (existingMood != null) {
                    TextButton(onClick = { viewModel.onMoodDeleted() }) {
                        Text("Remove", color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onDismissMoodSelector() }) {
                    Text("Cancel")
                }
            }
        )
    }
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
    val textColor = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .navigationBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .adaptiveWidth()
                .padding(start = horizontalPadding, end = horizontalPadding, top = 16.dp.scaled())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp.scaled()),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.onPreviousMonth() }) {
                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Month", tint = textColor)
                }
                val monthText = state.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                Text(
                    text = "$monthText ${state.currentMonth.year}",
                    fontSize = 20.sp.scaled(),
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                IconButton(onClick = { viewModel.onNextMonth() }) {
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Month", tint = textColor)
                }
            }
            val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp.scaled(),
                        color = textColor.copy(alpha = 0.8f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp.scaled()))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) 
                    .verticalScroll(rememberScrollState())
            ) {
                for (row in state.calendarGrid) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        for (date in row) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp.scaled())
                            ) {
                                if (date != null) {
                                    val dateString = date.toString()
                                    val moodForDay = state.moodsForMonth.find { it.dateString == dateString }
                                    val moodEnum = Mood.fromString(moodForDay?.moodValue)
                                    val isToday = date == LocalDate.now()
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    moodEnum != null -> MaterialTheme.colorScheme.primaryContainer
                                                    isToday -> MaterialTheme.colorScheme.secondaryContainer
                                                    else -> textColor.copy(alpha = 0.15f)
                                                }
                                            )
                                            .clickable { viewModel.onDateClicked(date) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (moodEnum != null) {
                                            Icon(
                                                imageVector = moodEnum.icon,
                                                contentDescription = moodEnum.stringValue,
                                                modifier = Modifier.size(24.dp.scaled()),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        } else {
                                            Text(
                                                text = date.dayOfMonth.toString(),
                                                fontSize = 16.sp.scaled(),
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = textColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp.scaled())) 
            }
        }
    }
}
