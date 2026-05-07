package com.thesecretplace.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.data.sampleMeditations
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.navigation.Routes
import com.thesecretplace.app.ui.screens.meditate.categoryIcon
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.MeditationImage
import com.thesecretplace.app.util.heroHeight
import javax.inject.Inject

@Composable
fun DetailScreen(
    meditationId: String,
    navController: NavController,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val meditation by viewModel.getMeditation(meditationId).collectAsState(null)
    val isFavorite by viewModel.isFavorite(meditationId).collectAsState(false)
    val showIntention by viewModel.showIntentionScreen.collectAsState()

    if (meditation == null) {
        // Loading state while cloud meditations are fetched
        ThemedBackground {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        }
        return
    }

    val med = meditation!!
    val bannerHeight = heroHeight()

    ThemedBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // iOS-style inline nav bar with back button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Accent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Back", color = Accent, fontSize = 16.sp)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero image area
                Box(
                    contentAlignment = Alignment.BottomStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bannerHeight)
                ) {
                    MeditationImage(
                        imageName = med.imageName,
                        contentDescription = med.title,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.fillMaxSize().background(HeroGradient))

                    Column(modifier = Modifier.padding(24.dp)) {
                    // Category badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.40f))
                            .padding(horizontal = 11.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            categoryIcon(med.category),
                            contentDescription = null,
                            tint = med.category.color,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = med.category.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = med.category.color
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = med.title,
                        fontFamily = SerifDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Schedule, null, tint = Color.White.copy(0.70f), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(med.duration, fontSize = 14.sp, color = Color.White.copy(0.72f))
                    }
                }
            }

            // Content
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 22.dp)) {
                ThemeDivider()
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = med.description,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.82f),
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Save button
                    OutlinedButton(
                        onClick = { viewModel.toggleFavorite(meditationId) },
                        shape = RoundedCornerShape(ButtonRadius),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isFavorite) Accent else Color.Transparent
                        ),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Icon(
                            if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            null,
                            tint = if (isFavorite) Color.Black else Color.White
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (isFavorite) "Saved" else "Save",
                            color = if (isFavorite) Color.Black else Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Play button — goes through intention screen if enabled
                    Button(
                        onClick = {
                            val route = if (showIntention) Routes.intention(meditationId) else Routes.player(meditationId)
                            navController.navigate(route)
                        },
                        shape = RoundedCornerShape(ButtonRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                        modifier = Modifier.weight(1f).height(52.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black)
                        Spacer(Modifier.width(8.dp))
                        Text("Play", color = Color.Black, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            }
        }
    }
}
