package com.android.purebilibili.feature.home.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import kotlin.math.max
import kotlin.math.min

/** Home dock resting indicator is 56dp in a typically ~75dp slot (~1.35). */
internal const val FLOATING_DOCK_MIN_INDICATOR_ASPECT = 1.35f

internal const val FLOATING_DOCK_PREDICTIVE_BACK_EDGE_DP = 24f

internal const val FLOATING_DOCK_REFERENCE_SHELL_HEIGHT_DP = 64f

internal const val FLOATING_DOCK_SHELL_LENS_DP = 24f

internal const val FLOATING_DOCK_PRESS_BLOOM_DP = 16f

internal const val FLOATING_DOCK_INDICATOR_LENS_HEIGHT_DP = 10f

internal const val FLOATING_DOCK_INDICATOR_LENS_AMOUNT_DP = 14f

internal const val FLOATING_DOCK_INNER_SHADOW_RADIUS_DP = 8f

internal const val FLOATING_DOCK_TAB_PRESS_SCALE_EXTRA = 0.2f

private const val FLOATING_DOCK_MAX_VELOCITY_SCALE_X = 1.25f
private const val FLOATING_DOCK_MAX_VELOCITY_SCALE_Y = 1.2f
private const val FLOATING_DOCK_PANEL_OFFSET_DP = 4f

internal data class FloatingDockCaptureInsets(
    val horizontalDp: Float,
    val verticalDp: Float,
)

/**
 * Short chrome (search 36dp, top tabs ~40dp) cannot use the home dock's 24dp lens:
 * top and bottom refraction meet in the middle as a black shrimp line.
 * Scale lens with shell height so a 64dp dock stays full strength.
 */
internal fun resolveCompactDockShellLensIntensity(
    shellHeightDp: Float,
    referenceShellHeightDp: Float = FLOATING_DOCK_REFERENCE_SHELL_HEIGHT_DP,
): Float {
    if (shellHeightDp <= 0f || referenceShellHeightDp <= 0f) return 0f
    return (shellHeightDp / referenceShellHeightDp).coerceIn(0f, 1f)
}

internal fun resolveCompactDockLensDp(shellHeightDp: Float): Float =
    FLOATING_DOCK_SHELL_LENS_DP * resolveCompactDockShellLensIntensity(shellHeightDp)

internal fun resolveCompactDockPressBloomDp(shellHeightDp: Float): Float =
    FLOATING_DOCK_PRESS_BLOOM_DP * resolveCompactDockShellLensIntensity(shellHeightDp)

internal fun resolveCompactDockIndicatorLensHeightDp(shellHeightDp: Float): Float =
    FLOATING_DOCK_INDICATOR_LENS_HEIGHT_DP * resolveCompactDockShellLensIntensity(shellHeightDp)

internal fun resolveCompactDockIndicatorLensAmountDp(shellHeightDp: Float): Float =
    FLOATING_DOCK_INDICATOR_LENS_AMOUNT_DP * resolveCompactDockShellLensIntensity(shellHeightDp)

internal fun resolveCompactDockInnerShadowRadiusDp(shellHeightDp: Float): Float =
    FLOATING_DOCK_INNER_SHADOW_RADIUS_DP * resolveCompactDockShellLensIntensity(shellHeightDp)

internal fun resolveCompactDockTabPressScale(shellHeightDp: Float): Float =
    1f + FLOATING_DOCK_TAB_PRESS_SCALE_EXTRA * resolveCompactDockShellLensIntensity(shellHeightDp)

/**
 * Half the extra height a pressed indicator needs beyond the dock.
 * Compact 40dp chrome must reserve this or the 78/56 bloom is clipped by
 * siblings such as the detail-page pager.
 */
internal fun resolveCompactDockScaleOverflowDp(
    shellHeightDp: Float,
    indicatorHeightDp: Float,
): Float {
    val geometry = com.android.purebilibili.core.ui.resolveMatchedLiquidIndicatorGeometry(
        dockHeightDp = shellHeightDp,
        indicatorHeightDp = indicatorHeightDp,
    )
    return ((geometry.pressedHeightDp - shellHeightDp) / 2f).coerceAtLeast(0f)
}

/** Resting 64/56 dock keeps a 4dp inset on each side of the moving pill. */
internal fun resolveFloatingDockRestIndicatorVerticalInsetDp(
    shellHeightDp: Float,
    indicatorHeightDp: Float,
): Float {
    if (shellHeightDp <= 0f || indicatorHeightDp <= 0f) return 0f
    return ((shellHeightDp - indicatorHeightDp) / 2f).coerceAtLeast(0f)
}

/**
 * Press bloom needs extra vertical room. Only consume it from the layout when the
 * caller did not already lock height to the shell; otherwise the 56dp rest pill is
 * clamped to the same height as the 64dp dock and the idle inset becomes 0.
 */
internal fun shouldReserveFloatingDockScaleOverflow(
    incomingMaxHeightPx: Int,
    shellHeightPx: Int,
    overflowPx: Int,
): Boolean {
    if (overflowPx <= 0 || shellHeightPx <= 0) return false
    if (incomingMaxHeightPx == Constraints.Infinity) return true
    return incomingMaxHeightPx >= shellHeightPx + overflowPx * 2
}

internal fun Modifier.floatingDockScaleOverflow(
    overflow: Dp,
    shellHeight: Dp,
): Modifier = layout { measurable, constraints ->
    val overflowPx = overflow.roundToPx().coerceAtLeast(0)
    val shellPx = shellHeight.roundToPx().coerceAtLeast(0)
    val reserve = shouldReserveFloatingDockScaleOverflow(
        incomingMaxHeightPx = if (constraints.hasBoundedHeight) {
            constraints.maxHeight
        } else {
            Constraints.Infinity
        },
        shellHeightPx = shellPx,
        overflowPx = overflowPx,
    )
    if (!reserve) {
        val placeable = measurable.measure(constraints)
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    } else {
        val innerMaxHeight = if (constraints.hasBoundedHeight) {
            (constraints.maxHeight - overflowPx * 2).coerceAtLeast(0)
        } else {
            constraints.maxHeight
        }
        val placeable = measurable.measure(
            constraints.copy(minHeight = 0, maxHeight = innerMaxHeight)
        )
        val width = placeable.width.coerceIn(constraints.minWidth, constraints.maxWidth)
        val height = (placeable.height + overflowPx * 2)
            .coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(width, height) {
            placeable.placeRelative(0, overflowPx)
        }
    }
}

internal fun resolveFloatingDockIndicatorHeightDp(
    requestedHeightDp: Float,
    tabWidthDp: Float,
): Float {
    if (requestedHeightDp <= 0f) return 0f
    if (tabWidthDp <= 0f) return requestedHeightDp
    val maxHeightForCapsule = tabWidthDp / FLOATING_DOCK_MIN_INDICATOR_ASPECT
    return min(requestedHeightDp, maxHeightForCapsule)
}

internal fun resolveFloatingDockCaptureInsets(
    shellHeightDp: Float,
    requestedIndicatorHeightDp: Float,
    indicatorWidthDp: Float,
): FloatingDockCaptureInsets {
    if (shellHeightDp <= 0f || indicatorWidthDp <= 0f) {
        return FloatingDockCaptureInsets(horizontalDp = 0f, verticalDp = 0f)
    }
    val fittedIndicatorHeightDp = resolveFloatingDockIndicatorHeightDp(
        requestedHeightDp = requestedIndicatorHeightDp,
        tabWidthDp = indicatorWidthDp,
    )
    val geometry = com.android.purebilibili.core.ui.resolveMatchedLiquidIndicatorGeometry(
        dockHeightDp = shellHeightDp,
        indicatorHeightDp = fittedIndicatorHeightDp,
    )
    val samplingReachDp = max(
        resolveCompactDockIndicatorLensHeightDp(shellHeightDp),
        resolveCompactDockIndicatorLensAmountDp(shellHeightDp),
    )
    val horizontalScale = geometry.pressedScale * FLOATING_DOCK_MAX_VELOCITY_SCALE_X
    val verticalPressedHeightDp = geometry.pressedHeightDp * FLOATING_DOCK_MAX_VELOCITY_SCALE_Y
    return FloatingDockCaptureInsets(
        horizontalDp = indicatorWidthDp * (horizontalScale - 1f).coerceAtLeast(0f) / 2f +
            samplingReachDp + FLOATING_DOCK_PANEL_OFFSET_DP,
        verticalDp = ((verticalPressedHeightDp - shellHeightDp) / 2f).coerceAtLeast(0f) +
            samplingReachDp + FLOATING_DOCK_PANEL_OFFSET_DP,
    )
}

internal fun resolveFloatingDockDragEdgeInsetPx(
    systemInsetPx: Float,
    fallbackPx: Float,
): Float = max(systemInsetPx, fallbackPx)

internal fun shouldAcceptFloatingDockDragAtWindowX(
    windowX: Float,
    screenWidthPx: Float,
    leftInsetPx: Float,
    rightInsetPx: Float,
): Boolean {
    if (screenWidthPx <= 0f) return true
    return windowX >= leftInsetPx && windowX <= screenWidthPx - rightInsetPx
}
