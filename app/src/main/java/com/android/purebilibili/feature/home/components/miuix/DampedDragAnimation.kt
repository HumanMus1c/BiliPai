// Used exclusively by FloatingBottomBar so home dock interactions match that component, not the
// design-system DampedDragAnimationState used by top tabs / segmented controls.
package com.android.purebilibili.feature.home.components.miuix

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class DampedDragTrackingMode {
    SPRING,
    DIRECT,
}

/**
 * Floating dock damped-drag kernel: spring-followed value, press/scale springs, velocity
 * deformation sampling, and [modifier] driven by [inspectDragGestures] + [canDrag].
 */
class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    pressedScale: Float,
    private val trackingMode: DampedDragTrackingMode = DampedDragTrackingMode.SPRING,
    val canDrag: (Offset) -> Boolean = { true },
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
    val onDragStopped: DampedDragAnimation.() -> Unit,
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
) {

    private val valueAnimationSpec =
        spring(1f, 1000f, visibilityThreshold)
    private val velocityAnimationSpec =
        spring(0.5f, 300f, visibilityThreshold * 10f)
    private val pressProgressAnimationSpec =
        spring(1f, 1000f, 0.001f)
    private val scaleXAnimationSpec =
        spring(0.82f, 520f, 0.001f)
    private val scaleYAnimationSpec =
        spring(0.86f, 560f, 0.001f)

    private val valueAnimation =
        Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation =
        Animatable(0f, 5f)
    private val pressProgressAnimation =
        Animatable(0f, 0.001f)
    private val scaleXAnimation =
        Animatable(initialScale, 0.001f)
    private val scaleYAnimation =
        Animatable(initialScale, 0.001f)

    // Pointer events may arrive again before the coroutine launched by updateValue starts.
    // Keep the requested target synchronous so every drag delta accumulates from the latest one.
    private var requestedValue = initialValue.coerceIn(valueRange)

    private val mutatorMutex = MutatorMutex()

    private val velocityTracker = VelocityTracker()
    // Pager progress can request a new position every frame. Keep exactly one value mutation
    // alive so an older coroutine can never run after a newer request and restore stale UI.
    private var valueTrackingJob: Job? = null

    val value: Float get() = valueAnimation.value
    val targetValue: Float get() = requestedValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    val velocity: Float get() = velocityAnimation.value

    var isDragging by mutableStateOf(false)
        private set

    var pressedScale: Float = pressedScale

    val modifier: Modifier = Modifier.pointerInput(Unit) {
        var gestureAccepted = false
        inspectDragGestures(
            onDragStart = { down ->
                // Decide ownership from the initial down and keep it for the whole gesture.
                // A predictive-back swipe may enter the dock after starting in the system edge
                // band; it must never be adopted halfway through by the liquid indicator.
                gestureAccepted = canDrag(down.position)
                if (gestureAccepted) {
                    isDragging = true
                    onDragStarted(down.position)
                    press()
                }
            },
            onDragEnd = {
                if (gestureAccepted) {
                    // Settle first so pager-follow observers cannot snap to the stale page
                    // between isDragging flipping false and the drag target being recorded.
                    onDragStopped()
                    isDragging = false
                    release()
                }
                gestureAccepted = false
            },
            onDragCancel = {
                if (gestureAccepted) {
                    onDragStopped()
                    isDragging = false
                    release()
                }
                gestureAccepted = false
            }
        ) { change, dragAmount ->
            if (!gestureAccepted) return@inspectDragGestures

            val position = change.position
            val previousPosition = change.previousPosition

            val isInside = canDrag(position)
            val wasInside = canDrag(previousPosition)

            if (isInside && wasInside) {
                if (dragAmount != Offset.Zero) {
                    change.consume()
                }
                onDrag(size, dragAmount)
            }
        }
    }

    val longPressModifier: Modifier = Modifier.pointerInput(Unit) {
        var gestureAccepted = false
        detectDragGesturesAfterLongPress(
            onDragStart = { position ->
                gestureAccepted = canDrag(position)
                if (gestureAccepted) {
                    isDragging = true
                    onDragStarted(position)
                    press()
                }
            },
            onDragEnd = {
                if (gestureAccepted) {
                    onDragStopped()
                    isDragging = false
                    release()
                }
                gestureAccepted = false
            },
            onDragCancel = {
                if (gestureAccepted) {
                    onDragStopped()
                    isDragging = false
                    release()
                }
                gestureAccepted = false
            },
            onDrag = { change, dragAmount ->
                if (!gestureAccepted) return@detectDragGesturesAfterLongPress
                val isInside = canDrag(change.position)
                val wasInside = canDrag(change.previousPosition)
                if (isInside && wasInside) {
                    if (dragAmount != Offset.Zero) change.consume()
                    onDrag(size, dragAmount)
                }
            },
        )
    }

    fun press() {
        velocityTracker.resetTracking()
        animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec) }
        }
    }

    fun release() {
        animationScope.launch {
            awaitFrame()
            if (value != targetValue) {
                val threshold = (valueRange.endInclusive - valueRange.start) * 0.025f
                snapshotFlow { valueAnimation.value }
                    .filter { abs(it - targetValue) < threshold }
                    .first()
            }
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
        }
    }

    fun snapTo(value: Float) {
        val next = value.coerceIn(valueRange)
        requestedValue = next
        launchValueTracking { valueAnimation.snapTo(next) }
    }

    fun updateValue(value: Float) {
        val targetValue = value.coerceIn(valueRange)
        requestedValue = targetValue
        if (trackingMode == DampedDragTrackingMode.DIRECT) {
            launchValueTracking {
                valueAnimation.snapTo(targetValue)
                updateVelocity()
            }
            return
        }
        launchValueTracking {
            valueAnimation.animateTo(targetValue, valueAnimationSpec) { updateVelocity() }
        }
    }

    fun animateToValue(
        value: Float,
        animatePress: Boolean = true,
    ) {
        val targetValue = value.coerceIn(valueRange)
        requestedValue = targetValue
        launchValueTracking {
            mutatorMutex.mutate {
                if (animatePress) press()
                launch { valueAnimation.animateTo(targetValue, valueAnimationSpec) }
                if (velocity != 0f) {
                    launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
                }
                if (animatePress) release()
            }
        }
    }

    /**
     * Installs the newest position mutation before the caller returns. Pointer updates can arrive
     * faster than the dispatcher gets a chance to run a normally launched coroutine; cancelling a
     * still-pending job on every event leaves [valueAnimation] frozen while [requestedValue] moves.
     * Undispatched start closes that scheduling gap while cancellation still guarantees latest-wins.
     */
    private fun launchValueTracking(block: suspend CoroutineScope.() -> Unit) {
        valueTrackingJob?.cancel()
        valueTrackingJob = animationScope.launch(
            start = CoroutineStart.UNDISPATCHED,
            block = block,
        )
    }

    private fun updateVelocity() {
        velocityTracker.addPosition(
            System.currentTimeMillis(),
            Offset(value, 0f)
        )
        val targetVelocity = velocityTracker.calculateVelocity().x / (valueRange.endInclusive - valueRange.start)
        animationScope.launch { velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec) }
    }
}
