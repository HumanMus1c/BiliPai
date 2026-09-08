package com.android.purebilibili.feature.video.player

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

sealed interface PlayerKeyAction {
    data object PlayPause : PlayerKeyAction
    data class SeekRelative(val deltaMs: Long) : PlayerKeyAction
    data class SeekPercent(val fraction: Float) : PlayerKeyAction
    data object VolumeUp : PlayerKeyAction
    data object VolumeDown : PlayerKeyAction
    data object ToggleMute : PlayerKeyAction
    data object ToggleFullscreen : PlayerKeyAction
    data object ToggleDanmaku : PlayerKeyAction
}

internal const val KEYBOARD_SEEK_SHORT_STEP_MS = 5_000L
internal const val KEYBOARD_SEEK_LONG_STEP_MS = 10_000L

internal fun resolvePlayerKeyAction(
    event: KeyEvent,
    isScreenLocked: Boolean = false,
    isInPipMode: Boolean = false,
    isTextInputActive: Boolean = false,
): PlayerKeyAction? {
    if (event.type != KeyEventType.KeyDown) return null
    if (isScreenLocked || isInPipMode || isTextInputActive) return null

    // Do not intercept system-level shortcuts (Ctrl, Alt, Meta/Cmd)
    if (event.isCtrlPressed || event.isAltPressed || event.isMetaPressed) {
        return null
    }

    val shift = event.isShiftPressed

    return when (event.key) {
        Key.Spacebar, Key.K -> PlayerKeyAction.PlayPause
        Key.DirectionLeft -> {
            val step = if (shift) KEYBOARD_SEEK_LONG_STEP_MS else KEYBOARD_SEEK_SHORT_STEP_MS
            PlayerKeyAction.SeekRelative(-step)
        }
        Key.DirectionRight -> {
            val step = if (shift) KEYBOARD_SEEK_LONG_STEP_MS else KEYBOARD_SEEK_SHORT_STEP_MS
            PlayerKeyAction.SeekRelative(step)
        }
        Key.J -> PlayerKeyAction.SeekRelative(-KEYBOARD_SEEK_LONG_STEP_MS)
        Key.L -> PlayerKeyAction.SeekRelative(KEYBOARD_SEEK_LONG_STEP_MS)
        Key.DirectionUp -> PlayerKeyAction.VolumeUp
        Key.DirectionDown -> PlayerKeyAction.VolumeDown
        Key.F, Key.Enter, Key.NumPadEnter -> PlayerKeyAction.ToggleFullscreen
        Key.M -> PlayerKeyAction.ToggleMute
        Key.D -> PlayerKeyAction.ToggleDanmaku
        Key.Zero, Key.NumPad0 -> PlayerKeyAction.SeekPercent(0.0f)
        Key.One, Key.NumPad1 -> PlayerKeyAction.SeekPercent(0.1f)
        Key.Two, Key.NumPad2 -> PlayerKeyAction.SeekPercent(0.2f)
        Key.Three, Key.NumPad3 -> PlayerKeyAction.SeekPercent(0.3f)
        Key.Four, Key.NumPad4 -> PlayerKeyAction.SeekPercent(0.4f)
        Key.Five, Key.NumPad5 -> PlayerKeyAction.SeekPercent(0.5f)
        Key.Six, Key.NumPad6 -> PlayerKeyAction.SeekPercent(0.6f)
        Key.Seven, Key.NumPad7 -> PlayerKeyAction.SeekPercent(0.7f)
        Key.Eight, Key.NumPad8 -> PlayerKeyAction.SeekPercent(0.8f)
        Key.Nine, Key.NumPad9 -> PlayerKeyAction.SeekPercent(0.9f)
        else -> null
    }
}

internal fun calculateSeekTargetPositionMs(
    currentPositionMs: Long,
    durationMs: Long,
    action: PlayerKeyAction,
): Long? {
    if (durationMs <= 0L) return null
    val current = currentPositionMs.coerceAtLeast(0L)
    return when (action) {
        is PlayerKeyAction.SeekRelative -> {
            (current + action.deltaMs).coerceIn(0L, durationMs)
        }
        is PlayerKeyAction.SeekPercent -> {
            (durationMs * action.fraction).toLong().coerceIn(0L, durationMs)
        }
        else -> null
    }
}
