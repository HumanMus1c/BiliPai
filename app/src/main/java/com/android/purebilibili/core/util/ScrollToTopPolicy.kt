package com.android.purebilibili.core.util

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState

data class ScrollToTopPlan(
    val preJumpIndex: Int?,
    val animateTargetIndex: Int = 0
)

/**
 * 长列表回顶策略：
 * - 近距离直接平滑到顶部
 * - 已知当前视口容量时，远距离先定位到约两屏外，再平滑到顶部
 * - 未提供视口容量的旧调用继续使用固定分段策略
 */
fun resolveScrollToTopPlan(
    firstVisibleItemIndex: Int,
    visibleItemCount: Int? = null,
): ScrollToTopPlan {
    val index = firstVisibleItemIndex.coerceAtLeast(0)
    val measuredViewportItems = visibleItemCount?.takeIf { it > 0 }
    val preJump = if (measuredViewportItems != null) {
        val viewportItems = measuredViewportItems.coerceIn(4, 16)
        val animatedWindowItems = (viewportItems * 2).coerceIn(8, 32)
        val directAnimationLimit = animatedWindowItems + viewportItems
        animatedWindowItems.takeIf { index > directAnimationLimit }
    } else {
        when {
            index > 180 -> 28
            index > 96 -> 20
            index > 36 -> 12
            index > 14 -> 6
            else -> null
        }
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
    val plan = resolveScrollToTopPlan(
        firstVisibleItemIndex = firstVisibleItemIndex,
        visibleItemCount = layoutInfo.visibleItemsInfo.size,
    )
    plan.preJumpIndex?.let { scrollToItem(it) }
    animateScrollToItem(plan.animateTargetIndex)
}

suspend fun LazyGridState.animateScrollToTop() {
    val plan = resolveScrollToTopPlan(
        firstVisibleItemIndex = firstVisibleItemIndex,
        visibleItemCount = layoutInfo.visibleItemsInfo.size,
    )
    plan.preJumpIndex?.let { scrollToItem(it) }
    animateScrollToItem(plan.animateTargetIndex)
}

suspend fun LazyStaggeredGridState.animateScrollToTop() {
    val plan = resolveScrollToTopPlan(
        firstVisibleItemIndex = firstVisibleItemIndex,
        visibleItemCount = layoutInfo.visibleItemsInfo.size,
    )
    plan.preJumpIndex?.let { scrollToItem(it) }
    animateScrollToItem(plan.animateTargetIndex)
}
