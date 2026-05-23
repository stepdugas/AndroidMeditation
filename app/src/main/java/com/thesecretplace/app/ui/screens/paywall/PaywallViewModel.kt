package com.thesecretplace.app.ui.screens.paywall

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.thesecretplace.app.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PaywallViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    val productDetails = billingManager.productDetails
    val isPurchasing = billingManager.isPurchasing
    val purchaseError = billingManager.purchaseError
    val isPremium = billingManager.isPremium

    fun purchase(activity: Activity) {
        billingManager.purchase(activity)
    }

    fun restorePurchases() {
        billingManager.restorePurchases()
    }
}
