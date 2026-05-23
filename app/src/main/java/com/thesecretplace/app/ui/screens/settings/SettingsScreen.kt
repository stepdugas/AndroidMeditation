package com.thesecretplace.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.thesecretplace.app.ui.components.*
import com.thesecretplace.app.ui.navigation.Routes
import com.thesecretplace.app.ui.theme.*
import androidx.compose.ui.platform.LocalUriHandler

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val uriHandler = LocalUriHandler.current
    val showIntention by viewModel.showIntention.collectAsState()
    val healthEnabled by viewModel.healthEnabled.collectAsState()
    val isRestoring by viewModel.isRestoring.collectAsState()
    val restoreToast by viewModel.restoreToast.collectAsState()
    var adminTapCount by remember { mutableIntStateOf(0) }
    var showAdminToast by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableIntStateOf(viewModel.getReminderHour()) }
    var reminderMinute by remember { mutableIntStateOf(viewModel.getReminderMinute()) }

    ThemedBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            // iOS-style back nav
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                TextButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBackIosNew, null, tint = Accent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Back", color = Accent, fontSize = 16.sp)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp)
            ) {
                Text(
                    "Settings",
                    fontFamily = SerifDisplay,
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

            Spacer(Modifier.height(8.dp))

            // Intention
            SettingsSection("Intention") {
                SettingsToggleRow(Icons.Default.AutoAwesome, Lavender, "Show Intention Screen", showIntention) {
                    viewModel.setShowIntention(it)
                }
                ThemeDivider(Modifier.padding(start = 60.dp))
                SettingsButtonRow(Icons.Default.Refresh, Accent, "Reset My Intention", isDestructive = true) {
                    viewModel.resetIntention()
                }
            }

            Spacer(Modifier.height(30.dp))

            // Reminders
            SettingsSection("Reminders") {
                SettingsNavRow(Icons.Default.Notifications, Lavender, "Set Daily Reminder") {
                    showTimePicker = true
                }
                ThemeDivider(Modifier.padding(start = 60.dp))
                val displayHour = if (reminderHour == 0) 12 else if (reminderHour > 12) reminderHour - 12 else reminderHour
                val amPm = if (reminderHour < 12) "AM" else "PM"
                Text(
                    "Current: %d:%02d %s".format(displayHour, reminderMinute, amPm),
                    fontSize = 12.sp, fontWeight = FontWeight.Light, color = Color.White.copy(0.42f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            Spacer(Modifier.height(30.dp))

            // Journal
            SettingsSection("Journal") {
                SettingsNavRow(Icons.Default.MenuBook, Accent, "My Reflections") {
                    navController.navigate(Routes.JOURNAL)
                }
            }

            Spacer(Modifier.height(30.dp))

            // Favorites
            SettingsSection("Favorites") {
                SettingsButtonRow(Icons.Default.StarBorder, Accent, "Reset All Favorites", isDestructive = true) {
                    viewModel.resetFavorites()
                }
            }

            Spacer(Modifier.height(30.dp))

            // Health
            SettingsSection("Health") {
                SettingsToggleRow(Icons.Default.Favorite, Color(0xFFF25964), "Log Mindful Minutes", healthEnabled) {
                    viewModel.setHealthEnabled(it)
                }
                ThemeDivider(Modifier.padding(start = 60.dp))
                Text(
                    "Completed sessions are saved to Health Connect as mindful minutes.",
                    fontSize = 12.sp, fontWeight = FontWeight.Light, color = Color.White.copy(0.42f),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            Spacer(Modifier.height(30.dp))

            // Share
            SettingsSection("Share") {
                SettingsNavRow(Icons.Default.Share, Color.White, "Share The Secret Place") {
                    com.thesecretplace.app.util.ShareUtil.shareApp(context)
                }
            }

            Spacer(Modifier.height(30.dp))

            // Subscription
            SettingsSection("Subscription") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isRestoring) { viewModel.restorePurchases() }
                        .padding(16.dp)
                ) {
                    SettingsIcon(Icons.Default.Restore, Accent)
                    Spacer(Modifier.width(14.dp))
                    Text("Restore Purchases", fontSize = 16.sp, color = Color.White)
                    Spacer(Modifier.weight(1f))
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White.copy(0.5f),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(0.28f), modifier = Modifier.size(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(30.dp))

            // Legal & Support
            SettingsSection("Legal & Support") {
                SettingsNavRow(Icons.Default.HelpOutline, Lavender, "Support") {
                    uriHandler.openUri("https://thesecretplaceapp.com/support.html")
                }
                ThemeDivider(Modifier.padding(start = 60.dp))
                SettingsNavRow(Icons.Default.Lock, Lavender, "Privacy Policy") {
                    uriHandler.openUri("https://thesecretplaceapp.com/privacy.html")
                }
                ThemeDivider(Modifier.padding(start = 60.dp))
                SettingsNavRow(Icons.Default.Description, Lavender, "Terms of Use") {
                    uriHandler.openUri("https://thesecretplaceapp.com/terms.html")
                }
            }

            Spacer(Modifier.height(30.dp))

            // About
            SettingsSection("About") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            adminTapCount++
                            if (adminTapCount >= 7) {
                                viewModel.unlockAdmin()
                                showAdminToast = true
                            }
                        }
                        .padding(16.dp)
                ) {
                    SettingsIcon(Icons.Default.Info, Lavender)
                    Spacer(Modifier.width(14.dp))
                    Text("Version", fontSize = 16.sp, color = Color.White)
                    Spacer(Modifier.weight(1f))
                    Text("${com.thesecretplace.app.BuildConfig.VERSION_NAME} (${com.thesecretplace.app.BuildConfig.VERSION_CODE})", fontSize = 14.sp, color = Color.White.copy(0.42f))
                }
            }

            if (showAdminToast) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Admin access enabled",
                    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(Accent, RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }

            // Admin panel — only visible after tapping Version 7 times
            if (showAdminToast || viewModel.isAdminUnlocked()) {
                Spacer(Modifier.height(24.dp))
                SettingsSection("Admin") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Routes.ADMIN) }
                            .padding(16.dp)
                    ) {
                        SettingsIcon(Icons.Default.AdminPanelSettings, Accent)
                        Spacer(Modifier.width(14.dp))
                        Text("Admin Panel", fontSize = 16.sp, color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                    }
                }
            }

            if (restoreToast != null) {
                Spacer(Modifier.height(16.dp))
                Text(
                    restoreToast ?: "",
                    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .background(Accent, RoundedCornerShape(50))
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
                LaunchedEffect(restoreToast) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearRestoreToast()
                }
            }

            Spacer(Modifier.height(28.dp))
            }
        }
    }

    // Time picker dialog for daily reminders
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Set Daily Reminder", color = Color.White) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Choose a time for your daily meditation reminder", fontSize = 14.sp, color = Color.White.copy(0.7f))
                    Spacer(Modifier.height(20.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hour picker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Hour", fontSize = 12.sp, color = Color.White.copy(0.5f))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { reminderHour = if (reminderHour <= 0) 23 else reminderHour - 1 }) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Accent)
                                }
                                val displayH = if (reminderHour == 0) 12 else if (reminderHour > 12) reminderHour - 12 else reminderHour
                                Text("%d".format(displayH), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                IconButton(onClick = { reminderHour = if (reminderHour >= 23) 0 else reminderHour + 1 }) {
                                    Icon(Icons.Default.KeyboardArrowUp, null, tint = Accent)
                                }
                            }
                        }
                        Text(":", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp))
                        // Minute picker
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Min", fontSize = 12.sp, color = Color.White.copy(0.5f))
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { reminderMinute = if (reminderMinute <= 0) 55 else reminderMinute - 5 }) {
                                    Icon(Icons.Default.KeyboardArrowDown, null, tint = Accent)
                                }
                                Text("%02d".format(reminderMinute), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                IconButton(onClick = { reminderMinute = if (reminderMinute >= 55) 0 else reminderMinute + 5 }) {
                                    Icon(Icons.Default.KeyboardArrowUp, null, tint = Accent)
                                }
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (reminderHour < 12) "AM" else "PM",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Accent
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setReminder(reminderHour, reminderMinute)
                    showTimePicker = false
                }) {
                    Text("Save", color = Accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = Color.White.copy(0.6f))
                }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        SectionHeader(title, Modifier.padding(horizontal = 4.dp))
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CardRadius))
                .background(Surface.copy(0.65f))
        ) {
            content()
        }
    }
}

@Composable
fun SettingsIcon(icon: ImageVector, color: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(30.dp)
            .background(color.copy(0.15f), RoundedCornerShape(8.dp))
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun SettingsToggleRow(icon: ImageVector, iconColor: Color, label: String, isOn: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        SettingsIcon(icon, iconColor)
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, color = Color.White)
        Spacer(Modifier.weight(1f))
        Switch(checked = isOn, onCheckedChange = onToggle, colors = SwitchDefaults.colors(checkedTrackColor = Lavender))
    }
}

@Composable
fun SettingsButtonRow(icon: ImageVector, iconColor: Color, label: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp)
    ) {
        SettingsIcon(icon, iconColor)
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, color = if (isDestructive) ErrorRed else Color.White)
        Spacer(Modifier.weight(1f))
        if (!isDestructive) Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(0.28f), modifier = Modifier.size(12.dp))
    }
}

@Composable
fun SettingsNavRow(icon: ImageVector, iconColor: Color, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp)
    ) {
        SettingsIcon(icon, iconColor)
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, color = Color.White)
        Spacer(Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(0.28f), modifier = Modifier.size(12.dp))
    }
}
