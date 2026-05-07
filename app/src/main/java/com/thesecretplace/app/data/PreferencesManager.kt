package com.thesecretplace.app.data

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val prefs: SharedPreferences
) {
    // Onboarding
    var hasSeenWelcome: Boolean
        get() = prefs.getBoolean("hasSeenWelcome", false)
        set(value) = prefs.edit().putBoolean("hasSeenWelcome", value).apply()

    var userName: String
        get() = prefs.getString("userName", "") ?: ""
        set(value) = prefs.edit().putString("userName", value).apply()

    // Theme
    var isDarkMode: Boolean
        get() = prefs.getBoolean("isDarkMode", true)
        set(value) = prefs.edit().putBoolean("isDarkMode", value).apply()

    // Favorites — comma-separated IDs
    var favoriteMeditationIDs: String
        get() = prefs.getString("favoriteMeditationIDs", "") ?: ""
        set(value) = prefs.edit().putString("favoriteMeditationIDs", value).apply()

    fun isFavorite(id: String): Boolean {
        return favoriteMeditationIDs.split(",").filter { it.isNotEmpty() }.contains(id)
    }

    fun toggleFavorite(id: String) {
        val ids = favoriteMeditationIDs.split(",").filter { it.isNotEmpty() }.toMutableSet()
        if (ids.contains(id)) ids.remove(id) else ids.add(id)
        favoriteMeditationIDs = ids.joinToString(",")
    }

    // Streak
    var streakCount: Int
        get() = prefs.getInt("streakCount", 0)
        set(value) = prefs.edit().putInt("streakCount", value).apply()

    var lastMeditationDate: String
        get() = prefs.getString("lastMeditationDate", "") ?: ""
        set(value) = prefs.edit().putString("lastMeditationDate", value).apply()

    // Stats
    var totalSessionsCompleted: Int
        get() = prefs.getInt("totalSessionsCompleted", 0)
        set(value) = prefs.edit().putInt("totalSessionsCompleted", value).apply()

    var totalMinutesMeditated: Int
        get() = prefs.getInt("totalMinutesMeditated", 0)
        set(value) = prefs.edit().putInt("totalMinutesMeditated", value).apply()

    // Intention
    var selectedIntention: String
        get() = prefs.getString("selectedIntention", "") ?: ""
        set(value) = prefs.edit().putString("selectedIntention", value).apply()

    var showIntentionScreen: Boolean
        get() = prefs.getBoolean("showIntentionScreen", true)
        set(value) = prefs.edit().putBoolean("showIntentionScreen", value).apply()

    // Reminders
    var dailyReminderHour: Int
        get() = prefs.getInt("dailyReminderHour", 8)
        set(value) = prefs.edit().putInt("dailyReminderHour", value).apply()

    var dailyReminderMinute: Int
        get() = prefs.getInt("dailyReminderMinute", 0)
        set(value) = prefs.edit().putInt("dailyReminderMinute", value).apply()

    // Admin
    var adminUnlocked: Boolean
        get() = prefs.getBoolean("adminUnlocked", false)
        set(value) = prefs.edit().putBoolean("adminUnlocked", value).apply()

    // Premium status (cached locally — verified via BillingClient)
    var isPremium: Boolean
        get() = prefs.getBoolean("isPremium", false)
        set(value) = prefs.edit().putBoolean("isPremium", value).apply()

    // Health Connect
    var healthConnectEnabled: Boolean
        get() = prefs.getBoolean("healthConnectEnabled", true)
        set(value) = prefs.edit().putBoolean("healthConnectEnabled", value).apply()

    // Navigation
    var selectedTab: String
        get() = prefs.getString("selectedTab", "Home") ?: "Home"
        set(value) = prefs.edit().putString("selectedTab", value).apply()

    // Anthropic cache helpers
    fun getCachedString(key: String): String? {
        val value = prefs.getString(key, null)
        return if (value.isNullOrEmpty()) null else value
    }

    fun setCachedString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getInt(key: String, default: Int = 0): Int = prefs.getInt(key, default)
    fun setInt(key: String, value: Int) = prefs.edit().putInt(key, value).apply()

    // Journal entries stored as JSON
    var journalEntriesJson: String
        get() = prefs.getString("journalEntriesJSON", "") ?: ""
        set(value) = prefs.edit().putString("journalEntriesJSON", value).apply()
}
