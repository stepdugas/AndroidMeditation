package com.thesecretplace.app.ui.screens.player

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesecretplace.app.data.local.JournalEntity
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.screens.journal.ReflectionBottomSheet
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.HapticUtil
import com.thesecretplace.app.util.ShareUtil
import kotlin.math.abs

@Composable
fun CompletionScreen(
    meditation: Meditation,
    streakCount: Int,
    onReplay: () -> Unit,
    onDone: () -> Unit,
    onSaveJournalEntry: ((JournalEntity) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var visible by remember { mutableStateOf(false) }
    var showReflection by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(850),
        label = "completionFade"
    )
    val checkScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.6f,
        animationSpec = spring(dampingRatio = 0.55f),
        label = "checkScale"
    )

    LaunchedEffect(Unit) {
        visible = true
        HapticUtil.success(context)
    }

    val milestoneDays = setOf(7, 14, 30, 100)
    val isMilestone = streakCount in milestoneDays

    // Scripture rotation
    val verses = listOf(
        "Be still, and know that I am God." to "Psalm 46:10",
        "Peace I leave with you; my peace I give to you." to "John 14:27",
        "Come to me, all you who are weary and burdened, and I will give you rest." to "Matthew 11:28",
        "The Lord is my shepherd; I lack nothing." to "Psalm 23:1",
        "You will keep in perfect peace those whose minds are steadfast, because they trust in you." to "Isaiah 26:3",
        "Cast all your anxiety on him because he cares for you." to "1 Peter 5:7",
        "He makes me lie down in green pastures, he leads me beside quiet waters." to "Psalm 23:2",
        "My presence will go with you, and I will give you rest." to "Exodus 33:14"
    )
    val scripture = verses[abs(meditation.id.hashCode()) % verses.size]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = 0.78f),
                        Color.Black.copy(alpha = 0.55f),
                        Color.Black.copy(alpha = 0.90f)
                    )
                )
            )
            .alpha(alpha)
    ) {
        // Lavender glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Lavender.copy(alpha = 0.18f), Color.Transparent),
                        radius = 800f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // Completion icon
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .background(Accent.copy(alpha = 0.14f), CircleShape)
                )
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Complete",
                    tint = Accent,
                    modifier = Modifier.size(52.dp).scale(checkScale)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = if (isMilestone) "$streakCount Day Milestone!" else "Session Complete",
                fontFamily = SerifDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = if (isMilestone) 30.sp else 28.sp,
                color = if (isMilestone) Accent else Color.White,
                textAlign = TextAlign.Center
            )

            if (!isMilestone) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = meditation.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }

            // Streak pill
            if (streakCount > 0) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("🔥", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "$streakCount day streak",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Scripture card
            PremiumCard(cornerRadius = CardRadius) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = "\u201C${scripture.first}\u201D",
                        fontFamily = SerifDisplay,
                        fontWeight = FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(modifier = Modifier.width(30.dp).height(1.dp).background(Accent.copy(alpha = 0.45f)))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "— ${scripture.second}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { ShareUtil.shareCompletion(context, meditation.title, streakCount) },
                    shape = RoundedCornerShape(ButtonRadius),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface.copy(alpha = 0.85f)),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Share", color = Color.White, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onReplay,
                    shape = RoundedCornerShape(ButtonRadius),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface.copy(alpha = 0.85f)),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Icon(Icons.Default.Replay, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Replay", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            // Reflect button (only shown when onSaveJournalEntry is provided)
            if (onSaveJournalEntry != null) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showReflection = true },
                    shape = RoundedCornerShape(ButtonRadius),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Surface.copy(alpha = 0.85f)),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Reflect", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            GoldButton(title = "Return Home", onClick = onDone)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Reflection bottom sheet — matches iOS ReflectionPromptSheet
    if (showReflection && onSaveJournalEntry != null) {
        ReflectionBottomSheet(
            meditationId = meditation.id,
            meditationTitle = meditation.title,
            onSave = onSaveJournalEntry,
            onDismiss = { showReflection = false }
        )
    }
}
