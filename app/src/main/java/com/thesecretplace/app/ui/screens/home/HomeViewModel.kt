package com.thesecretplace.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.data.sampleMeditations
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.network.SupabaseRepository
import com.thesecretplace.app.service.AudioServiceConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val supabase: SupabaseRepository,
    val audioConnection: AudioServiceConnection
) : ViewModel() {

    private val _userName = MutableStateFlow(prefs.userName)
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _streakCount = MutableStateFlow(prefs.streakCount)
    val streakCount: StateFlow<Int> = _streakCount.asStateFlow()

    private val _totalSessions = MutableStateFlow(prefs.totalSessionsCompleted)
    val totalSessions: StateFlow<Int> = _totalSessions.asStateFlow()

    private val _totalMinutes = MutableStateFlow(prefs.totalMinutesMeditated)
    val totalMinutes: StateFlow<Int> = _totalMinutes.asStateFlow()

    private val _allMeditations = MutableStateFlow(sampleMeditations)
    val allMeditations: StateFlow<List<Meditation>> = _allMeditations.asStateFlow()

    init {
        refreshCatalog()
    }

    private fun refreshCatalog() {
        viewModelScope.launch {
            try {
                val cloud = supabase.fetchMeditations()
                val cloudIds = cloud.map { it.id }.toSet()
                val localOnly = sampleMeditations.filter { it.id !in cloudIds }
                _allMeditations.value = cloud + localOnly
            } catch (_: Exception) { }
        }
    }

    fun refreshStats() {
        _streakCount.value = prefs.streakCount
        _totalSessions.value = prefs.totalSessionsCompleted
        _totalMinutes.value = prefs.totalMinutesMeditated
    }
}
