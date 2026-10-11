package com.android.purebilibili.core.ui.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class AppLiquidAwareTabRowConstraintPolicyTest {
    @Test
    fun liquidTabsOnlyUseClippedScrollViewportForActualOverflow() {
        val source = File(
            "app/src/main/java/com/android/purebilibili/core/ui/components/AppLiquidAwareTabRow.kt",
        ).readText()

        assertTrue(source.contains("BoxWithConstraints(modifier = modifier"))
        assertTrue(source.contains("resolveLiquidGlassTabMinWidth("))
        assertTrue(source.contains("constraints.hasBoundedWidth && contentWidth > maxWidth"))
        assertTrue(source.contains(".liquidDockViewport()"))
        assertTrue(source.contains("onIndicatorPositionChanged = { position ->"))
        assertTrue(source.contains("resolveScrollableTabIndicatorFollowDeltaPx("))
        assertTrue(source.contains("scrollState.dispatchRawDelta("))
    }
}
