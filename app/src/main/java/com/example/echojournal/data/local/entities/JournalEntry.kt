package com.example.echojournal.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transcription: String,
    val audioPath: String?,
    val mood: String, 
    val timestamp: Long = System.currentTimeMillis(),
    val dateTag: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(timestamp)),
    val summary: String? = null,
    val isAiResponse: Boolean = false,
    val tags: String = "" // Stored as comma-separated values or JSON
)
