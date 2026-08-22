package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.store.LiquidGlassAdvancedPreset
import kotlin.test.Test
import kotlin.test.assertEquals

class LiquidGlassPresetSliderPolicyTest {

    @Test
    fun `preset slider exposes four evenly spaced stops`() {
        assertEquals(0f, liquidGlassPresetSliderValue(LiquidGlassAdvancedPreset.READABLE))
        assertEquals(
            1f / 3f,
            liquidGlassPresetSliderValue(LiquidGlassAdvancedPreset.BALANCED),
        )
        assertEquals(
            2f / 3f,
            liquidGlassPresetSliderValue(LiquidGlassAdvancedPreset.PRISM),
        )
        assertEquals(1f, liquidGlassPresetSliderValue(LiquidGlassAdvancedPreset.CUSTOM))
    }

    @Test
    fun `preset slider rounds drag values to the nearest stop`() {
        assertEquals(
            LiquidGlassAdvancedPreset.READABLE,
            liquidGlassPresetFromSliderValue(0.08f),
        )
        assertEquals(
            LiquidGlassAdvancedPreset.BALANCED,
            liquidGlassPresetFromSliderValue(0.40f),
        )
        assertEquals(
            LiquidGlassAdvancedPreset.PRISM,
            liquidGlassPresetFromSliderValue(0.72f),
        )
        assertEquals(
            LiquidGlassAdvancedPreset.CUSTOM,
            liquidGlassPresetFromSliderValue(1.2f),
        )
    }
}
