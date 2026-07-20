package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

    @Test
    fun appearanceDescriptionCardAndAdaptiveTooltipWireOfficialPath() {
        val tooltipSource = load(
            "app/src/main/java/com/android/purebilibili/core/ui/AdaptiveTooltip.kt"
        )
        val appearanceSource = load(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt"
        )

        assertTrue(tooltipSource.contains("MiuixTooltipBox("))
        assertTrue(tooltipSource.contains("rememberPresetPrimitiveRenderer()"))
        assertTrue(appearanceSource.contains("AdaptivePlainTooltipBox("))
    }

    private fun load(path: String): String {
        val normalized = path.removePrefix("app/")
        return listOf(File(path), File(normalized))
            .first { it.exists() }
            .readText()
    }
}
