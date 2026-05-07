package com.thesecretplace.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.data.sampleMeditations
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.network.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val supabase: SupabaseRepository
) : ViewModel() {

    private val _favoritesChanged = MutableStateFlow(0)
    private val _cloudMeditations = MutableStateFlow<List<Meditation>>(emptyList())

    val showIntentionScreen = MutableStateFlow(prefs.showIntentionScreen)

    init {
        viewModelScope.launch {
            try { _cloudMeditations.value = supabase.fetchMeditations() } catch (_: Exception) {}
        }
    }

    fun findMeditation(id: String): Meditation? {
        return sampleMeditations.find { it.id == id }
            ?: _cloudMeditations.value.find { it.id == id }
    }

    // Reactive version — re-emits when cloud data arrives
    fun getMeditation(id: String): StateFlow<Meditation?> {
        return _cloudMeditations.map {
            sampleMeditations.find { it.id == id } ?: _cloudMeditations.value.find { it.id == id }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), findMeditation(id))
    }

    fun isFavorite(id: String): Flow<Boolean> {
        return _favoritesChanged.map { prefs.isFavorite(id) }
    }

    fun toggleFavorite(id: String) {
        prefs.toggleFavorite(id)
        _favoritesChanged.value++
    }
}
