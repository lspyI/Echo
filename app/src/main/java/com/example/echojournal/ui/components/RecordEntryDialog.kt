package com.example.echojournal.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.echojournal.data.audio.SpeechRecognizerManager
import com.example.echojournal.ui.JournalViewModel
import com.example.echojournal.ui.components.glassIcon
import com.example.echojournal.ui.theme.MoodColors

@Composable
fun RecordEntryDialog(
    viewModel: JournalViewModel,
    onDismiss: () -> Unit
) {
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val isLiquid = selectedTheme == "Liquid Glass"
    
    var transcript by remember { mutableStateOf("") }
    val isRecording by viewModel.isRecording.collectAsState()
    val context = LocalContext.current

    // Speech Recognizer
    val speechRecognizer = remember {
        SpeechRecognizerManager(context) { partialResult ->
            transcript = partialResult
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    // Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.startRecording(context.cacheDir)
                speechRecognizer.startListening()
            }
        }
    )

    Dialog(onDismissRequest = { 
        viewModel.setOverlayVisible(false)
        onDismiss() 
    }) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            isLiquid = isLiquid
        ) {
            Column(
                modifier = Modifier
                    .padding(28.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Новая мысль",
                        color = MoodColors.TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(
                        onClick = onDismiss
                    ) {
                        Icon(
                            Icons.Default.Close, 
                            contentDescription = "Закрыть", 
                            tint = MoodColors.TextPrimary,
                            modifier = Modifier.glassIcon(isLiquid = isLiquid)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TextField(
                    value = transcript,
                    onValueChange = { transcript = it },
                    placeholder = { Text("О чем ты думаешь?", color = MoodColors.TextSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .glassmorphism(shape = RoundedCornerShape(16.dp), isLiquid = isLiquid),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = MoodColors.TextPrimary,
                        unfocusedTextColor = MoodColors.TextPrimary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                AudioWaveform(
                    isRecording = isRecording,
                    color = if (isRecording) Color(0xFFEF5350) else MoodColors.TextPrimary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Record Button
                    Surface(
                        modifier = Modifier
                            .size(if (isRecording) 90.dp else 70.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (isRecording) {
                                    speechRecognizer.stopListening()
                                    viewModel.stopRecordingAndAnalyze(transcript)
                                    onDismiss()
                                } else {
                                    val permission = Manifest.permission.RECORD_AUDIO
                                    if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                        viewModel.startRecording(context.cacheDir)
                                        speechRecognizer.startListening()
                                    } else {
                                        permissionLauncher.launch(permission)
                                    }
                                }
                            }
                            .then(if (!isRecording) Modifier.glassmorphism(shape = CircleShape, isLiquid = isLiquid) else Modifier),
                        color = if (isRecording) Color(0xFFD32F2F) else Color.Transparent,
                        tonalElevation = 8.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record",
                                tint = Color.White,
                                modifier = Modifier.size(if (isRecording) 44.dp else 32.dp)
                            )
                        }
                    }

                    if (transcript.isNotBlank() && !isRecording) {
                        Spacer(modifier = Modifier.width(24.dp))

                        // Send Text Button
                        Surface(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .clickable {
                                    speechRecognizer.stopListening()
                                    viewModel.stopRecordingAndAnalyze(transcript)
                                    onDismiss()
                                }
                                .glassmorphism(shape = CircleShape, isLiquid = isLiquid),
                            color = Color.Transparent,
                            tonalElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
