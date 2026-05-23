package com.thesecretplace.app.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.thesecretplace.app.data.local.JournalEntity
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.theme.*
import com.thesecretplace.app.util.HapticUtil
import java.util.UUID
import kotlin.math.abs

// Reflection prompt sheet shown as a modal bottom sheet after meditation completion.
// Matches the iOS ReflectionPromptSheet behavior.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionBottomSheet(
    meditationId: String,
    meditationTitle: String,
    onSave: (JournalEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val view = androidx.compose.ui.platform.LocalView.current
    var reflectionText by remember { mutableStateOf("") }
    val canSave = reflectionText.isNotBlank()

    val prompts = listOf(
        "What did you notice in your body during this session?",
        "What is one thing you want to carry with you from this time?",
        "How do you feel compared to when you started?",
        "What is God saying to you today?",
        "Where did your mind wander? What does that tell you?",
        "What tension are you releasing? What are you holding onto?",
        "Write a one-sentence prayer from this moment."
    )
    val prompt = prompts[abs(meditationId.hashCode()) % prompts.size]

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Black.copy(alpha = 0.95f),
        dragHandle = null
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar with Skip and Save
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Skip", fontSize = 14.sp, color = Color.White.copy(alpha = 0.50f))
                }

                Text(
                    "Reflect",
                    fontFamily = SerifDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )

                TextButton(
                    onClick = {
                        HapticUtil.medium(view)
                        val entry = JournalEntity(
                            id = UUID.randomUUID().toString(),
                            date = System.currentTimeMillis(),
                            meditationTitle = meditationTitle,
                            prompt = prompt,
                            text = reflectionText.trim()
                        )
                        onSave(entry)
                        onDismiss()
                    },
                    enabled = canSave
                ) {
                    Text(
                        "Save",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (canSave) Accent else Accent.copy(alpha = 0.35f)
                    )
                }
            }

            ThemeDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 22.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Prompt card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CardRadius))
                        .background(Surface.copy(alpha = 0.65f))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome, null,
                            tint = Accent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "TODAY'S PROMPT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Accent,
                            letterSpacing = 1.5.sp
                        )
                    }

                    Text(
                        prompt,
                        fontFamily = SerifDisplay,
                        fontSize = 19.sp,
                        color = Color.White,
                        lineHeight = 28.sp
                    )
                }

                // Text editor area
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "YOUR REFLECTION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.45f),
                        letterSpacing = 2.sp
                    )

                    OutlinedTextField(
                        value = reflectionText,
                        onValueChange = { reflectionText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 190.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Accent,
                            focusedBorderColor = Lavender.copy(alpha = 0.18f),
                            unfocusedBorderColor = Lavender.copy(alpha = 0.18f),
                            focusedContainerColor = Surface.copy(alpha = 0.60f),
                            unfocusedContainerColor = Surface.copy(alpha = 0.60f)
                        ),
                        shape = RoundedCornerShape(CardRadius),
                        placeholder = {
                            Text(
                                "Write your thoughts...",
                                color = Color.White.copy(alpha = 0.30f)
                            )
                        }
                    )
                }
            }
        }
    }
}
