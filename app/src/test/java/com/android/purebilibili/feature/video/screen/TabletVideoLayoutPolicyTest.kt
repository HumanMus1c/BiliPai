package com.android.purebilibili.feature.video.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TabletVideoLayoutPolicyTest {

    @Test
    fun secondaryPaneAlwaysDefaultsToComments() {
        assertEquals(0, resolveTabletSecondaryDefaultTab())
        assertTrue(shouldShowTabletSecondaryDanmakuActions())
    }

    @Test
    fun secondaryPaneHostsDanmakuSendAndToggle() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/screen/TabletVideoLayout.kt"
        ).readText()

        assertTrue(source.contains("TabletSecondaryDanmakuActions("))
        assertTrue(source.contains("onDanmakuInputClick = playbackActions.showDanmakuSendDialog"))
        assertTrue(source.contains("fun TabletSecondaryDanmakuActions("))
        assertTrue(source.contains("NativeDanmakuToggleButton("))
    }

    @Test
    fun expandedTablet_prioritizesPrimaryPaneWidth() {
        val policy = resolveTabletVideoLayoutPolicy(widthDp = 1280)

        assertEquals(0.72f, policy.primaryRatio)
        assertEquals(1080, policy.playerMaxWidthDp)
        assertEquals(1000, policy.infoMaxWidthDp)
    }

    @Test
    fun ultraWideTablet_balancesPaneRatioAndPlayerCap() {
        val policy = resolveTabletVideoLayoutPolicy(widthDp = 1920)

        assertEquals(0.66f, policy.primaryRatio)
        assertTrue(policy.playerMaxWidthDp >= 1240)
        assertTrue(policy.infoMaxWidthDp >= 1160)
    }

    @Test
    fun ultraWidePolicy_keepsLargePrimaryPane() {
        val policy = resolveTabletVideoLayoutPolicy(widthDp = 1920)

        assertEquals(0.66f, policy.primaryRatio)
        assertEquals(1240, policy.playerMaxWidthDp)
        assertEquals(1160, policy.infoMaxWidthDp)
    }
}
