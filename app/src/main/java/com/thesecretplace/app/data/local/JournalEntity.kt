package com.thesecretplace.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntity(
    @PrimaryKey val id: String,
    val date: Long,
    val meditationTitle: String,
    val prompt: String,
    val text: String
)
