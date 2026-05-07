package com.thesecretplace.app.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thesecretplace.app.data.PreferencesManager
import com.thesecretplace.app.data.sampleMeditations
import com.thesecretplace.app.service.AudioServiceConnection
import com.thesecretplace.app.ui.navigation.BottomTab
import com.thesecretplace.app.ui.navigation.Routes
import com.thesecretplace.app.ui.screens.admin.AdminScreen
import com.thesecretplace.app.ui.screens.detail.DetailScreen
import com.thesecretplace.app.ui.screens.favorites.FavoritesScreen
import com.thesecretplace.app.ui.screens.home.HomeScreen
import com.thesecretplace.app.ui.screens.journal.JournalScreen
import com.thesecretplace.app.ui.screens.learn.LearnScreen
import com.thesecretplace.app.ui.screens.meditate.MeditateScreen
import com.thesecretplace.app.ui.screens.onboarding.OnboardingScreen
import com.thesecretplace.app.ui.screens.paywall.PaywallScreen
import com.thesecretplace.app.ui.screens.player.IntentionScreen
import com.thesecretplace.app.ui.screens.player.PlayerScreen
import com.thesecretplace.app.ui.screens.selah.SelahScreen
import com.thesecretplace.app.ui.screens.settings.SettingsScreen
import com.thesecretplace.app.ui.theme.*

@Composable
fun MainScreen(
    pendingDeepLink: String?,
    onDeepLinkConsumed: () -> Unit,
    viewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val hasSeenWelcome by viewModel.hasSeenWelcome.collectAsState()
    val showPaywall by viewModel.showPaywall.collectAsState()

    // Handle deep links
    LaunchedEffect(pendingDeepLink) {
        if (pendingDeepLink == "meditate") {
            navController.navigate(Routes.MEDITATE) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
            onDeepLinkConsumed()
        }
    }

    // Connect audio service
    LaunchedEffect(Unit) {
        viewModel.connectAudio()
        viewModel.refreshCatalog()
    }

    if (!hasSeenWelcome) {
        OnboardingScreen(onComplete = { name ->
            viewModel.completeOnboarding(name)
        })
    } else {
        // Show paywall dialog
        if (showPaywall) {
            PaywallScreen(
                onDismiss = { viewModel.dismissPaywall() },
                navController = navController
            )
        } else {
            MainScaffold(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun MainScaffold(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val adminUnlocked by viewModel.adminUnlocked.collectAsState()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    // Hide bottom bar only on player and intention (immersive screens)
    val hideNavRoutes = listOf(Routes.PLAYER, Routes.INTENTION)

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (currentRoute !in hideNavRoutes) {
                BottomNavBar(
                    navController = navController,
                    currentRoute = currentRoute,
                    adminUnlocked = adminUnlocked
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundGradient)
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME
            ) {
                composable(Routes.HOME) {
                    HomeScreen(navController = navController)
                }
                composable(Routes.MEDITATE) {
                    MeditateScreen(navController = navController)
                }
                composable(Routes.SELAH) {
                    SelahScreen()
                }
                composable(Routes.FAVORITES) {
                    FavoritesScreen(navController = navController)
                }
                composable(Routes.LEARN) {
                    LearnScreen()
                }
                composable(Routes.ADMIN) {
                    AdminScreen()
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(navController = navController)
                }
                composable(Routes.JOURNAL) {
                    JournalScreen()
                }
                composable(Routes.PAYWALL) {
                    PaywallScreen(
                        onDismiss = { navController.popBackStack() },
                        navController = navController
                    )
                }
                composable(Routes.DETAIL) { backStackEntry ->
                    val meditationId = backStackEntry.arguments?.getString("meditationId") ?: ""
                    DetailScreen(meditationId = meditationId, navController = navController)
                }
                composable(Routes.PLAYER) { backStackEntry ->
                    val meditationId = backStackEntry.arguments?.getString("meditationId") ?: ""
                    PlayerScreen(meditationId = meditationId, navController = navController)
                }
                composable(Routes.INTENTION) { backStackEntry ->
                    val meditationId = backStackEntry.arguments?.getString("meditationId") ?: ""
                    IntentionScreen(
                        meditationId = meditationId,
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(
    navController: NavHostController,
    currentRoute: String?,
    adminUnlocked: Boolean
) {
    NavigationBar(
        containerColor = Surface.copy(alpha = 0.95f),
        contentColor = Cream,
        tonalElevation = 0.dp
    ) {
        val tabs = buildList {
            addAll(BottomTab.entries)
            if (adminUnlocked) add(null) // placeholder for admin
        }

        for (tab in BottomTab.entries) {
            val selected = currentRoute == tab.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = tabIcon(tab),
                        contentDescription = tab.label
                    )
                },
                label = { Text(tab.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    unselectedIconColor = Cream.copy(alpha = 0.5f),
                    unselectedTextColor = Cream.copy(alpha = 0.5f),
                    indicatorColor = Accent.copy(alpha = 0.12f)
                )
            )
        }

        if (adminUnlocked) {
            NavigationBarItem(
                selected = currentRoute == Routes.ADMIN,
                onClick = {
                    navController.navigate(Routes.ADMIN) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                label = { Text("Admin") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Accent,
                    selectedTextColor = Accent,
                    unselectedIconColor = Cream.copy(alpha = 0.5f),
                    unselectedTextColor = Cream.copy(alpha = 0.5f),
                    indicatorColor = Accent.copy(alpha = 0.12f)
                )
            )
        }
    }
}

private fun tabIcon(tab: BottomTab): ImageVector {
    return when (tab) {
        BottomTab.HOME -> Icons.Default.Home
        BottomTab.MEDITATE -> Icons.Default.SelfImprovement
        BottomTab.SELAH -> Icons.Default.Timer
        BottomTab.FAVORITES -> Icons.Default.Star
        BottomTab.LEARN -> Icons.Default.MenuBook
    }
}
