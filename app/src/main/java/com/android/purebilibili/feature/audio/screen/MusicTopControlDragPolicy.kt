package com.android.purebilibili.feature.audio.screen

import androidx.compose.runtime.Immutable
import kotlin.math.abs

@Immutable
internal data class MusicTopControlTransform(
    val translationX: Float,
    val translationY: Float,
    val scaleX: Float,
    val scaleY: Float,
    val rotationZ: Float,
)

/** Keeps the experimental top controls tactile without letting them escape their touch target. */
internal fun resolveMusicTopControlTransform(
    dragX: Float,
    dragY: Float,
    maxDragPx: Float,
): MusicTopControlTransform {
    val safeMaxDragPx = maxDragPx.coerceAtLeast(1f)
    val clampedX = dragX.coerceIn(-safeMaxDragPx, safeMaxDragPx)
    val clampedY = dragY.coerceIn(-safeMaxDragPx, safeMaxDragPx)
    val horizontalPull = abs(clampedX) / safeMaxDragPx
    val verticalPull = abs(clampedY) / safeMaxDragPx
    return MusicTopControlTransform(
        translationX = clampedX,
        translationY = clampedY,
        scaleX = 1f + horizontalPull * 0.18f - verticalPull * 0.06f,
        scaleY = 1f + verticalPull * 0.18f - horizontalPull * 0.06f,
        rotationZ = (clampedX / safeMaxDragPx) * 5f,
    )
}
