package com.android.purebilibili.feature.video.ui.section

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoPlayerSurfacePolicyTest {

    @Test
    fun `flip disabled keeps default surface type`() {
        assertFalse(
            shouldUseTextureSurfaceForFlip(
                isFlippedHorizontal = false,
                isFlippedVertical = false
            )
        )
    }

    @Test
    fun `horizontal flip requires texture surface`() {
        assertTrue(
            shouldUseTextureSurfaceForFlip(
                isFlippedHorizontal = true,
                isFlippedVertical = false
            )
        )
    }

    @Test
    fun `vertical flip requires texture surface`() {
        assertTrue(
            shouldUseTextureSurfaceForFlip(
                isFlippedHorizontal = false,
                isFlippedVertical = true
            )
        )
    }

    @Test
    fun `live back preview requires texture surface`() {
        assertTrue(
            shouldUseTextureSurfaceForFlip(
                isFlippedHorizontal = false,
                isFlippedVertical = false,
                liveBackPreview = true
            )
        )
    }

    @Test
    fun `navigation transform requires texture surface before back starts`() {
        assertTrue(
            shouldUseTextureSurfaceForFlip(
                isFlippedHorizontal = false,
                isFlippedVertical = false,
                navigationTransformEnabled = true
            )
        )
    }

    @Test
    fun `player surface stays hidden until smooth reveal starts`() {
        val spec = resolveVideoPlayerSurfaceRevealSpec(
            forceCoverDuringReturnAnimation = false,
            shouldKeepCoverForManualStart = false,
            hasStartedSmoothReveal = false
        )

        assertEquals(0f, spec.alpha)
        assertEquals(0.985f, spec.scale)
    }

    @Test
    fun `player surface animates to fully visible when smooth reveal starts`() {
        val spec = resolveVideoPlayerSurfaceRevealSpec(
            forceCoverDuringReturnAnimation = false,
            shouldKeepCoverForManualStart = false,
            hasStartedSmoothReveal = true
        )

        assertEquals(1f, spec.alpha)
        assertEquals(1f, spec.scale)
    }

    @Test
    fun `forced return and manual start keep player surface hidden`() {
        val forcedReturnSpec = resolveVideoPlayerSurfaceRevealSpec(
            forceCoverDuringReturnAnimation = true,
            shouldKeepCoverForManualStart = false,
            hasStartedSmoothReveal = true
        )
        val manualStartSpec = resolveVideoPlayerSurfaceRevealSpec(
            forceCoverDuringReturnAnimation = false,
            shouldKeepCoverForManualStart = true,
            hasStartedSmoothReveal = true
        )

        assertEquals(0f, forcedReturnSpec.alpha)
        assertEquals(1f, forcedReturnSpec.scale)
        assertEquals(0f, manualStartSpec.alpha)
        assertEquals(1f, manualStartSpec.scale)
    }

    @Test
    fun `inline player view stays visible during manual start cover so first frame can render`() {
        // CoverFirst 只靠封面叠层 + surface alpha 挡画面，不得 INVISIBLE PlayerView，
        // 否则重进/点播后永远等不到 RENDERED_FIRST_FRAME。
        assertTrue(
            shouldShowInlinePlayerView(
                isPortraitFullscreen = false,
                forceCoverDuringReturnAnimation = false,
                shouldKeepCoverForManualStart = true
            )
        )
        assertTrue(
            shouldShowInlinePlayerView(
                isPortraitFullscreen = false,
                forceCoverDuringReturnAnimation = false,
                shouldKeepCoverForManualStart = false
            )
        )
        assertFalse(
            shouldShowInlinePlayerView(
                isPortraitFullscreen = false,
                forceCoverDuringReturnAnimation = true,
                shouldKeepCoverForManualStart = false
            )
        )
    }
}
