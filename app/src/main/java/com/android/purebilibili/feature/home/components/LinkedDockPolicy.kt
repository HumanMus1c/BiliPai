package com.android.purebilibili.feature.home.components

import kotlin.math.roundToInt

/** Accumulate one direction before changing chrome; tiny reversals must not cause flicker. */
internal fun accumulateDockScroll(previous: Float, delta: Float): Float =
    if (previous * delta < 0f) delta else previous + delta

internal data class LinkedDockGeometry(
    val searchWidth: Int,
    val audioWidth: Int,
    val audioX: Int,
    val audioY: Int,
    val top: Int,
    val height: Int,
)

internal fun resolveLinkedDockGeometry(
    width: Int,
    button: Int,
    barHeight: Int,
    gap: Int,
    hasAudio: Boolean,
    searchEnabled: Boolean,
    mergeProgress: Float,
    searchProgress: Float,
): LinkedDockGeometry {
    val merge = mergeProgress.coerceIn(0f, 1f)
    val search = searchProgress.coerceIn(0f, 1f)
    val top = ((if (hasAudio) barHeight + gap else 0) * (1f - merge)).roundToInt()
    val searchWidth = if (!searchEnabled) 0 else (
        button + (width - button * (if (hasAudio) 3 else 2) - gap * (if (hasAudio) 2 else 1)) * search
    ).roundToInt().coerceAtLeast(button).coerceAtMost((width - button).coerceAtLeast(0))
    // Both playback and search retain separate capsule surfaces.
    val playbackGap = gap
    val compactAudioWidth = (width - button - searchWidth -
        playbackGap * (if (searchEnabled) 2 else 1)).coerceAtLeast(0)
    return LinkedDockGeometry(
        searchWidth = searchWidth,
        audioWidth = if (hasAudio) (width + (compactAudioWidth - width) * merge).roundToInt() else 0,
        audioX = ((button + playbackGap) * merge).roundToInt(),
        audioY = (top * merge).roundToInt(),
        top = top,
        height = top + barHeight,
    )
}

internal data class LinkedDockImpact(
    val translationYDp: Float,
    val scaleX: Float,
    val scaleY: Float,
)

internal data class LinkedDockSearchStretch(
    val scaleX: Float,
    val scaleY: Float,
)

/** Apply the spring's overshoot after safe layout measurement, anchored at the trailing edge. */
internal fun resolveLinkedDockSearchStretch(progress: Float): LinkedDockSearchStretch {
    val overshoot = (progress - progress.coerceIn(0f, 1f)).coerceIn(-0.10f, 0.10f)
    return LinkedDockSearchStretch(
        scaleX = 1f + overshoot * 0.30f,
        scaleY = 1f - overshoot * 0.14f,
    )
}

/** Preserve spring overshoot in the draw layer while measurement stays within valid bounds. */
internal fun resolveLinkedDockImpact(
    progress: Float,
    response: Float = 1f,
): LinkedDockImpact {
    val overshoot = (progress - progress.coerceIn(0f, 1f)).coerceIn(-0.12f, 0.12f)
    val effectiveOvershoot = overshoot * response.coerceIn(0f, 1f)
    return LinkedDockImpact(
        translationYDp = effectiveOvershoot * 52f,
        scaleX = 1f + effectiveOvershoot * 0.32f,
        scaleY = 1f - effectiveOvershoot * 0.58f,
    )
}
