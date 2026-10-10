package com.android.purebilibili.feature.home

import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import com.android.purebilibili.core.store.CommonListHeaderCollapseMode
import com.android.purebilibili.core.store.HomeBarHideType
import com.android.purebilibili.feature.home.policy.BottomBarVisibilityIntent
import com.android.purebilibili.feature.home.policy.canRevealHomeHeaderForList
import com.android.purebilibili.feature.home.policy.reduceHomePreScroll
import com.android.purebilibili.feature.home.policy.resolveHomeHeaderListIndex
import com.android.purebilibili.feature.home.policy.shouldHandleHomeVerticalPreScroll

@Immutable
internal data class HomeHeaderScrollConfiguration(
    val collapseEnabled: Boolean,
    val collapseDistancePx: Float,
    val collapseTabs: Boolean,
    val bottomBarAutoHideEnabled: Boolean,
    val useSideNavigation: Boolean,
    val liquidGlassEnabled: Boolean,
    val hideType: HomeBarHideType,
)

@Composable
internal fun rememberHomeHeaderScrollConnection(
    configuration: HomeHeaderScrollConfiguration,
    pagerState: PagerState,
    topTabEntries: List<HomeTopTabEntry>,
    activeGridState: LazyStaggeredGridState?,
    subscriptionListState: LazyStaggeredGridState,
    revealLocked: Boolean,
    headerOffsetProvider: () -> Float,
    globalScrollOffset: MutableFloatState,
    onHeaderOffsetChanged: (Float, Boolean) -> Unit,
    onTabsCollapsedChanged: (Boolean) -> Unit,
    onBottomBarVisibleChanged: (Boolean) -> Unit,
): NestedScrollConnection {
    val currentOnHeaderOffsetChanged = rememberUpdatedState(onHeaderOffsetChanged)
    val currentOnTabsCollapsedChanged = rememberUpdatedState(onTabsCollapsedChanged)
    val currentOnBottomBarVisibleChanged = rememberUpdatedState(onBottomBarVisibleChanged)
    return remember(
        configuration,
        pagerState,
        topTabEntries,
        activeGridState,
        subscriptionListState,
        revealLocked,
        headerOffsetProvider,
        globalScrollOffset,
    ) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (revealLocked || !shouldHandleHomeVerticalPreScroll(
                        deltaX = available.x,
                        deltaY = available.y,
                    )
                ) {
                    return Offset.Zero
                }
                val onSubscriptionTab = resolveHomeTopTabEntryOrNull(
                    topTabEntries,
                    pagerState.currentPage,
                ) == HomeTopTabEntry.Subscriptions
                val headerListIndex = resolveHomeHeaderListIndex(
                    displayedEntryIsSubscription = onSubscriptionTab,
                    categoryFirstVisibleIndex = activeGridState?.firstVisibleItemIndex ?: 0,
                    subscriptionFirstVisibleIndex = subscriptionListState.firstVisibleItemIndex,
                )
                val firstItemVisible = canRevealHomeHeaderForList(
                    firstVisibleItemIndex = headerListIndex,
                    listMissing = !onSubscriptionTab && activeGridState == null,
                )
                val scrollUpdate = reduceHomePreScroll(
                    currentHeaderOffsetPx = headerOffsetProvider(),
                    deltaY = available.y,
                    minHeaderOffsetPx = -configuration.collapseDistancePx,
                    canRevealHeader = firstItemVisible,
                    collapseMode = CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY,
                    isHeaderCollapseEnabled = configuration.collapseEnabled,
                    isBottomBarAutoHideEnabled = configuration.bottomBarAutoHideEnabled,
                    useSideNavigation = configuration.useSideNavigation,
                    liquidGlassEnabled = configuration.liquidGlassEnabled,
                    currentGlobalScrollOffset = globalScrollOffset.floatValue,
                    hideType = configuration.hideType,
                    isHeaderRevealLocked = revealLocked,
                )
                currentOnHeaderOffsetChanged.value(
                    scrollUpdate.headerOffsetPx,
                    scrollUpdate.shouldAnimateHeader,
                )
                currentOnTabsCollapsedChanged.value(
                    configuration.collapseTabs && !revealLocked &&
                        (headerListIndex > 0 || headerOffsetProvider() < -0.5f)
                )
                scrollUpdate.globalScrollOffset?.let { globalScrollOffset.floatValue = it }
                when (scrollUpdate.bottomBarVisibilityIntent) {
                    BottomBarVisibilityIntent.SHOW -> currentOnBottomBarVisibleChanged.value(true)
                    BottomBarVisibilityIntent.HIDE -> currentOnBottomBarVisibleChanged.value(false)
                    null -> Unit
                }
                if (headerListIndex == 0 && headerOffsetProvider() >= -0.5f) {
                    currentOnTabsCollapsedChanged.value(false)
                }
                // Observe the feed's scroll without consuming it.
                return Offset.Zero
            }
        }
    }
}
