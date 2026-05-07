package com.thesecretplace.app.ui.screens.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thesecretplace.app.network.AnthropicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val anthropic: AnthropicRepository
) : ViewModel() {

    private val _insight = MutableStateFlow<String?>(anthropic.cachedDailyInsight())
    val insight: StateFlow<String?> = _insight.asStateFlow()

    private val _prayer = MutableStateFlow<String?>(anthropic.cachedDailyPrayer())
    val prayer: StateFlow<String?> = _prayer.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        if (_insight.value == null) fetchInsight()
    }

    private fun fetchInsight() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val result = anthropic.todaysDailyInsight()
            _isLoading.value = false
            if (result != null) {
                _insight.value = result
                _prayer.value = anthropic.cachedDailyPrayer()
            } else {
                _errorMessage.value = anthropic.errorMessage
            }
        }
    }

    fun retry() {
        fetchInsight()
    }
}
