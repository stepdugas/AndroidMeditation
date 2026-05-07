package com.thesecretplace.app.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val healthConnectClient: HealthConnectClient? by lazy {
        try {
            if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE) {
                HealthConnectClient.getOrCreate(context)
            } else null
        } catch (_: Exception) { null }
    }

    val permissions = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    suspend fun logMindfulSession(durationMs: Long) {
        val client = healthConnectClient ?: return
        if (durationMs <= 0) return

        try {
            val endTime = Instant.now()
            val startTime = endTime.minusMillis(durationMs)
            val record = ExerciseSessionRecord(
                startTime = startTime,
                startZoneOffset = null,
                endTime = endTime,
                endZoneOffset = null,
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_YOGA,
                title = "The Secret Place Meditation"
            )
            client.insertRecords(listOf(record))
            val minutes = (durationMs / 60_000).toInt()
            println("✅ Mindful session logged to Health Connect: $minutes min")
        } catch (e: Exception) {
            println("❌ Health Connect save failed: ${e.message}")
        }
    }
}
