package com.android.purebilibili.core.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class AppLiquidAwareSearchFieldStructureTest {
    @Test
    fun `liquid search matches shared dock geometry and keeps native fallback`() {
        val source = File(
            "app/src/main/java/com/android/purebilibili/core/ui/components/AppLiquidAwareSearchField.kt"
        ).readText()

        assertTrue(source.contains("BottomBarMatchedReusableLiquidDock("))
        assertTrue(source.contains("shape = CircleShape"))
        assertTrue(source.contains("useNeutralLiquidContainer = true"))
        assertTrue(source.contains("drawShellLens = true"))
        assertTrue(source.contains("shellLensIntensity = resolveFloatingDockGeometryScale("))
        assertTrue(source.contains("BottomBarMatchedSegmentedControlHeightDp.dp"))
        assertTrue(source.contains("containerColor = if (liquidChromeActive)"))
        assertTrue(source.contains("heightOverride = if (liquidChromeActive)"))
    }
}
