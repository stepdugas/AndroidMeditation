package com.thesecretplace.app.model

data class Meditation(
    val id: String,
    val title: String,
    val duration: String,
    val description: String,
    val imageName: String,
    val audioFileName: String,
    val category: MeditationCategory,
    val secondaryCategory: MeditationCategory? = null,
    val isNew: Boolean = false,
    val remoteAudioURL: String? = null
) {
    // Returns true if this meditation belongs to the given category (primary or secondary)
    fun belongsTo(cat: MeditationCategory): Boolean {
        return category == cat || secondaryCategory == cat
    }

    // Duration in seconds for stats tracking (approximate)
    val durationSeconds: Int
        get() {
            val parts = duration.split(" ")
            val value = parts.firstOrNull()?.toIntOrNull() ?: 0
            return value * 60
        }
}
