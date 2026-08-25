package com.android.purebilibili.core.ui

import kotlin.math.roundToInt

/** Home floating bottom-bar shell height. Other chrome scales from this. */
const val BottomBarReferenceShellHeightDp = 64f

/** Home floating bottom-bar rest indicator height. */
const val BottomBarReferenceIndicatorHeightDp = 48f

/** Home floating bottom-bar pressed / drag height. */
const val BottomBarReferencePressedHeightDp = 78f

/** Shared compact bloom used by the home floating bottom bar itself. */
const val BottomBarReferencePressedScale = BottomBarReferencePressedHeightDp /
    BottomBarReferenceIndicatorHeightDp

data class MatchedLiquidIndicatorGeometry(
    val dockHeightDp: Float,
    val indicatorHeightDp: Float,
    val pressedScale: Float,
) {
    val pressedHeightDp: Float get() = indicatorHeightDp * pressedScale
}

fun resolveMatchedLiquidIndicatorHeightDp(dockHeightDp: Float): Float {
    if (dockHeightDp <= 0f) return 0f
    return dockHeightDp *
        (BottomBarReferenceIndicatorHeightDp / BottomBarReferenceShellHeightDp)
}

fun roundMatchedLiquidIndicatorHeightDp(dockHeightDp: Float): Int {
    return resolveMatchedLiquidIndicatorHeightDp(dockHeightDp)
        .roundToInt()
        .coerceAtLeast(1)
}

fun resolveMatchedLiquidIndicatorPressedScale(
    dockHeightDp: Float,
    indicatorHeightDp: Float,
): Float {
    if (dockHeightDp <= 0f || indicatorHeightDp <= 0f) return 1f
    val pressedHeight = dockHeightDp *
        (BottomBarReferencePressedHeightDp / BottomBarReferenceShellHeightDp)
    return (pressedHeight / indicatorHeightDp).coerceAtLeast(1f)
}

fun resolveMatchedLiquidIndicatorGeometry(
    dockHeightDp: Float,
    indicatorHeightDp: Float = resolveMatchedLiquidIndicatorHeightDp(dockHeightDp),
): MatchedLiquidIndicatorGeometry {
    val restHeight = if (indicatorHeightDp > 0f) {
        indicatorHeightDp
    } else {
        resolveMatchedLiquidIndicatorHeightDp(dockHeightDp)
    }
    return MatchedLiquidIndicatorGeometry(
        dockHeightDp = dockHeightDp,
        indicatorHeightDp = restHeight,
        pressedScale = resolveMatchedLiquidIndicatorPressedScale(
            dockHeightDp = dockHeightDp,
            indicatorHeightDp = restHeight,
        ),
    )
}
