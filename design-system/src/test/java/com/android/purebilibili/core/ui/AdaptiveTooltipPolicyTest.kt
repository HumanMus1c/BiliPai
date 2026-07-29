package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTooltipPolicyTest {

    @Test
    fun miuixUsesOfficialTooltipBoxRenderer() {
        assertEquals(
            AdaptiveTooltipRenderer.MIUIX_TOOLTIP_BOX,
            resolveAdaptiveTooltipRenderer(UiPreset.MD3, AndroidNativeVariant.MIUIX)
        )
    }

    @Test
    fun materialAndIosPassThroughTooltip() {
        assertEquals(
            AdaptiveTooltipRenderer.PASSTHROUGH,
            resolveAdaptiveTooltipRenderer(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        )
        assertEquals(
            AdaptiveTooltipRenderer.PASSTHROUGH,
            resolveAdaptiveTooltipRenderer(UiPreset.IOS, AndroidNativeVariant.MATERIAL3)
        )
    }
}
