package com.thesecretplace.app.ui.screens.learn

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LearnScreen(viewModel: LearnViewModel = hiltViewModel()) {
    val insight by viewModel.insight.collectAsState()
    val prayer by viewModel.prayer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(insight) { if (insight != null) contentVisible = true }

    val formattedToday = remember {
        SimpleDateFormat("EEEE, MMMM d", Locale.US).format(Date())
    }

    ThemedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            // Header
            SectionHeader("Daily Practice")
            Spacer(Modifier.height(6.dp))
            Text("Today's Insight", fontFamily = SerifDisplay, fontWeight = FontWeight.Bold, fontSize = 34.sp, color = Color.White)
            Text(formattedToday, fontSize = 14.sp, color = Color.White.copy(0.45f))

            Spacer(Modifier.height(28.dp))

            // Prayer card
            if (prayer != null) {
                PremiumCard {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(contentAlignment = Alignment.Center) {
                                Box(Modifier.size(36.dp).background(Lavender.copy(0.18f), CircleShape))
                                Text("🙏", fontSize = 16.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("DAILY PRAYER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Lavender, letterSpacing = 1.8.sp)
                                Text("Paired with today's insight", fontSize = 13.sp, color = Color.White.copy(0.45f))
                            }
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            prayer ?: "",
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            color = Color.White.copy(0.88f),
                            lineHeight = 26.sp,
                            modifier = Modifier.alpha(if (contentVisible) 1f else 0f)
                        )
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            // Lesson card
            PremiumCard {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Card header
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(Modifier.size(36.dp).background(Accent.copy(0.18f), CircleShape))
                            Icon(Icons.Default.AutoAwesome, null, tint = Accent, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("DAILY INSIGHT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Accent, letterSpacing = 1.8.sp)
                            Text("Refreshes each morning", fontSize = 13.sp, color = Color.White.copy(0.45f))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Card body
                    when {
                        isLoading -> ShimmerLoadingView()
                        errorMessage != null -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, null, tint = Color(0xFFFFA500), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Couldn't load insight", fontWeight = FontWeight.Medium, color = Color.White)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(errorMessage ?: "", fontSize = 12.sp, color = Color.White.copy(0.5f))
                            Spacer(Modifier.height(12.dp))
                            TextButton(onClick = { viewModel.retry() }) {
                                Icon(Icons.Default.Refresh, null, tint = Accent, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Try Again", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                        insight != null -> {
                            Text(
                                insight ?: "",
                                fontSize = 16.sp,
                                color = Color.White.copy(0.92f),
                                lineHeight = 26.sp,
                                modifier = Modifier.alpha(if (contentVisible) 1f else 0f)
                            )
                        }
                        else -> {
                            Text("Finding your insight for today…", fontSize = 14.sp, color = Color.White.copy(0.4f), fontStyle = FontStyle.Italic)
                        }
                    }

                    // Footer
                    if (insight != null) {
                        Spacer(Modifier.height(20.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = Color.White.copy(0.25f), modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(formattedToday, fontSize = 10.sp, color = Color.White.copy(0.25f))
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.CheckCircle, null, tint = Accent.copy(0.5f), modifier = Modifier.size(11.dp))
                        }
                    }
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ShimmerLoadingView() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(0.9f, 0.75f, 0.85f).forEach { widthFraction ->
            Box(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(14.dp)
                    .background(Lavender.copy(alpha = alpha), RoundedCornerShape(4.dp))
            )
        }
    }
}
