package com.thesecretplace.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.data.sampleMeditations
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.model.MeditationCategory
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.navigation.Routes
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.MeditationImage
import com.thesecretplace.app.util.heroHeight
import com.thesecretplace.app.util.responsiveHorizontalPadding
import com.thesecretplace.app.util.responsiveSp
import java.util.Calendar

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val totalSessions by viewModel.totalSessions.collectAsState()
    val totalMinutes by viewModel.totalMinutes.collectAsState()
    val allMeditations by viewModel.allMeditations.collectAsState()
    val audioState by viewModel.audioConnection.audioState.collectAsState()

    val greeting = remember {
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    val newMeditations = allMeditations.filter { it.isNew }
    val featuredMeditations = remember(allMeditations) {
        listOf(MeditationCategory.MORNING, MeditationCategory.SLEEP, MeditationCategory.STRESS_RELIEF, MeditationCategory.MEDITATIO)
            .mapNotNull { cat -> allMeditations.firstOrNull { it.category == cat } }
    }

    val bannerHeight = heroHeight()
    val hPadding = responsiveHorizontalPadding()
    val titleSize = responsiveSp(28.sp, compact = 24.sp)

    ThemedBackground {
        Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero banner
            Box(
                contentAlignment = Alignment.BottomStart,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerHeight)
                    .background(Surface)
            ) {
                // Beach background image
                MeditationImage(
                    imageName = "beach",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(HeroGradient)
                )

                Column(
                    modifier = Modifier.padding(28.dp)
                ) {
                    Text(
                        text = greeting.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White.copy(alpha = 0.70f),
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The Secret Place\nMeditation App",
                        fontFamily = SerifDisplay,
                        fontWeight = FontWeight.Bold,
                        fontSize = titleSize,
                        color = Color.White
                    )
                    if (userName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userName.replaceFirstChar { it.uppercase() },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Accent
                        )
                    }
                }
            }

            // Settings button row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = { navController.navigate(Routes.SETTINGS) }) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White.copy(alpha = 0.80f)
                    )
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = hPadding)
            ) {
                // Streak
                SectionHeader("Your Streak")
                Spacer(modifier = Modifier.height(14.dp))
                StreakBadge(
                    streakCount = streakCount,
                    onClick = { navController.navigate(Routes.MEDITATE) }
                )

                // Stats
                if (totalSessions > 0) {
                    Spacer(modifier = Modifier.height(32.dp))
                    StatsCard(sessions = totalSessions, minutes = totalMinutes, streak = streakCount)
                }

                // New this month
                if (newMeditations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    SectionHeader("New This Month")
                    Spacer(modifier = Modifier.height(14.dp))
                    newMeditations.forEach { meditation ->
                        FeaturedRow(
                            meditation = meditation,
                            onClick = { navController.navigate(Routes.detail(meditation.id)) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }

                // Featured
                Spacer(modifier = Modifier.height(32.dp))
                SectionHeader("Start Today")
                Spacer(modifier = Modifier.height(14.dp))
                featuredMeditations.forEach { meditation ->
                    FeaturedRow(
                        meditation = meditation,
                        onClick = { navController.navigate(Routes.detail(meditation.id)) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Now Playing bar at bottom
        if (audioState.nowPlayingMeditationId.isNotEmpty() && audioState.isPlaying) {
            NowPlayingBar(
                title = audioState.nowPlayingTitle,
                isPlaying = audioState.isPlaying,
                onPlayPause = {
                    if (audioState.isPlaying) viewModel.audioConnection.pause()
                    else viewModel.audioConnection.resume()
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
        } // Box
    }
}

@Composable
fun StreakBadge(streakCount: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Surface.copy(alpha = 0.70f))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Text("🔥", fontSize = 32.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(
                text = "$streakCount Day Streak",
                fontFamily = SerifDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.White
            )
            Text(
                text = if (streakCount > 0) "Keep it going!" else "Start your streak today",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
fun StatsCard(sessions: Int, minutes: Int, streak: Int) {
    SectionHeader("Your Journey")
    Spacer(modifier = Modifier.height(14.dp))
    PremiumCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            StatPill("$sessions", "Sessions", Icons.Default.SelfImprovement, Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Lavender.copy(alpha = 0.15f))
                    .align(Alignment.CenterVertically)
            )
            StatPill("$minutes", "Minutes", Icons.Default.Timer, Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(36.dp)
                    .background(Lavender.copy(alpha = 0.15f))
                    .align(Alignment.CenterVertically)
            )
            StatPill("$streak", "Day Streak", Icons.Default.LocalFireDepartment, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatPill(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Lavender, modifier = Modifier.size(17.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            fontFamily = SerifDisplay,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.50f)
        )
    }
}

@Composable
fun FeaturedRow(meditation: Meditation, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Surface.copy(alpha = 0.70f))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        // Thumbnail image
        MeditationImage(
            imageName = meditation.imageName,
            contentDescription = meditation.title,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(ThumbRadius))
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = meditation.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Accent.copy(alpha = 0.80f),
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = meditation.duration,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.62f)
                )
            }
        }

        Icon(
            Icons.Default.PlayCircleFilled,
            contentDescription = "Play",
            tint = Accent,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun NowPlayingBar(
    title: String,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(CardRadius))
            .background(Surface.copy(alpha = 0.88f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("NOW PLAYING", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Accent, letterSpacing = 1.sp)
            Spacer(Modifier.height(3.dp))
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onPlayPause) {
            Icon(
                if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Resume",
                tint = Color.White
            )
        }
    }
}
