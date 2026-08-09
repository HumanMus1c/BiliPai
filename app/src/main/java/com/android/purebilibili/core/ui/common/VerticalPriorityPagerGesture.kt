package com.android.purebilibili.core.ui.common

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalLayoutDirection
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.math.abs
import kotlin.math.sign
import kotlin.coroutines.resume

internal enum class PagerGestureDirection {
    UNDECIDED,
    HORIZONTAL,
    VERTICAL,
}

internal const val PAGER_HORIZONTAL_DOMINANCE_RATIO = 1.5f
internal const val PAGER_DIRECTION_SLOP_MULTIPLIER = 1.5f

internal fun resolveVerticalPriorityPagerGestureDirection(
    totalX: Float,
    totalY: Float,
    touchSlop: Float,
    horizontalDominanceRatio: Float = PAGER_HORIZONTAL_DOMINANCE_RATIO,
    directionSlopMultiplier: Float = PAGER_DIRECTION_SLOP_MULTIPLIER,
): PagerGestureDirection {
    val requiredDistance = touchSlop.coerceAtLeast(0f) * directionSlopMultiplier.coerceAtLeast(1f)
    val totalDistanceSquared = totalX * totalX + totalY * totalY
    if (totalDistanceSquared < requiredDistance * requiredDistance) {
        return PagerGestureDirection.UNDECIDED
    }

    return if (abs(totalX) >= abs(totalY) * horizontalDominanceRatio.coerceAtLeast(1f)) {
        PagerGestureDirection.HORIZONTAL
    } else {
        PagerGestureDirection.VERTICAL
    }
}

/**
 * Gives a vertical child list priority over a surrounding [PagerState].
 *
 * The pager's built-in touch scrolling must be disabled. This modifier observes the complete
 * two-dimensional pointer stream without consuming it, then takes ownership only after the
 * accumulated gesture is clearly horizontal. Vertical and ambiguous gestures remain untouched so
 * the child LazyColumn/LazyGrid can continue handling them normally.
 */
internal fun Modifier.verticalPriorityHorizontalPagerSwipe(
    state: PagerState,
    enabled: Boolean,
    reverseLayout: Boolean = false,
): Modifier = composed {
    if (!enabled) return@composed this

    val flingBehavior = PagerDefaults.flingBehavior(state = state)
    val layoutDirection = LocalLayoutDirection.current
    val reverseDirection = remember(layoutDirection, reverseLayout) {
        ScrollableDefaults.reverseDirection(
            layoutDirection = layoutDirection,
            orientation = Orientation.Horizontal,
            reverseScrolling = reverseLayout,
        )
    }

    pointerInput(state, flingBehavior, reverseDirection) {
        val dragCoroutineScope = CoroutineScope(currentCoroutineContext())
        val velocityTracker = VelocityTracker()
        awaitEachGesture gesture@{
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial,
            )
            if (state.isScrollInProgress) {
                dragCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    state.scroll(MutatePriority.UserInput) { }
                }
            }
            velocityTracker.resetTracking()
            velocityTracker.addPosition(down.uptimeMillis, down.position)

            var totalDrag = Offset.Zero
            var direction = PagerGestureDirection.UNDECIDED
            var trackedPointerId = down.id
            var horizontalLockChange: PointerInputChange? = null

            while (direction == PagerGestureDirection.UNDECIDED) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == trackedPointerId }
                    ?: event.changes.firstOrNull { it.pressed }
                    ?: return@gesture
                trackedPointerId = change.id

                if (change.changedToUpIgnoreConsumed() || !change.pressed) return@gesture
                if (change.isConsumed) return@gesture

                totalDrag += change.positionChangeIgnoreConsumed()
                velocityTracker.addPosition(change.uptimeMillis, change.position)
                direction = resolveVerticalPriorityPagerGestureDirection(
                    totalX = totalDrag.x,
                    totalY = totalDrag.y,
                    touchSlop = viewConfiguration.touchSlop,
                )
                if (direction == PagerGestureDirection.HORIZONTAL) {
                    horizontalLockChange = change
                }
            }

            if (direction != PagerGestureDirection.HORIZONTAL) return@gesture
            horizontalLockChange?.consume()

            val directionThreshold =
                viewConfiguration.touchSlop * PAGER_DIRECTION_SLOP_MULTIPLIER
            val initialHorizontalDelta =
                totalDrag.x - sign(totalDrag.x) * directionThreshold
            val scrollDirectionMultiplier = if (reverseDirection) -1f else 1f
            val dragSession = PagerDragScrollSession()
            val scrollJob = dragCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                state.scroll(MutatePriority.UserInput) {
                    val horizontalVelocity = with(dragSession) {
                        awaitReleaseVelocity(
                            initialDelta = initialHorizontalDelta * scrollDirectionMultiplier,
                        )
                    }
                    with(flingBehavior) {
                        performFling(horizontalVelocity)
                    }
                }
            }

            var releasedNormally = false
            try {
                var released = false
                while (!released) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    val change = event.changes.firstOrNull { it.id == trackedPointerId }
                        ?: event.changes.firstOrNull { it.pressed }
                    if (change == null) {
                        released = true
                        continue
                    }
                    trackedPointerId = change.id
                    velocityTracker.addPosition(change.uptimeMillis, change.position)

                    if (change.changedToUpIgnoreConsumed() || !change.pressed) {
                        released = true
                    } else {
                        val horizontalDelta = change.positionChangeIgnoreConsumed().x
                        change.consume()
                        if (horizontalDelta != 0f) {
                            dragSession.dragBy(horizontalDelta * scrollDirectionMultiplier)
                        }
                    }
                }

                val horizontalVelocity = velocityTracker.calculateVelocity().x
                dragSession.release(horizontalVelocity * scrollDirectionMultiplier)
                releasedNormally = true
            } finally {
                if (!releasedNormally) {
                    dragSession.cancel()
                    if (!scrollJob.isCompleted) scrollJob.cancel()
                }
            }
        }
    }
}

private class PagerDragScrollSession {
    private var scrollScope: ScrollScope? = null
    private var releaseContinuation: CancellableContinuation<Float>? = null

    suspend fun ScrollScope.awaitReleaseVelocity(initialDelta: Float): Float =
        suspendCancellableCoroutine { continuation ->
            scrollScope = this
            releaseContinuation = continuation
            if (initialDelta != 0f) scrollBy(initialDelta)
            continuation.invokeOnCancellation {
                scrollScope = null
                releaseContinuation = null
            }
        }

    fun dragBy(delta: Float) {
        if (delta != 0f) scrollScope?.scrollBy(delta)
    }

    fun release(velocity: Float) {
        val continuation = releaseContinuation ?: return
        releaseContinuation = null
        scrollScope = null
        if (continuation.isActive) continuation.resume(velocity)
    }

    fun cancel() {
        val continuation = releaseContinuation ?: return
        releaseContinuation = null
        scrollScope = null
        continuation.cancel()
    }
}
