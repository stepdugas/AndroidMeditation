package com.thesecretplace.app.util

import android.content.Context
import android.content.Intent

object ShareUtil {

    fun shareApp(context: Context) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "The Secret Place Meditation App")
            putExtra(
                Intent.EXTRA_TEXT,
                "I've been using The Secret Place for meditation — it's a beautiful app for finding peace and stillness. Check it out!"
            )
        }
        context.startActivity(Intent.createChooser(intent, "Share The Secret Place"))
    }

    fun shareCompletion(context: Context, meditationTitle: String, streakCount: Int) {
        val streakText = if (streakCount > 0) " ($streakCount day streak 🔥)" else ""
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "The Secret Place")
            putExtra(
                Intent.EXTRA_TEXT,
                "Just completed \"$meditationTitle\" on The Secret Place$streakText ✨"
            )
        }
        context.startActivity(Intent.createChooser(intent, "Share"))
    }
}
