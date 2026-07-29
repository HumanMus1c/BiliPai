package com.android.purebilibili.core.ui

import androidx.compose.ui.graphics.Color

data class TopReadabilityChromeSpec(
    val heightDp: Int,
    val surfaceColor: Color,
    val surfaceAlpha: Float,
    val bottomAlpha: Float,
    val drawGradient: Boolean,
    val useHaze: Boolean,
)

fun resolveTopReadabilityChromeSpec(
    requestedHeightDp: Int,
    surfaceColor: Color,
    surfaceAlpha: Float,
    hazeRequested: Boolean,
    hasHazeState: Boolean,
    drawGradient: Boolean = true,
): TopReadabilityChromeSpec {
    val height = requestedHeightDp.coerceAtLeast(0)
    val alpha = surfaceAlpha.coerceIn(0f, 1f)
    return TopReadabilityChromeSpec(
        heightDp = height,
        surfaceColor = surfaceColor,
        surfaceAlpha = alpha,
        bottomAlpha = 0f,
        drawGradient = drawGradient && height > 0,
        useHaze = hazeRequested && hasHazeState && height > 0,
    )
}
