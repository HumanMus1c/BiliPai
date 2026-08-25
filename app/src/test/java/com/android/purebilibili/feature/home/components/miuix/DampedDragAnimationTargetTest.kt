package com.android.purebilibili.feature.home.components.miuix

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlin.test.Test
import kotlin.test.assertEquals

class DampedDragAnimationTargetTest {

    @Test
    fun `requested target changes synchronously for delta accumulation`() {
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
        // newest requested target even though the spring has not advanced a frame yet.
        assertEquals(0f, animation.targetValue)
    }

    @Test
    fun `direct tracking takes ownership before dispatcher advances`() {
        val dispatcher = StandardTestDispatcher()
        val animation = DampedDragAnimation(
            animationScope = TestScope(dispatcher),
            initialValue = 0f,
            valueRange = 0f..1f,
            visibilityThreshold = 0.001f,
            initialScale = 1f,
            pressedScale = 78f / 56f,
            trackingMode = DampedDragTrackingMode.DIRECT,
            onDragStarted = {},
            onDragStopped = {},
            onDrag = { _, _ -> },
        )

        animation.updateValue(1f)

        // No scheduler advance: a high-frequency next pointer event must not be able to cancel a
        // still-pending position mutation before it has taken ownership of the Animatable.
        assertEquals(1f, animation.value)
        assertEquals(1f, animation.targetValue)
    }
}
