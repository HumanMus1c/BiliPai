package com.android.purebilibili.core.ui.adaptive

import com.android.purebilibili.core.util.WindowWidthSizeClass

data class DeviceUiProfile(
    val widthSizeClass: WindowWidthSizeClass,
    val isTablet: Boolean,
    val motionTier: MotionTier,
)

fun resolveDeviceUiProfile(
    widthSizeClass: WindowWidthSizeClass,
): DeviceUiProfile {
    val spec = resolveDeviceUiProfileSpec(
        widthClass = widthSizeClass.toAdaptiveWidthClass(),
    )
    return DeviceUiProfile(
        widthSizeClass = widthSizeClass,
        isTablet = spec.isTablet,
        motionTier = spec.motionTier,
    )
}

internal fun WindowWidthSizeClass.toAdaptiveWidthClass(): AdaptiveWidthClass = when (this) {
    WindowWidthSizeClass.Compact -> AdaptiveWidthClass.Compact
    WindowWidthSizeClass.Medium -> AdaptiveWidthClass.Medium
    WindowWidthSizeClass.Expanded -> AdaptiveWidthClass.Expanded
}
