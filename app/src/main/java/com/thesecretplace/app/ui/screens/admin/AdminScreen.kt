package com.thesecretplace.app.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.thesecretplace.app.model.Meditation
import com.thesecretplace.app.model.MeditationCategory
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.theme.*

@Composable
fun AdminScreen(viewModel: AdminViewModel = hiltViewModel()) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val loginError by viewModel.loginError.collectAsState()
    val uploadResult by viewModel.uploadResult.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()
    val cloudMeditations by viewModel.cloudMeditations.collectAsState()
    val editResult by viewModel.editResult.collectAsState()
    val deleteResult by viewModel.deleteResult.collectAsState()

    // Edit sheet state
    var editingMeditation by remember { mutableStateOf<Meditation?>(null) }
    // Delete confirmation state
    var meditationToDelete by remember { mutableStateOf<Meditation?>(null) }

    ThemedBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            SectionHeader("Admin Panel")
            Spacer(Modifier.height(6.dp))

            if (!isLoggedIn) {
                LoginPanel(viewModel, loginError)
            } else {
                UploadPanel(viewModel, uploadResult, isUploading)

                Spacer(Modifier.height(28.dp))

                // Published meditations management
                ManagementSection(
                    meditations = cloudMeditations,
                    deleteResult = deleteResult,
                    onEdit = { editingMeditation = it },
                    onDelete = { meditationToDelete = it }
                )
            }
        }
    }

    // Edit bottom sheet
    editingMeditation?.let { meditation ->
        EditMeditationSheet(
            meditation = meditation,
            editResult = editResult,
            onSave = { title, desc, dur, cat, secCat, img, isNew ->
                viewModel.updateMeditation(meditation.id, title, desc, dur, cat, secCat, img, isNew)
            },
            onDismiss = {
                editingMeditation = null
                viewModel.clearEditResult()
            }
        )
    }

    // Delete confirmation dialog
    meditationToDelete?.let { meditation ->
        AlertDialog(
            onDismissRequest = { meditationToDelete = null },
            title = { Text("Delete this meditation?", color = Cream) },
            text = {
                Text(
                    "This will remove \"${meditation.title}\" and its audio for all users. This cannot be undone.",
                    color = Cream.copy(alpha = 0.7f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMeditation(meditation.id)
                    meditationToDelete = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { meditationToDelete = null }) {
                    Text("Cancel", color = Cream)
                }
            },
            containerColor = Surface,
            titleContentColor = Cream,
            textContentColor = Cream.copy(alpha = 0.7f)
        )
    }
}

// Reusable admin text field colors
@Composable
private fun adminFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedContainerColor = Color.White.copy(alpha = 0.08f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.08f),
    focusedBorderColor = Lavender.copy(alpha = 0.25f),
    unfocusedBorderColor = Lavender.copy(alpha = 0.12f),
    cursorColor = Accent
)

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Accent.copy(alpha = 0.7f),
        letterSpacing = 1.6.sp
    )
}

// ── Login Panel ──

@Composable
private fun LoginPanel(viewModel: AdminViewModel, loginError: String?) {
    Text("The Secret Place", fontFamily = SerifDisplay, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
    Text("Sign in to publish new meditations", fontSize = 14.sp, color = Color.White.copy(alpha = 0.45f))
    Spacer(Modifier.height(28.dp))

    PremiumCard {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            var email by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("EMAIL")
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    placeholder = { Text("cassia@example.com", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    colors = adminFieldColors()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("PASSWORD")
                OutlinedTextField(
                    value = password, onValueChange = { password = it },
                    placeholder = { Text("••••••••", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    colors = adminFieldColors()
                )
            }

            loginError?.let {
                Text(it, fontSize = 12.sp, color = ErrorRed.copy(alpha = 0.8f))
            }

            GoldButton("Sign In") { viewModel.signIn(email, password) }
        }
    }
}

// ── Upload Panel ──

@Composable
private fun UploadPanel(viewModel: AdminViewModel, uploadResult: String?, isUploading: Boolean) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MeditationCategory.MORNING) }
    var secondaryCategory by remember { mutableStateOf<MeditationCategory?>(null) }
    var imageName by remember { mutableStateOf("morningBeach") }
    var isNew by remember { mutableStateOf(true) }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var audioFilename by remember { mutableStateOf("") }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            audioUri = it
            audioFilename = uri.lastPathSegment?.substringAfterLast("/") ?: "audio.mp3"
            if (title.isEmpty()) {
                title = audioFilename
                    .removeSuffix(".mp3")
                    .replace("-", " ")
                    .replace("_", " ")
            }
        }
    }

    // Header
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text("Upload Meditation", fontFamily = SerifDisplay, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.White)
            Text("Signed in as ${viewModel.currentUserEmail ?: ""}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.35f))
        }
        TextButton(onClick = { viewModel.signOut() }) {
            Text("Sign Out", color = Accent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }

    Spacer(Modifier.height(20.dp))

    // Audio file picker
    PremiumCard {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FieldLabel("AUDIO FILE")

            if (audioUri != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Default.MusicNote, null, tint = Accent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(audioFilename, fontSize = 14.sp, color = Color.White, modifier = Modifier.weight(1f), maxLines = 1)
                    TextButton(onClick = { filePicker.launch("audio/*") }) {
                        Text("Change", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Accent.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .clickable { filePicker.launch("audio/*") }
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.UploadFile, null, tint = Accent, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Choose MP3 File", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.White)
                            Text("Tap to open file picker", fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(16.dp))

    // Metadata form
    PremiumCard {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("TITLE")
                OutlinedTextField(
                    value = title, onValueChange = { title = it },
                    placeholder = { Text("e.g. Morning Calm", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = adminFieldColors()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("DESCRIPTION")
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    placeholder = { Text("Describe the meditation…", color = Color.White.copy(alpha = 0.3f)) },
                    maxLines = 4, modifier = Modifier.fillMaxWidth(), colors = adminFieldColors()
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("DURATION (e.g. 8 min)")
                OutlinedTextField(
                    value = duration, onValueChange = { duration = it },
                    placeholder = { Text("8 min", color = Color.White.copy(alpha = 0.3f)) },
                    singleLine = true, modifier = Modifier.fillMaxWidth(), colors = adminFieldColors()
                )
            }

            // Category picker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("CATEGORY")
                CategoryPicker(selected = category, onSelect = { category = it })
            }

            // Secondary category
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("ALSO SHOW IN (optional)")
                CategoryPicker(
                    selected = secondaryCategory,
                    onSelect = { secondaryCategory = if (it == secondaryCategory) null else it },
                    allowNone = true
                )
            }

            // Cover image picker
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FieldLabel("COVER IMAGE")
                ImagePicker(selected = imageName, onSelect = { imageName = it })
            }

            // Mark as New toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mark as New", fontSize = 14.sp, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.weight(1f))
                Switch(checked = isNew, onCheckedChange = { isNew = it }, colors = SwitchDefaults.colors(checkedTrackColor = Accent))
            }
        }
    }

    // Upload result
    uploadResult?.let {
        Spacer(Modifier.height(16.dp))
        Text(it, fontSize = 14.sp, color = if (it.startsWith("✓")) Accent else ErrorRed.copy(alpha = 0.8f))
    }

    Spacer(Modifier.height(16.dp))

    val canUpload = audioUri != null && title.isNotBlank() && description.isNotBlank() && duration.isNotBlank() && !isUploading

    GoldButton(
        title = if (isUploading) "Uploading…" else "Publish Meditation",
        enabled = canUpload
    ) {
        audioUri?.let { uri ->
            val data = context.contentResolver.openInputStream(uri)?.readBytes()
            if (data != null) {
                viewModel.uploadMeditation(
                    title = title, description = description, duration = duration,
                    category = category, secondaryCategory = secondaryCategory,
                    imageName = imageName, isNew = isNew, audioData = data
                )
                // Reset form on success (viewModel handles upload result)
                title = ""; description = ""; duration = ""
                secondaryCategory = null; isNew = true
                audioUri = null; audioFilename = ""
            }
        }
    }
}

// ── Category Picker ──

@Composable
private fun CategoryPicker(
    selected: MeditationCategory?,
    onSelect: (MeditationCategory) -> Unit,
    allowNone: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = selected?.displayName ?: "None"

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White.copy(alpha = 0.08f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(displayText, color = selected?.color ?: Color.White.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, null, tint = Color.White.copy(alpha = 0.5f))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Surface)
        ) {
            if (allowNone) {
                DropdownMenuItem(
                    text = { Text("None", color = if (selected == null) Accent else Color.White.copy(alpha = 0.7f)) },
                    onClick = { onSelect(selected ?: MeditationCategory.MORNING); expanded = false }
                )
            }
            MeditationCategory.entries.forEach { cat ->
                DropdownMenuItem(
                    text = { Text(cat.displayName, color = if (cat == selected) cat.color else Color.White.copy(alpha = 0.7f)) },
                    onClick = { onSelect(cat); expanded = false }
                )
            }
        }
    }
}

// ── Image Picker ──

// Store in iOS format (camelCase/hyphenated) — Android converts on read via SupabaseRepository
private val imageOptions = listOf(
    "morningBeach", "beach", "feather", "rock-stack", "rock-image",
    "floatingWaterCandles", "bible-coffeecup", "misty-forest", "starry-lake",
    "calm-lake", "sunrise-trees", "bible-light", "flowing-stream",
    "rain-window", "prayer-hands", "lavender-field", "purple-sunset"
)

// Convert iOS-format image name to Android drawable name
private fun toAndroidDrawableName(iosName: String): String {
    return iosName
        .replace("-", "_")
        .replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }
        .lowercase()
}

@Composable
private fun ImagePicker(selected: String, onSelect: (String) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        imageOptions.forEach { name ->
            val drawableName = toAndroidDrawableName(name)
            val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
            if (resId != 0) {
                Image(
                    painter = painterResource(resId),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .then(
                            if (selected == name)
                                Modifier.border(2.dp, Accent, RoundedCornerShape(10.dp))
                            else Modifier
                        )
                        .clickable { onSelect(name) }
                )
            }
        }
    }
}

// ── Management Section ──

@Composable
private fun ManagementSection(
    meditations: List<Meditation>,
    deleteResult: String?,
    onEdit: (Meditation) -> Unit,
    onDelete: (Meditation) -> Unit
) {
    SectionHeader("Published Meditations")
    Spacer(Modifier.height(12.dp))

    deleteResult?.let {
        Text(it, fontSize = 12.sp, color = if (it.startsWith("✓")) Accent else ErrorRed.copy(alpha = 0.8f))
        Spacer(Modifier.height(8.dp))
    }

    if (meditations.isEmpty()) {
        Text("No cloud meditations yet.", fontSize = 14.sp, color = Color.White.copy(alpha = 0.4f))
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            meditations.forEach { meditation ->
                val context = LocalContext.current
                PremiumCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        // Thumbnail
                        val resId = context.resources.getIdentifier(
                            meditation.imageName, "drawable", context.packageName
                        )
                        if (resId != 0) {
                            Image(
                                painter = painterResource(resId),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.width(12.dp))
                        }

                        // Info
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    meditation.title, fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold, color = Color.White,
                                    maxLines = 1
                                )
                                if (meditation.isNew) {
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "NEW", fontSize = 8.sp, fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .background(Accent, RoundedCornerShape(50))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                "${meditation.category.displayName} · ${meditation.duration}",
                                fontSize = 12.sp, color = Color.White.copy(alpha = 0.45f)
                            )
                        }

                        // Edit
                        IconButton(onClick = { onEdit(meditation) }) {
                            Icon(Icons.Default.Edit, "Edit", tint = Accent, modifier = Modifier.size(22.dp))
                        }

                        // Delete
                        IconButton(onClick = { onDelete(meditation) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = ErrorRed.copy(alpha = 0.7f), modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(40.dp))
}

// ── Edit Meditation Sheet ──

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMeditationSheet(
    meditation: Meditation,
    editResult: String?,
    onSave: (String, String, String, MeditationCategory, MeditationCategory?, String, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(meditation.title) }
    var description by remember { mutableStateOf(meditation.description) }
    var duration by remember { mutableStateOf(meditation.duration) }
    var category by remember { mutableStateOf(meditation.category) }
    var secondaryCategory by remember { mutableStateOf(meditation.secondaryCategory) }
    // Find the iOS-format image name that matches this meditation's Android-format name
    var imageName by remember {
        val androidName = meditation.imageName
        val iosMatch = imageOptions.find { toAndroidDrawableName(it) == androidName }
        mutableStateOf(iosMatch ?: androidName)
    }
    var isNew by remember { mutableStateOf(meditation.isNew) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Edit Meditation", fontFamily = SerifDisplay, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)

            PremiumCard {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FieldLabel("TITLE")
                        OutlinedTextField(value = title, onValueChange = { title = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = adminFieldColors())
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FieldLabel("DESCRIPTION")
                        OutlinedTextField(value = description, onValueChange = { description = it }, maxLines = 4, modifier = Modifier.fillMaxWidth(), colors = adminFieldColors())
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FieldLabel("DURATION")
                        OutlinedTextField(value = duration, onValueChange = { duration = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), colors = adminFieldColors())
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FieldLabel("CATEGORY")
                        CategoryPicker(selected = category, onSelect = { category = it })
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FieldLabel("ALSO SHOW IN (optional)")
                        CategoryPicker(selected = secondaryCategory, onSelect = { secondaryCategory = if (it == secondaryCategory) null else it }, allowNone = true)
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        FieldLabel("COVER IMAGE")
                        ImagePicker(selected = imageName, onSelect = { imageName = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Text("Mark as New", fontSize = 14.sp, color = Color.White.copy(alpha = 0.75f), modifier = Modifier.weight(1f))
                        Switch(checked = isNew, onCheckedChange = { isNew = it }, colors = SwitchDefaults.colors(checkedTrackColor = Accent))
                    }
                }
            }

            editResult?.let {
                Text(it, fontSize = 14.sp, color = if (it.startsWith("✓")) Accent else ErrorRed.copy(alpha = 0.8f))
            }

            GoldButton(title = "Save Changes", enabled = title.isNotBlank()) {
                onSave(title, description, duration, category, secondaryCategory, imageName, isNew)
            }
        }
    }
}
