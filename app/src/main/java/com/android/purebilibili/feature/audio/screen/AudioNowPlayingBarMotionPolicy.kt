package com.android.purebilibili.feature.audio.screen

import androidx.compose.animation.core.CubicBezierEasing

/**
 * 底部小横条落位回弹贝塞尔曲线：
 * 控制点 P1(0.34, 1.45) 使动画在到达终点附近时产生轻微超调，
 * 呈现自然物理特性的落地挤压与缓冲回弹效果。
 */
internal val AudioNowPlayingBarLandingEasing = CubicBezierEasing(0.34f, 1.45f, 0.64f, 1.0f)

internal const val AUDIO_NOW_PLAYING_BAR_LANDING_DELAY_MS = 240L
internal const val AUDIO_NOW_PLAYING_BAR_LANDING_DURATION_MS = 420

internal fun resolveAudioNowPlayingBarLandingOffsetY(
    progress: Float,
    maxDropDp: Float = 14f
): Float {
    if (progress == 1f) return 0f
    return (1f - progress) * -maxDropDp
}

internal fun resolveAudioNowPlayingBarLandingScale(
    progress: Float
): Pair<Float, Float> {
    if (progress == 1f) return 1f to 1f
    return if (progress > 1f) {
        val overshoot = progress - 1f
        // 落地挤压（Squash）：横向微扩，纵向微压，随后回弹恢复
        (1f + overshoot * 0.25f) to (1f - overshoot * 0.25f)
    } else {
        val enterScale = (0.95f + 0.05f * progress).coerceIn(0.95f, 1f)
        enterScale to enterScale
    }
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
    currentBvid: String
): Boolean {
    if (!isReturningFromDetail || currentBvid.isBlank()) return false
    return targetBvid.isNullOrBlank() || targetBvid == currentBvid
}

internal fun resolveAudioNowPlayingBarLandingMotionEnabled(
    reduceMotion: Boolean
): Boolean = !reduceMotion
