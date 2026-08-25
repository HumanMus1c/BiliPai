package com.android.purebilibili.core.util

/**
 * 统一平板导航模式判定，避免不同页面断点不一致导致底栏/侧栏显示冲突。
 * 折叠屏 Book/Tabletop 姿态下强制禁用侧栏，防止铰链遮挡导航。
 */
internal fun shouldUseSidebarNavigationForLayout(
    windowSizeClass: WindowSizeClass,
    tabletUseSidebar: Boolean,
    foldPosture: AppFoldPosture = AppFoldPosture.None
): Boolean {
    if (foldPosture == AppFoldPosture.Book || foldPosture == AppFoldPosture.Tabletop) {
        return false
    }
    return tabletUseSidebar && windowSizeClass.shouldUseSideNavigation
}

/**
 * 首页侧边抽屉仅在底栏导航模式下启用，避免与平板侧栏模式叠层冲突。
 */
internal fun shouldEnableHomeDrawer(useSideNavigation: Boolean): Boolean {
    return !useSideNavigation
}

/**
 * 是否使用展开式 NavigationRail（仅 Large/ExtraLarge 且无折叠姿态）。
 */
internal fun shouldUseExpandedNavigationRailForLayout(
    windowSizeClass: WindowSizeClass,
    foldPosture: AppFoldPosture = AppFoldPosture.None
): Boolean {
    if (foldPosture == AppFoldPosture.Book || foldPosture == AppFoldPosture.Tabletop) {
        return false
    }
    return windowSizeClass.shouldUseExpandedNavigationRail
}
