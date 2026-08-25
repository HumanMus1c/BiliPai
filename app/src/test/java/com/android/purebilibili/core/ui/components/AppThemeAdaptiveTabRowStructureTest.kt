package com.android.purebilibili.core.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppThemeAdaptiveTabRowStructureTest {
    @Test
    fun `global tab row lets liquid setting override md3 underline`() {
        val source = File(
            "app/src/main/java/com/android/purebilibili/core/ui/components/AppLiquidAwareTabRow.kt"
        ).readText()
        val adaptiveEntry = source
            .substringAfter("fun <T> AppThemeAdaptiveTabRow(")
            .substringBefore("fun <T> AppLiquidAwareTabRow(")

        assertTrue(adaptiveEntry.contains("AppLiquidAwareTabRow("))
        assertTrue(adaptiveEntry.contains("miuixBackdrop = miuixBackdrop"))
        assertFalse(adaptiveEntry.contains("AppNativeTabRow("))
        assertFalse(adaptiveEntry.contains("LocalAppUiStyle"))
    }
}
