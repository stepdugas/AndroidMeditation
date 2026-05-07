package com.thesecretplace.app.ui.navigation

// All navigation routes in the app
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val MEDITATE = "meditate"
    const val SELAH = "selah"
    const val FAVORITES = "favorites"
    const val LEARN = "learn"
    const val ADMIN = "admin"
    const val SETTINGS = "settings"
    const val JOURNAL = "journal"
    const val PAYWALL = "paywall"

    // Routes with arguments
    const val DETAIL = "detail/{meditationId}"
    const val PLAYER = "player/{meditationId}"
    const val INTENTION = "intention/{meditationId}"

    fun detail(id: String) = "detail/$id"
    fun player(id: String) = "player/$id"
    fun intention(id: String) = "intention/$id"
}

// Bottom navigation tabs
enum class BottomTab(val route: String, val label: String, val iconName: String) {
    HOME(Routes.HOME, "Home", "Home"),
    MEDITATE(Routes.MEDITATE, "Meditate", "SelfImprovement"),
    SELAH(Routes.SELAH, "Selah", "Timer"),
    FAVORITES(Routes.FAVORITES, "Favorites", "Star"),
    LEARN(Routes.LEARN, "Learn", "MenuBook")
}
