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
    fun `ordinary bottom tab selection keeps its existing page motion path`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/MainBottomPagerState.kt")
        val switchNavigationSource = source
            .substringAfter("fun switchToPage(")
            .substringBefore("Applies the system predictive-back progress")

        assertTrue(source.contains("navigationStartPage"))
        assertTrue(switchNavigationSource.contains("pagerState.scrollToPage(safeTargetIndex)"))
        assertTrue(switchNavigationSource.contains("animatePageChange(safeTargetIndex)"))
        assertFalse(switchNavigationSource.contains("pagerState.animateScrollBy("))
        assertFalse(switchNavigationSource.contains("pagerState.animateScrollToPage("))
        assertFalse(switchNavigationSource.contains("tween("))
    }

    @Test
    fun `predictive tab return drives pager progress and settles only the remainder`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/MainBottomPagerState.kt")

        assertTrue(source.contains("suspend fun seekPredictiveReturnToPage("))
        assertTrue(source.contains("pagerState.scroll(scrollPriority = MutatePriority.UserInput)"))
        assertTrue(source.contains("scrollBy(deltaPx)"))
        assertTrue(source.contains("progressDistance = 1f - session.lastProgress"))
        assertTrue(source.contains("progressDistance = session.lastProgress"))
    }

    @Test
    fun `main bottom pager keeps low cost budget after instant switch`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/MainBottomPagerState.kt")
        val switchNavigationSource = source
            .substringAfter("fun switchToPage(")
            .substringBefore("private suspend fun")

        assertCallsInOrder(
            switchNavigationSource,
            "awaitScrollIdle()",
            "awaitNextFrame()",
            "pagerState.scrollToPage(safeTargetIndex)",
            "delay(BOTTOM_TAB_RENDER_BUDGET_HOLD_MILLIS)"
        )
    }

    @Test
    fun `main bottom pager cancels stale switch and reconciles settled page`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/navigation/MainBottomPagerState.kt")
        val switchNavigationSource = source
            .substringAfter("fun switchToPage(")
            .substringBefore("fun syncPage(")

        assertTrue(switchNavigationSource.contains("previousJob?.cancel()"))
        assertTrue(switchNavigationSource.contains("previousJob?.join()"))
        assertTrue(switchNavigationSource.contains("if (navJob == myJob)"))
        assertTrue(switchNavigationSource.contains("selectedPage = pagerState.currentPage"))
        assertTrue(switchNavigationSource.contains("navigationStartPage = pagerState.currentPage"))
    }

    private fun assertCallsInOrder(source: String, vararg calls: String) {
        var previousIndex = -1
        calls.forEach { call ->
            val currentIndex = source.indexOf(call)
            assertTrue(currentIndex > previousIndex, "$call should appear after previous call")
            previousIndex = currentIndex
        }
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
