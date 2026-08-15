package com.android.purebilibili.core.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState

data class ScrollToTopPlan(
    val preJumpIndex: Int?,
    val animateTargetIndex: Int = 0
)

/**
 * 长列表回顶策略：
 * - 近距离直接平滑到顶部
 * - 远距离先瞬移到较近位置，再平滑到顶部，减少一次性测量造成的卡顿
 */
fun resolveScrollToTopPlan(firstVisibleItemIndex: Int): ScrollToTopPlan {
    val index = firstVisibleItemIndex.coerceAtLeast(0)
    val preJump = when {
        index > 180 -> 28
        index > 96 -> 20
        index > 36 -> 12
        index > 14 -> 6
        else -> null
    }
    return ScrollToTopPlan(preJumpIndex = preJump)
}

fun shouldShowScrollToTop(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    offsetThresholdPx: Int = 600,
): Boolean {
    return firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset >= offsetThresholdPx
}

suspend fun LazyListState.animateScrollToTop() {
    val plan = resolveScrollToTopPlan(firstVisibleItemIndex)
    plan.preJumpIndex?.let { scrollToItem(it) }
    animateScrollToItem(plan.animateTargetIndex)
}

suspend fun LazyGridState.animateScrollToTop() {
    val plan = resolveScrollToTopPlan(firstVisibleItemIndex)
    plan.preJumpIndex?.let { scrollToItem(it) }
    animateScrollToItem(plan.animateTargetIndex)
}
