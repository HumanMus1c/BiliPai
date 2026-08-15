package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AppUiStyle

internal enum class AppPrimitiveRenderer {
    MATERIAL,
    MIUIX,
}

internal fun resolveAppSliderRenderer(
    uiStyle: AppUiStyle,
): AppPrimitiveRenderer = when (uiStyle) {
    AppUiStyle.MIUIX -> AppPrimitiveRenderer.MIUIX
    AppUiStyle.MATERIAL3 -> AppPrimitiveRenderer.MATERIAL
}

internal fun shouldUseMiuixOutlinedTextField(
    uiStyle: AppUiStyle,
    hasPrefix: Boolean,
    hasSuffix: Boolean,
): Boolean = uiStyle == AppUiStyle.MIUIX && !hasPrefix && !hasSuffix
