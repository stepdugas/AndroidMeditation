package com.thesecretplace.app.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesecretplace.app.data.local.JournalDao
import com.thesecretplace.app.data.local.JournalEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalViewModel @Inject constructor(
    private val journalDao: JournalDao
) : ViewModel() {

    val entries: StateFlow<List<JournalEntity>> = journalDao.getAllEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteEntry(entry: JournalEntity) {
        viewModelScope.launch {
            journalDao.delete(entry)
        }
    }

    fun saveEntry(entry: JournalEntity) {
        viewModelScope.launch {
            journalDao.insert(entry)
        }
    }
}
