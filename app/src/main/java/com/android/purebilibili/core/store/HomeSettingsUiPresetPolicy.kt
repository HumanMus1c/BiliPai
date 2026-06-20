package com.android.purebilibili.core.store

import com.android.purebilibili.core.theme.UiPreset

internal fun resolveEffectiveLiquidGlassEnabled(
    requestedEnabled: Boolean,
    uiPreset: UiPreset,
    androidNativeLiquidGlassEnabled: Boolean = false
): Boolean {
    if (!requestedEnabled) return false
    return uiPreset == UiPreset.IOS || androidNativeLiquidGlassEnabled
}

internal fun resolveEffectiveHomeSettings(
    homeSettings: HomeSettings,
    uiPreset: UiPreset
): HomeSettings = when (uiPreset) {
    UiPreset.IOS,
    UiPreset.MD3 -> homeSettings
}
