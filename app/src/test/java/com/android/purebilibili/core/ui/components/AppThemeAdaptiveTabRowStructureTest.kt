package com.android.purebilibili.core.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppThemeAdaptiveTabRowStructureTest {
    @Test
    fun `global tab row uses native theme renderer when liquid glass is disabled`() {
        val source = File(
            "app/src/main/java/com/android/purebilibili/core/ui/components/AppLiquidAwareTabRow.kt"
        ).readText()
        val adaptiveEntry = source
            .substringAfter("fun <T> AppThemeAdaptiveTabRow(")
            .substringBefore("fun <T> AppLiquidAwareTabRow(")

        assertTrue(adaptiveEntry.contains("AppLiquidAwareTabRow("))
        assertTrue(adaptiveEntry.contains("miuixBackdrop = miuixBackdrop"))
        assertTrue(adaptiveEntry.contains("dragSelectionEnabled = dragSelectionEnabled"))
        assertTrue(adaptiveEntry.contains("tapPressRefractionEnabled = tapPressRefractionEnabled"))
        assertTrue(adaptiveEntry.contains("height = height"))
        assertTrue(adaptiveEntry.contains("indicatorHeight = indicatorHeight"))
        assertEquals(
            2,
            source.lineSequence().count {
                it.contains("minTabWidth: Dp = AppChromeSizeTokens.MinimumTouchTarget")
            },
        )
        assertFalse(adaptiveEntry.contains("AppNativeTabRow("))
        assertFalse(adaptiveEntry.contains("LocalAppUiStyle"))

        val adaptiveRenderer = source.substringAfter("fun <T> AppLiquidAwareTabRow(")
        assertTrue(adaptiveRenderer.contains("LocalAppThemeConfig.current.liquidGlassEnabled"))
        assertTrue(adaptiveRenderer.contains("!liquidGlassEnabled"))
        assertTrue(adaptiveRenderer.contains("AppNativeTabRow("))
        assertTrue(adaptiveRenderer.contains("allowLabelOverflow = true"))
        assertTrue(adaptiveRenderer.contains("indicatorPositionProvider = indicatorPositionProvider"))
        assertTrue(adaptiveRenderer.contains("isScrollInProgressProvider = isScrollInProgressProvider"))
        assertTrue(adaptiveRenderer.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(adaptiveRenderer.contains("resolvedDragSelectionEnabled"))
        assertTrue(adaptiveRenderer.contains("tapPressRefractionEnabled = tapPressRefractionEnabled"))
        assertTrue(adaptiveRenderer.contains("height = height"))
        assertTrue(adaptiveRenderer.contains("indicatorHeight = indicatorHeight"))
    }
}
