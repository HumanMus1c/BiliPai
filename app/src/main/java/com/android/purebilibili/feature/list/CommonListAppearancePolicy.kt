package com.android.purebilibili.feature.list

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.store.CommonListHeaderCollapseMode
import com.android.purebilibili.core.store.HomeHeaderCollapseMode
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.resolveHomeHeaderBlurEnabled
import com.android.purebilibili.core.ui.AppTopChromePolicy
import com.android.purebilibili.core.ui.AppTopTabPresentation

internal data class CommonListVideoCardAppearance(
    val glassEnabled: Boolean,
    val blurEnabled: Boolean,
    val showCoverGlassBadges: Boolean,
    val showInfoGlassBadges: Boolean
)

internal fun resolveCommonListSingleColumnMaxWidth(): Dp = 840.dp

internal fun resolveCommonListGridMinColumnWidth(isExpandedScreen: Boolean): Dp =
    if (isExpandedScreen) 240.dp else 170.dp

internal fun resolveFavoriteSubscribedFolderPreviewWidth(): Dp = 112.dp

internal data class FavoriteProgressBadgeWidthSpec(
    val minWidth: Dp,
    val maxWidth: Dp,
)

internal fun resolveFavoriteProgressBadgeWidthSpec(): FavoriteProgressBadgeWidthSpec =
    FavoriteProgressBadgeWidthSpec(minWidth = 104.dp, maxWidth = 150.dp)

internal data class CommonListFavoriteHeaderLayout(
    val searchBarHeightDp: Int,
    val searchBarHorizontalPaddingDp: Int,
    val searchBarVerticalPaddingDp: Int,
    val browseToggleHeightDp: Int,
    val browseToggleIndicatorHeightDp: Int,
    val browseToggleLabelFontSizeSp: Int,
    val browseToggleHorizontalPaddingDp: Int,
    val browseToggleTopPaddingDp: Int,
    val folderChipMinHeightDp: Int,
    val folderChipHorizontalPaddingDp: Int,
    val folderChipRowHorizontalPaddingDp: Int,
    val folderChipRowTopPaddingDp: Int,
    val folderChipSpacingDp: Int,
    val headerBottomPaddingDp: Int,
    val headerBackgroundAlphaMultiplier: Float
)

internal fun resolveCommonListHeaderBlurEnabled(
    homeSettings: HomeSettings,
): Boolean {
    return resolveHomeHeaderBlurEnabled(
        mode = homeSettings.headerBlurMode,
    )
}

internal fun shouldUseCommonListHeaderLocalBlur(
    headerBlurEnabled: Boolean,
    globalWallpaperVisible: Boolean
): Boolean = headerBlurEnabled && !globalWallpaperVisible

internal fun shouldUseFloatingCommonListHeaderChrome(
    isHistoryPage: Boolean,
    globalLiquidGlassReuseEnabled: Boolean,
): Boolean = isHistoryPage && globalLiquidGlassReuseEnabled

internal fun resolveCommonListViewportTopPadding(headerHeight: Dp): Dp {
    return headerHeight.coerceAtLeast(0.dp)
}

/**
 * 首页式折叠只移走搜索/标题区，保留状态栏安全区与末尾标签 Dock。
 */
internal fun resolveCommonListHeaderMaxCollapsePx(
    headerHeightPx: Int,
    pinnedDockHeightPx: Int,
    topInsetPx: Float,
    retainPinnedDock: Boolean,
): Float {
    if (!retainPinnedDock) return headerHeightPx.coerceAtLeast(0).toFloat()
    return (headerHeightPx - pinnedDockHeightPx - topInsetPx).coerceAtLeast(0f)
}

internal fun resolveCommonListHeaderOffsetPx(
    currentOffsetPx: Float,
    scrollDeltaYPx: Float,
    maxCollapsePx: Float,
    isAtTop: Boolean,
    mode: CommonListHeaderCollapseMode
): Float {
    if (mode == CommonListHeaderCollapseMode.ALWAYS_VISIBLE || maxCollapsePx <= 0f) return 0f
    if (isAtTop && scrollDeltaYPx >= 0f) return 0f
    if (mode == CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY && scrollDeltaYPx > 0f) {
        return currentOffsetPx.coerceIn(-maxCollapsePx, 0f)
    }
    return (currentOffsetPx + scrollDeltaYPx).coerceIn(-maxCollapsePx, 0f)
}

/**
 * 收藏夹在列表反向滚动时不重新展开整个顶栏；只在真正回到顶部时恢复。
 * 历史记录等共用列表仍遵循用户选择的顶栏折叠模式。
 */
internal fun resolveCommonListHeaderCollapseModeForScreen(
    configuredMode: CommonListHeaderCollapseMode,
    isFavoritePage: Boolean,
    isHistoryPage: Boolean = false,
    homeHeaderCollapseMode: HomeHeaderCollapseMode = HomeHeaderCollapseMode.BOTH,
): CommonListHeaderCollapseMode {
    if (isHistoryPage) {
        // 与首页推荐流保持同一展开策略：开启时随列表收起，只有回到顶部才展开；
        // 首页关闭折叠时，历史页也固定显示。
        return if (homeHeaderCollapseMode.hasAnyCollapse) {
            CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY
        } else {
            CommonListHeaderCollapseMode.ALWAYS_VISIBLE
        }
    }
    return if (
        isFavoritePage && configuredMode == CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
    ) {
        CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY
    } else {
        configuredMode
    }
}

internal fun resolveCommonListHeaderOffsetAfterContentScroll(
    currentOffsetPx: Float,
    contentConsumedDeltaYPx: Float,
    maxCollapsePx: Float,
    isAtTop: Boolean,
    mode: CommonListHeaderCollapseMode
): Float = resolveCommonListHeaderOffsetPx(
    currentOffsetPx = currentOffsetPx,
    scrollDeltaYPx = contentConsumedDeltaYPx,
    maxCollapsePx = maxCollapsePx,
    isAtTop = isAtTop,
    mode = mode
)

internal fun resolveCommonListHeaderOffsetForSettledContent(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    maxCollapsePx: Float,
    mode: CommonListHeaderCollapseMode
): Float {
    if (mode == CommonListHeaderCollapseMode.ALWAYS_VISIBLE || maxCollapsePx <= 0f) return 0f
    return if (firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0) {
        0f
    } else {
        -maxCollapsePx
    }
}

internal fun resolveCommonListVideoCardAppearance(
    homeSettings: HomeSettings,
    liquidGlassEnabled: Boolean,
): CommonListVideoCardAppearance {
    val headerBlurEnabled = resolveCommonListHeaderBlurEnabled(
        homeSettings = homeSettings,
    )
    return CommonListVideoCardAppearance(
        glassEnabled = liquidGlassEnabled,
        blurEnabled = headerBlurEnabled || homeSettings.isBottomBarBlurEnabled,
        showCoverGlassBadges = false,
        showInfoGlassBadges = false
    )
}

internal fun resolveCommonListFavoriteHeaderLayout(
    topChromePolicy: AppTopChromePolicy,
): CommonListFavoriteHeaderLayout {
    val compactChrome = topChromePolicy.compactChromeSpec
    return when (topChromePolicy.tabPresentation) {
        AppTopTabPresentation.TONAL_CAPSULE -> {
            CommonListFavoriteHeaderLayout(
                searchBarHeightDp = compactChrome.primaryHeightDp,
                searchBarHorizontalPaddingDp = 16,
                searchBarVerticalPaddingDp = 6,
                browseToggleHeightDp = compactChrome.primaryHeightDp,
                browseToggleIndicatorHeightDp = 30,
                browseToggleLabelFontSizeSp = 14,
                browseToggleHorizontalPaddingDp = 16,
                browseToggleTopPaddingDp = 2,
                folderChipMinHeightDp = compactChrome.chipHeightDp,
                folderChipHorizontalPaddingDp = 12,
                folderChipRowHorizontalPaddingDp = 16,
                folderChipRowTopPaddingDp = 6,
                folderChipSpacingDp = 8,
                headerBottomPaddingDp = 6,
                headerBackgroundAlphaMultiplier = 0.84f
            )
        }
        AppTopTabPresentation.MATERIAL_UNDERLINE -> {
            CommonListFavoriteHeaderLayout(
                searchBarHeightDp = compactChrome.primaryHeightDp,
                searchBarHorizontalPaddingDp = 16,
                searchBarVerticalPaddingDp = 6,
                browseToggleHeightDp = compactChrome.primaryHeightDp,
                browseToggleIndicatorHeightDp = 30,
                browseToggleLabelFontSizeSp = 14,
                browseToggleHorizontalPaddingDp = 16,
                browseToggleTopPaddingDp = 2,
                folderChipMinHeightDp = compactChrome.chipHeightDp,
                folderChipHorizontalPaddingDp = 12,
                folderChipRowHorizontalPaddingDp = 16,
                folderChipRowTopPaddingDp = 6,
                folderChipSpacingDp = 8,
                headerBottomPaddingDp = 6,
                headerBackgroundAlphaMultiplier = 0.86f
            )
        }
        AppTopTabPresentation.MOVING_CAPSULE -> {
            CommonListFavoriteHeaderLayout(
                searchBarHeightDp = compactChrome.primaryHeightDp,
                searchBarHorizontalPaddingDp = 16,
                searchBarVerticalPaddingDp = 6,
                browseToggleHeightDp = compactChrome.primaryHeightDp,
                browseToggleIndicatorHeightDp = 30,
                browseToggleLabelFontSizeSp = 14,
                browseToggleHorizontalPaddingDp = 16,
                browseToggleTopPaddingDp = 2,
                folderChipMinHeightDp = compactChrome.compactChipHeightDp,
                folderChipHorizontalPaddingDp = 11,
                folderChipRowHorizontalPaddingDp = 12,
                folderChipRowTopPaddingDp = 6,
                folderChipSpacingDp = 8,
                headerBottomPaddingDp = 4,
                headerBackgroundAlphaMultiplier = 0.82f
            )
        }
    }
}
