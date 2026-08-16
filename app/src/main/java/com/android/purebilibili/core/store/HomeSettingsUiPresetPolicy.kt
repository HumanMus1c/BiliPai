package com.android.purebilibili.core.store

import com.android.purebilibili.core.theme.AppUiStyle

/**
 * Legacy gate used by bottom-bar liquid glass: the per-surface toggle must be on,
 * and the MATERIAL3 style also needs the global android-native reuse switch.
 */
internal fun resolveEffectiveLiquidGlassEnabled(
    requestedEnabled: Boolean,
    uiStyle: AppUiStyle,
    androidNativeLiquidGlassEnabled: Boolean = false
): Boolean {
    if (!requestedEnabled) return false
    // 迁移语义：历史 iOS 预设已由迁移表落到 MIUIX，"无需全局开关"行为由 MIUIX 承接。
    return uiStyle == AppUiStyle.MIUIX || androidNativeLiquidGlassEnabled
}

/**
 * Global liquid-glass reuse master switch ("安卓原生液态玻璃").
 * When enabled, every reusable chrome surface shares the bottom-bar liquid material.
 */
internal fun resolveGlobalLiquidGlassReuseEnabled(
    androidNativeLiquidGlassEnabled: Boolean
): Boolean = androidNativeLiquidGlassEnabled

internal enum class LiquidGlassReuseSurface {
    HOME_BOTTOM_BAR,
    COMMENT_BOTTOM_BAR,
    HOME_TOP_DOCK,
    HOME_SEARCH,
}

internal fun resolveLiquidGlassReuseParticipates(
    surface: LiquidGlassReuseSurface,
    androidNativeLiquidGlassEnabled: Boolean,
): Boolean = resolveGlobalLiquidGlassReuseEnabled(androidNativeLiquidGlassEnabled)

/**
 * Effective enablement for any chrome surface that can reuse bottom-bar liquid glass.
 *
 * - Global reuse ON → force enabled (master switch for top dock / search / bottom bar)
 * - Global reuse OFF → keep the legacy per-surface + preset gate
 */
internal fun resolveSharedLiquidGlassChromeEnabled(
    individualEnabled: Boolean,
    uiStyle: AppUiStyle,
    androidNativeLiquidGlassEnabled: Boolean
): Boolean {
    if (resolveGlobalLiquidGlassReuseEnabled(androidNativeLiquidGlassEnabled)) {
        return true
    }
    return resolveEffectiveLiquidGlassEnabled(
        requestedEnabled = individualEnabled,
        uiStyle = uiStyle,
        androidNativeLiquidGlassEnabled = false
    )
}

internal fun resolveEffectiveHomeSettings(
    homeSettings: HomeSettings,
): HomeSettings = homeSettings
