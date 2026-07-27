package com.example.echojournal.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "security_settings")

@Singleton
class SecurityPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
    private val IS_FINGERPRINT_ENABLED = booleanPreferencesKey("is_fingerprint_enabled")
    private val IS_FACE_ENABLED = booleanPreferencesKey("is_face_enabled")
    private val USER_PIN = stringPreferencesKey("user_pin")
    private val SELECTED_THEME = stringPreferencesKey("selected_theme")
    private val IS_AUTO_COLOR_ENABLED = booleanPreferencesKey("is_auto_color_enabled")
    private val AI_PERSONALITY = stringPreferencesKey("ai_personality")
    private val IS_MUSIC_ENABLED = booleanPreferencesKey("is_music_enabled")
    private val MUSIC_PROVIDER = stringPreferencesKey("music_provider")
    private val NOTIFICATION_TIME = stringPreferencesKey("notification_time")
    private val IS_NOTIFICATIONS_ENABLED = booleanPreferencesKey("is_notifications_enabled")
    private val IS_STEALTH_MODE_ENABLED = booleanPreferencesKey("is_stealth_mode_enabled")

    val isPinEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_PIN_ENABLED] ?: false }
    val isFingerprintEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_FINGERPRINT_ENABLED] ?: false }
    val isFaceEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_FACE_ENABLED] ?: false }
    val userPin: Flow<String?> = context.dataStore.data.map { it[USER_PIN] }
    val selectedTheme: Flow<String> = context.dataStore.data.map { it[SELECTED_THEME] ?: "Standard" }
    val isAutoColorEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_AUTO_COLOR_ENABLED] ?: true }
    val aiPersonality: Flow<String> = context.dataStore.data.map { it[AI_PERSONALITY] ?: "Психолог" }
    val isMusicEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_MUSIC_ENABLED] ?: false }
    val musicProvider: Flow<String> = context.dataStore.data.map { it[MUSIC_PROVIDER] ?: "Spotify" }
    val notificationTime: Flow<String> = context.dataStore.data.map { it[NOTIFICATION_TIME] ?: "21:00" }
    val isNotificationsEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_NOTIFICATIONS_ENABLED] ?: false }
    val isStealthModeEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_STEALTH_MODE_ENABLED] ?: false }
    
    // Emissions start as soon as DataStore is ready to read from disk
    val isLoaded: Flow<Boolean> = context.dataStore.data.map { true }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[SELECTED_THEME] = theme }
    }

    suspend fun setAutoColorEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_AUTO_COLOR_ENABLED] = enabled }
    }

    suspend fun setAiPersonality(personality: String) {
        context.dataStore.edit { it[AI_PERSONALITY] = personality }
    }

    suspend fun setMusicEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_MUSIC_ENABLED] = enabled }
    }

    suspend fun setMusicProvider(provider: String) {
        context.dataStore.edit { it[MUSIC_PROVIDER] = provider }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setNotificationTime(time: String) {
        context.dataStore.edit { it[NOTIFICATION_TIME] = time }
    }

    suspend fun setStealthModeEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_STEALTH_MODE_ENABLED] = enabled }
    }

    suspend fun setPinEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_PIN_ENABLED] = enabled }
    }

    suspend fun setFingerprintEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_FINGERPRINT_ENABLED] = enabled }
    }

    suspend fun setFaceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[IS_FACE_ENABLED] = enabled }
    }

    suspend fun setUserPin(pin: String) {
        context.dataStore.edit { it[USER_PIN] = pin }
    }
}
