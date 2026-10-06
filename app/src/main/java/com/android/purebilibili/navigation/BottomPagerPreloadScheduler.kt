package com.android.purebilibili.navigation

import com.android.purebilibili.feature.home.components.BottomNavItem
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive

/** The caller cancels this work whenever navigation, card motion or feed scrolling begins. */
internal suspend fun preloadBottomPagerPages(
    visibleItems: List<BottomNavItem>,
    preloadedItems: () -> Set<BottomNavItem>,
    awaitFrame: suspend () -> Unit,
    onPreload: (BottomNavItem) -> Unit,
) {
    delay(BOTTOM_PAGER_PRELOAD_IDLE_MILLIS)
    while (nextBottomPagerPreloadItem(visibleItems, preloadedItems()) != null) {
        awaitFrame()
        currentCoroutineContext().ensureActive()
        // The user may have visited a tab while waiting for the frame; sample again.
        val item = nextBottomPagerPreloadItem(visibleItems, preloadedItems()) ?: break
        onPreload(item)
        delay(BOTTOM_PAGER_PRELOAD_INTERVAL_MILLIS)
    }
}
