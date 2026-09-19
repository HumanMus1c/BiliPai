package com.android.purebilibili.feature.audio.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AudioNowPlayingBarMotionPolicyTest {

    @Test
    fun landingEasingProducesOvershoot() {
        val start = AudioNowPlayingBarLandingEasing.transform(0f)
        val end = AudioNowPlayingBarLandingEasing.transform(1f)
        assertEquals(0f, start, 0.001f)
        assertEquals(1f, end, 0.001f)

        // Overshoot curve reaches > 1.0 around mid-to-late progress
        val mid = AudioNowPlayingBarLandingEasing.transform(0.6f)
        assertTrue(mid > 1.0f, "Expected overshoot above 1.0f at t=0.6, got $mid")
    }

    @Test
    fun landingOffsetYBehavior() {
        assertEquals(0f, resolveAudioNowPlayingBarLandingOffsetY(1f, 14f))
        assertEquals(-14f, resolveAudioNowPlayingBarLandingOffsetY(0f, 14f))
        val overshootOffset = resolveAudioNowPlayingBarLandingOffsetY(1.08f, 14f)
        assertTrue(overshootOffset > 0f, "Expected positive cushion dip during overshoot, got $overshootOffset")
    }

    @Test
    fun landingScaleSquashAndStretch() {
        val (restX, restY) = resolveAudioNowPlayingBarLandingScale(1f)
        assertEquals(1f, restX)
        assertEquals(1f, restY)

        val (startX, startY) = resolveAudioNowPlayingBarLandingScale(0f)
        assertEquals(0.95f, startX, 0.001f)
        assertEquals(0.95f, startY, 0.001f)

        val (squashX, squashY) = resolveAudioNowPlayingBarLandingScale(1.08f)
        assertTrue(squashX > 1f, "Expected horizontal expansion on impact")
        assertTrue(squashY < 1f, "Expected vertical compression on impact")
    }

    @Test
    fun landingAlphaSoftFade() {
        assertEquals(0.75f, resolveAudioNowPlayingBarLandingAlpha(0f), 0.001f)
        assertEquals(1f, resolveAudioNowPlayingBarLandingAlpha(1f), 0.001f)
        assertEquals(1f, resolveAudioNowPlayingBarLandingAlpha(1.15f), 0.001f)
    }

    @Test
    fun shouldTriggerLandingGuard() {
        // Not returning: do not trigger
        assertFalse(resolveAudioNowPlayingBarShouldTriggerLanding(false, "BV123", "BV123"))
        // Returning and matches: trigger
        assertTrue(resolveAudioNowPlayingBarShouldTriggerLanding(true, "BV123", "BV123"))
        // Returning and target is null/blank (unrestricted): trigger
        assertTrue(resolveAudioNowPlayingBarShouldTriggerLanding(true, null, "BV123"))
        assertTrue(resolveAudioNowPlayingBarShouldTriggerLanding(true, "", "BV123"))
        // Target mismatch: do not trigger
        assertFalse(resolveAudioNowPlayingBarShouldTriggerLanding(true, "BV999", "BV123"))
        // Blank current: do not trigger
        assertFalse(resolveAudioNowPlayingBarShouldTriggerLanding(true, "BV123", ""))
        // Shared transition active: suppress duplicate landing bounce
        assertFalse(resolveAudioNowPlayingBarShouldTriggerLanding(true, "BV123", "BV123", isSharedTransitionActive = true))
    }

    @Test
    fun reduceMotionPolicy() {
        assertFalse(resolveAudioNowPlayingBarLandingMotionEnabled(reduceMotion = true))
        assertTrue(resolveAudioNowPlayingBarLandingMotionEnabled(reduceMotion = false))
    }

    @Test
    fun audioNowPlayingBarIntegratesSharedTransitionAndLandingBounce() {
        val barSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/audio/screen/AudioNowPlayingBar.kt"
        )
        assertTrue(barSource.contains("CardPositionManager.recordVideoCardPosition"))
        assertTrue(barSource.contains("AudioNowPlayingBarLandingEasing"))
        assertTrue(barSource.contains("resolveAudioNowPlayingBarLandingScale"))
        assertTrue(barSource.contains("resolveAudioNowPlayingBarLandingOffsetY"))
        assertTrue(barSource.contains("resolveAudioNowPlayingBarLandingAlpha"))
        assertTrue(barSource.contains("resolveAudioNowPlayingBarShouldTriggerLanding"))

        val navSource = loadSource(
            "app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt"
        )
        assertTrue(navSource.contains("isReturningFromDetail = navigation3ReturnSession.isReturningFromDetail"))
        assertTrue(navSource.contains("returningDetailBvid = navigation3ReturnSession.transitionSession?.bvid"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        return listOf(File(path), File(normalizedPath)).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
