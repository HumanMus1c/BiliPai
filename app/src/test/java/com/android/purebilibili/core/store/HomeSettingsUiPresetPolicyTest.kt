package com.android.purebilibili.core.store

import com.android.purebilibili.core.theme.AppUiStyle
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

        assertTrue(resolveEffectiveHomeSettings(settings).isBottomBarLiquidGlassEnabled)
    }

    @Test
    fun bottomBarLiquidGlass_keepsTopDockIndependent() {
        val disabled = resolveEffectiveHomeSettings(
            homeSettings = HomeSettings(
                isTopBarLiquidGlassEnabled = true,
                isBottomBarLiquidGlassEnabled = true,
                androidNativeLiquidGlassEnabled = false
            ),
        )

        assertTrue(disabled.isTopBarLiquidGlassEnabled)
        assertTrue(disabled.isBottomBarLiquidGlassEnabled)

        val enabled = resolveEffectiveHomeSettings(
            homeSettings = HomeSettings(
                isTopBarLiquidGlassEnabled = true,
                isBottomBarLiquidGlassEnabled = true,
                androidNativeLiquidGlassEnabled = true
            ),
        )

        assertTrue(enabled.isTopBarLiquidGlassEnabled)
        assertTrue(enabled.isBottomBarLiquidGlassEnabled)
    }

    @Test
    fun effectiveLiquidGlass_material3RequiresAndroidNativeToggle() {
        assertFalse(
            resolveEffectiveLiquidGlassEnabled(
                requestedEnabled = true,
                uiStyle = AppUiStyle.MATERIAL3,
                androidNativeLiquidGlassEnabled = false
            )
        )
        assertTrue(
            resolveEffectiveLiquidGlassEnabled(
                requestedEnabled = true,
                uiStyle = AppUiStyle.MATERIAL3,
                androidNativeLiquidGlassEnabled = true
            )
        )
    }

    @Test
    fun sharedLiquidGlass_globalMasterEnablesAllReusableChrome() {
        assertTrue(
            resolveSharedLiquidGlassChromeEnabled(
                individualEnabled = false,
                uiStyle = AppUiStyle.MATERIAL3,
                androidNativeLiquidGlassEnabled = true
            )
        )
        assertTrue(
            resolveSharedLiquidGlassChromeEnabled(
                individualEnabled = false,
                uiStyle = AppUiStyle.MIUIX,
                androidNativeLiquidGlassEnabled = true
            )
        )
    }

    @Test
    fun sharedLiquidGlass_material3WithoutGlobalKeepsIndividualOff() {
        assertFalse(
            resolveSharedLiquidGlassChromeEnabled(
                individualEnabled = true,
                uiStyle = AppUiStyle.MATERIAL3,
                androidNativeLiquidGlassEnabled = false
            )
        )
    }

    @Test
    fun sharedLiquidGlass_miuixAllowsIndividualWithoutGlobal() {
        assertTrue(
            resolveSharedLiquidGlassChromeEnabled(
                individualEnabled = true,
                uiStyle = AppUiStyle.MIUIX,
                androidNativeLiquidGlassEnabled = false
            )
        )
        assertFalse(
            resolveSharedLiquidGlassChromeEnabled(
                individualEnabled = false,
                uiStyle = AppUiStyle.MIUIX,
                androidNativeLiquidGlassEnabled = false
            )
        )
    }
}
