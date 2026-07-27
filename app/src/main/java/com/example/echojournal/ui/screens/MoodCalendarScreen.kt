package com.example.echojournal.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodCalendarScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit,
    onDateClick: (String) -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isAutoColor by viewModel.isAutoColorEnabled.collectAsState()
    
    // Dynamic background
    val latestMood = entries.maxByOrNull { it.timestamp }?.mood
    val (targetPrimary, targetSecondary) = if (isAutoColor && latestMood != null) {
        MoodColors.getColorsForMood(latestMood)
    } else {
        MoodColors.BackgroundTop to MoodColors.BackgroundBottom
    }

    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthName = SimpleDateFormat("MMMM yyyy", Locale("ru")).format(calendar.time)

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
                    title = { Text(monthName, color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(20.dp)) {
                GlassCard(modifier = Modifier.fillMaxWidth().wrapContentHeight()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..daysInMonth).toList()) { day ->
                            val dateStr = SimpleDateFormat("yyyy-MM-", Locale.getDefault()).format(calendar.time) + String.format("%02d", day)
                            val dayEntries = entries.filter { it.dateTag == dateStr }
                            val dominantMood = dayEntries.groupBy { it.mood }.maxByOrNull { it.value.size }?.key
                            
                            val moodColor = dominantMood?.let { MoodColors.getColorsForMood(it).first } ?: Color.White.copy(alpha = 0.05f)

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .background(moodColor.copy(alpha = if (dominantMood != null) 0.8f else 0.1f))
                                    .clickable { onDateClick(dateStr) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.toString(),
                                    color = if (dominantMood != null) Color.Black else Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
