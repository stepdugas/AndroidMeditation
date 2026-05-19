package com.thesecretplace.app.ui.screens.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.navigation.Routes
import com.thesecretplace.app.ui.screens.meditate.categoryIcon
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.HapticUtil
import com.thesecretplace.app.util.MeditationImage
import com.thesecretplace.app.util.playerRingSize
import com.thesecretplace.app.util.responsiveSp

@Composable
fun PlayerScreen(
    meditationId: String,
    navController: NavController,
    viewModel: PlayerViewModel = hiltViewModel()
) {
    // Stable flow reference — avoids creating a new StateFlow on every recomposition
    val meditationFlow = remember(meditationId) { viewModel.getMeditation(meditationId) }
    val meditation by meditationFlow.collectAsState()
    if (meditation == null) {
        ThemedBackground {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        }
        return
    }
    // Safe non-null reference for the rest of the screen
    val med = meditation!!
    val view = androidx.compose.ui.platform.LocalView.current

    val audioState by viewModel.audioState.collectAsState()
    val isFavorite by viewModel.isFavorite(meditationId).collectAsState(false)
    val isRepeating by viewModel.isRepeating.collectAsState()
    val showCompletion by viewModel.showCompletion.collectAsState()

    // Start playback once when screen opens
    LaunchedEffect(meditationId) {
        meditation?.let { viewModel.startPlayback(it) }
    }

    // Handle completion
    LaunchedEffect(audioState.didFinish) {
        if (audioState.didFinish) {
            meditation?.let { viewModel.onMeditationCompleted(it) }
        }
    }

    // Stop audio when system back button/gesture is used
    BackHandler {
        viewModel.stop()
        navController.popBackStack()
    }

    if (showCompletion) {
        CompletionScreen(
            meditation = med,
            streakCount = viewModel.getStreakCount(),
            onReplay = { viewModel.replay(med) },
            onDone = {
                viewModel.stop()
                navController.popBackStack()
            },
            onReflect = {
                viewModel.stop()
                navController.navigate(Routes.JOURNAL) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                }
            }
        )
        return
    }

    val ringSize = playerRingSize()
    val innerSize = ringSize - 12.dp
    val titleFontSize = responsiveSp(26.sp, compact = 22.sp)

    val progress = if (audioState.duration > 0) {
        (audioState.currentPosition.toFloat() / audioState.duration.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        MeditationImage(
            imageName = med.imageName,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        // Player vignette overlay
        Box(modifier = Modifier.fillMaxSize().background(PlayerVignette))

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 8.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back button
                Button(
                    onClick = {
                        viewModel.stop()
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 9.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.Black, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Back", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                // Category badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(categoryIcon(med.category), null, tint = med.category.color, modifier = Modifier.size(11.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(med.category.displayName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = med.category.color)
                }

                // Favorite button
                IconButton(onClick = { HapticUtil.light(view); viewModel.toggleFavorite(meditationId) }) {
                    Icon(
                        if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) Accent else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Progress ring + title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.size(ringSize),
                        color = Accent,
                        trackColor = Color.White.copy(alpha = 0.12f),
                        strokeWidth = 3.dp
                    )
                    // Thumbnail
                    MeditationImage(
                        imageName = med.imageName,
                        contentDescription = med.title,
                        modifier = Modifier
                            .size(innerSize)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = med.title,
                    fontFamily = SerifDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = titleFontSize,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = med.duration,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.55f),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Controls panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.Black.copy(alpha = 0.48f))
                    .padding(24.dp)
            ) {
                // Scrubber
                Slider(
                    value = audioState.currentPosition.toFloat(),
                    onValueChange = { viewModel.seekTo(it.toLong()) },
                    valueRange = 0f..audioState.duration.toFloat().coerceAtLeast(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Accent,
                        activeTrackColor = Accent,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatTime(audioState.currentPosition), fontSize = 12.sp, color = Color.White.copy(0.60f))
                    Text(formatTime(audioState.duration), fontSize = 12.sp, color = Color.White.copy(0.60f))
                }

                // Download indicator
                if (audioState.isDownloading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Accent, strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Loading audio…", fontSize = 12.sp, color = Color.White.copy(0.55f))
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Playback controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repeat
                    IconButton(onClick = { HapticUtil.light(view); viewModel.toggleRepeat() }) {
                        Icon(
                            Icons.Default.Repeat, "Repeat",
                            tint = if (isRepeating) Accent else Color.White.copy(0.55f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Skip back 10s
                    IconButton(onClick = { HapticUtil.light(view); viewModel.skipBack() }) {
                        Icon(Icons.Default.Replay10, "Skip back", tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    // Play/Pause
                    FloatingActionButton(
                        onClick = { HapticUtil.medium(view); viewModel.togglePlayPause() },
                        containerColor = Accent,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (audioState.isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Skip forward 10s
                    IconButton(onClick = { HapticUtil.light(view); viewModel.skipForward() }) {
                        Icon(Icons.Default.Forward10, "Skip forward", tint = Color.White, modifier = Modifier.size(24.dp))
                    }

                    // Stop
                    IconButton(onClick = { viewModel.stop() }) {
                        Icon(Icons.Default.Stop, "Stop", tint = Color.White.copy(0.50f), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
