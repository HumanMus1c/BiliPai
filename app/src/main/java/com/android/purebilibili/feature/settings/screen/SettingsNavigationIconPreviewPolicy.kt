package com.android.purebilibili.feature.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.android.purebilibili.core.ui.AppSemanticIconFamily

private enum class SettingsNavigationIconRole {
    HOME,
    DYNAMIC,
    STORY,
    HISTORY,
    LISTEN_VIDEO,
    PROFILE,
    FAVORITE,
    LIVE,
    WATCH_LATER,
    SETTINGS,
    PLUGINS,
    FOLLOW,
    POPULAR,
    ANIME,
    GAME,
    PARTITION,
    KNOWLEDGE,
    TECH,
}

/** Keeps the configurable-navigation preview unchanged while home runtime uses Miuix. */
internal fun resolveSettingsNavigationPreviewIcon(
    tabId: String,
    iconFamily: AppSemanticIconFamily,
    selected: Boolean = false,
): ImageVector {
    val role = when (tabId.trim().uppercase()) {
        "HOME", "RECOMMEND" -> SettingsNavigationIconRole.HOME
        "DYNAMIC" -> SettingsNavigationIconRole.DYNAMIC
        "STORY" -> SettingsNavigationIconRole.STORY
        "HISTORY" -> SettingsNavigationIconRole.HISTORY
        "LISTEN_VIDEO" -> SettingsNavigationIconRole.LISTEN_VIDEO
        "PROFILE" -> SettingsNavigationIconRole.PROFILE
        "FAVORITE" -> SettingsNavigationIconRole.FAVORITE
        "LIVE" -> SettingsNavigationIconRole.LIVE
        "WATCHLATER", "WATCH_LATER" -> SettingsNavigationIconRole.WATCH_LATER
        "SETTINGS" -> SettingsNavigationIconRole.SETTINGS
        "PLUGINS" -> SettingsNavigationIconRole.PLUGINS
        "FOLLOW" -> SettingsNavigationIconRole.FOLLOW
        "POPULAR" -> SettingsNavigationIconRole.POPULAR
        "ANIME" -> SettingsNavigationIconRole.ANIME
        "GAME" -> SettingsNavigationIconRole.GAME
        "PARTITION" -> SettingsNavigationIconRole.PARTITION
        "KNOWLEDGE" -> SettingsNavigationIconRole.KNOWLEDGE
        "TECH" -> SettingsNavigationIconRole.TECH
        else -> SettingsNavigationIconRole.HOME
    }

    return when (iconFamily) {
        AppSemanticIconFamily.MATERIAL -> when (role) {
            SettingsNavigationIconRole.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
            SettingsNavigationIconRole.DYNAMIC -> if (selected) Icons.Filled.Notifications else Icons.Outlined.NotificationsNone
            SettingsNavigationIconRole.STORY -> if (selected) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircleOutline
            SettingsNavigationIconRole.HISTORY -> if (selected) Icons.Filled.History else Icons.Outlined.History
            SettingsNavigationIconRole.LISTEN_VIDEO -> if (selected) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic
            SettingsNavigationIconRole.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
            SettingsNavigationIconRole.FAVORITE -> if (selected) Icons.Filled.CollectionsBookmark else Icons.Outlined.CollectionsBookmark
            SettingsNavigationIconRole.LIVE -> if (selected) Icons.Filled.LiveTv else Icons.Outlined.LiveTv
            SettingsNavigationIconRole.WATCH_LATER -> if (selected) Icons.Filled.WatchLater else Icons.Outlined.WatchLater
            SettingsNavigationIconRole.SETTINGS -> if (selected) Icons.Filled.Settings else Icons.Outlined.Settings
            SettingsNavigationIconRole.PLUGINS -> if (selected) Icons.Filled.Extension else Icons.Outlined.Extension
            SettingsNavigationIconRole.FOLLOW -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
            SettingsNavigationIconRole.POPULAR -> if (selected) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Outlined.TrendingUp
            SettingsNavigationIconRole.ANIME -> if (selected) Icons.Filled.Tv else Icons.Outlined.Tv
            SettingsNavigationIconRole.GAME -> if (selected) Icons.Filled.SportsEsports else Icons.Outlined.SportsEsports
            SettingsNavigationIconRole.PARTITION -> if (selected) Icons.Filled.GridView else Icons.Outlined.GridView
            SettingsNavigationIconRole.KNOWLEDGE -> if (selected) Icons.Filled.Lightbulb else Icons.Outlined.Lightbulb
            SettingsNavigationIconRole.TECH -> if (selected) Icons.Filled.SmartToy else Icons.Outlined.SmartToy
        }
    }
}
