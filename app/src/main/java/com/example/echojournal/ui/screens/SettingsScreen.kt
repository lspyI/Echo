package com.example.echojournal.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.echojournal.ui.JournalViewModel
import com.example.echojournal.ui.components.FluidBackground
import com.example.echojournal.ui.components.GlassCard
import com.example.echojournal.ui.components.PinSetupDialog
import com.example.echojournal.ui.components.glow
import com.example.echojournal.ui.theme.MoodColors

enum class SettingsMenu {
    MAIN, SECURITY, APPEARANCE, PERSONALITY, MUSIC, NOTIFICATIONS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: JournalViewModel,
    onBack: () -> Unit
) {
    var currentMenu by remember { mutableStateOf(SettingsMenu.MAIN) }
    val context = LocalContext.current

    val isPinEnabled by viewModel.isPinEnabled.collectAsState()
    val isFingerprintEnabled by viewModel.isFingerprintEnabled.collectAsState()
    val isFaceEnabled by viewModel.isFaceEnabled.collectAsState()
    val isAutoColorEnabled by viewModel.isAutoColorEnabled.collectAsState()
    val isStealthModeEnabled by viewModel.isStealthModeEnabled.collectAsState()
    val selectedTheme by viewModel.selectedTheme.collectAsState()
    val aiPersonality by viewModel.aiPersonality.collectAsState()
    val isMusicEnabled by viewModel.isMusicEnabled.collectAsState()
    val musicProvider by viewModel.musicProvider.collectAsState()
    val isNotificationsEnabled by viewModel.isNotificationsEnabled.collectAsState()
    val notificationTime by viewModel.notificationTime.collectAsState()
    
    var showPinSetup by remember { mutableStateOf(false) }

    // Logic for permission request when toggle is ON
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            viewModel.setStealthModeEnabled(false)
        }
    }

    // Effect to check permission whenever Stealth Mode is turned ON
    LaunchedEffect(isStealthModeEnabled) {
        if (isStealthModeEnabled) {
            val permission = Manifest.permission.CAMERA
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                cameraPermissionLauncher.launch(permission)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showPinSetup) {
            viewModel.setOverlayVisible(true)
            PinSetupDialog(
                isLiquid = selectedTheme == "Liquid Glass",
                onDismiss = { 
                    showPinSetup = false 
                    viewModel.setOverlayVisible(false)
                },
                onPinSet = { viewModel.setUserPin(it); viewModel.setPinEnabled(true) }
            )
        }
        
        FluidBackground(
            color1 = MoodColors.BackgroundTop,
            color2 = MoodColors.BackgroundBottom,
            isLiquidGlass = selectedTheme == "Liquid Glass"
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        val title = when(currentMenu) {
                            SettingsMenu.MAIN -> "Настройки"
                            SettingsMenu.SECURITY -> "Приватность"
                            SettingsMenu.APPEARANCE -> "Оформление"
                            SettingsMenu.PERSONALITY -> "Личность ИИ"
                            SettingsMenu.MUSIC -> "Музыка"
                            SettingsMenu.NOTIFICATIONS -> "Уведомления"
                        }
                        Text(text = title, color = Color.White, fontWeight = FontWeight.Bold) 
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (currentMenu != SettingsMenu.MAIN) currentMenu = SettingsMenu.MAIN
                            else onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            AnimatedContent(
                targetState = currentMenu,
                transitionSpec = {
                    if (initialState == SettingsMenu.MAIN) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }
                },
                label = "settings_nav"
            ) { menu ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (menu) {
                        SettingsMenu.MAIN -> {
                            item {
                                SettingsCategoryItem(
                                    title = "Приватность и безопасность",
                                    icon = Icons.Default.Shield,
                                    onClick = { currentMenu = SettingsMenu.SECURITY }
                                )
                            }
                            item {
                                SettingsCategoryItem(
                                    title = "Оформление",
                                    icon = Icons.Default.Palette,
                                    onClick = { currentMenu = SettingsMenu.APPEARANCE }
                                )
                            }
                            item {
                                SettingsCategoryItem(
                                    title = "Личность ИИ",
                                    icon = Icons.Default.SmartToy,
                                    onClick = { currentMenu = SettingsMenu.PERSONALITY }
                                )
                            }
                            item {
                                SettingsCategoryItem(
                                    title = "Музыка",
                                    icon = Icons.Default.MusicNote,
                                    onClick = { currentMenu = SettingsMenu.MUSIC }
                                )
                            }
                            item {
                                SettingsCategoryItem(
                                    title = "Уведомления",
                                    icon = Icons.Default.Notifications,
                                    onClick = { currentMenu = SettingsMenu.NOTIFICATIONS }
                                )
                            }
                        }
                        SettingsMenu.SECURITY -> {
                            item {
                                SecurityToggleItem(
                                    title = "Пароль (4 цифры)",
                                    icon = Icons.Default.Lock,
                                    checked = isPinEnabled,
                                    onCheckedChange = { 
                                        if (it) showPinSetup = true 
                                        else viewModel.setPinEnabled(false)
                                    }
                                )
                            }

                            item {
                                SecurityToggleItem(
                                    title = "Отпечаток пальца",
                                    icon = Icons.Default.Fingerprint,
                                    checked = isFingerprintEnabled,
                                    onCheckedChange = { viewModel.setFingerprintEnabled(it) }
                                )
                            }
                            
                            item {
                                SecurityToggleItem(
                                    title = "Распознавание лица",
                                    icon = Icons.Default.Face,
                                    checked = isFaceEnabled,
                                    onCheckedChange = { viewModel.setFaceEnabled(it) }
                                )
                            }
                            
                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                                SecurityToggleItem(
                                    title = "Режим Невидимка (ИИ)",
                                    icon = Icons.Default.PrivacyTip,
                                    checked = isStealthModeEnabled,
                                    onCheckedChange = { enabled ->
                                        viewModel.setStealthModeEnabled(enabled)
                                    }
                                )
                                Text(
                                    text = "Автоматически размывает экран, если в камеру заглядывает кто-то еще.",
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        SettingsMenu.APPEARANCE -> {
                            item {
                                ThemeSelectionItem(
                                    title = "Стандартная",
                                    selected = selectedTheme == "Standard",
                                    onClick = { viewModel.setTheme("Standard") }
                                )
                            }
                            item {
                                ThemeSelectionItem(
                                    title = "Liquid Glass",
                                    selected = selectedTheme == "Liquid Glass",
                                    onClick = { viewModel.setTheme("Liquid Glass") }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                SecurityToggleItem(
                                    title = "Авто-цвет (по настроению)",
                                    icon = Icons.Default.AutoAwesome,
                                    checked = isAutoColorEnabled,
                                    onCheckedChange = { viewModel.setAutoColorEnabled(it) }
                                )
                            }
                        }
                        SettingsMenu.PERSONALITY -> {
                            listOf("Психолог", "Друг", "Любящая девушка", "Философ", "Строгий коуч", "Дзен-мастер").forEach { p ->
                                item {
                                    ThemeSelectionItem(
                                        title = p,
                                        selected = aiPersonality == p,
                                        onClick = { viewModel.setAiPersonality(p) }
                                    )
                                }
                            }
                        }
                        SettingsMenu.MUSIC -> {
                            item {
                                SecurityToggleItem(
                                    title = "Музыкальное настроение",
                                    icon = Icons.Default.Headset,
                                    checked = isMusicEnabled,
                                    onCheckedChange = { viewModel.setMusicEnabled(it) }
                                )
                            }
                            if (isMusicEnabled) {
                                item { Spacer(modifier = Modifier.height(16.dp)) }
                                listOf("Spotify", "Яндекс Музыка", "Apple Music").forEach { provider ->
                                    item {
                                        ThemeSelectionItem(
                                            title = provider,
                                            selected = musicProvider == provider,
                                            onClick = { viewModel.setMusicProvider(provider) }
                                        )
                                    }
                                }
                            }
                        }
                        SettingsMenu.NOTIFICATIONS -> {
                            item {
                                SecurityToggleItem(
                                    title = "Ежедневные напоминания",
                                    icon = Icons.Default.Alarm,
                                    checked = isNotificationsEnabled,
                                    onCheckedChange = { viewModel.setNotificationsEnabled(it) }
                                )
                            }
                            if (isNotificationsEnabled) {
                                item {
                                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("Время уведомления", color = Color.White)
                                            Text(notificationTime, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
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
}

@Composable
fun ThemeSelectionItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        borderColor = if (selected) Color.White.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = Color.White, fontSize = 16.sp)
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = Color.White)
            )
        }
    }
}

@Composable
fun SettingsCategoryItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, 
                    null, 
                    tint = Color.White, 
                    modifier = Modifier.size(24.dp).glow(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = Color.White.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun SecurityToggleItem(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = { onCheckedChange(it) },
                role = Role.Switch
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon, 
                    null, 
                    tint = Color.White, 
                    modifier = Modifier.size(24.dp).glow(alpha = 0.1f)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(title, color = Color.White, fontSize = 16.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = null, // Handled by toggleable
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
        }
    }
}
