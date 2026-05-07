package com.thesecretplace.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thesecretplace.app.data.PreferencesManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = context.getSharedPreferences("the_secret_place_prefs", Context.MODE_PRIVATE)
            val hour = prefs.getInt("dailyReminderHour", -1)
            val minute = prefs.getInt("dailyReminderMinute", 0)
            if (hour >= 0) {
                ReminderScheduler(context).scheduleDailyReminder(hour, minute)
            }
        }
    }
}
