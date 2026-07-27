package com.example.echojournal.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echojournal.ui.JournalViewModel
import com.example.echojournal.ui.components.FluidBackground
import com.example.echojournal.ui.components.GlassCard
import com.example.echojournal.ui.theme.MoodColors
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun JournalDetailScreen(
    date: String,
    viewModel: JournalViewModel,
    onBack: () -> Unit,
    onAddEntry: () -> Unit
) {
    val entries by viewModel.getEntriesForDate(date).collectAsState(initial = emptyList())
    val isPlaying by viewModel.isPlaying.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isAutoColor by viewModel.isAutoColorEnabled.collectAsState()
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val isMusicEnabled by viewModel.isMusicEnabled.collectAsState()
    val musicProvider by viewModel.musicProvider.collectAsState()
    
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    var inputText by remember { mutableStateOf("") }

    // Dynamic background color based on mood
    val dominantMood = entries.filter { !it.isAiResponse }.groupBy { it.mood }.maxByOrNull { it.value.size }?.key
    val (targetPrimary, targetSecondary) = if (isAutoColor && dominantMood != null) {
        MoodColors.getColorsForMood(dominantMood)
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
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = date,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f).animateContentSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(entries) { entry ->
                        val isAi = entry.isAiResponse
                        val (primary, _) = MoodColors.getColorsForMood(entry.mood)
                        
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = if (isAi) Alignment.CenterStart else Alignment.CenterEnd
                        ) {
                            val isLiquid = selectedTheme == "Liquid Glass"
                            
                            // Clean technical prefixes from AI response
                            val cleanedText = if (isAi) {
                                entry.transcription
                                    .replace("Mood:", "", ignoreCase = true)
                                    .replace("Summary:", "", ignoreCase = true)
                                    .replace("Теги:", "", ignoreCase = true)
                                    .replace("Суммаризация:", "", ignoreCase = true)
                                    .trim()
                                    .removePrefix("|").trim()
                            } else {
                                entry.transcription
                            }

                            GlassCard(
                                modifier = Modifier.widthIn(max = 280.dp),
                                shape = if (isAi) 
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                                else 
                                    RoundedCornerShape(topStart = 24.dp, topEnd = 4.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
                                borderColor = if (isAi) primary.copy(alpha = if (isLiquid) 0.6f else 0.4f) else Color.White.copy(alpha = 0.15f),
                                isLiquid = isLiquid
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = cleanedText,
                                        color = if (isAi) Color.White else MoodColors.TextPrimary,
                                        fontSize = 15.sp,
                                        lineHeight = 20.sp
                                    )

                                    if (!isAi && entry.tags.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            entry.tags.split(",").forEach { tag ->
                                                Surface(
                                                    color = primary.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                ) {
                                                    Text(
                                                        tag.trim(),
                                                        color = primary,
                                                        fontSize = 10.sp,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (entry.audioPath != null) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.1f))
                                                .clickable { 
                                                    if (isPlaying) viewModel.stopAudio() 
                                                    else viewModel.playAudio(entry.audioPath) 
                                                }
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                        ) {
                                            Icon(
                                                if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                contentDescription = "Play",
                                                tint = primary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Прослушать", color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                    
                                    if (isAi && isMusicEnabled) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text("Музыка настроения:", color = primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "Открыть в $musicProvider",
                                                color = primary,
                                                fontSize = 11.sp,
                                                modifier = Modifier
                                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                                    .clickable { 
                                                        val query = if (dominantMood != null) "playlist+for+$dominantMood+mood" else "relaxing+music"
                                                        val url = when(musicProvider) {
                                                            "Spotify" -> "https://open.spotify.com/search/$query"
                                                            "Яндекс Музыка" -> "https://music.yandex.ru/search?text=$query"
                                                            else -> "https://music.apple.com/search?term=$query"
                                                        }
                                                        context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                                                    }
                                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    
                                    Text(
                                        text = timeFormat.format(Date(entry.timestamp)),
                                        color = Color.White.copy(alpha = 0.3f),
                                        fontSize = 9.sp,
                                        modifier = Modifier.align(if (isAi) Alignment.Start else Alignment.End).padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Chat Input Bar
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Black.copy(alpha = 0.3f),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding()
                            .imePadding(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Дополни мысль...", color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        if (inputText.isBlank()) {
                            IconButton(
                                onClick = onAddEntry,
                                modifier = Modifier.background(Color.White, CircleShape)
                            ) {
                                Icon(Icons.Default.Mic, "Mic", tint = Color.Black)
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    viewModel.sendTextMessage(inputText, date)
                                    inputText = ""
                                },
                                modifier = Modifier.background(Color.White, CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = Color.Black)
                            }
                        }
                    }
                }
            }
        }
    }
}
