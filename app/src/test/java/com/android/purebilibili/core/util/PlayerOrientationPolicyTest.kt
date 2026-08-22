package com.android.purebilibili.core.util

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerOrientationPolicyTest {
    @Test
    fun `physical orientation remains available below 600dp`() {
        assertTrue(shouldRequestPhysicalPlayerOrientation(599))
    }

    @Test
    fun `large screens use in-window fullscreen from 600dp`() {
        assertFalse(shouldRequestPhysicalPlayerOrientation(600))
        assertFalse(shouldRequestPhysicalPlayerOrientation(720))
    }
}
