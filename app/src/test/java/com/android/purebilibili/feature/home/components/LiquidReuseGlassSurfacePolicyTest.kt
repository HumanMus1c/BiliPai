package com.android.purebilibili.feature.home.components

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LiquidReuseGlassSurfacePolicyTest {

    @Test
    fun glassColorPathRequiresActiveMotionWhenRequested() {
        assertFalse(
            resolveSharedLiquidIndicatorUseGlassColorPath(
                liquidGlassEnabled = true,
                lensProgress = 1f,
                requireActiveMotion = true,
                isDragging = false,
                motionProgress = 0f,
            )
        )
        assertTrue(
            resolveSharedLiquidIndicatorUseGlassColorPath(
                liquidGlassEnabled = true,
                lensProgress = 1f,
                requireActiveMotion = true,
                isDragging = true,
                motionProgress = 0f,
            )
        )
        assertTrue(
            resolveSharedLiquidIndicatorUseGlassColorPath(
                liquidGlassEnabled = true,
                lensProgress = 0.5f,
                requireActiveMotion = true,
                isDragging = false,
                motionProgress = 0.2f,
            )
        )
    }

    @Test
    fun glassColorPathWithoutMotionRequirementKeepsLegacyBehavior() {
        assertTrue(
            resolveSharedLiquidIndicatorUseGlassColorPath(
                liquidGlassEnabled = true,
                lensProgress = 0.5f,
            )
        )
        assertFalse(
            resolveSharedLiquidIndicatorUseGlassColorPath(
                liquidGlassEnabled = true,
                lensProgress = 0f,
            )
        )
    }

    @Test
    fun inContentShellIsMoreTransparentThanDockBase() {
        val base = Color.White.copy(alpha = 0.55f)
        val dock = resolveLiquidReuseShellContainerColor(
            baseColor = base,
            glassEnabled = true,
            chromeContext = LiquidReuseChromeContext.FLOATING_DOCK,
        )
        val inContent = resolveLiquidReuseShellContainerColor(
            baseColor = base,
            glassEnabled = true,
            chromeContext = LiquidReuseChromeContext.IN_CONTENT_SEGMENTED,
        )
        assertEquals(base.alpha, dock.alpha, absoluteTolerance = 0.02f)
        assertTrue(inContent.alpha + 0.001f < dock.alpha)
        assertTrue(inContent.alpha <= 0.10f)
    }

    @Test
    fun topTabIdleSurfaceMatchesFloatingDock() {
        listOf(false, true).forEach { darkTheme ->
            assertEquals(
                resolveLiquidReuseIndicatorIdleSurfaceColor(
                    darkTheme = darkTheme,
                    chromeContext = LiquidReuseChromeContext.FLOATING_DOCK,
                ),
                resolveLiquidReuseIndicatorIdleSurfaceColor(
                    darkTheme = darkTheme,
                    chromeContext = LiquidReuseChromeContext.TOP_TAB,
                ),
            )
        }
        assertEquals(
            1f,
            resolveLiquidReuseIdleSurfaceMaxAlpha(LiquidReuseChromeContext.FLOATING_DOCK),
            absoluteTolerance = 0.001f,
        )
        assertEquals(
            resolveLiquidReuseIdleSurfaceMaxAlpha(LiquidReuseChromeContext.FLOATING_DOCK),
            resolveLiquidReuseIdleSurfaceMaxAlpha(LiquidReuseChromeContext.TOP_TAB),
            absoluteTolerance = 0.001f,
        )
        assertTrue(
            resolveLiquidReuseIdleSurfaceMaxAlpha(LiquidReuseChromeContext.IN_CONTENT_SEGMENTED) <= 0.28f
        )
    }

    @Test
    fun exportSurfaceIsNearlyClearForInContentReuse() {
        val shell = Color.White.copy(alpha = 0.08f)
        val export = resolveLiquidReuseExportSurfaceColor(
            shellContainerColor = shell,
            chromeContext = LiquidReuseChromeContext.IN_CONTENT_SEGMENTED,
        )
        assertTrue(export.alpha <= 0.04f)
        assertEquals(
            shell.alpha,
            resolveLiquidReuseExportSurfaceColor(
                shellContainerColor = shell,
                chromeContext = LiquidReuseChromeContext.FLOATING_DOCK,
            ).alpha,
            absoluteTolerance = 0.001f,
        )
    }
}
