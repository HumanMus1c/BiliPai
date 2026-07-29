package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset

enum class AdaptiveScaffoldRenderer {
    MATERIAL3_SCAFFOLD,
    MIUIX_SCAFFOLD_WITH_POPUP_HOST
}

fun resolveAdaptiveScaffoldRenderer(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): AdaptiveScaffoldRenderer = when (
    resolvePresetPrimitiveRenderer(uiPreset, androidNativeVariant)
) {
    PresetPrimitiveRenderer.MIUIX_BRIDGED -> AdaptiveScaffoldRenderer.MIUIX_SCAFFOLD_WITH_POPUP_HOST
    PresetPrimitiveRenderer.IOS,
    PresetPrimitiveRenderer.MATERIAL3 -> AdaptiveScaffoldRenderer.MATERIAL3_SCAFFOLD
}

fun shouldMountMiuixPopupHostOnAdaptiveScaffold(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): Boolean = resolveAdaptiveScaffoldRenderer(uiPreset, androidNativeVariant) ==
    AdaptiveScaffoldRenderer.MIUIX_SCAFFOLD_WITH_POPUP_HOST