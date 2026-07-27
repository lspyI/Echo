package com.example.echojournal.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.echojournal.data.local.dao.JournalDao
import com.example.echojournal.data.local.entities.JournalEntry

@Database(entities = [JournalEntry::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun journalDao(): JournalDao
}
