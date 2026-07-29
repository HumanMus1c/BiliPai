package com.android.purebilibili.navigation

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.math.abs

internal class MainBottomPagerState(
    val pagerState: PagerState,
    private val coroutineScope: CoroutineScope
) {
    var selectedPage by mutableIntStateOf(pagerState.currentPage)
        private set

    var isNavigating by mutableStateOf(false)
        private set

    var navigationStartPage by mutableIntStateOf(pagerState.currentPage)
        private set

    private var navJob: Job? = null

    fun switchToPage(targetIndex: Int) {
        val lastPage = pagerState.pageCount - 1
        if (lastPage < 0) return
        val safeTargetIndex = targetIndex.coerceIn(0, lastPage)
        if (safeTargetIndex == selectedPage) return

        val previousJob = navJob
        navJob = null
        previousJob?.cancel()

        navigationStartPage = pagerState.currentPage
        selectedPage = safeTargetIndex
        isNavigating = true

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                previousJob?.join()
                awaitScrollIdle()
                awaitNextFrame()
                if (!animatePageChange(safeTargetIndex)) {
                    pagerState.scrollToPage(safeTargetIndex)
                }
                delay(BOTTOM_TAB_RENDER_BUDGET_HOLD_MILLIS)
            } catch (_: IllegalStateException) {
                // Pager 在测量竞争期间可能拒绝切页，保持当前页并避免快速点击闪退。
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    selectedPage = pagerState.currentPage
                    navigationStartPage = pagerState.currentPage
                    navJob = null
                }
            }
        }
    }

    fun syncPage() {
        if (!isNavigating && selectedPage != pagerState.currentPage) {
            selectedPage = pagerState.currentPage
        }
    }

    private suspend fun animatePageChange(targetIndex: Int): Boolean {
        val layoutInfo = pagerState.layoutInfo
        if (layoutInfo.pageSize <= 0) return false

        val currentPage = pagerState.currentPage
        if (targetIndex == currentPage && abs(pagerState.currentPageOffsetFraction) < 0.001f) {
            return false
        }

        val durationMillis = resolveBottomPagerNavigationDurationMillis(
            pageDistance = abs(targetIndex - currentPage)
        ).coerceAtMost(BOTTOM_PAGER_ANIMATED_SCROLL_MAX_MILLIS)
        pagerState.run {
            animateScrollToPage(
                page = targetIndex,
                animationSpec = tween(
                    durationMillis = durationMillis,
                    easing = LinearOutSlowInEasing,
                ),
            )
        }
        return true
    }

    private suspend fun awaitScrollIdle() {
        if (pagerState.isScrollInProgress) {
            snapshotFlow { pagerState.isScrollInProgress }.first { !it }
        }
    }

    private suspend fun awaitNextFrame() {
        withFrameNanos { }
    }
}

private const val BOTTOM_PAGER_ANIMATED_SCROLL_MAX_MILLIS = 280

@Composable
internal fun rememberMainBottomPagerState(
    pagerState: PagerState,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): MainBottomPagerState {
    return remember(pagerState, coroutineScope) {
        MainBottomPagerState(
            pagerState = pagerState,
            coroutineScope = coroutineScope
        )
    }
}
