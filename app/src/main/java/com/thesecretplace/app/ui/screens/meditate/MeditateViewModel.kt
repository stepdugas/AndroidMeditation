package com.thesecretplace.app.ui.screens.meditate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesecretplace.app.data.sampleMeditations
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.model.MeditationCategory
import com.thesecretplace.app.network.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MeditateViewModel @Inject constructor(
    private val supabase: SupabaseRepository
) : ViewModel() {

    private val _allMeditations = MutableStateFlow(sampleMeditations)

    private val _selectedCategory = MutableStateFlow(MeditationCategory.MORNING)
    val selectedCategory: StateFlow<MeditationCategory> = _selectedCategory.asStateFlow()

    val filteredMeditations: StateFlow<List<Meditation>> = combine(
        _allMeditations, _selectedCategory
    ) { all, cat ->
        all.filter { it.belongsTo(cat) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val availableCategories: StateFlow<List<MeditationCategory>> = _allMeditations.map { all ->
        MeditationCategory.entries.filter { cat -> all.any { it.belongsTo(cat) } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MeditationCategory.entries.toList())

    init {
        viewModelScope.launch {
            try {
                val cloud = supabase.fetchMeditations()
                val localById = sampleMeditations.associateBy { it.id }
                val merged = cloud.map { item ->
                    val local = localById[item.id]
                    if (local != null) item.copy(audioFileName = local.audioFileName, remoteAudioURL = null)
                    else item
                }
                val cloudIds = cloud.map { it.id }.toSet()
                val localOnly = sampleMeditations.filter { it.id !in cloudIds }
                _allMeditations.value = merged + localOnly
            } catch (_: Exception) { }
        }
    }

    fun selectCategory(category: MeditationCategory) {
        _selectedCategory.value = category
    }
}
