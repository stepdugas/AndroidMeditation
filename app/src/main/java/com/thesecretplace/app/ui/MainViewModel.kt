package com.thesecretplace.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesecretplace.app.billing.BillingManager
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
class MainViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val supabase: SupabaseRepository,
    val audioConnection: AudioServiceConnection,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _hasSeenWelcome = MutableStateFlow(prefs.hasSeenWelcome)
    val hasSeenWelcome: StateFlow<Boolean> = _hasSeenWelcome.asStateFlow()

    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    private val _adminUnlocked = MutableStateFlow(prefs.adminUnlocked)
    val adminUnlocked: StateFlow<Boolean> = _adminUnlocked.asStateFlow()

    // Full catalog: cloud + local merged
    private val _allMeditations = MutableStateFlow(sampleMeditations)
    val allMeditations: StateFlow<List<Meditation>> = _allMeditations.asStateFlow()

    fun completeOnboarding(name: String) {
        prefs.userName = name
        prefs.hasSeenWelcome = true
        _hasSeenWelcome.value = true
    }

    fun dismissPaywall() {
        _showPaywall.value = false
    }

    fun connectAudio() {
        audioConnection.connect()
    }

    fun connectBilling() {
        billingManager.connect()
    }

    fun refreshCatalog() {
        viewModelScope.launch {
            try {
                val cloudItems = supabase.fetchMeditations()
                val localById = sampleMeditations.associateBy { it.id }
                // Merge: for meditations that exist locally, keep bundled audio
                // instead of remote URL so playback doesn't depend on network
                val merged = cloudItems.map { cloud ->
                    val local = localById[cloud.id]
                    if (local != null) {
                        // Keep cloud metadata but use local bundled audio
                        cloud.copy(
                            audioFileName = local.audioFileName,
                            remoteAudioURL = null
                        )
                    } else {
                        cloud
                    }
                }
                val cloudIDs = cloudItems.map { it.id }.toSet()
                val localOnly = sampleMeditations.filter { it.id !in cloudIDs }
                _allMeditations.value = merged + localOnly
                println("✅ Catalog: ${merged.size} cloud (${merged.count { localById.containsKey(it.id) }} with local audio) + ${localOnly.size} local-only = ${_allMeditations.value.size} total")
            } catch (e: Exception) {
                println("⚠️ Catalog fetch failed: ${e.message}")
            }
        }
    }

    fun findMeditation(id: String): Meditation? {
        return _allMeditations.value.find { it.id == id }
    }

    override fun onCleared() {
        super.onCleared()
        audioConnection.disconnect()
        billingManager.disconnect()
    }
}
