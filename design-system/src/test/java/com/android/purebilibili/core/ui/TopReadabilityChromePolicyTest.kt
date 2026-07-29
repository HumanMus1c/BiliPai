package com.android.purebilibili.core.ui

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TopReadabilityChromePolicyTest {

    @Test
    fun `top readability chrome uses lightweight gradient as the default layer`() {
        val spec = resolveTopReadabilityChromeSpec(
            requestedHeightDp = 96,
            surfaceColor = Color.White,
            surfaceAlpha = 0.72f,
            hazeRequested = false,
            hasHazeState = false,
        )

        assertEquals(96, spec.heightDp)
        assertEquals(0.72f, spec.surfaceAlpha)
        assertEquals(0f, spec.bottomAlpha)
        assertTrue(spec.drawGradient)
        assertFalse(spec.useHaze)
    }

    @Test
    fun `top readability chrome enables haze only when both requested and available`() {
        assertTrue(
            resolveTopReadabilityChromeSpec(
                requestedHeightDp = 88,
                surfaceColor = Color.Black,
                surfaceAlpha = 0.64f,
                hazeRequested = true,
                hasHazeState = true,
            ).useHaze
        )
        assertFalse(
            resolveTopReadabilityChromeSpec(
                requestedHeightDp = 88,
                surfaceColor = Color.Black,
                surfaceAlpha = 0.64f,
                hazeRequested = true,
                hasHazeState = false,
            ).useHaze
        )
    }
}
