package com.example.echojournal.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echojournal.R
import com.example.echojournal.ui.JournalViewModel
import com.example.echojournal.ui.components.FluidBackground
import com.example.echojournal.ui.components.GlassCard
import com.example.echojournal.ui.components.glassIcon
import com.example.echojournal.ui.components.glow
import com.example.echojournal.ui.theme.MoodColors

@Composable
fun JournalListScreen(
    viewModel: JournalViewModel,
    onDateClick: (String) -> Unit,
    onAnalyticsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onRecordClick: () -> Unit
) {
    val dates by viewModel.uniqueDates.collectAsState()
    val allEntries by viewModel.entries.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isAutoColor by viewModel.isAutoColorEnabled.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var isListeningCommand by remember { mutableStateOf(false) }
    val speechRecognizer = remember {
        com.example.echojournal.data.audio.SpeechRecognizerManager(context) { command ->
            if (command.contains("настройки", true)) onSettingsClick()
            if (command.contains("график", true) || command.contains("анализ", true)) onAnalyticsClick()
            if (command.contains("календарь", true)) onCalendarClick()
            isListeningCommand = false
        }
    }

    // Dynamic background based on overall latest mood
    val latestMood = allEntries.maxByOrNull { it.timestamp }?.mood
    val (targetPrimary, targetSecondary) = if (isAutoColor && latestMood != null) {
        MoodColors.getColorsForMood(latestMood)
    } else {
        MoodColors.BackgroundTop to MoodColors.BackgroundBottom
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        FluidBackground(
            color1 = targetPrimary.copy(alpha = 0.4f).compositeOver(MoodColors.BackgroundTop),
            color2 = targetSecondary.copy(alpha = 0.2f).compositeOver(MoodColors.BackgroundBottom),
            isLiquidGlass = selectedTheme == "Liquid Glass"
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                val isLiquid = selectedTheme == "Liquid Glass"
                IconButton(
                    onClick = {
                        viewModel.setOverlayVisible(true)
                        onRecordClick()
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .glassIcon(isLiquid = isLiquid, backgroundColor = Color.White.copy(alpha = 0.1f))
                        .glow(color = Color.White, alpha = 0.15f, radius = 24.dp)
                ) {
                    Icon(
                        Icons.Default.Add, 
                        contentDescription = "Add Entry",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Echo",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = MoodColors.TextPrimary,
                        letterSpacing = (-1).sp
                    )
                    Row {
                        val isLiquid = selectedTheme == "Liquid Glass"
                        IconButton(
                            onClick = { 
                                isListeningCommand = true
                                speechRecognizer.startListening()
                            }
                        ) {
                            Icon(
                                Icons.Default.Mic, 
                                "Assistant", 
                                tint = if (isListeningCommand) Color.Red else Color.White,
                                modifier = Modifier
                                    .glassIcon(isLiquid = isLiquid)
                                    .glow(color = if (isListeningCommand) Color.Red else Color.White, alpha = 0.1f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onCalendarClick) {
                            Icon(
                                Icons.Default.CalendarMonth, 
                                "Calendar", 
                                tint = Color.White,
                                modifier = Modifier.glassIcon(isLiquid = isLiquid).glow(alpha = 0.1f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onAnalyticsClick) {
                            Icon(
                                Icons.Default.Analytics, 
                                "Analytics", 
                                tint = Color.White,
                                modifier = Modifier.glassIcon(isLiquid = isLiquid).glow(alpha = 0.1f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onSettingsClick) {
                            Icon(
                                Icons.Default.Settings, 
                                "Settings", 
                                tint = Color.White,
                                modifier = Modifier.glassIcon(isLiquid = isLiquid).glow(alpha = 0.1f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(dates, key = { it }) { date ->
                        val dayEntries = allEntries.filter { it.dateTag == date }
                        val latestEntry = dayEntries.maxByOrNull { it.timestamp } ?: return@items
                        val (primary, _) = MoodColors.getColorsForMood(latestEntry.mood)
                        val isLiquid = selectedTheme == "Liquid Glass"

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            GlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onDateClick(date) },
                                isLiquid = isLiquid
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Surface(
                                            color = primary.copy(alpha = 0.15f),
                                            shape = CircleShape
                                        ) {
                                            val moodDisplay = when (latestEntry.mood.lowercase()) {
                                                "happy" -> "Радость"
                                                "sad" -> "Грусть"
                                                "stressed" -> "Стресс"
                                                else -> "Нейтрально"
                                            }
                                            Text(
                                                text = moodDisplay,
                                                color = primary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                            )
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = date,
                                                color = MoodColors.TextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(
                                                onClick = { viewModel.deleteEntry(latestEntry) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete",
                                                    tint = Color.White.copy(alpha = 0.3f),
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    val displaySummary = latestEntry.summary ?: latestEntry.transcription

                                    Text(
                                        text = displaySummary,
                                        color = MoodColors.TextPrimary,
                                        fontSize = 17.sp,
                                        lineHeight = 24.sp,
                                        maxLines = 5,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    
                                    if (dayEntries.size > 1) {
                                        Text(
                                            text = "+ еще ${dayEntries.size - 1} мыслей",
                                            color = primary.copy(alpha = 0.6f),
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
