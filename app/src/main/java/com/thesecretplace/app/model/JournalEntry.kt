package com.thesecretplace.app.model

import java.util.UUID

data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val date: Long = System.currentTimeMillis(),
    val meditationTitle: String,
    val prompt: String,
    val text: String
)
