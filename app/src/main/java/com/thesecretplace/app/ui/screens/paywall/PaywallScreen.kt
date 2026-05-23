package com.thesecretplace.app.ui.screens.paywall

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.theme.*

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    navController: NavController,
    viewModel: PaywallViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val productDetails by viewModel.productDetails.collectAsState()
    val isPurchasing by viewModel.isPurchasing.collectAsState()
    val purchaseError by viewModel.purchaseError.collectAsState()
    val isPremium by viewModel.isPremium.collectAsState()

    // Auto-dismiss when purchase succeeds
    LaunchedEffect(isPremium) {
        if (isPremium) onDismiss()
    }

    val features = listOf(
        Icons.Default.AutoAwesome to "Unlimited guided meditations",
        Icons.Default.Psychology to "Daily AI-powered microlessons",
        Icons.Default.GridView to "All 4 meditation categories",
        Icons.Default.Refresh to "New content added regularly"
    )

    // Price text from Google Play or fallback
    val priceText = productDetails?.subscriptionOfferDetails
        ?.firstOrNull()?.pricingPhases?.pricingPhaseList
        ?.find { it.billingPeriod == "P1M" }
        ?.formattedPrice?.let { "$it/month after free trial" }
        ?: "$4.99/month after free trial"

    ThemedBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(Modifier.height(56.dp))

                // Hero — matches iOS sparkles glow
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(88.dp).background(Accent.copy(0.18f), CircleShape))
                    Icon(Icons.Default.AutoAwesome, null, tint = Accent, modifier = Modifier.size(48.dp))
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "The Secret Place Premium",
                    fontFamily = SerifDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = Cream,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Begin your 7-day free trial",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Accent,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(priceText, fontSize = 15.sp, color = Cream.copy(0.65f))

                Spacer(Modifier.height(28.dp))

                // Features card
                PremiumCard {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        features.forEachIndexed { index, (icon, label) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
                            ) {
                                Icon(icon, null, tint = Accent, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(14.dp))
                                Text(label, fontSize = 15.sp, color = Cream)
                            }
                            if (index < features.size - 1) ThemeDivider(Modifier.padding(start = 52.dp))
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Summary card
                PremiumCard(cornerRadius = CardRadius) {
                    Column {
                        SummaryRow(Icons.Default.CardGiftcard, "Free for 7 days")
                        ThemeDivider(Modifier.padding(start = 52.dp))
                        SummaryRow(Icons.Default.Refresh, "Then $4.99/month, auto-renews")
                        ThemeDivider(Modifier.padding(start = 52.dp))
                        SummaryRow(Icons.Default.Cancel, "Cancel anytime in Play Store")
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Error banner — matches iOS yellow triangle style
                if (purchaseError != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CardRadius))
                            .background(Color.Red.copy(alpha = 0.18f))
                            .padding(14.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning, null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            purchaseError ?: "",
                            fontSize = 13.sp,
                            color = Cream
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                }

                // CTA — "Start Free Trial" or unavailable
                if (productDetails != null) {
                    GoldButton("Start Free Trial") {
                        activity?.let { viewModel.purchase(it) }
                    }
                } else {
                    Text(
                        "Subscription Not Available",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Cream.copy(0.40f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(ButtonRadius))
                            .background(Surface.copy(0.50f))
                            .padding(vertical = 18.dp)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Restore purchases
                TextButton(onClick = { viewModel.restorePurchases() }) {
                    Text("Restore Purchases", fontSize = 14.sp, color = Cream.copy(0.55f))
                }

                Spacer(Modifier.height(12.dp))

                // Legal text
                Text(
                    "Subscription auto-renews monthly at $4.99 unless canceled at least 24 hours before the end of the trial. Manage or cancel anytime in your Google Play settings.",
                    fontSize = 11.sp,
                    color = Cream.copy(0.35f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(32.dp))
            }

            // Close button — top right, matches iOS
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 20.dp)
                    .statusBarsPadding()
                    .size(36.dp)
                    .background(Surface.copy(0.60f), CircleShape)
            ) {
                Icon(Icons.Default.Close, "Close", tint = Cream.copy(0.7f), modifier = Modifier.size(15.dp))
            }

            // Purchasing overlay — matches iOS full-screen loading
            if (isPurchasing) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(CardRadius))
                            .background(Surface.copy(0.90f))
                            .padding(32.dp)
                    ) {
                        CircularProgressIndicator(color = Accent, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("Processing...", fontSize = 15.sp, color = Cream)
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp)
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(14.dp))
        Text(text, fontSize = 14.sp, color = Cream.copy(0.85f))
    }
}
