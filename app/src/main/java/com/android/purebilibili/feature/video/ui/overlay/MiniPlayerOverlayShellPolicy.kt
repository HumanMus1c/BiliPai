package com.android.purebilibili.feature.video.ui.overlay

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.isNativeMiuixEnabled
import kotlin.math.roundToInt

/**
 * MIUIX shell tuning for the floating mini player: flatter elevation, token-aligned corners,
 * and theme primary accents for transport controls.
 */
data class MiniPlayerOverlayShellVisual(
    val cardCornerRadiusDp: Int,
    val cardElevationDp: Int,
    val cardShadowDp: Int,
    val seekHintCornerRadiusDp: Int,
    val useThemePrimaryAccent: Boolean
)

fun resolveMiniPlayerOverlayShellVisual(
    layout: MiniPlayerOverlayLayoutPolicy,
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): MiniPlayerOverlayShellVisual {
    if (!isNativeMiuixEnabled(uiPreset, androidNativeVariant)) {
        return MiniPlayerOverlayShellVisual(
            cardCornerRadiusDp = layout.cardCornerRadiusDp,
            cardElevationDp = layout.cardElevationDp,
            cardShadowDp = layout.cardShadowDp,
            seekHintCornerRadiusDp = layout.seekHintCornerRadiusDp,
            useThemePrimaryAccent = false
        )
    }
    val tokenCardCorner = AppShapes.resolveContainerCornerDp(
        level = ContainerLevel.Card,
        uiPreset = uiPreset,
        androidNativeVariant = androidNativeVariant
    ).value.roundToInt()
    val tokenFloatingCorner = AppShapes.resolveContainerCornerDp(
        level = ContainerLevel.Floating,
        uiPreset = uiPreset,
        androidNativeVariant = androidNativeVariant
    ).value.roundToInt()
    // Floating mini-player chrome sits between card and floating token radii.
    val cardCorner = ((tokenCardCorner + tokenFloatingCorner) / 2)
        .coerceAtLeast(layout.cardCornerRadiusDp)
    val chipCorner = AppShapes.resolveContainerCornerDp(
        level = ContainerLevel.Chip,
        uiPreset = uiPreset,
        androidNativeVariant = androidNativeVariant
    ).value.roundToInt().coerceAtLeast(layout.seekHintCornerRadiusDp)
    return MiniPlayerOverlayShellVisual(
        cardCornerRadiusDp = cardCorner,
        cardElevationDp = 0,
        cardShadowDp = (layout.cardShadowDp * 0.55f).roundToInt().coerceAtLeast(6),
        seekHintCornerRadiusDp = chipCorner,
        useThemePrimaryAccent = true
    )
}
