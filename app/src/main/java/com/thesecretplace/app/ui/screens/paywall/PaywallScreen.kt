package com.thesecretplace.app.ui.screens.paywall

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.theme.*

@Composable
fun PaywallScreen(
    onDismiss: () -> Unit,
    navController: NavController
) {
    val features = listOf(
        Icons.Default.AutoAwesome to "Unlimited guided meditations",
        Icons.Default.Psychology to "Daily AI-powered microlessons",
        Icons.Default.GridView to "All 4 meditation categories",
        Icons.Default.Refresh to "New content added regularly"
    )

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

                // Hero
                Box(contentAlignment = Alignment.Center) {
                    Box(Modifier.size(88.dp).background(Accent.copy(0.18f), CircleShape))
                    Icon(Icons.Default.AutoAwesome, null, tint = Accent, modifier = Modifier.size(48.dp))
                }

                Spacer(Modifier.height(16.dp))
                Text("The Secret Place Premium", fontFamily = SerifDisplay, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Cream, textAlign = TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("Begin your 7-day free trial", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Accent, textAlign = TextAlign.Center)
                Spacer(Modifier.height(6.dp))
                Text("$4.99/month after free trial", fontSize = 15.sp, color = Cream.copy(0.65f))

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

                // Summary
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

                // CTA
                GoldButton("Start Free Trial") {
                    // Wire to BillingManager
                }

                Spacer(Modifier.height(14.dp))

                TextButton(onClick = { /* Restore */ }) {
                    Text("Restore Purchases", fontSize = 14.sp, color = Cream.copy(0.55f))
                }

                Spacer(Modifier.height(12.dp))

                // Legal
                Text(
                    "Subscription auto-renews monthly at $4.99 unless canceled at least 24 hours before the end of the trial. Manage or cancel anytime in your Google Play settings.",
                    fontSize = 11.sp, color = Cream.copy(0.35f), textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(32.dp))
            }

            // Close button
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
