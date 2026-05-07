package com.thesecretplace.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {
    @Query("SELECT * FROM journal_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: JournalEntity)

    @Delete
    suspend fun delete(entry: JournalEntity)

    @Query("SELECT DISTINCT meditationTitle FROM journal_entries ORDER BY meditationTitle ASC")
    fun getMeditationTitles(): Flow<List<String>>
}
