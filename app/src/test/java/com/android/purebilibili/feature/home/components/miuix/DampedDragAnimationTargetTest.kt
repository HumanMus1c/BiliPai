package com.android.purebilibili.feature.home.components.miuix

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals

class DampedDragAnimationTargetTest {

    @Test
    fun `requested target changes before animation coroutine starts`() {
        val dispatcher = StandardTestDispatcher()
        val animation = DampedDragAnimation(
            animationScope = TestScope(dispatcher),
            initialValue = 1f,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 78f / 56f,
            onDragStarted = {},
            onDragStopped = {},
            onDrag = { _, _ -> },
        )

        animation.updateValue(0f)

        // Do not advance the dispatcher: the next pointer event must already accumulate from the
        // new target even though Animatable has not started its spring yet.
        assertEquals(0f, animation.targetValue)
    }
}
