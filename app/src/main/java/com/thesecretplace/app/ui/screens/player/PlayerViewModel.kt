package com.thesecretplace.app.ui.screens.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.data.sampleMeditations
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.network.SupabaseRepository
import com.thesecretplace.app.service.AudioServiceConnection
import com.thesecretplace.app.service.AudioState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val audioConnection: AudioServiceConnection,
    private val supabase: SupabaseRepository
) : ViewModel() {

    val audioState: StateFlow<AudioState> = audioConnection.audioState

    private val _isRepeating = MutableStateFlow(false)
    val isRepeating: StateFlow<Boolean> = _isRepeating.asStateFlow()

    private val _showCompletion = MutableStateFlow(false)
    val showCompletion: StateFlow<Boolean> = _showCompletion.asStateFlow()

    private val _favoritesChanged = MutableStateFlow(0)

    private val _cloudMeditations = MutableStateFlow<List<Meditation>>(emptyList())

    init {
        audioConnection.connect()
        viewModelScope.launch {
            try { _cloudMeditations.value = supabase.fetchMeditations() } catch (_: Exception) {}
        }
    }

    fun findMeditation(id: String): Meditation? {
        return sampleMeditations.find { it.id == id }
            ?: _cloudMeditations.value.find { it.id == id }
    }

    fun getMeditation(id: String): StateFlow<Meditation?> {
        return _cloudMeditations.map {
            sampleMeditations.find { it.id == id } ?: _cloudMeditations.value.find { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), findMeditation(id))
    }

    fun isFavorite(id: String): Flow<Boolean> = _favoritesChanged.map { prefs.isFavorite(id) }

    fun toggleFavorite(id: String) {
        prefs.toggleFavorite(id)
        _favoritesChanged.value++
    }

    fun startPlayback(meditation: Meditation) {
        if (audioState.value.nowPlayingMeditationId == meditation.id) return
        _showCompletion.value = false

        if (meditation.remoteAudioURL != null) {
            viewModelScope.launch {
                audioConnection.downloadAndPlay(
                    remoteUrl = meditation.remoteAudioURL,
                    title = meditation.title,
                    meditationId = meditation.id
                )
            }
        } else {
            audioConnection.playAudio(
                fileName = meditation.audioFileName,
                title = meditation.title,
                meditationId = meditation.id
            )
        }
    }

    fun togglePlayPause() {
        if (audioState.value.isPlaying) audioConnection.pause()
        else audioConnection.resume()
    }

    fun skipBack() {
        val newPos = (audioState.value.currentPosition - 10_000).coerceAtLeast(0)
        audioConnection.seekTo(newPos)
    }

    fun skipForward() {
        val newPos = (audioState.value.currentPosition + 10_000).coerceAtMost(audioState.value.duration)
        audioConnection.seekTo(newPos)
    }

    fun seekTo(positionMs: Long) {
        audioConnection.seekTo(positionMs)
    }

    fun stop() {
        audioConnection.stop()
    }

    fun toggleRepeat() {
        _isRepeating.value = !_isRepeating.value
        audioConnection.setLooping(_isRepeating.value)
    }

    fun replay(meditation: Meditation) {
        _showCompletion.value = false
        audioConnection.consumeFinishEvent()
        startPlayback(meditation)
    }

    fun onMeditationCompleted(meditation: Meditation) {
        audioConnection.consumeFinishEvent()
        recordCompletion(meditation)
        _showCompletion.value = true
    }

    fun getStreakCount(): Int = prefs.streakCount

    private fun recordCompletion(meditation: Meditation) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        val todayStr = sdf.format(today.time)

        val lastDate = try {
            sdf.parse(prefs.lastMeditationDate)
        } catch (_: Exception) { null }

        val daysBetween = if (lastDate != null) {
            val lastCal = Calendar.getInstance().apply { time = lastDate }
            lastCal.set(Calendar.HOUR_OF_DAY, 0)
            lastCal.set(Calendar.MINUTE, 0)
            lastCal.set(Calendar.SECOND, 0)
            lastCal.set(Calendar.MILLISECOND, 0)
            ((today.timeInMillis - lastCal.timeInMillis) / (24 * 60 * 60 * 1000)).toInt()
        } else {
            Int.MAX_VALUE
        }

        when (daysBetween) {
            0 -> { /* Same day, no streak change */ }
            1 -> {
                prefs.streakCount = prefs.streakCount + 1
                prefs.lastMeditationDate = todayStr
            }
            else -> {
                prefs.streakCount = 1
                prefs.lastMeditationDate = todayStr
            }
        }

        prefs.totalSessionsCompleted = prefs.totalSessionsCompleted + 1
        val minutes = maxOf(1, (audioState.value.duration / 60_000).toInt())
        prefs.totalMinutesMeditated = prefs.totalMinutesMeditated + minutes
        println("✅ Meditation completed. Streak: ${prefs.streakCount} | Sessions: ${prefs.totalSessionsCompleted} | Minutes: ${prefs.totalMinutesMeditated}")
    }
}
