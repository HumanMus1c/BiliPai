package com.android.purebilibili.navigation

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * 底栏 HorizontalPager 状态。切页动画对齐 KernelSU [MainPagerState.animateToPage]：
 * 用 [PagerState.animateScrollBy] 连续滚过中间页，而不是绝对 seek / 闪切。
 */
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

    /**
     * KernelSU `MainPagerState.animateToPage`：
     * - 先更新 [selectedPage]（底栏指示器跟目标页）；
     * - [animateScrollBy] 按像素连续滚到目标，跨多页有翻页滚动过渡。
     */
    fun switchToPage(targetIndex: Int) {
        val lastPage = pagerState.pageCount - 1
        if (lastPage < 0) return
        val safeTargetIndex = targetIndex.coerceIn(0, lastPage)
        if (safeTargetIndex == selectedPage) return

        navJob?.cancel()

        navigationStartPage = pagerState.currentPage
        selectedPage = safeTargetIndex
        isNavigating = true

        val layoutInfo = pagerState.layoutInfo
        val pageSize = layoutInfo.pageSize + layoutInfo.pageSpacing
        if (pageSize <= 0) {
            navJob = coroutineScope.launch {
                val myJob = coroutineContext.job
                try {
                    pagerState.scrollToPage(safeTargetIndex)
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
            return
        }

        val currentDistanceInPages =
            safeTargetIndex - pagerState.currentPage - pagerState.currentPageOffsetFraction
        val scrollPixels = currentDistanceInPages * pageSize
        // KernelSU: duration = 100 * max(distance, 2) + 100
        val duration = resolveBottomPagerNavigationDurationMillis(
            pageDistance = abs(safeTargetIndex - pagerState.currentPage),
        )

        navJob = coroutineScope.launch {
            val myJob = coroutineContext.job
            try {
                pagerState.animateScrollBy(
                    value = scrollPixels,
                    animationSpec = tween(
                        easing = EaseInOut,
                        durationMillis = duration,
                    ),
                )
            } catch (_: IllegalStateException) {
                // Pager 在测量竞争期间可能拒绝强制滚动，避免底栏快速切换直接闪退。
            } finally {
                if (navJob == myJob) {
                    isNavigating = false
                    if (pagerState.currentPage != safeTargetIndex) {
                        selectedPage = pagerState.currentPage
                    }
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
}

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
