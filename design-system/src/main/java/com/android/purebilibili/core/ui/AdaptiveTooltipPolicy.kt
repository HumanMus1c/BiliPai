package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset

enum class AdaptiveTooltipRenderer {
    MIUIX_TOOLTIP_BOX,
    PASSTHROUGH
}

fun resolveAdaptiveTooltipRenderer(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): AdaptiveTooltipRenderer = when (
    resolvePresetPrimitiveRenderer(uiPreset, androidNativeVariant)
) {
    PresetPrimitiveRenderer.MIUIX_BRIDGED -> AdaptiveTooltipRenderer.MIUIX_TOOLTIP_BOX
    PresetPrimitiveRenderer.IOS,
    PresetPrimitiveRenderer.MATERIAL3 -> AdaptiveTooltipRenderer.PASSTHROUGH
}
