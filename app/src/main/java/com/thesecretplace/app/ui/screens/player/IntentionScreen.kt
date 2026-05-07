package com.thesecretplace.app.ui.screens.player

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.navigation.Routes
import com.thesecretplace.app.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun IntentionScreen(
    meditationId: String,
    navController: NavController,
    viewModel: IntentionViewModel = hiltViewModel()
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customIntention by remember { mutableStateOf("") }
    var opacity by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (opacity) 1f else 0f,
        animationSpec = tween(500),
        label = "fade"
    )

    LaunchedEffect(Unit) { opacity = true }

    val intentions = listOf("Relax", "Clear My Mind", "Feel Grateful", "Breathe Deeply", "Let Go of Stress")

    ThemedBackground(modifier = Modifier.alpha(alpha)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(52.dp))

            // Icon
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Accent.copy(alpha = 0.12f), CircleShape)
                )
                Icon(Icons.Default.AutoAwesome, null, tint = Accent, modifier = Modifier.size(36.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Set Your Intention",
                fontFamily = SerifDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "What are you here for today?",
                fontSize = 15.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.58f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Intention options
            intentions.forEach { intention ->
                Button(
                    onClick = {
                        viewModel.setIntention(intention)
                        navController.navigate(Routes.player(meditationId)) {
                            popUpTo(Routes.intention(meditationId)) { inclusive = true }
                        }
                    },
                    shape = RoundedCornerShape(ButtonRadius),
                    colors = ButtonDefaults.buttonColors(containerColor = Surface.copy(alpha = 0.75f)),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text(intention, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Write your own
            OutlinedButton(
                onClick = { showCustomInput = !showCustomInput },
                shape = RoundedCornerShape(ButtonRadius),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Accent.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Icon(Icons.Default.Edit, null, tint = Accent, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Write Your Own", color = Accent, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

            if (showCustomInput) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = customIntention,
                    onValueChange = { customIntention = it },
                    placeholder = { Text("Enter your intention") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Lavender.copy(alpha = 0.25f),
                        unfocusedBorderColor = Lavender.copy(alpha = 0.25f),
                        focusedContainerColor = Surface,
                        unfocusedContainerColor = Surface,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Accent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                GoldButton(
                    title = "Begin Meditation",
                    enabled = customIntention.trim().isNotEmpty(),
                    onClick = {
                        viewModel.setIntention(customIntention.trim())
                        navController.navigate(Routes.player(meditationId)) {
                            popUpTo(Routes.intention(meditationId)) { inclusive = true }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(52.dp))
        }
    }
}

@HiltViewModel
class IntentionViewModel @Inject constructor(
    private val prefs: PreferencesManager
) : androidx.lifecycle.ViewModel() {
    fun setIntention(intention: String) {
        prefs.selectedIntention = intention
    }
}
