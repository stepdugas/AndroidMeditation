package com.thesecretplace.app.ui.screens.selah

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.health.HealthConnectManager
import com.thesecretplace.app.service.AudioServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

enum class AmbientSound(val displayName: String, val fileName: String?) {
    NONE("None", null),
    RAIN("Rain", "ambient_rain"),
    OCEAN("Ocean", "ambient_ocean"),
    FOREST("Forest", "ambient_forest"),
    WIND("Wind", "ambient_wind"),
    FIRE("Fire", "ambient_fire")
}

@HiltViewModel
class SelahViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val audioConnection: AudioServiceConnection,
    private val healthConnect: HealthConnectManager
) : ViewModel() {

    init {
        audioConnection.connect()
    }

    private val _selectedMinutes = MutableStateFlow(10)
    val selectedMinutes: StateFlow<Int> = _selectedMinutes.asStateFlow()

    private val _selectedSound = MutableStateFlow(AmbientSound.RAIN)
    val selectedSound: StateFlow<AmbientSound> = _selectedSound.asStateFlow()

    private val _sessionActive = MutableStateFlow(false)
    val sessionActive: StateFlow<Boolean> = _sessionActive.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _timeRemaining = MutableStateFlow(600)
    val timeRemaining: StateFlow<Int> = _timeRemaining.asStateFlow()

    private val _showCompletion = MutableStateFlow(false)
    val showCompletion: StateFlow<Boolean> = _showCompletion.asStateFlow()

    private var timerJob: Job? = null

    fun setMinutes(min: Int) {
        _selectedMinutes.value = min
        _timeRemaining.value = min * 60
    }

    fun setSound(sound: AmbientSound) {
        _selectedSound.value = sound
    }

    fun startSession() {
        _timeRemaining.value = _selectedMinutes.value * 60
        _sessionActive.value = true
        _isPaused.value = false

        _selectedSound.value.fileName?.let { fileName ->
            audioConnection.playAudio(
                fileName = fileName,
                title = "${_selectedSound.value.displayName} Ambience",
                loop = true
            )
        }

        startTimer()
        println("✅ Silent timer started — ${_selectedMinutes.value} min, sound: ${_selectedSound.value.displayName}")
    }

    fun togglePause() {
        _isPaused.value = !_isPaused.value
        if (_isPaused.value) {
            timerJob?.cancel()
            audioConnection.pause()
        } else {
            audioConnection.resume()
            startTimer()
        }
    }

    fun stopSession() {
        timerJob?.cancel()
        _sessionActive.value = false
        _isPaused.value = false
        _timeRemaining.value = _selectedMinutes.value * 60
        audioConnection.stop()
    }

    private fun completeSession() {
        timerJob?.cancel()
        _sessionActive.value = false
        _isPaused.value = false
        audioConnection.stop()
        // Play bell chime
        audioConnection.playAudio(fileName = "bell_chime", title = "")

        // Update streak
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val todayStr = sdf.format(today.time)
        val lastDate = try { sdf.parse(prefs.lastMeditationDate) } catch (_: Exception) { null }
        val daysBetween = if (lastDate != null) {
            val lastCal = Calendar.getInstance().apply { time = lastDate }
            ((today.timeInMillis - lastCal.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
        } else Int.MAX_VALUE

        when (daysBetween) {
            0 -> {}
            1 -> { prefs.streakCount++; prefs.lastMeditationDate = todayStr }
            else -> { prefs.streakCount = 1; prefs.lastMeditationDate = todayStr }
        }
        prefs.totalSessionsCompleted++
        prefs.totalMinutesMeditated += _selectedMinutes.value

        // Log to Health Connect
        if (prefs.healthConnectEnabled) {
            val durationMs = _selectedMinutes.value * 60_000L
            viewModelScope.launch { healthConnect.logMindfulSession(durationMs) }
        }

        println("✅ Selah complete. Streak: ${prefs.streakCount}")
        _showCompletion.value = true
    }

    fun dismissCompletion() {
        _showCompletion.value = false
        _timeRemaining.value = _selectedMinutes.value * 60
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _timeRemaining.value > 0) {
                delay(1000)
                _timeRemaining.value -= 1
            }
            if (_timeRemaining.value <= 0) {
                completeSession()
            }
        }
    }

    fun getSoundIcon(sound: AmbientSound): ImageVector {
        return when (sound) {
            AmbientSound.NONE -> Icons.Default.VolumeOff
            AmbientSound.RAIN -> Icons.Default.WaterDrop
            AmbientSound.OCEAN -> Icons.Default.Water
            AmbientSound.FOREST -> Icons.Default.Forest
            AmbientSound.WIND -> Icons.Default.Air
            AmbientSound.FIRE -> Icons.Default.LocalFireDepartment
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
