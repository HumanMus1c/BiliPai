package com.android.purebilibili.navigation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BottomPagerStatePersistenceStructureTest {

    @Test
    fun `bottom tabs are hosted by main horizontal pager state`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt")

        assertTrue(source.contains("BiliPaiNavDisplayHost("))
        assertTrue(source.contains("rememberPagerState("))
        assertTrue(source.contains("rememberMainBottomPagerState("))
        assertTrue(source.contains("HorizontalPager("))
        assertTrue(source.contains("rememberSaveableStateHolder()"))
        assertTrue(source.contains("bottomPagerSaveableStateHolder.SaveableStateProvider("))
        assertTrue(source.contains("resolveBottomPagerSaveableStateKey(slotItem)"))
        assertTrue(source.contains("historyViewModel.loadData("))
        assertTrue(source.contains("isBottomPagerPageActive"))
        assertTrue(source.contains("userScrollEnabled = shouldEnableBottomPagerUserScroll()"))
        assertTrue(source.contains("resolveBottomPagerBeyondViewportPageCount("))
        assertTrue(source.contains("pageCount = visibleBottomBarItems.size"))
        assertTrue(source.contains("contentReady = bottomPagerContentReady"))
        assertTrue(source.contains("resolveBottomPagerRenderBudget(isNavigating = mainBottomPagerState.isNavigating)"))
        assertFalse(source.contains("pendingBottomTabTransitionRoute"))
        assertFalse(source.contains("retainedBottomNavItem"))
        assertFalse(source.contains("resolveBottomTabTransitionTargetRoute"))
        assertFalse(source.contains("VerticalPager("))
    }

    @Test
    fun `bottom tab switch follows user input scroll mutation`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/MainBottomPagerState.kt")
        val switchNavigationSource = source
            .substringAfter("fun switchToPage(")
            .substringBefore("fun syncPage(")

        assertTrue(source.contains("navigationStartPage"))
        assertTrue(switchNavigationSource.contains("pagerState.scroll(MutatePriority.UserInput)"))
        assertTrue(switchNavigationSource.contains("scrollBy(currentValue - previousValue)"))
        assertTrue(switchNavigationSource.contains("easing = EaseInOut"))
        assertTrue(switchNavigationSource.contains("resolveBottomPagerNavigationDurationMillis("))
        assertTrue(switchNavigationSource.contains("pagerState.scrollToPage(safeTargetIndex)"))
        assertFalse(switchNavigationSource.contains("pagerState.animateScrollBy("))
        assertTrue(switchNavigationSource.contains("scrollPixels"))
        // No self-invented absolute seek / predictive progress path.
        assertFalse(source.contains("seekPredictiveReturnToPage"))
        assertFalse(source.contains("dispatchRawDelta"))
        assertFalse(source.contains("commitPredictiveReturnToPage"))
        assertFalse(source.contains("cancelPredictiveReturn"))
    }

    @Test
    fun `main bottom pager reconciles stale switches`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/MainBottomPagerState.kt")
        val switchNavigationSource = source
            .substringAfter("fun switchToPage(")
            .substringBefore("fun syncPage(")

        assertTrue(switchNavigationSource.contains("navJob?.cancel()"))
        assertTrue(switchNavigationSource.contains("if (navJob == job)"))
        assertTrue(switchNavigationSource.contains("selectedPage = pagerState.currentPage"))
        assertTrue(switchNavigationSource.contains("navigationStartPage = pagerState.currentPage"))
        assertFalse(switchNavigationSource.contains("withContext(NonCancellable)"))
        assertFalse(switchNavigationSource.contains("settleLatestNavigation"))
    }

    @Test
    fun `tab back handler wires BiliPai onBackCompleted to switchToPage home`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt")

        assertTrue(source.contains("MainHostTabBackHandler("))
        assertTrue(source.contains("onReturnToHomeTab = {"))
        assertTrue(source.contains("mainBottomPagerState.switchToPage(homeIndex)"))
        assertFalse(source.contains("seekPredictiveReturnToPage"))
        assertFalse(source.contains("commitPredictiveReturnToPage"))
        assertFalse(source.contains("cancelPredictiveReturn"))
        assertFalse(source.contains("onPredictiveProgress"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
