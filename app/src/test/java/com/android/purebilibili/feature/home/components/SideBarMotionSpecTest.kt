package com.android.purebilibili.feature.home.components

import kotlin.test.Test
import kotlin.test.assertEquals

class SideBarMotionSpecTest {
    @Test
    fun interactionSprings_preserveSelectionAndWobblePhysics() {
        val selection = sideBarSelectionScaleMotionSpec<Float>()
        val wobble = sideBarWobbleMotionSpec<Float>()

        assertEquals(0.35f, selection.dampingRatio)
        assertEquals(300f, selection.stiffness)
        assertEquals(0.2f, wobble.dampingRatio)
        assertEquals(600f, wobble.stiffness)
    }
}
