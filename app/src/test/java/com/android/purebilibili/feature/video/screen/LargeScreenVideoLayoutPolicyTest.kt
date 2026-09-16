package com.android.purebilibili.feature.video.screen

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LargeScreenVideoLayoutPolicyTest {

    @Test
    fun landscapeLayoutHidesIntroRelatedAndPutsRelatedTabFirst() {
        val source = java.io.File(
            "app/src/main/java/com/android/purebilibili/feature/video/screen/LargeScreenVideoLayout.kt"
        ).takeIf { it.exists() } ?: java.io.File(
            "src/main/java/com/android/purebilibili/feature/video/screen/LargeScreenVideoLayout.kt"
        )
        val text = source.readText()
        assertTrue(text.contains("showRelatedInIntro = false"))
        assertTrue(text.contains("includeRelatedTab = true"))
        assertTrue(text.contains("includeOwnerUploadsTab = true"))
        assertTrue(text.contains("showRelatedVideos = showRelatedInIntro"))
        assertTrue(text.contains("LargeScreenVideoLayoutMode.AlmostSquare"))
        assertFalse(text.contains("fixedTab = TabletSecondaryTab.COLLECTION"))
    }

    @Test

    @Test
    fun collectionGetsOwnColumnOnlyWhenEachPaneIsAtLeast280dp() {
        assertFalse(
            shouldUseDedicatedCollectionColumn(
                availableWidthDp = 800f,
                hasCollection = true,
            )
        )
        assertTrue(
            shouldUseDedicatedCollectionColumn(
                availableWidthDp = 960f,
                hasCollection = true,
            )
        )
        assertFalse(
            shouldUseDedicatedCollectionColumn(
                availableWidthDp = 1280f,
                hasCollection = false,
            )
        )
    }

    @Test
    fun landscapeTabletUsesLeftPlayerAndClampedSidePane() {
        val metrics = resolveLargeScreenVideoMetrics(
            windowWidthDp = 1280f,
            windowHeightDp = 800f,
            isVerticalVideo = false,
        )
        assertEquals(LargeScreenVideoLayoutMode.Landscape, metrics.mode)
        assertTrue(metrics.introBelowPlayer)
        assertTrue(metrics.sidePaneWidthDp in 280f..425f)
        assertEquals(1280f - metrics.sidePaneWidthDp, metrics.playerWidthDp, 0.5f)
        assertEquals(metrics.playerWidthDp / (16f / 9f), metrics.playerHeightDp, 1f)
    }

    @Test
    fun verticalVideoInLandscapeUsesThreePanes() {
        val metrics = resolveLargeScreenVideoMetrics(
            windowWidthDp = 1280f,
            windowHeightDp = 800f,
            isVerticalVideo = true,
            enableVerticalExpand = true,
        )
        assertEquals(LargeScreenVideoLayoutMode.VerticalThreePane, metrics.mode)
        assertFalse(metrics.introBelowPlayer)
        assertEquals(800f / (16f / 9f), metrics.playerWidthDp, 1f)
        assertEquals((1280f - metrics.playerWidthDp) / 2f, metrics.sidePaneWidthDp, 1f)
    }

    @Test
    fun compactPortraitStaysOnPhoneLayout() {
        assertFalse(
            shouldUseLargeScreenVideoLayout(
                windowWidthDp = 393f,
                windowHeightDp = 851f,
                horizontalAdaptationEnabled = true,
            )
        )
        val metrics = resolveLargeScreenVideoMetrics(
            windowWidthDp = 393f,
            windowHeightDp = 851f,
            isVerticalVideo = false,
        )
        assertEquals(LargeScreenVideoLayoutMode.Phone, metrics.mode)
    }

    @Test
    fun disabledHorizontalAdaptationNeverEntersLargeScreenLayout() {
        assertFalse(
            shouldUseLargeScreenVideoLayout(
                windowWidthDp = 1280f,
                windowHeightDp = 800f,
                horizontalAdaptationEnabled = false,
            )
        )
    }

    @Test
    fun verticalVideoDefaultsToLandscapeWithoutExpand() {
        val metrics = resolveLargeScreenVideoMetrics(
            windowWidthDp = 1280f,
            windowHeightDp = 800f,
            isVerticalVideo = true,
        )
        assertEquals(LargeScreenVideoLayoutMode.Landscape, metrics.mode)
    }

    @Test
    fun landscapeSidePaneStaysBetween280And425() {
        val width = resolveLargeScreenLandscapePlayerWidthDp(1280f, 800f)
        val side = 1280f - width
        assertTrue(side in 280f..425f)
    }
}
