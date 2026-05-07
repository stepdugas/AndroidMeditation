package com.thesecretplace.app.ui.screens.selah

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.HapticUtil
import com.thesecretplace.app.util.responsiveSp
import com.thesecretplace.app.util.timerRingSize

@Composable
fun SelahScreen(viewModel: SelahViewModel = hiltViewModel()) {
    val selectedMinutes by viewModel.selectedMinutes.collectAsState()
    val selectedSound by viewModel.selectedSound.collectAsState()
    val sessionActive by viewModel.sessionActive.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val showCompletion by viewModel.showCompletion.collectAsState()

    val view = androidx.compose.ui.platform.LocalView.current
    val ringSize = timerRingSize()
    val timerFontSize = responsiveSp(54.sp, compact = 40.sp)
    val totalSeconds = selectedMinutes * 60
    val progress = if (totalSeconds > 0) 1f - (timeRemaining.toFloat() / totalSeconds) else 0f
    val timeDisplay = "%d:%02d".format(timeRemaining / 60, timeRemaining % 60)

    if (showCompletion) {
        TimerCompletionScreen(
            duration = selectedMinutes,
            onDismiss = { viewModel.dismissCompletion() }
        )
        return
    }

    ThemedBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
        ) {
            // Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Selah", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text("Hebrew for pause and rest", fontSize = 11.sp, color = Accent.copy(alpha = 0.80f))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timer ring
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(ringSize),
                    color = Accent,
                    trackColor = Surface,
                    strokeWidth = 14.dp
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeDisplay,
                        fontSize = timerFontSize,
                        fontWeight = FontWeight.ExtraLight,
                        fontFamily = FontFamily.Monospace,
                        color = Color.White
                    )
                    if (sessionActive) {
                        Text(
                            text = if (isPaused) "Paused" else selectedSound.displayName,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Accent.copy(alpha = 0.80f)
                        )
                    } else {
                        Text("$selectedMinutes min", fontSize = 13.sp, color = Color.White.copy(0.40f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(44.dp))

            // Settings pickers (shown when not active)
            if (!sessionActive) {
                // Duration picker
                var showDurationMenu by remember { mutableStateOf(false) }
                Box {
                    PickerRow(icon = Icons.Default.Timer, label = "Duration", value = "$selectedMinutes min",
                        onClick = { showDurationMenu = true })
                    DropdownMenu(expanded = showDurationMenu, onDismissRequest = { showDurationMenu = false }) {
                        listOf(5, 10, 15, 20, 30, 45, 60).forEach { min ->
                            DropdownMenuItem(
                                text = { Text("$min minutes") },
                                onClick = { viewModel.setMinutes(min); showDurationMenu = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sound picker
                var showSoundMenu by remember { mutableStateOf(false) }
                Box {
                    PickerRow(icon = viewModel.getSoundIcon(selectedSound), label = "Sound", value = selectedSound.displayName,
                        onClick = { showSoundMenu = true })
                    DropdownMenu(expanded = showSoundMenu, onDismissRequest = { showSoundMenu = false }) {
                        AmbientSound.entries.forEach { sound ->
                            DropdownMenuItem(
                                text = { Text(sound.displayName) },
                                onClick = { viewModel.setSound(sound); showSoundMenu = false }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Controls
            if (sessionActive) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButton(
                        onClick = { HapticUtil.medium(view); viewModel.stopSession() },
                        modifier = Modifier
                            .size(58.dp)
                            .background(Surface.copy(alpha = 0.80f), CircleShape)
                    ) {
                        Icon(Icons.Default.Stop, "Stop", tint = Color.White.copy(0.80f))
                    }
                    FloatingActionButton(
                        onClick = { HapticUtil.light(view); viewModel.togglePause() },
                        containerColor = Accent,
                        shape = CircleShape,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = Color.Black, modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                GoldButton(
                    title = "Begin Session",
                    icon = Icons.Default.SelfImprovement,
                    onClick = { HapticUtil.medium(view); viewModel.startSession() }
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun PickerRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Surface.copy(alpha = 0.65f))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Icon(icon, null, tint = Accent, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color.White)
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontSize = 15.sp, color = Color.White.copy(0.55f))
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Default.UnfoldMore, null, tint = Color.White.copy(0.30f), modifier = Modifier.size(11.dp))
    }
}

@Composable
fun TimerCompletionScreen(duration: Int, onDismiss: () -> Unit) {
    ThemedBackground {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.size(88.dp).background(Accent.copy(0.14f), CircleShape))
                Icon(Icons.Default.CheckCircle, null, tint = Accent, modifier = Modifier.size(56.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("Session Complete", fontFamily = SerifDisplay, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Color.White)
            Spacer(Modifier.height(10.dp))
            Text("$duration minutes of stillness", fontSize = 16.sp, fontWeight = FontWeight.Light, color = Color.White.copy(0.55f))
            Spacer(Modifier.height(48.dp))
            GoldButton("Done", onClick = onDismiss)
        }
    }
}
