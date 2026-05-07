package com.thesecretplace.app.ui.screens.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesecretplace.app.ui.components.GoldButton
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.MeditationImage
import com.thesecretplace.app.util.responsiveSp

@Composable
fun OnboardingScreen(onComplete: (String) -> Unit) {
    val appTitleSize = responsiveSp(36.sp, compact = 28.sp)
    var nameInput by remember { mutableStateOf("") }
    var fadeIn by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val alpha by animateFloatAsState(
        targetValue = if (fadeIn) 1f else 0f,
        animationSpec = tween(1000),
        label = "fadeIn"
    )

    LaunchedEffect(Unit) { fadeIn = true }

    val canContinue = nameInput.trim().isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image (matches iOS sunrise-trees)
        MeditationImage(
            imageName = "sunrise_trees",
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        // Gradient overlays on top of image
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.30f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.88f)
                        )
                    )
                )
        )
        // Warm accent glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Accent.copy(alpha = 0.20f), Color.Transparent),
                        radius = 900f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 36.dp)
        ) {
            Spacer(modifier = Modifier.height(100.dp))

            // App icon glow
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            Accent.copy(alpha = 0.14f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
                Text("🧘", fontSize = 50.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App name
            Text(
                text = "The Secret Place",
                fontFamily = SerifDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = appTitleSize,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Scripture
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(1.dp)
                    .background(Accent.copy(alpha = 0.50f))
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "\u201CHe who dwells in the secret place\nof the Most High shall abide under\nthe shadow of the Almighty.\u201D",
                fontFamily = SerifDisplay,
                fontWeight = FontWeight.Light,
                fontStyle = FontStyle.Italic,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.78f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "— Psalm 91:1",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Accent,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Name entry
            Text(
                text = "What should we call you?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                placeholder = { Text("Your name", color = Color.White.copy(alpha = 0.35f)) },
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontSize = 17.sp
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canContinue) onComplete(nameInput.trim())
                    }
                ),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Accent.copy(alpha = 0.7f),
                    unfocusedBorderColor = Accent.copy(alpha = 0.35f),
                    focusedContainerColor = Color.White.copy(alpha = 0.10f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.10f),
                    cursorColor = Accent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // CTA
            GoldButton(
                title = "Enter the Secret Place",
                enabled = canContinue,
                onClick = { if (canContinue) onComplete(nameInput.trim()) },
                modifier = Modifier.alpha(if (canContinue) 1f else 0.45f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Legal
            Text(
                text = "By continuing you agree to our Privacy Policy & Terms",
                fontSize = 10.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.40f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
