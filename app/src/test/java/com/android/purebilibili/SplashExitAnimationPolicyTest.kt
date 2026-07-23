package com.android.purebilibili

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SplashExitAnimationPolicyTest {

    @Test
    fun clipsSplashFlyoutToLauncherStyleRoundedSquare() {
        assertEquals(24f, splashFlyoutCornerRadiusPx(sizePx = 100), 0.001f)
        assertEquals(48f, splashFlyoutCornerRadiusPx(sizePx = 200), 0.001f)
    }

    @Test
    fun enablesRealtimeBlurOnlyOnAndroid14And15() {
        assertFalse(shouldUseRealtimeSplashBlur(31))
        assertFalse(shouldUseRealtimeSplashBlur(33))
        assertTrue(shouldUseRealtimeSplashBlur(34))
    }

    @Test
    fun disablesRealtimeBlurOnAndroid16RenderThread() {
        assertFalse(shouldUseRealtimeSplashBlur(36))
    }

    @Test
    fun disablesRealtimeBlurBelowAndroid14() {
        assertFalse(shouldUseRealtimeSplashBlur(30))
    }

    @Test
    fun allowsCustomSplashOverlayWhenFlyoutEnabledAndDataPresent() {
        assertTrue(
            shouldShowCustomSplashOverlay(
                customSplashEnabled = true,
                splashUri = "content://splash.jpg"
            )
        )
    }

    @Test
    fun allowsCustomSplashOverlayWhenFlyoutDisabledAndDataPresent() {
        assertTrue(
            shouldShowCustomSplashOverlay(
                customSplashEnabled = true,
                splashUri = "content://splash.jpg"
            )
        )
    }

    @Test
    fun appliesRealtimeBlurOnlyAfterAnimationProgressStarts() {
        assertFalse(shouldApplySplashRealtimeBlur(useRealtimeBlur = true, progress = 0f))
        assertTrue(shouldApplySplashRealtimeBlur(useRealtimeBlur = true, progress = 0.12f))
        assertFalse(shouldApplySplashRealtimeBlur(useRealtimeBlur = false, progress = 0.5f))
    }
}
