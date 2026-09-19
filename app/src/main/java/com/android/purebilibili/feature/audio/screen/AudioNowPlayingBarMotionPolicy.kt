package com.android.purebilibili.feature.audio.screen

import androidx.compose.animation.core.CubicBezierEasing

/**
 * 底部小横条落位回弹贝塞尔曲线：
 * 控制点 P1(0.30, 1.35) 与 P2(0.50, 1.0) 形成拟合 Miuix Folme / iOS 物理弹簧阻尼的超调落位曲线，
 * 在 58% 进度处产生约 4% 的精细超调缓冲，并迅速收敛回弹，呈现自然通透的物理落地触感。
 */
internal val AudioNowPlayingBarLandingEasing = CubicBezierEasing(0.30f, 1.35f, 0.50f, 1.0f)

internal const val AUDIO_NOW_PLAYING_BAR_LANDING_DELAY_MS = 220L
internal const val AUDIO_NOW_PLAYING_BAR_LANDING_DURATION_MS = 320

internal fun resolveAudioNowPlayingBarLandingOffsetY(
    progress: Float,
    maxDropDp: Float = 8f
): Float {
    if (progress == 1f) return 0f
    return (1f - progress) * -maxDropDp
}

internal fun resolveAudioNowPlayingBarLandingScaleX(
    progress: Float
): Float {
    if (progress == 1f) return 1f
    return if (progress > 1f) {
        val overshoot = progress - 1f
        // 落地挤压（Squash）：横向微扩，随后回弹恢复
        1f + overshoot * 0.25f
    } else {
        (0.95f + 0.05f * progress).coerceIn(0.95f, 1f)
    }
}

internal fun resolveAudioNowPlayingBarLandingScaleY(
    progress: Float
): Float {
    if (progress == 1f) return 1f
    return if (progress > 1f) {
        val overshoot = progress - 1f
        // 落地挤压（Squash）：纵向微压，随后回弹恢复
        1f - overshoot * 0.25f
    } else {
        (0.95f + 0.05f * progress).coerceIn(0.95f, 1f)
    }
}

internal fun resolveAudioNowPlayingBarLandingScale(
    progress: Float
): Pair<Float, Float> {
    if (progress == 1f) return 1f to 1f
    return resolveAudioNowPlayingBarLandingScaleX(progress) to resolveAudioNowPlayingBarLandingScaleY(progress)
}

internal fun resolveAudioNowPlayingBarLandingAlpha(
    progress: Float
): Float {
    if (progress >= 1f) return 1f
    return (0.75f + progress * 0.25f).coerceIn(0f, 1f)
}

internal fun resolveAudioNowPlayingBarShouldTriggerLanding(
    isReturningFromDetail: Boolean,
    targetBvid: String?,
    currentBvid: String,
    isSharedTransitionActive: Boolean = false,
): Boolean {
    if (!isReturningFromDetail || currentBvid.isBlank() || isSharedTransitionActive) return false
    return targetBvid.isNullOrBlank() || targetBvid == currentBvid
}

internal fun resolveAudioNowPlayingBarLandingMotionEnabled(
    reduceMotion: Boolean
): Boolean = !reduceMotion
