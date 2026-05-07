package com.thesecretplace.app.ui.screens.journal

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thesecretplace.app.data.local.JournalEntity
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun JournalScreen(viewModel: JournalViewModel = hiltViewModel()) {
    val entries by viewModel.entries.collectAsState()
    var expandedId by remember { mutableStateOf<String?>(null) }
    var entryToDelete by remember { mutableStateOf<JournalEntity?>(null) }

    // Delete confirmation dialog
    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete this reflection?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    entryToDelete?.let { viewModel.deleteEntry(it) }
                    entryToDelete = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel", color = Cream) }
            },
            containerColor = Surface,
            titleContentColor = Cream,
            textContentColor = Cream.copy(alpha = 0.7f)
        )
    }

    ThemedBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                "Journal",
                fontFamily = SerifDisplay,
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                color = Color.White,
                modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 8.dp)
            )

            if (entries.isEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(contentAlignment = Alignment.Center) {
                            Box(Modifier.size(72.dp).background(Accent.copy(0.10f), CircleShape))
                            Icon(Icons.Default.MenuBook, null, tint = Accent.copy(0.65f), modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.height(18.dp))
                        Text("No Reflections Yet", fontFamily = SerifDisplay, fontSize = 22.sp, color = Color.White)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "After completing a meditation,\ntap Reflect to journal your thoughts.",
                            fontSize = 14.sp, color = Color.White.copy(0.50f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        val isExpanded = expandedId == entry.id
                        JournalEntryCard(
                            entry = entry,
                            isExpanded = isExpanded,
                            onTap = { expandedId = if (isExpanded) null else entry.id },
                            onDelete = { entryToDelete = entry }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntity,
    isExpanded: Boolean,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(entry.date) {
        val sdf = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)
        sdf.format(Date(entry.date))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CardRadius))
            .background(Surface.copy(0.68f))
            .clickable(onClick = onTap)
            .animateContentSize()
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.meditationTitle, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(dateStr, fontSize = 12.sp, fontWeight = FontWeight.Light, color = Color.White.copy(0.48f))
            }
            Icon(
                Icons.Default.ExpandMore, null,
                tint = Accent.copy(0.6f),
                modifier = Modifier.size(12.dp).rotate(if (isExpanded) 180f else 0f)
            )
        }

        Spacer(Modifier.height(14.dp))
        ThemeDivider()
        Spacer(Modifier.height(14.dp))

        Text(
            entry.prompt,
            fontFamily = SerifDisplay,
            fontSize = 14.sp,
            fontStyle = FontStyle.Italic,
            color = Lavender,
            lineHeight = 20.sp
        )

        if (entry.text.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                entry.text,
                fontSize = 14.sp,
                color = Color.White.copy(0.78f),
                lineHeight = 22.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (isExpanded) {
            Spacer(Modifier.height(14.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = ErrorRed.copy(0.7f), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", color = ErrorRed.copy(0.7f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
