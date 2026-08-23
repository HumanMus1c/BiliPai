package com.android.purebilibili.core.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerOrientationPolicyTest {
    @Test
    fun `physical orientation remains available below 600dp`() {
        assertTrue(
            shouldRequestPhysicalPlayerOrientation(
                smallestScreenWidthDp = 599,
                platformIgnoresLargeScreenOrientationRequests = true,
            )
        )
    }

    @Test
    fun `pre Android 16 tablets retain direct fullscreen rotation`() {
        assertTrue(
            shouldRequestPhysicalPlayerOrientation(
                smallestScreenWidthDp = 600,
                platformIgnoresLargeScreenOrientationRequests = false,
            )
        )
        assertTrue(
            shouldRequestPhysicalPlayerOrientation(
                smallestScreenWidthDp = 720,
                platformIgnoresLargeScreenOrientationRequests = false,
            )
        )
    }

    @Test
    fun `Android 16 plus large screens use platform adaptive orientation`() {
        assertFalse(
            shouldRequestPhysicalPlayerOrientation(
                smallestScreenWidthDp = 600,
                platformIgnoresLargeScreenOrientationRequests = true,
            )
        )
        assertFalse(
            shouldRequestPhysicalPlayerOrientation(
                smallestScreenWidthDp = 720,
                platformIgnoresLargeScreenOrientationRequests = true,
            )
        )
    }
}
