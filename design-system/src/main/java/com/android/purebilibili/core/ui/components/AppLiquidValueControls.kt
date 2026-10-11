/*
 * Adapted from Kyant AndroidLiquidGlass LiquidToggle, LiquidSlider and DampedDragAnimation.
 * Copyright 2025 Kyant. Apache-2.0; see assets/licenses/AndroidLiquidGlass.LICENSE.
 * BiliPai: native theme fallback, safe sibling track capture, horizontal gesture ownership,
 * discrete values, accessibility actions, and interruption-safe coroutine jobs.
 */
package com.android.purebilibili.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import top.yukonga.miuix.kmp.blur.*
import com.android.purebilibili.core.ui.effect.lens
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import kotlin.math.roundToInt

private class LiquidValueMotion(private val scope: CoroutineScope, initial: Float) {
    val value = Animatable(initial)
    val press = Animatable(0f)
    val scaleX = Animatable(1f)
    val scaleY = Animatable(1f)
    val velocity = Animatable(0f)
    var target = initial
        private set
    private var valueJob: Job? = null
    private var pressJob: Job? = null
    private var velocityJob: Job? = null
    private var lastFrameNanos = 0L
    private var lastFrameValue = initial

    fun update(next: Float) {
        target = next.coerceIn(0f, 1f)
        valueJob?.cancel()
        valueJob = scope.launch {
            lastFrameNanos = 0L
            lastFrameValue = value.value
            value.animateTo(target, spring(1f, 1000f, 0.001f)) {
                val now = System.nanoTime()
                if (lastFrameNanos != 0L) {
                    val deltaSeconds = (now - lastFrameNanos) / 1_000_000_000f
                    if (deltaSeconds > 0f) {
                        val speed = (value - lastFrameValue) / deltaSeconds
                        velocityJob?.cancel()
                        velocityJob = scope.launch { this@LiquidValueMotion.velocity.animateTo(speed, spring(0.5f, 300f, 0.01f)) }
                    }
                }
                lastFrameValue = value
                lastFrameNanos = now
            }
            velocityJob?.cancel()
            velocityJob = scope.launch { velocity.animateTo(0f, spring(0.5f, 300f, 0.01f)) }
        }
    }

    fun pressed(active: Boolean) {
        pressJob?.cancel()
        pressJob = scope.launch {
            if (!active) {
                withFrameNanos { }
                withTimeoutOrNull(300L) {
                    snapshotFlow { value.value }.first { abs(it - target) < 0.025f }
                }
            }
            launch { press.animateTo(if (active) 1f else 0f, spring(1f, 1000f, 0.001f)) }
            launch { scaleX.animateTo(if (active) 1.5f else 1f, spring(0.6f, 250f, 0.001f)) }
            launch { scaleY.animateTo(if (active) 1.5f else 1f, spring(0.7f, 250f, 0.001f)) }
        }
    }
}

/** Horizontal drag claims input only after slop; vertical list scrolling remains native. */
private fun Modifier.liquidValueGesture(
    motion: LiquidValueMotion,
    onDelta: (Float, Float) -> Unit,
    onFinish: (Boolean, Float?) -> Unit,
    onCancel: () -> Unit,
    onStart: () -> Unit = {},
): Modifier = pointerInput(motion) {
    awaitEachGesture {
        val down = awaitFirstDown(false, PointerEventPass.Initial)
        var complete = false
        onStart()
        motion.pressed(true)
        try {
            val drag = awaitHorizontalTouchSlopOrCancellation(down.id) { change, over ->
                if (currentEvent.changes.count { it.pressed } > 1) throw CancellationException("Multiple pointers")
                change.consume()
                onDelta(over, size.width.toFloat())
            }
            if (drag != null) {
                complete = horizontalDrag(drag.id) { change ->
                    if (currentEvent.changes.count { it.pressed } > 1) throw CancellationException("Multiple pointers")
                    onDelta(change.position.x - change.previousPosition.x, size.width.toFloat())
                    change.consume()
                }
                if (complete) onFinish(true, null)
            } else {
                val end = currentEvent.changes.firstOrNull { it.id == down.id }
                if (end != null && !end.pressed && !end.isConsumed) {
                    complete = true
                    end.consume()
                    onFinish(false, end.position.x / size.width.coerceAtLeast(1))
                }
            }
        } finally {
            if (!complete) onCancel()
            motion.pressed(false)
        }
    }
}

@Composable
private fun LiquidValueThumb(motion: LiquidValueMotion, track: Backdrop, modifier: Modifier) {
    Box(modifier
        .dropShadow(CircleShape, Shadow(radius = 4.dp, color = Color.Black, alpha = 0.05f))
        .drawBackdrop(
            backdrop = track,
            shape = { CircleShape },
            effects = {
                val progress = motion.press.value
                blur(8.dp.toPx() * (1f - progress))
                lens(10.dp.toPx() * progress, 14.dp.toPx() * progress, chromaticAberration = 1f)
            },
            highlight = { Highlight(width = (0.8f / 1.5f).dp, alpha = motion.press.value) },
            layerBlock = {
                val velocity = motion.velocity.value / 10f
                scaleX = motion.scaleX.value / (1f - (velocity * 0.75f).coerceIn(-0.2f, 0.2f))
                scaleY = motion.scaleY.value * (1f - (velocity * 0.25f).coerceIn(-0.2f, 0.2f))
            },
            onDrawSurface = { drawRect(Color.White.copy(alpha = 1f - motion.press.value.coerceIn(0f, 1f))) },
        )
        .innerShadow(CircleShape) {
            radius = 4.dp.toPx() * motion.press.value
            alpha = motion.press.value.coerceIn(0f, 1f)
            color = Color.Black.copy(alpha = 0.15f)
        }
        .size(40.dp, 24.dp))
}

@Composable
internal fun AppLiquidSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier) {
    val thisDensity = androidx.compose.ui.platform.LocalDensity.current.density
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val hapticsEnabled = com.android.purebilibili.core.ui.LocalAppThemeConfig.current.hapticFeedbackEnabled
    val scope = rememberCoroutineScope()
    val motion = remember { LiquidValueMotion(scope, if (checked) 1f else 0f) }
    val latestChecked by rememberUpdatedState(checked)
    val latestChange by rememberUpdatedState(onCheckedChange)
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val latestLtr by rememberUpdatedState(isLtr)
    val track = rememberLayerBackdrop()
    val active = MaterialTheme.colorScheme.primary
    val inactive = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    LaunchedEffect(checked) { motion.update(if (checked) 1f else 0f) }
    fun select(next: Boolean) {
        if (hapticsEnabled && next != latestChecked) haptic.performHapticFeedback(if (next) androidx.compose.ui.hapticfeedback.HapticFeedbackType.ToggleOn else androidx.compose.ui.hapticfeedback.HapticFeedbackType.ToggleOff)
        motion.update(if (next) 1f else 0f)
        latestChange(next)
    }
    fun activate() { motion.pressed(true); select(!latestChecked); motion.pressed(false) }
    Box(modifier
        .sizeIn(minWidth = 64.dp, minHeight = 48.dp)
        .semantics {
            role = Role.Switch
            toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            onClick { activate(); true }
        }
        .onKeyEvent {
            if (it.type == KeyEventType.KeyUp && (it.key == Key.Spacebar || it.key == Key.Enter || it.key == Key.DirectionCenter)) {
                activate(); true
            } else false
        }
        .focusable()
        .liquidValueGesture(
            motion,
            onDelta = { delta, _ -> motion.update(motion.target + delta / (20.dp.value * thisDensity) * if (latestLtr) 1f else -1f) },
            onFinish = { dragged, _ -> select(if (dragged) motion.target >= 0.5f else !latestChecked) },
            onCancel = { motion.update(if (latestChecked) 1f else 0f) },
        ), contentAlignment = Alignment.CenterStart) {
        Box(Modifier.align(Alignment.CenterStart).size(64.dp, 28.dp).layerBackdrop(track).clip(CircleShape)
            .drawBehind { drawRect(lerp(inactive, active, motion.value.value.coerceIn(0f, 1f))) })
        LiquidValueThumb(motion, track, Modifier.align(Alignment.CenterStart).graphicsLayer {
            val fraction = if (isLtr) motion.value.value else 1f - motion.value.value
            translationX = 2.dp.toPx() + 20.dp.toPx() * fraction
        })
    }
}

@Composable
internal fun AppLiquidSlider(
    value: Float, onValueChange: (Float) -> Unit, modifier: Modifier,
    valueRange: ClosedFloatingPointRange<Float>, steps: Int, onValueChangeFinished: (() -> Unit)?,
    colors: AppSliderColors?,
) {
    val scope = rememberCoroutineScope()
    val span = valueRange.endInclusive - valueRange.start
    fun fraction(value: Float) = ((value - valueRange.start) / span).coerceIn(0f, 1f)
    val motion = remember(valueRange) { LiquidValueMotion(scope, fraction(value)) }
    var rawDragFraction by remember(valueRange) { mutableFloatStateOf(fraction(value)) }
    val latestValue by rememberUpdatedState(value)
    val latestChange by rememberUpdatedState(onValueChange)
    val latestFinish by rememberUpdatedState(onValueChangeFinished)
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val latestLtr by rememberUpdatedState(isLtr)
    val track = rememberLayerBackdrop()
    val active = colors?.activeTrackColor ?: MaterialTheme.colorScheme.primary
    val inactive = colors?.inactiveTrackColor ?: MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    fun update(progress: Float) {
        val bounded = progress.coerceIn(0f, 1f)
        val snapped = if (steps > 0) (bounded * (steps + 1)).roundToInt().toFloat() / (steps + 1) else bounded
        motion.update(snapped)
        latestChange(valueRange.start + snapped * span)
    }
    LaunchedEffect(value, valueRange) { motion.update(fraction(value)) }
    BoxWithConstraints(modifier.fillMaxWidth().heightIn(min = 48.dp)
        .semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(value.coerceIn(valueRange), valueRange, steps)
            setProgress { update(fraction(it)); latestFinish?.invoke(); true }
        }
        .onKeyEvent {
            if (it.type == KeyEventType.KeyDown && (it.key == Key.DirectionLeft || it.key == Key.DirectionRight)) {
                val direction = if ((it.key == Key.DirectionRight) == latestLtr) 1f else -1f
                update(motion.target + direction / if (steps > 0) (steps + 1) else 20)
                latestFinish?.invoke(); true
            } else false
        }
        .focusable()
        .liquidValueGesture(
            motion,
            onDelta = { delta, width ->
                if (width > 0f) {
                    rawDragFraction = (rawDragFraction + delta / width * if (latestLtr) 1f else -1f).coerceIn(0f, 1f)
                    update(rawDragFraction)
                }
            },
            onStart = { rawDragFraction = motion.target },
            onFinish = { dragged, position ->
                if (!dragged && position != null) update(if (latestLtr) position else 1f - position)
                latestFinish?.invoke()
            },
            onCancel = { motion.update(fraction(latestValue)) },
        ), contentAlignment = Alignment.CenterStart) {
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth.toFloat() else 144.dp.value * androidx.compose.ui.platform.LocalDensity.current.density
        Box((if (constraints.hasBoundedWidth) Modifier.fillMaxWidth() else Modifier.width(144.dp)).height(6.dp).layerBackdrop(track).clip(CircleShape).background(inactive)) {
            Box(Modifier.fillMaxSize().drawBehind {
                val progress = motion.value.value.coerceIn(0f, 1f)
                val left = if (isLtr) 0f else size.width * (1f - progress)
                drawRect(active, topLeft = Offset(left, 0f), size = androidx.compose.ui.geometry.Size(size.width * progress, size.height))
            })
        }
        LiquidValueThumb(motion, track, Modifier.graphicsLayer {
            val progress = if (isLtr) motion.value.value else 1f - motion.value.value
            translationX = (-size.width / 2f + width * progress).coerceIn(-size.width / 4f, (width - size.width * 3f / 4f).coerceAtLeast(-size.width / 4f))
        })
    }
}
