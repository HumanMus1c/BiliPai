package com.android.purebilibili.core.store

import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeSettingsUiPresetPolicyTest {

    @Test
    fun bottomBarLiquidGlass_respectsUserChoiceForMd3Preset() {
        val settings = HomeSettings(
            isBottomBarLiquidGlassEnabled = true,
            androidNativeLiquidGlassEnabled = false
        )

        assertTrue(resolveEffectiveHomeSettings(settings, UiPreset.MD3).isBottomBarLiquidGlassEnabled)
    }

    @Test
    fun bottomBarLiquidGlass_keepsTopDockIndependent() {
        val disabled = resolveEffectiveHomeSettings(
            homeSettings = HomeSettings(
                isTopBarLiquidGlassEnabled = true,
                isBottomBarLiquidGlassEnabled = true,
                androidNativeLiquidGlassEnabled = false
            ),
            uiPreset = UiPreset.MD3
        )

        assertTrue(disabled.isTopBarLiquidGlassEnabled)
        assertTrue(disabled.isBottomBarLiquidGlassEnabled)

        val enabled = resolveEffectiveHomeSettings(
            homeSettings = HomeSettings(
                isTopBarLiquidGlassEnabled = true,
                isBottomBarLiquidGlassEnabled = true,
                androidNativeLiquidGlassEnabled = true
            ),
            uiPreset = UiPreset.MD3
        )

        assertTrue(enabled.isTopBarLiquidGlassEnabled)
        assertTrue(enabled.isBottomBarLiquidGlassEnabled)
    }
}
