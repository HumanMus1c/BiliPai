package com.android.purebilibili.feature.settings

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.resolveBottomSafeAreaPadding

internal enum class SettingsBackTarget {
    NONE,
    CACHE_ANIMATION,
    CACHE_DIALOG,
    PATH_DIALOG,
    IMAGE_SAVE_PATH_DIALOG,
    EASTER_EGG_DIALOG,
    DONATE_DIALOG,
    RELEASE_DISCLAIMER_DIALOG,
    UPDATE_RESULT,
    CHANGELOG_RESULT,
    BLOCKED_LIST,
}

internal fun resolveSettingsBackTarget(
    showCacheAnimation: Boolean = false,
    showCacheDialog: Boolean = false,
    showPathDialog: Boolean = false,
    showImageSavePathDialog: Boolean = false,
    showEasterEggDialog: Boolean = false,
    showDonateDialog: Boolean = false,
    showReleaseDisclaimerDialog: Boolean = false,
    showUpdateResult: Boolean = false,
    showChangelogResult: Boolean = false,
    showBlockedList: Boolean = false,
): SettingsBackTarget = when {
    showBlockedList -> SettingsBackTarget.BLOCKED_LIST
    showChangelogResult -> SettingsBackTarget.CHANGELOG_RESULT
    showUpdateResult -> SettingsBackTarget.UPDATE_RESULT
    showReleaseDisclaimerDialog -> SettingsBackTarget.RELEASE_DISCLAIMER_DIALOG
    showDonateDialog -> SettingsBackTarget.DONATE_DIALOG
    showEasterEggDialog -> SettingsBackTarget.EASTER_EGG_DIALOG
    showImageSavePathDialog -> SettingsBackTarget.IMAGE_SAVE_PATH_DIALOG
    showPathDialog -> SettingsBackTarget.PATH_DIALOG
    showCacheDialog && !showCacheAnimation -> SettingsBackTarget.CACHE_DIALOG
    showCacheAnimation -> SettingsBackTarget.CACHE_ANIMATION
    else -> SettingsBackTarget.NONE
}

internal fun resolveSettingsBottomBarReservedPadding(
    bottomBarVisible: Boolean,
    isBottomBarFloating: Boolean,
    bottomBarLabelMode: Int,
    isTablet: Boolean
): Dp {
    if (!bottomBarVisible) return 0.dp

    val floatingBodyHeight = when (bottomBarLabelMode) {
        0 -> if (isTablet) 76.dp else 70.dp
        2 -> if (isTablet) 56.dp else 54.dp
        else -> if (isTablet) 68.dp else 62.dp
    }
    val dockedBodyHeight = when (bottomBarLabelMode) {
        0 -> 72.dp
        2 -> if (isTablet) 52.dp else 56.dp
        else -> 64.dp
    }
    val floatingInset = if (isBottomBarFloating) {
        if (isTablet) 20.dp else 16.dp
    } else {
        0.dp
    }

    return if (isBottomBarFloating) {
        floatingBodyHeight + floatingInset + 12.dp
    } else {
        dockedBodyHeight + 12.dp
    }
}

internal fun resolveSettingsContentBottomPadding(
    navigationBarsBottom: Dp,
    bottomBarVisible: Boolean,
    isBottomBarFloating: Boolean,
    bottomBarLabelMode: Int,
    isTablet: Boolean,
    extraBottomPadding: Dp = 28.dp
): Dp {
    return resolveBottomSafeAreaPadding(
        navigationBarsBottom = navigationBarsBottom,
        extraBottomPadding = extraBottomPadding + resolveSettingsBottomBarReservedPadding(
            bottomBarVisible = bottomBarVisible,
            isBottomBarFloating = isBottomBarFloating,
            bottomBarLabelMode = bottomBarLabelMode,
            isTablet = isTablet
        )
    )
}
