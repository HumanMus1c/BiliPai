package com.android.purebilibili.core.ui.common

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.abs
import kotlin.math.sign

internal enum class PagerGestureDirection {
    UNDECIDED,
    HORIZONTAL,
    VERTICAL,
}

internal const val PAGER_HORIZONTAL_DOMINANCE_RATIO = 1.5f
internal const val PAGER_AMBIGUOUS_DIRECTION_SLOP_MULTIPLIER = 1.5f
internal const val PAGER_RELEASE_POSITION_THRESHOLD = 0.2f
internal const val PAGER_RELEASE_MIN_FLING_VELOCITY_DP = 300f

internal fun resolveVerticalPriorityPagerGestureDirection(
    totalX: Float,
    totalY: Float,
    touchSlop: Float,
    horizontalDominanceRatio: Float = PAGER_HORIZONTAL_DOMINANCE_RATIO,
    ambiguousDirectionSlopMultiplier: Float = PAGER_AMBIGUOUS_DIRECTION_SLOP_MULTIPLIER,
): PagerGestureDirection {
    val systemTouchSlop = touchSlop.coerceAtLeast(0f)
    val totalDistanceSquared = totalX * totalX + totalY * totalY
    if (totalDistanceSquared == 0f || totalDistanceSquared < systemTouchSlop * systemTouchSlop) {
        return PagerGestureDirection.UNDECIDED
    }

    val horizontalDistance = abs(totalX)
    val verticalDistance = abs(totalY)
    if (horizontalDistance >= verticalDistance * horizontalDominanceRatio.coerceAtLeast(1f)) {
        return PagerGestureDirection.HORIZONTAL
    }
    if (verticalDistance >= horizontalDistance) return PagerGestureDirection.VERTICAL

    // A slightly horizontal diagonal gets a short grace distance to clarify intent. Clear
    // horizontal and vertical gestures do not pay this extra threshold.
    val ambiguousDirectionSlop = systemTouchSlop *
        ambiguousDirectionSlopMultiplier.coerceAtLeast(1f)
    return if (totalDistanceSquared < ambiguousDirectionSlop * ambiguousDirectionSlop) {
        PagerGestureDirection.UNDECIDED
    } else {
        PagerGestureDirection.VERTICAL
    }
}

internal fun resolvePagerInitialHorizontalDelta(
    totalX: Float,
    touchSlop: Float,
): Float {
    val consumedSlop = touchSlop.coerceAtLeast(0f).coerceAtMost(abs(totalX))
    return totalX - sign(totalX) * consumedSlop
}

internal fun resolvePagerReleaseTargetPage(
    startPage: Int,
    pageCount: Int,
    pageSizePx: Float,
    scrollDeltaPx: Float,
    scrollVelocityPxPerSecond: Float,
    positionalThresholdFraction: Float = PAGER_RELEASE_POSITION_THRESHOLD,
    minimumFlingVelocityPxPerSecond: Float,
): Int {
    if (pageCount <= 0) return 0
    val boundedStartPage = startPage.coerceIn(0, pageCount - 1)
    val isFastFling = scrollVelocityPxPerSecond != 0f &&
        abs(scrollVelocityPxPerSecond) >= minimumFlingVelocityPxPerSecond.coerceAtLeast(0f)
    val crossedPositionThreshold = pageSizePx > 0f &&
        abs(scrollDeltaPx) >= pageSizePx * positionalThresholdFraction.coerceIn(0f, 1f)
    if (!isFastFling && !crossedPositionThreshold) return boundedStartPage

    val releaseDirection = if (isFastFling) {
        sign(scrollVelocityPxPerSecond)
    } else {
        sign(scrollDeltaPx)
    }
    if (releaseDirection == 0f) return boundedStartPage
    return (boundedStartPage + if (releaseDirection > 0f) 1 else -1)
        .coerceIn(0, pageCount - 1)
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

    val layoutDirection = LocalLayoutDirection.current
    val density = LocalDensity.current
    val minimumFlingVelocityPx = with(density) {
        PAGER_RELEASE_MIN_FLING_VELOCITY_DP.dp.toPx()
    }
    val reverseDirection = remember(layoutDirection, reverseLayout) {
        ScrollableDefaults.reverseDirection(
            layoutDirection = layoutDirection,
            orientation = Orientation.Horizontal,
            reverseScrolling = reverseLayout,
        )
    }

    pointerInput(state, reverseDirection, minimumFlingVelocityPx) {
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
            val gestureStartPage = state.currentPage

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

            val initialHorizontalDelta = resolvePagerInitialHorizontalDelta(
                totalX = totalDrag.x,
                touchSlop = viewConfiguration.touchSlop,
            )
            val scrollDirectionMultiplier = if (reverseDirection) -1f else 1f
            val dragSession = PagerDragScrollSession()
            val scrollJob = dragCoroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                var release: PagerDragRelease? = null
                state.scroll(MutatePriority.UserInput) {
                    release = with(dragSession) {
                        awaitRelease(
                            initialDelta = initialHorizontalDelta * scrollDirectionMultiplier,
                        )
                    }
                }
                release?.let { dragRelease ->
                    state.animateScrollToPage(
                        resolvePagerReleaseTargetPage(
                            startPage = gestureStartPage,
                            pageCount = state.pageCount,
                            pageSizePx = state.layoutInfo.pageSize.toFloat(),
                            scrollDeltaPx = dragRelease.scrollDeltaPx,
                            scrollVelocityPxPerSecond = dragRelease.velocityPxPerSecond,
                            minimumFlingVelocityPxPerSecond = minimumFlingVelocityPx,
                        ),
                    )
                }
            }

            var accumulatedScrollDelta = initialHorizontalDelta * scrollDirectionMultiplier
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
                            val scrollDelta = horizontalDelta * scrollDirectionMultiplier
                            accumulatedScrollDelta += scrollDelta
                            dragSession.dragBy(scrollDelta)
                        }
                    }
                }

                dragSession.release(
                    PagerDragRelease(
                        velocityPxPerSecond = velocityTracker.calculateVelocity().x *
                            scrollDirectionMultiplier,
                        scrollDeltaPx = accumulatedScrollDelta,
                    ),
                )
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

private data class PagerDragRelease(
    val velocityPxPerSecond: Float,
    val scrollDeltaPx: Float,
)

private class PagerDragScrollSession {
    private var scrollScope: ScrollScope? = null
    private var releaseContinuation: CancellableContinuation<PagerDragRelease>? = null

    suspend fun ScrollScope.awaitRelease(initialDelta: Float): PagerDragRelease =
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

    fun release(release: PagerDragRelease) {
        val continuation = releaseContinuation ?: return
        releaseContinuation = null
        scrollScope = null
        if (continuation.isActive) continuation.resume(release)
    }

    fun cancel() {
        val continuation = releaseContinuation ?: return
        releaseContinuation = null
        scrollScope = null
        continuation.cancel()
    }
}
