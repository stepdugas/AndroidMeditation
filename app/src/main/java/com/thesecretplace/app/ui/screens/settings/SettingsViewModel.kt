package com.thesecretplace.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.thesecretplace.app.billing.BillingManager
import com.thesecretplace.app.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesManager,
    private val billingManager: BillingManager
) : ViewModel() {

    private val _showIntention = MutableStateFlow(prefs.showIntentionScreen)
    val showIntention: StateFlow<Boolean> = _showIntention.asStateFlow()

    private val _healthEnabled = MutableStateFlow(prefs.healthConnectEnabled)
    val healthEnabled: StateFlow<Boolean> = _healthEnabled.asStateFlow()

    private val _isRestoring = MutableStateFlow(false)
    val isRestoring: StateFlow<Boolean> = _isRestoring.asStateFlow()

    private val _restoreToast = MutableStateFlow<String?>(null)
    val restoreToast: StateFlow<String?> = _restoreToast.asStateFlow()

    fun setShowIntention(value: Boolean) {
        prefs.showIntentionScreen = value
        _showIntention.value = value
    }

    fun resetIntention() {
        prefs.selectedIntention = ""
    }

    fun resetFavorites() {
        prefs.favoriteMeditationIDs = ""
    }

    fun setHealthEnabled(value: Boolean) {
        prefs.healthConnectEnabled = value
        _healthEnabled.value = value
    }

    fun unlockAdmin() {
        prefs.adminUnlocked = true
    }

    fun isAdminUnlocked(): Boolean = prefs.adminUnlocked

    fun restorePurchases() {
        if (_isRestoring.value) return
        _isRestoring.value = true
        billingManager.restorePurchases()
        // Check result after a brief delay for the async query to complete
        _isRestoring.value = false
        _restoreToast.value = if (billingManager.isPremium.value) {
            "Purchase restored!"
        } else {
            "No active subscription found."
        }
    }

    fun clearRestoreToast() {
        _restoreToast.value = null
    }

    fun getReminderHour(): Int = prefs.dailyReminderHour
    fun getReminderMinute(): Int = prefs.dailyReminderMinute

    fun setReminder(hour: Int, minute: Int) {
        prefs.dailyReminderHour = hour
        prefs.dailyReminderMinute = minute
    }
}
