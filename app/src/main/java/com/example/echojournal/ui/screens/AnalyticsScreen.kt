package com.example.echojournal.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.echojournal.ui.JournalViewModel
import com.example.echojournal.ui.components.FluidBackground
import com.example.echojournal.ui.components.GlassCard
import com.example.echojournal.ui.theme.MoodColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    val entries by viewModel.entries.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isAutoColor by viewModel.isAutoColorEnabled.collectAsState()
    val weeklyInsight by viewModel.weeklyInsight.collectAsState()
    
    // Dynamic background
    val latestMood = entries.maxByOrNull { it.timestamp }?.mood
    val (targetPrimary, targetSecondary) = if (isAutoColor && latestMood != null) {
        MoodColors.getColorsForMood(latestMood)
    } else {
        MoodColors.BackgroundTop to MoodColors.BackgroundBottom
    }

    LaunchedEffect(Unit) {
        viewModel.generateWeeklyReport()
    }
    val moodValues = entries.take(10).reversed().map { entry ->
        when(entry.mood.lowercase()) {
            "happy" -> 3f
            "neutral" -> 2f
            "stressed" -> 1f
            "sad" -> 0f
            else -> 2f
        }
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
                    title = { Text("Карта состояний", color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth().height(240.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Динамика настроения", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            MoodChart(moodValues)
                        }
                    }
                }

                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("ИИ Аналитика недели", color = MoodColors.HappyPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = weeklyInsight ?: "ИИ анализирует вашу неделю...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MoodChart(values: List<Float>) {
    if (values.isEmpty()) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text("Недостаточно данных", color = Color.Gray)
        }
        return
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val spacing = width / (values.size.coerceAtLeast(2) - 1)
        
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = index * spacing
            val y = height - (value / 3f * height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            
            drawCircle(
                color = Color.White,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
