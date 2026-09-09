package com.android.purebilibili.feature.audio.screen

import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens

internal data class MusicPlayerChromeSpec(
    val uiStyle: AppUiStyle,
    val glassEnabled: Boolean,
    val usePaletteImmersiveBackdrop: Boolean,
    val coverShapeIsCircle: Boolean,
    val horizontalPaddingDp: Int,
    val playButtonSizeDp: Int,
    val skipButtonSizeDp: Int
)

internal fun resolveMusicPlayerChromeSpec(
    uiStyle: AppUiStyle,
    glassEnabled: Boolean
): MusicPlayerChromeSpec {
    val tokens = resolveAndroidNativeChromeTokens(uiStyle)
    return MusicPlayerChromeSpec(
        uiStyle = uiStyle,
        glassEnabled = glassEnabled,
        usePaletteImmersiveBackdrop = glassEnabled,
        coverShapeIsCircle = true,
        horizontalPaddingDp = tokens.denseHorizontalSpacingDp,
        playButtonSizeDp = if (uiStyle == AppUiStyle.MIUIX) 72 else 80,
        skipButtonSizeDp = if (uiStyle == AppUiStyle.MIUIX) 48 else 56
    )
}
