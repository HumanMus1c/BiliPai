package com.android.purebilibili.feature.home.components

import androidx.compose.animation.core.Spring
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeTopTabMotionSpecTest {
    @Test
    fun iosCapsule_preservesItsTrackingPhysics() {
        val spec = iosTopTabCapsuleMotionSpec<Float>()

        assertEquals(0.68f, spec.dampingRatio)
        assertEquals(Spring.StiffnessMediumLow, spec.stiffness)
    }
}
