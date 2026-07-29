package com.android.purebilibili.feature.home.components

/** Expanded width class gets the 0.9.3 expandable rail; Medium stays classic fixed. */
fun shouldUseExpandableMiuixSideBar(isExpandedWidthClass: Boolean): Boolean = isExpandedWidthClass

/** Official [top.yukonga.miuix.kmp.basic.NavigationRailItem] cannot host skin bitmaps. */
fun shouldUseMiuixOfficialSideBarItem(skinIconPath: String?): Boolean = skinIconPath == null

enum class HomeSideBarClickAction {
    NAVIGATE,
    HOME_DOUBLE_TAP
}

/**
 * Maps rapid successive Home taps onto the existing scroll-to-top double-tap behavior
 * when the official Miuix rail item only exposes a single [onClick].
 */
fun resolveHomeSideBarClickAction(
    item: BottomNavItem,
    nowMs: Long,
    lastHomeClickMs: Long,
    doubleTapWindowMs: Long = 300L
): HomeSideBarClickAction {
    if (item != BottomNavItem.HOME) return HomeSideBarClickAction.NAVIGATE
    if (lastHomeClickMs > 0L && nowMs - lastHomeClickMs <= doubleTapWindowMs) {
        return HomeSideBarClickAction.HOME_DOUBLE_TAP
    }
    return HomeSideBarClickAction.NAVIGATE
}
