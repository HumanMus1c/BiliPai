package com.android.purebilibili.feature.home.components.cards

import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase

internal data class VideoCardScrollLiteVisualPolicy(
    val coverShadowElevationDp: Float,
    val showCoverGradientMask: Boolean,
    val showHistoryProgressBar: Boolean,
    val showCompactStatsOnCover: Boolean,
    val showSecondaryStatsRow: Boolean
)

internal fun resolveVideoCardScrollLiteVisualPolicy(
    scrollLiteModeEnabled: Boolean,
    compactStatsOnCover: Boolean
): VideoCardScrollLiteVisualPolicy {
    if (scrollLiteModeEnabled) {
        return VideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showCoverGradientMask = false,
            showHistoryProgressBar = false,
            showCompactStatsOnCover = compactStatsOnCover,
            showSecondaryStatsRow = !compactStatsOnCover
        )
    }

    return VideoCardScrollLiteVisualPolicy(
        coverShadowElevationDp = 0f,
        // 统计信息移到封面外时也不需要暗渐变；保持静止和滚动状态一致，避免整批封面明暗闪烁。
        showCoverGradientMask = false,
        showHistoryProgressBar = true,
        showCompactStatsOnCover = compactStatsOnCover,
        showSecondaryStatsRow = !compactStatsOnCover
    )
}

internal fun shouldEnableVideoCardCoverCrossfade(
    isScrollInProgress: Boolean,
    isReturningFromDetail: Boolean,
    useCoverSharedBounds: Boolean,
    isSharedReturnTarget: Boolean
): Boolean {
    if (isScrollInProgress) return false
    // 返回目标封面由 sharedBounds 承接播放器画面，Coil 淡入会在落位后再次改变亮度导致闪烁。
    return !(isReturningFromDetail && useCoverSharedBounds && isSharedReturnTarget)
}

/**
 * 首页卡片 → 详情页 CARD_SHELL morph 期间，源卡片封面应让位给 overlay，
 * 避免列表里仍显示一张静态封面而与详情页封面重影。
 *
 * 仅 forward 进入详情(OPENING) 时隐藏；返回 morph（HELD/IDLE、预测式手势、已提交返回）
 * 必须保留真实封面，否则会露出 surfaceVariant 占位色。
 */
internal fun shouldHideHomeCardCoverDuringShellMorph(
    useCardContainerSharedBounds: Boolean,
    isSharedMorphSourceCard: Boolean,
    isReturningFromDetail: Boolean,
    isSharedTransitionActive: Boolean,
    transitionBackgroundPhase: VideoCardTransitionBackgroundPhase,
    isVideoCardReturnGestureInProgress: Boolean,
): Boolean {
    if (!useCardContainerSharedBounds || !isSharedMorphSourceCard || !isSharedTransitionActive) {
        return false
    }
    if (
        isReturningFromDetail ||
        isVideoCardReturnGestureInProgress ||
        transitionBackgroundPhase != VideoCardTransitionBackgroundPhase.OPENING
    ) {
        return false
    }
    return true
}

internal data class StoryVideoCardScrollLiteVisualPolicy(
    val coverShadowElevationDp: Float,
    val showSecondaryStatsRow: Boolean
)

internal fun resolveStoryVideoCardScrollLiteVisualPolicy(
    scrollLiteModeEnabled: Boolean
): StoryVideoCardScrollLiteVisualPolicy {
    return if (scrollLiteModeEnabled) {
        StoryVideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showSecondaryStatsRow = true
        )
    } else {
        StoryVideoCardScrollLiteVisualPolicy(
            coverShadowElevationDp = 0f,
            showSecondaryStatsRow = true
        )
    }
}
