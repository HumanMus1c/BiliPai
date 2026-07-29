package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

data class AppBottomNavigationVisualPolicy(
    val liquidGlassEnabled: Boolean,
)

internal fun resolveAppBottomNavigationVisualPolicy(
    renderer: PresetPrimitiveRenderer,
    individualLiquidGlassEnabled: Boolean,
    androidNativeLiquidGlassEnabled: Boolean,
): AppBottomNavigationVisualPolicy {
    val liquidGlassEnabled = androidNativeLiquidGlassEnabled ||
        (renderer == PresetPrimitiveRenderer.IOS && individualLiquidGlassEnabled)
    return AppBottomNavigationVisualPolicy(
        liquidGlassEnabled = liquidGlassEnabled,
    )
}

/** Selects the active bottom-navigation implementation without leaking a style enum to callers. */
@Composable
fun AppBottomNavigationHost(
    individualLiquidGlassEnabled: Boolean,
    androidNativeLiquidGlassEnabled: Boolean,
    cupertinoContent: @Composable (AppBottomNavigationVisualPolicy) -> Unit,
    materialContent: @Composable (AppBottomNavigationVisualPolicy) -> Unit,
    platformContent: @Composable (AppBottomNavigationVisualPolicy) -> Unit,
) {
    val renderer = rememberPresetPrimitiveRenderer()
    val policy = remember(
        renderer,
        individualLiquidGlassEnabled,
        androidNativeLiquidGlassEnabled,
    ) {
        resolveAppBottomNavigationVisualPolicy(
            renderer = renderer,
            individualLiquidGlassEnabled = individualLiquidGlassEnabled,
            androidNativeLiquidGlassEnabled = androidNativeLiquidGlassEnabled,
        )
    }
    when (renderer) {
        PresetPrimitiveRenderer.IOS -> cupertinoContent(policy)
        PresetPrimitiveRenderer.MATERIAL3 -> materialContent(policy)
        PresetPrimitiveRenderer.MIUIX_BRIDGED -> platformContent(policy)
    }
}
