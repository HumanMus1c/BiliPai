package com.android.purebilibili.feature.home.components

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur

internal const val BILIPAI_PROGRESSIVE_TOP_BLUR_RADIUS_DP = 10f
private const val BILIPAI_PROGRESSIVE_TOP_BLUR_MIN_EXTENSION_DP = 20f
private const val BILIPAI_PROGRESSIVE_TOP_BLUR_EXTRA_EXTENSION_DP = 28f
private val BiliPaiProgressiveTopBlurShape = RoundedCornerShape(
    bottomStart = 28.dp,
    bottomEnd = 28.dp,
)

internal fun shouldUseBiliPaiProgressiveTopBlur(
    enabled: Boolean,
    hasBackdrop: Boolean,
    sdkInt: Int = Build.VERSION.SDK_INT,
): Boolean = enabled && hasBackdrop && sdkInt >= Build.VERSION_CODES.TIRAMISU

internal fun resolveProgressiveTopBlurBottomExtension(
    enabled: Boolean,
    endFraction: Float,
): Dp = if (enabled) {
    (
        BILIPAI_PROGRESSIVE_TOP_BLUR_MIN_EXTENSION_DP +
            endFraction.coerceIn(0f, 1f) * BILIPAI_PROGRESSIVE_TOP_BLUR_EXTRA_EXTENSION_DP
    ).dp
} else {
    0.dp
}

internal fun shouldExtendProgressiveTopBlurBelowTabs(
    progressiveBlurEnabled: Boolean,
    tabRowIncludedInBlur: Boolean,
): Boolean = progressiveBlurEnabled && !tabRowIncludedInBlur

/** Shared home-style edge blur for immersive floating top chrome. */
internal fun Modifier.biliPaiProgressiveTopBlur(
    backdrop: Backdrop?,
    enabled: Boolean,
    shape: Shape = BiliPaiProgressiveTopBlurShape,
    blurRadiusDp: Float = BILIPAI_PROGRESSIVE_TOP_BLUR_RADIUS_DP,
    gradient: ProgressiveBlur = ProgressiveBlur.Top,
): Modifier {
    if (
        !shouldUseBiliPaiProgressiveTopBlur(enabled, backdrop != null) ||
        blurRadiusDp <= 0.001f
    ) {
        return this
    }
    return progressiveTextureBlur(
        backdrop = requireNotNull(backdrop),
        shape = shape,
        blurRadius = blurRadiusDp,
        gradient = gradient,
    )
}
