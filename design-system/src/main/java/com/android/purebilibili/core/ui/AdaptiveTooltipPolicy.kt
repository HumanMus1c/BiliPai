package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AppUiStyle

enum class AdaptiveTooltipRenderer {
    MIUIX_TOOLTIP_BOX,
    PASSTHROUGH
}

fun resolveAdaptiveTooltipRenderer(
    uiStyle: AppUiStyle
): AdaptiveTooltipRenderer = when (uiStyle) {
    AppUiStyle.MIUIX -> AdaptiveTooltipRenderer.MIUIX_TOOLTIP_BOX
    AppUiStyle.MATERIAL3 -> AdaptiveTooltipRenderer.PASSTHROUGH
}
