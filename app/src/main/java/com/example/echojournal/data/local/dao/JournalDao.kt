package com.example.echojournal.data.local.dao

import androidx.room.*
import com.example.echojournal.data.local.entities.JournalEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)

    @Query("SELECT * FROM journal_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): JournalEntry?

    @Query("SELECT * FROM journal_entries WHERE dateTag = :date ORDER BY timestamp ASC")
    fun getEntriesByDate(date: String): Flow<List<JournalEntry>>

    @Query("SELECT DISTINCT dateTag FROM journal_entries ORDER BY timestamp DESC")
    fun getAllUniqueDates(): Flow<List<String>>
}
