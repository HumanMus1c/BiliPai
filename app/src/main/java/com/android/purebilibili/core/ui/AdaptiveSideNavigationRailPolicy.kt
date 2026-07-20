package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset

enum class AdaptiveSideNavigationRailRenderer {
    MATERIAL3,
    MIUIX
}

fun resolveAdaptiveSideNavigationRailRenderer(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): AdaptiveSideNavigationRailRenderer = when (
    resolvePresetPrimitiveRenderer(uiPreset, androidNativeVariant)
) {
    PresetPrimitiveRenderer.MIUIX_BRIDGED -> AdaptiveSideNavigationRailRenderer.MIUIX
    PresetPrimitiveRenderer.IOS,
    PresetPrimitiveRenderer.MATERIAL3 -> AdaptiveSideNavigationRailRenderer.MATERIAL3
}

fun shouldUseExpandableMiuixNavigationRail(isExpandedWidthClass: Boolean): Boolean =
    isExpandedWidthClass
