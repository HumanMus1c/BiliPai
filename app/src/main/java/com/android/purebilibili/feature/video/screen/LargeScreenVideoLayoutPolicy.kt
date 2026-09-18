package com.android.purebilibili.feature.video.screen

/**
 * Large-screen video geometry for the current application window.
 * Landscape uses a left player column and a 280–425dp side pane.
 */
internal const val LARGE_SCREEN_VIDEO_LANDSCAPE_RATIO = 1.2f
internal const val LARGE_SCREEN_VIDEO_ASPECT_16_9 = 16f / 9f
internal const val LARGE_SCREEN_VIDEO_MIN_SIDE_PANE_DP = 280f
internal const val LARGE_SCREEN_VIDEO_MAX_SIDE_PANE_DP = 425f
internal const val LARGE_SCREEN_VIDEO_SIDE_PANE_BREAKPOINT_DP = 560f
internal const val LARGE_SCREEN_VIDEO_SQUARE_PLAYER_HEIGHT_FRACTION = 0.4f

internal enum class LargeScreenVideoLayoutMode {
    Phone,
    Landscape,
    Split,
    VerticalThreePane,
    AlmostSquare,
}

internal data class LargeScreenVideoMetrics(
    val mode: LargeScreenVideoLayoutMode,
    val playerWidthDp: Float,
    val playerHeightDp: Float,
    val sidePaneWidthDp: Float,
    val introBelowPlayer: Boolean,
)

internal fun shouldUseLargeScreenVideoLayout(
    windowWidthDp: Float,
    windowHeightDp: Float,
    horizontalAdaptationEnabled: Boolean,
): Boolean {
    if (!horizontalAdaptationEnabled) return false
    if (windowWidthDp <= 0f || windowHeightDp <= 0f) return false
    if (windowWidthDp / windowHeightDp >= LARGE_SCREEN_VIDEO_LANDSCAPE_RATIO) return true
    val fullWidthPlayerHeight = windowWidthDp / LARGE_SCREEN_VIDEO_ASPECT_16_9
    return fullWidthPlayerHeight >= 0.4f * windowHeightDp
}

internal fun shouldUseDedicatedCollectionColumn(
    availableWidthDp: Float,
    hasCollection: Boolean,
): Boolean {
    if (!hasCollection || availableWidthDp <= 0f) return false
    return availableWidthDp / 3f >= LARGE_SCREEN_VIDEO_MIN_SIDE_PANE_DP
}

internal fun resolveLargeScreenLandscapePlayerWidthDp(
    windowWidthDp: Float,
    windowHeightDp: Float,
): Float {
    var width = (windowHeightDp / windowWidthDp * 1.08f).coerceIn(0.5f, 0.7f) * windowWidthDp
    if (windowWidthDp >= LARGE_SCREEN_VIDEO_SIDE_PANE_BREAKPOINT_DP) {
        val side = (windowWidthDp - width).coerceIn(
            LARGE_SCREEN_VIDEO_MIN_SIDE_PANE_DP,
            LARGE_SCREEN_VIDEO_MAX_SIDE_PANE_DP,
        )
        width = windowWidthDp - side
    }
    return width
}

internal fun resolveLargeScreenVideoMetrics(
    windowWidthDp: Float,
    windowHeightDp: Float,
    isVerticalVideo: Boolean,
    enableVerticalExpand: Boolean = false,
): LargeScreenVideoMetrics {
    if (windowWidthDp <= 0f || windowHeightDp <= 0f) {
        return LargeScreenVideoMetrics(
            mode = LargeScreenVideoLayoutMode.Phone,
            playerWidthDp = windowWidthDp,
            playerHeightDp = windowHeightDp,
            sidePaneWidthDp = 0f,
            introBelowPlayer = true,
        )
    }
    val landscape = windowWidthDp / windowHeightDp >= LARGE_SCREEN_VIDEO_LANDSCAPE_RATIO
    if (enableVerticalExpand && isVerticalVideo && landscape) {
        val playerHeight = windowHeightDp
        val playerWidth = playerHeight / LARGE_SCREEN_VIDEO_ASPECT_16_9
        val side = ((windowWidthDp - playerWidth) / 2f).coerceAtLeast(0f)
        return LargeScreenVideoMetrics(
            mode = LargeScreenVideoLayoutMode.VerticalThreePane,
            playerWidthDp = playerWidth,
            playerHeightDp = playerHeight,
            sidePaneWidthDp = side,
            introBelowPlayer = false,
        )
    }
    if (landscape) {
        val playerWidth = resolveLargeScreenLandscapePlayerWidthDp(windowWidthDp, windowHeightDp)
        val playerHeight = playerWidth / LARGE_SCREEN_VIDEO_ASPECT_16_9
        if (playerHeight > windowHeightDp) {
            val splitHeight = windowHeightDp
            val splitWidth = splitHeight * LARGE_SCREEN_VIDEO_ASPECT_16_9
            return LargeScreenVideoMetrics(
                mode = LargeScreenVideoLayoutMode.Split,
                playerWidthDp = splitWidth.coerceAtMost(windowWidthDp),
                playerHeightDp = splitHeight,
                sidePaneWidthDp = (windowWidthDp - splitWidth).coerceAtLeast(0f),
                introBelowPlayer = false,
            )
        }
        return LargeScreenVideoMetrics(
            mode = LargeScreenVideoLayoutMode.Landscape,
            playerWidthDp = playerWidth,
            playerHeightDp = playerHeight,
            sidePaneWidthDp = (windowWidthDp - playerWidth).coerceAtLeast(0f),
            introBelowPlayer = true,
        )
    }
    val fullWidthPlayerHeight = windowWidthDp / LARGE_SCREEN_VIDEO_ASPECT_16_9
    if (fullWidthPlayerHeight < 0.4f * windowHeightDp) {
        return LargeScreenVideoMetrics(
            mode = LargeScreenVideoLayoutMode.Phone,
            playerWidthDp = windowWidthDp,
            playerHeightDp = fullWidthPlayerHeight,
            sidePaneWidthDp = 0f,
            introBelowPlayer = true,
        )
    }
    val squareHeight = windowHeightDp * LARGE_SCREEN_VIDEO_SQUARE_PLAYER_HEIGHT_FRACTION
    return LargeScreenVideoMetrics(
        mode = LargeScreenVideoLayoutMode.AlmostSquare,
        playerWidthDp = windowWidthDp,
        playerHeightDp = squareHeight,
        sidePaneWidthDp = 0f,
        introBelowPlayer = true,
    )
}

internal fun resolveShowRelatedInIntro(mode: LargeScreenVideoLayoutMode): Boolean {
    return mode == LargeScreenVideoLayoutMode.AlmostSquare
}

internal fun resolveIncludeRelatedTabInSecondary(mode: LargeScreenVideoLayoutMode): Boolean {
    return mode != LargeScreenVideoLayoutMode.AlmostSquare
}

internal fun resolveRelatedTabFirstInSecondary(mode: LargeScreenVideoLayoutMode): Boolean {
    return mode == LargeScreenVideoLayoutMode.Landscape || mode == LargeScreenVideoLayoutMode.Split
}
