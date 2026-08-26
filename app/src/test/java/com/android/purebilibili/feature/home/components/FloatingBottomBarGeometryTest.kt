package com.android.purebilibili.feature.home.components

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FloatingBottomBarGeometryTest {

    @Test
    fun `upstream effect padding combines 24dp lens and 16dp press bloom`() {
        assertEquals(
            40f,
            resolveFloatingDockEffectPaddingDp(
                refractionAmountDp = MIUIX_UPSTREAM_DOCK_SHELL_LENS_DP,
                pressBloomDp = MIUIX_UPSTREAM_DOCK_PRESS_BLOOM_DP,
            ),
            0.001f,
        )
    }

    @Test
    fun `upstream bottom dock keeps the 56dp resting indicator in a 78dp slot`() {
        assertEquals(
            56f,
            resolveFloatingDockIndicatorHeightDp(requestedHeightDp = 56f, tabWidthDp = 78f),
        )
        assertTrue(78f / 56f >= FLOATING_DOCK_MIN_INDICATOR_ASPECT)
    }

    @Test
    fun `narrow dynamic slots flatten the indicator instead of drawing a circle`() {
        val height = resolveFloatingDockIndicatorHeightDp(
            requestedHeightDp = 44f,
            tabWidthDp = 48f,
        )
        assertEquals(48f / FLOATING_DOCK_MIN_INDICATOR_ASPECT, height, 0.001f)
        assertTrue(height < 44f)
        assertTrue(48f / height >= FLOATING_DOCK_MIN_INDICATOR_ASPECT - 0.001f)
    }

    @Test
    fun `wider search-mode indicator stays inside first and last dock edges`() {
        assertEquals(
            0f,
            resolveFloatingDockIndicatorOffsetPx(
                position = 0f,
                tabWidthPx = 54.2f,
                tabsCount = 5,
                indicatorWidthPx = 69f,
            ),
            0.001f,
        )
        assertEquals(
            202f,
            resolveFloatingDockIndicatorOffsetPx(
                position = 4f,
                tabWidthPx = 54.2f,
                tabsCount = 5,
                indicatorWidthPx = 69f,
            ),
            0.001f,
        )
        assertEquals(
            7.4f,
            resolveFloatingDockIndicatorContentAlignmentPx(
                position = 0f,
                tabWidthPx = 54.2f,
                tabsCount = 5,
                indicatorWidthPx = 69f,
            ),
            0.001f,
        )
        assertEquals(
            -7.4f,
            resolveFloatingDockIndicatorContentAlignmentPx(
                position = 4f,
                tabWidthPx = 54.2f,
                tabsCount = 5,
                indicatorWidthPx = 69f,
            ),
            0.001f,
        )
        assertEquals(
            0f,
            resolveFloatingDockClippedContentTranslationPx(
                position = 0f,
                tabWidthPx = 54.2f,
                tabsCount = 5,
                indicatorWidthPx = 69f,
            ),
            0.001f,
        )
        assertEquals(
            -(54.2f * 5f - 69f),
            resolveFloatingDockClippedContentTranslationPx(
                position = 4f,
                tabWidthPx = 54.2f,
                tabsCount = 5,
                indicatorWidthPx = 69f,
            ),
            0.001f,
        )
    }

    @Test
    fun `drag is rejected in the predictive-back edge bands`() {
        assertFalse(
            shouldAcceptFloatingDockDragAtWindowX(
                windowX = 10f,
                screenWidthPx = 1080f,
                leftInsetPx = 72f,
                rightInsetPx = 72f,
            )
        )
        assertFalse(
            shouldAcceptFloatingDockDragAtWindowX(
                windowX = 1070f,
                screenWidthPx = 1080f,
                leftInsetPx = 72f,
                rightInsetPx = 72f,
            )
        )
        assertTrue(
            shouldAcceptFloatingDockDragAtWindowX(
                windowX = 540f,
                screenWidthPx = 1080f,
                leftInsetPx = 72f,
                rightInsetPx = 72f,
            )
        )
    }

    @Test
    fun `short search and top docks scale lens so refraction cannot meet in the middle`() {
        assertEquals(1f, resolveFloatingDockGeometryScale(shellHeightDp = 64f))
        assertEquals(40f / 64f, resolveFloatingDockGeometryScale(40f), 0.001f)
        assertEquals(36f / 64f, resolveFloatingDockGeometryScale(36f), 0.001f)
        val shortLens = MIUIX_UPSTREAM_DOCK_SHELL_LENS_DP *
            resolveFloatingDockGeometryScale(36f)
        assertTrue(shortLens * 2f < 36f)
    }

    @Test
    fun `interactive highlight radius covers long capsules without shrinking compact docks`() {
        assertEquals(
            64f * 1.2f,
            resolveDockInteractiveHighlightRadiusPx(
                shellMinDimensionPx = 64f,
                tabWidthPx = 70f,
            ),
            0.001f,
        )
        assertEquals(
            200f,
            resolveDockInteractiveHighlightRadiusPx(
                shellMinDimensionPx = 40f,
                tabWidthPx = 400f,
            ),
            0.001f,
        )
    }

    @Test
    fun `pill highlight rim stays 1dp on home docks and thickens on long indicators`() {
        assertEquals(
            1f,
            resolveDockPillHighlightWidthDp(
                indicatorWidthDp = 70f,
                indicatorHeightDp = 56f,
            ),
            0.001f,
        )
        assertEquals(
            2f,
            resolveDockPillHighlightWidthDp(
                indicatorWidthDp = 400f,
                indicatorHeightDp = 40f,
            ),
            0.001f,
        )
    }

    @Test
    fun `compact docks reuse bottom-bar highlight motion at scaled size`() {
        assertEquals(24f, resolveCompactDockLensDp(64f), 0.001f)
        assertEquals(16f, resolveCompactDockPressBloomDp(64f), 0.001f)
        assertEquals(10f, resolveCompactDockIndicatorLensHeightDp(64f), 0.001f)
        assertEquals(14f, resolveCompactDockIndicatorLensAmountDp(64f), 0.001f)
        assertEquals(8f, resolveCompactDockInnerShadowRadiusDp(64f), 0.001f)
        assertEquals(1.2f, resolveCompactDockTabPressScale(64f), 0.001f)

        val compact = 40f
        val intensity = 40f / 64f
        assertEquals(24f * intensity, resolveCompactDockLensDp(compact), 0.001f)
        assertEquals(16f * intensity, resolveCompactDockPressBloomDp(compact), 0.001f)
        assertEquals(10f * intensity, resolveCompactDockIndicatorLensHeightDp(compact), 0.001f)
        assertTrue(resolveCompactDockLensDp(compact) * 2f < compact)
        assertTrue(resolveCompactDockTabPressScale(compact) < 1.2f)
        assertTrue(resolveCompactDockTabPressScale(compact) > 1f)
    }

    @Test
    fun `home rest indicator keeps a 4dp vertical inset inside the 64dp shell`() {
        assertEquals(
            4f,
            resolveFloatingDockRestIndicatorVerticalInsetDp(
                shellHeightDp = 64f,
                indicatorHeightDp = 56f,
            ),
            0.001f,
        )
        assertEquals(
            0f,
            resolveFloatingDockRestIndicatorVerticalInsetDp(
                shellHeightDp = 56f,
                indicatorHeightDp = 56f,
            ),
            0.001f,
        )
    }

    @Test
    fun `tight shell-height constraints do not steal rest inset for press overflow`() {
        val overflowPx = 7
        assertFalse(
            shouldReserveFloatingDockScaleOverflow(
                incomingMaxHeightPx = 64,
                shellHeightPx = 64,
                overflowPx = overflowPx,
            )
        )
        assertTrue(
            shouldReserveFloatingDockScaleOverflow(
                incomingMaxHeightPx = 64 + overflowPx * 2,
                shellHeightPx = 64,
                overflowPx = overflowPx,
            )
        )
        assertTrue(
            shouldReserveFloatingDockScaleOverflow(
                incomingMaxHeightPx = androidx.compose.ui.unit.Constraints.Infinity,
                shellHeightPx = 64,
                overflowPx = overflowPx,
            )
        )
    }

    @Test
    fun `pressed indicator overflow is reserved so compact docks are not clipped`() {
        val homeOverflow = resolveCompactDockScaleOverflowDp(
            shellHeightDp = 64f,
            indicatorHeightDp = 48f,
        )
        val compactOverflow = resolveCompactDockScaleOverflowDp(
            shellHeightDp = 40f,
            indicatorHeightDp = 30f,
        )
        assertEquals((78f - 64f) / 2f, homeOverflow, 0.001f)
        assertEquals((40f * 78f / 64f - 40f) / 2f, compactOverflow, 0.001f)
        assertTrue(compactOverflow > 0f)
    }

    @Test
    fun `narrow fitted indicator still grows beyond the dock while dragging`() {
        val fittedHeight = resolveFloatingDockIndicatorHeightDp(
            requestedHeightDp = 39f,
            tabWidthDp = 42f,
        )
        val geometry = com.android.purebilibili.core.ui.resolveMatchedLiquidIndicatorGeometry(
            dockHeightDp = 44f,
            indicatorHeightDp = fittedHeight,
        )

        assertTrue(fittedHeight < 39f)
        assertTrue(geometry.pressedHeightDp > geometry.dockHeightDp)
    }

    @Test
    fun `wide edge indicator capture covers drag scale and velocity stretch`() {
        val indicatorWidth = 470f
        val captureInsets = resolveFloatingDockCaptureInsets(
            shellHeightDp = 44f,
            requestedIndicatorHeightDp = 39f,
            indicatorWidthDp = indicatorWidth,
        )
        val geometry = com.android.purebilibili.core.ui.resolveMatchedLiquidIndicatorGeometry(
            dockHeightDp = 44f,
            indicatorHeightDp = 39f,
        )
        val restingScaleOverflow = indicatorWidth * (geometry.pressedScale - 1f) / 2f

        assertTrue(captureInsets.horizontalDp > restingScaleOverflow)
        assertTrue(captureInsets.verticalDp > 0f)
        assertTrue(captureInsets.horizontalDp > captureInsets.verticalDp)
    }

    @Test
    fun `system gesture inset never shrinks below the fallback edge`() {
        assertEquals(24f, resolveFloatingDockDragEdgeInsetPx(systemInsetPx = 0f, fallbackPx = 24f))
        assertEquals(80f, resolveFloatingDockDragEdgeInsetPx(systemInsetPx = 80f, fallbackPx = 24f))
    }
}
