@file:OptIn(ExperimentalMindfulnessSessionApi::class)

package com.thesecretplace.app.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.feature.ExperimentalMindfulnessSessionApi
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.MindfulnessSessionRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.time.ZoneOffset
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
        HealthPermission.getWritePermission(MindfulnessSessionRecord::class)
    )

    suspend fun logMindfulSession(durationMs: Long) {
        val client = healthConnectClient ?: return
        if (durationMs <= 0) return

        try {
            val endTime = Instant.now()
            val startTime = endTime.minusMillis(durationMs)
            val zoneOffset = ZoneOffset.systemDefault().rules.getOffset(endTime)
            val record = MindfulnessSessionRecord(
                startTime = startTime,
                startZoneOffset = zoneOffset,
                endTime = endTime,
                endZoneOffset = zoneOffset,
                metadata = Metadata.activelyRecorded(Device(type = Device.TYPE_PHONE)),
                mindfulnessSessionType = MindfulnessSessionRecord.MINDFULNESS_SESSION_TYPE_MEDITATION,
                title = "The Secret Place Meditation"
            )
            client.insertRecords(listOf(record))
            val minutes = (durationMs / 60_000).toInt()
            println("✅ Mindfulness session logged to Health Connect: $minutes min")
        } catch (e: Exception) {
            println("❌ Health Connect save failed: ${e.message}")
        }
    }
}
