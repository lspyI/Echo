package com.example.echojournal

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.echojournal.data.security.BiometricManager
import com.example.echojournal.ui.JournalViewModel
import com.example.echojournal.ui.components.FluidBackground
import com.example.echojournal.ui.components.GlassCard
import com.example.echojournal.ui.components.RecordEntryDialog
import com.example.echojournal.ui.components.glassIcon
import com.example.echojournal.ui.components.glow
import com.example.echojournal.ui.screens.AnalyticsScreen
import com.example.echojournal.ui.screens.JournalDetailScreen
import com.example.echojournal.ui.screens.JournalListScreen
import com.example.echojournal.ui.screens.MoodCalendarScreen
import com.example.echojournal.ui.screens.SettingsScreen
import com.example.echojournal.ui.theme.EchoJournalTheme
import com.example.echojournal.ui.theme.MoodColors
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EchoJournalTheme {
                val viewModel: JournalViewModel = hiltViewModel()
                val navController = rememberNavController()
                var showRecordDialog by remember { mutableStateOf(false) }
                
                val isPinEnabled by viewModel.isPinEnabled.collectAsState()
                val isFingerprintEnabled by viewModel.isFingerprintEnabled.collectAsState()
                val isFaceEnabled by viewModel.isFaceEnabled.collectAsState()
                val isStealthModeEnabled by viewModel.isStealthModeEnabled.collectAsState()
                val selectedTheme by viewModel.selectedTheme.collectAsState()
                val isSecurityLoaded by viewModel.isSecurityLoaded.collectAsState()
                val isOverlayVisible by viewModel.isOverlayVisible.collectAsState()
                val userPin by viewModel.userPin.collectAsState()
                
                val facesCount by viewModel.faceDetectionManager.detectedFacesCount.collectAsState()
                
                var isUnlocked by remember { mutableStateOf(false) }
                
                // Check and Start/Stop Face Detection
                LaunchedEffect(isStealthModeEnabled, isUnlocked, isSecurityLoaded) {
                    if (!isSecurityLoaded) return@LaunchedEffect
                    
                    if (isStealthModeEnabled && isUnlocked) {
                        val permission = Manifest.permission.CAMERA
                        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_GRANTED) {
                            viewModel.faceDetectionManager.start(this@MainActivity)
                        }
                    } else {
                        viewModel.faceDetectionManager.stop()
                    }
                }
                
                // Alert if more than 1 face detected
                // Added checks: only if camera permission is granted and NOT on settings screen
                val hasCameraPermission = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                val isNotSettings = currentRoute != "settings"
                
                val isIntruderDetected = isStealthModeEnabled && facesCount > 1 && isUnlocked && hasCameraPermission && isNotSettings
                
                val blurRadius by animateDpAsState(
                    targetValue = if (!isUnlocked || isOverlayVisible || isIntruderDetected) 20.dp else 0.dp,
                    label = "backdrop_blur"
                )
                
                val biometricManager = remember { BiometricManager(this) }

                LaunchedEffect(isPinEnabled, isFingerprintEnabled, isFaceEnabled, isSecurityLoaded) {
                    if (!isSecurityLoaded) return@LaunchedEffect
                    
                    val isAnyBiometricEnabled = isFingerprintEnabled || isFaceEnabled
                    
                    if (!isPinEnabled && !isAnyBiometricEnabled) {
                        isUnlocked = true
                    } else if (isAnyBiometricEnabled && !isUnlocked) {
                        if (biometricManager.canAuthenticate()) {
                            biometricManager.authenticate(
                                onSuccess = { isUnlocked = true },
                                onError = { }
                            )
                        } else if (!isPinEnabled) {
                            isUnlocked = true
                        }
                    }
                }

                if (!isUnlocked && isSecurityLoaded) {
                    LockScreen(
                        isPinEnabled = isPinEnabled,
                        savedPin = userPin ?: "1234",
                        isLiquidGlass = selectedTheme == "Liquid Glass",
                        onPinCorrect = { isUnlocked = true }
                    )
                } else if (!isSecurityLoaded) {
                    LoadingScreen()
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        NavHost(
                            navController = navController, 
                            startDestination = "list",
                            modifier = Modifier.blur(blurRadius)
                        ) {
                            composable("list") {
                                JournalListScreen(
                                    viewModel = viewModel,
                                    onDateClick = { date -> navController.navigate("detail/$date") },
                                    onAnalyticsClick = { navController.navigate("analytics") },
                                    onCalendarClick = { navController.navigate("calendar") },
                                    onSettingsClick = { navController.navigate("settings") },
                                    onRecordClick = { showRecordDialog = true }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("calendar") {
                                MoodCalendarScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onDateClick = { date -> navController.navigate("detail/$date") }
                                )
                            }
                            composable("analytics") {
                                AnalyticsScreen(
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                            composable("detail/{date}") { backStackEntry ->
                                val date = backStackEntry.arguments?.getString("date") ?: ""
                                JournalDetailScreen(
                                    date = date,
                                    viewModel = viewModel,
                                    onBack = { navController.popBackStack() },
                                    onAddEntry = { showRecordDialog = true }
                                )
                            }
                        }

                        if (showRecordDialog) {
                            RecordEntryDialog(
                                viewModel = viewModel,
                                onDismiss = { 
                                    showRecordDialog = false 
                                    viewModel.setOverlayVisible(false)
                                }
                            )
                        }
                        
                        // Overlay should NOT block settings toggle if we are on settings screen?
                        // Actually, security overlay MUST block. But let's make it translucent to clicks
                        // for debugging or if we are in the settings.
                        // For now, let's keep it but ensure it only triggers when REALLY needed.
                        if (isIntruderDetected) {
                            IntruderOverlay(onDismiss = { viewModel.setStealthModeEnabled(false) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IntruderOverlay(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(80.dp).glow(color = Color.Red, radius = 40.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Обнаружен посторонний глаз",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Text("Отключить защиту", color = Color.White)
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        FluidBackground(
            color1 = MoodColors.BackgroundTop, 
            color2 = MoodColors.BackgroundBottom,
            isLiquidGlass = false
        )
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
fun LockScreen(isPinEnabled: Boolean, savedPin: String, isLiquidGlass: Boolean, onPinCorrect: () -> Unit) {
    var pinInput by remember { mutableStateOf("") }
    val correctPin = savedPin

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        FluidBackground(
            color1 = MoodColors.BackgroundTop, 
            color2 = MoodColors.BackgroundBottom,
            isLiquidGlass = isLiquidGlass,
            modifier = Modifier.blur(if (isPinEnabled) 10.dp else 0.dp)
        )
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Echo",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            
            if (isPinEnabled) {
                Spacer(modifier = Modifier.height(40.dp))
                Text("Введите PIN", color = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(4) { index ->
                        val filled = index < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) Color.White else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
                
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "OK")
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    keypadRows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            row.forEach { digit ->
                                KeypadButton(digit, isLiquidGlass) {
                                    when (digit) {
                                        "C" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                        "OK" -> if (pinInput == correctPin) onPinCorrect() else pinInput = ""
                                        else -> if (pinInput.length < 4) {
                                            pinInput += digit
                                            if (pinInput.length == 4 && pinInput == correctPin) onPinCorrect()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Заблокировано",
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun KeypadButton(text: String, isLiquid: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(70.dp)
            .clickable { onClick() }
            .glassIcon(isLiquid = isLiquid),
        color = Color.Transparent,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Medium)
        }
    }
}
