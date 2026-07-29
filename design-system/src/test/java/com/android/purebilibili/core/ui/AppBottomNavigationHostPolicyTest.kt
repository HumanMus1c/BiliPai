package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppBottomNavigationHostPolicyTest {

    @Test
    fun `cupertino renderer keeps the individual liquid glass switch`() {
        val policy = resolveAppBottomNavigationVisualPolicy(
            renderer = PresetPrimitiveRenderer.IOS,
            individualLiquidGlassEnabled = true,
            androidNativeLiquidGlassEnabled = false,
        )

        assertTrue(policy.liquidGlassEnabled)
    }

    @Test
    fun `android renderers require the native liquid glass switch`() {
        listOf(
            PresetPrimitiveRenderer.MATERIAL3,
            PresetPrimitiveRenderer.MIUIX_BRIDGED,
        ).forEach { renderer ->
            val policy = resolveAppBottomNavigationVisualPolicy(
                renderer = renderer,
                individualLiquidGlassEnabled = true,
                androidNativeLiquidGlassEnabled = false,
            )

            assertFalse(policy.liquidGlassEnabled)
        }
    }

    @Test
    fun `native liquid glass switch enables every renderer`() {
        PresetPrimitiveRenderer.entries.forEach { renderer ->
            val policy = resolveAppBottomNavigationVisualPolicy(
                renderer = renderer,
                individualLiquidGlassEnabled = false,
                androidNativeLiquidGlassEnabled = true,
            )

            assertTrue(policy.liquidGlassEnabled)
        }
    }
}
