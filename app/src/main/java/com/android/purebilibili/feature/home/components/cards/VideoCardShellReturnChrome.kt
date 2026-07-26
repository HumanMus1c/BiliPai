package com.android.purebilibili.feature.home.components.cards

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.transition.LocalVideoCardTransitionBackgroundState
import com.android.purebilibili.core.util.CardPositionManager

/**
 * 源卡信息区（标题/UP 等）在 shell morph 时的 chrome 视觉。
 * 封面保持可见；返回末段按景深进度淡入字，避免叠实时画面又落后封面；
 * 横卡可选择随主进度短距离移动；快速返回不藏字。
 * 所有进度都在绘制阶段读取，避免整卡重组。
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun Modifier.videoCardShellReturnChromeAlpha(
    enabled: Boolean,
    bvid: String,
    sourceRoute: String?,
    isReturningFromDetail: Boolean = false,
    isQuickReturnFromDetail: Boolean = false,
    followShellMotion: Boolean = false,
): Modifier {
    if (!enabled || bvid.isBlank()) return this
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val bgState = LocalVideoCardTransitionBackgroundState.current
    val followDistancePx = with(LocalDensity.current) { 8.dp.toPx() }
    val isSharedMorphSourceCard = remember(
        bvid,
        sourceRoute,
        CardPositionManager.lastClickedVideoSourceKey,
    ) {
        isVideoCardSharedReturnTarget(
            bvid = bvid,
            sourceRoute = sourceRoute,
            lastClickedVideoSourceKey = CardPositionManager.lastClickedVideoSourceKey,
        )
    }
    return graphicsLayer {
        val phase = bgState.phaseProvider()
        val returnGestureInProgress = bgState.isReturnGestureInProgressProvider()
        val transitionActive = sharedTransitionScope?.isTransitionActive == true
        val progress = bgState.progressProvider()
        val quickReturn = isQuickReturnFromDetail ||
            bgState.isQuickReturnFromDetailProvider()
        if (followShellMotion) {
            val frame = resolveHorizontalCardChromeMotionFrame(
                useCardContainerSharedBounds = enabled,
                isSharedMorphSourceCard = isSharedMorphSourceCard,
                isReturningFromDetail = isReturningFromDetail,
                transitionBackgroundPhase = phase,
                isVideoCardReturnGestureInProgress = returnGestureInProgress,
                isSharedTransitionActive = transitionActive,
                transitionBackgroundProgress = progress,
                isQuickReturnFromDetail = quickReturn,
            )
            alpha = frame.alpha
            translationY = if (bgState.motionTierProvider() == MotionTier.Reduced) {
                0f
            } else {
                -followDistancePx * frame.translationProgress
            }
        } else {
            alpha = resolveHomeCardChromeAlphaDuringShellReturnMorph(
                useCardContainerSharedBounds = enabled,
                isSharedMorphSourceCard = isSharedMorphSourceCard,
                isReturningFromDetail = isReturningFromDetail,
                transitionBackgroundPhase = phase,
                isVideoCardReturnGestureInProgress = returnGestureInProgress,
                isSharedTransitionActive = transitionActive,
                transitionBackgroundProgress = progress,
                isQuickReturnFromDetail = quickReturn,
            )
            translationY = 0f
        }
    }
}
