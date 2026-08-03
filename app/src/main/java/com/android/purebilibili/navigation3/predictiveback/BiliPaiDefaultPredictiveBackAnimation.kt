package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.defaultPopTransitionSpec
import androidx.navigation3.ui.defaultTransitionSpec

import androidx.navigationevent.NavigationEvent
import androidx.navigationevent.NavigationEventTransitionState
import com.android.purebilibili.navigation3.BiliPaiNavKey

internal class BiliPaiDefaultPredictiveBackAnimation(
    private val exitDirection: BiliPaiPredictiveBackExitDirection =
        BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT,
) : BiliPaiPredictiveBackAnimationHandler {
    override suspend fun onBackPressed(
        transitionState: NavigationEventTransitionState?,
        currentPageKey: BiliPaiNavKey?,
    ) = Unit

    @Composable
    override fun Modifier.predictiveBackAnimationDecorator(
        transitionState: NavigationEventTransitionState?,
        contentPageKey: Any,
        currentPageKey: BiliPaiNavKey?,
    ): Modifier = this

    override fun AnimatedContentTransitionScope<Scene<BiliPaiNavKey>>.onPredictivePopTransitionSpec(
        @NavigationEvent.SwipeEdge swipeEdge: Int,
    ): ContentTransform = ContentTransform(
        // 目标页保持全屏，避免默认的 -25% 入场位移露出 windowBackground。
        targetContentEnter = EnterTransition.None,
        initialContentExit = slideOutHorizontally(
            targetOffsetX = resolvePredictiveBackSlideOffsetX(swipeEdge),
            animationSpec = tween(durationMillis = 550, easing = LinearEasing),
        ),
    )

    /**
     * 预测预览横滑方向：
     * - [BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT]：恒向右滑出（与手势无关）；
     * - [BiliPaiPredictiveBackExitDirection.ALWAYS_LEFT]：恒向左滑出；
     * - [BiliPaiPredictiveBackExitDirection.FOLLOW_GESTURE]：跟随手势边缘 —— 左缘手势右滑、
     *   右缘手势左滑，EDGE_NONE 兜底右滑（与 NavDisplay 默认 seek 方向一致）。
     */
    private fun resolvePredictiveBackSlideOffsetX(@NavigationEvent.SwipeEdge swipeEdge: Int): (Int) -> Int {
        val slideRight = when (exitDirection) {
            BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT -> true
            BiliPaiPredictiveBackExitDirection.ALWAYS_LEFT -> false
            BiliPaiPredictiveBackExitDirection.FOLLOW_GESTURE ->
                swipeEdge != NavigationEvent.EDGE_RIGHT
        }
        return if (slideRight) { offset -> offset } else { offset -> -offset }
    }

    override fun AnimatedContentTransitionScope<Scene<BiliPaiNavKey>>.onPopTransitionSpec(): ContentTransform =
        defaultPopTransitionSpec<BiliPaiNavKey>().invoke(this)

    override fun AnimatedContentTransitionScope<Scene<BiliPaiNavKey>>.onTransitionSpec(): ContentTransform =
        defaultTransitionSpec<BiliPaiNavKey>().invoke(this)
}
