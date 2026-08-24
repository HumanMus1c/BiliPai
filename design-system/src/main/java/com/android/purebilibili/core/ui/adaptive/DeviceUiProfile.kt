package com.android.purebilibili.core.ui.adaptive

enum class AdaptiveWidthClass {
    Compact,
    Medium,
    Expanded,
    Large,
    ExtraLarge,
}

data class DeviceUiProfileSpec(
    val isTablet: Boolean,
    val motionTier: MotionTier,
)

fun resolveDeviceUiProfileSpec(
    widthClass: AdaptiveWidthClass,
): DeviceUiProfileSpec {
    val motionTier = if (widthClass >= AdaptiveWidthClass.Expanded) {
        MotionTier.Enhanced
    } else {
        MotionTier.Normal
    }

    return DeviceUiProfileSpec(
        isTablet = widthClass != AdaptiveWidthClass.Compact,
        motionTier = motionTier,
    )
}
