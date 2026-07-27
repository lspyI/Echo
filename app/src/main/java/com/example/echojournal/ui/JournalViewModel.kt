package com.example.echojournal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.echojournal.data.ai.FaceDetectionManager
import com.example.echojournal.data.audio.AudioPlayerManager
import com.example.echojournal.data.audio.AudioRecorder
import com.example.echojournal.data.local.SecurityPreferences
import com.example.echojournal.data.local.dao.JournalDao
import com.example.echojournal.data.local.entities.JournalEntry
import com.example.echojournal.data.remote.GeminiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalDao: JournalDao,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayerManager,
    private val geminiClient: GeminiClient,
    private val securityPreferences: SecurityPreferences,
    val faceDetectionManager: FaceDetectionManager
) : ViewModel() {

    // Security states
    val isPinEnabled = securityPreferences.isPinEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isFingerprintEnabled = securityPreferences.isFingerprintEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isFaceEnabled = securityPreferences.isFaceEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val userPin = securityPreferences.userPin.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val selectedTheme = securityPreferences.selectedTheme.stateIn(viewModelScope, SharingStarted.Eagerly, "Standard")
    val isAutoColorEnabled = securityPreferences.isAutoColorEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)
    val aiPersonality = securityPreferences.aiPersonality.stateIn(viewModelScope, SharingStarted.Eagerly, "Психолог")
    val isMusicEnabled = securityPreferences.isMusicEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val musicProvider = securityPreferences.musicProvider.stateIn(viewModelScope, SharingStarted.Eagerly, "Spotify")
    val isNotificationsEnabled = securityPreferences.isNotificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val notificationTime = securityPreferences.notificationTime.stateIn(viewModelScope, SharingStarted.Eagerly, "21:00")
    val isStealthModeEnabled = securityPreferences.isStealthModeEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val isSecurityLoaded = securityPreferences.isLoaded.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setTheme(theme: String) = viewModelScope.launch { securityPreferences.setTheme(theme) }
    fun setAutoColorEnabled(enabled: Boolean) = viewModelScope.launch { securityPreferences.setAutoColorEnabled(enabled) }
    fun setAiPersonality(personality: String) {
        viewModelScope.launch {
            securityPreferences.setAiPersonality(personality)
            geminiClient.updatePersonality(personality)
        }
    }
    fun setMusicEnabled(enabled: Boolean) = viewModelScope.launch { securityPreferences.setMusicEnabled(enabled) }
    fun setMusicProvider(provider: String) = viewModelScope.launch { securityPreferences.setMusicProvider(provider) }
    fun setNotificationsEnabled(enabled: Boolean) = viewModelScope.launch { securityPreferences.setNotificationsEnabled(enabled) }
    fun setNotificationTime(time: String) = viewModelScope.launch { securityPreferences.setNotificationTime(time) }
    fun setStealthModeEnabled(enabled: Boolean) = viewModelScope.launch { securityPreferences.setStealthModeEnabled(enabled) }
    fun setPinEnabled(enabled: Boolean) = viewModelScope.launch { securityPreferences.setPinEnabled(enabled) }
    fun setFingerprintEnabled(enabled: Boolean) = viewModelScope.launch { securityPreferences.setFingerprintEnabled(enabled) }
    fun setFaceEnabled(enabled: Boolean) = viewModelScope.launch { securityPreferences.setFaceEnabled(enabled) }
    fun setUserPin(pin: String) = viewModelScope.launch { securityPreferences.setUserPin(pin) }

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isOverlayVisible = MutableStateFlow(false)
    val isOverlayVisible: StateFlow<Boolean> = _isOverlayVisible.asStateFlow()

    fun setOverlayVisible(visible: Boolean) {
        _isOverlayVisible.value = visible
    }

    private var currentAudioFile: File? = null

    val isPlaying: StateFlow<Boolean> = audioPlayer.isPlaying

    val uniqueDates: StateFlow<List<String>> = journalDao.getAllUniqueDates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val entries: StateFlow<List<JournalEntry>> = journalDao.getAllEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getEntriesForDate(date: String): Flow<List<JournalEntry>> = journalDao.getEntriesByDate(date)

    fun startRecording(cacheDir: File) {
        val file = File(cacheDir, "recording_${System.currentTimeMillis()}.m4a")
        currentAudioFile = file
        audioRecorder.start(file)
        _isRecording.value = true
    }

    fun stopRecordingAndAnalyze(transcript: String) {
        viewModelScope.launch {
            audioRecorder.stop()
            _isRecording.value = false
            
            val (mood, tags, summary) = geminiClient.analyzeMoodAndSummarize(transcript)
            
            val userEntry = JournalEntry(
                transcription = transcript,
                audioPath = currentAudioFile?.absolutePath,
                mood = mood,
                summary = summary,
                tags = tags
            )
            journalDao.insertEntry(userEntry)
            
            // Auto AI Response for Therapy
            val aiResponse = geminiClient.sendMessage(transcript)
            journalDao.insertEntry(
                JournalEntry(
                    transcription = aiResponse,
                    audioPath = null,
                    mood = mood, // Keep the day mood
                    isAiResponse = true,
                    dateTag = userEntry.dateTag,
                    tags = tags
                )
            )
        }
    }

    fun sendTextMessage(text: String, dateTag: String) {
        viewModelScope.launch {
            val (mood, tags, summary) = geminiClient.analyzeMoodAndSummarize(text)
            val userEntry = JournalEntry(
                transcription = text,
                audioPath = null,
                mood = mood,
                summary = summary,
                dateTag = dateTag,
                tags = tags
            )
            journalDao.insertEntry(userEntry)

            val aiResponse = geminiClient.sendMessage(text)
            journalDao.insertEntry(
                JournalEntry(
                    transcription = aiResponse,
                    audioPath = null,
                    mood = mood,
                    isAiResponse = true,
                    dateTag = dateTag,
                    tags = tags
                )
            )
        }
    }

    private val _weeklyInsight = MutableStateFlow<String?>(null)
    val weeklyInsight: StateFlow<String?> = _weeklyInsight

    fun generateWeeklyReport() {
        viewModelScope.launch {
            val weekEntries = entries.value.take(20).map { it.transcription }
            if (weekEntries.isNotEmpty()) {
                _weeklyInsight.value = geminiClient.generateWeeklyInsight(weekEntries)
            }
        }
    }

    fun playAudio(path: String) {
        val file = File(path)
        if (file.exists()) {
            audioPlayer.play(file)
        }
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    fun deleteEntry(entry: JournalEntry) {
        viewModelScope.launch {
            journalDao.deleteEntry(entry)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
