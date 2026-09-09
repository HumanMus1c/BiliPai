package com.android.purebilibili.feature.audio.screen

import com.android.purebilibili.feature.video.player.PlayMode
import kotlin.math.abs

internal data class MusicLyricFocusStyle(
    val blurRadiusDp: Int,
    val alphaPercent: Int
)

internal fun resolveMusicPagerIndicatorPosition(
    currentPage: Int,
    currentPageOffsetFraction: Float
): Float = (currentPage + currentPageOffsetFraction).coerceIn(0f, 1f)

internal fun resolveMusicPlayModeIndex(mode: PlayMode): Int = when (mode) {
    PlayMode.SEQUENTIAL -> 0
    PlayMode.SHUFFLE -> 1
    PlayMode.REPEAT_ONE -> 2
    PlayMode.REPEAT_ALL -> 3
}

internal fun resolveMusicPlayMode(index: Int): PlayMode = when (index) {
    1 -> PlayMode.SHUFFLE
    2 -> PlayMode.REPEAT_ONE
    3 -> PlayMode.REPEAT_ALL
    else -> PlayMode.SEQUENTIAL
}

internal enum class MusicRepeatGlyph {
    OFF,
    ONE,
    ALL
}

internal data class MusicSecondaryTransportState(
    val shuffleEnabled: Boolean,
    val repeatGlyph: MusicRepeatGlyph
)

internal fun resolveMusicRepeatGlyph(mode: PlayMode): MusicRepeatGlyph = when (mode) {
    PlayMode.REPEAT_ONE -> MusicRepeatGlyph.ONE
    PlayMode.REPEAT_ALL, PlayMode.SHUFFLE -> MusicRepeatGlyph.ALL
    PlayMode.SEQUENTIAL -> MusicRepeatGlyph.OFF
}

internal fun resolveMusicSecondaryTransport(
    mode: PlayMode,
    shuffleEnabled: Boolean = mode == PlayMode.SHUFFLE
): MusicSecondaryTransportState = MusicSecondaryTransportState(
    shuffleEnabled = shuffleEnabled || mode == PlayMode.SHUFFLE,
    repeatGlyph = resolveMusicRepeatGlyph(mode)
)

internal fun resolvePlayModeAfterShuffleToggle(mode: PlayMode): PlayMode =
    if (mode == PlayMode.SHUFFLE) PlayMode.SEQUENTIAL else PlayMode.SHUFFLE

internal fun resolvePlayModeAfterRepeatToggle(mode: PlayMode): PlayMode = when (mode) {
    PlayMode.SEQUENTIAL, PlayMode.SHUFFLE -> PlayMode.REPEAT_ONE
    PlayMode.REPEAT_ONE -> PlayMode.REPEAT_ALL
    PlayMode.REPEAT_ALL -> PlayMode.SEQUENTIAL
}

internal fun resolveRepeatModeAfterToggle(mode: PlayMode): PlayMode =
    resolvePlayModeAfterRepeatToggle(
        if (mode == PlayMode.SHUFFLE) PlayMode.REPEAT_ALL else mode
    )

internal fun resolveMusicLyricsBlurEnabled(
    sdkInt: Int,
    effectsEnabled: Boolean,
    reduceMotion: Boolean
): Boolean = sdkInt >= 31 && effectsEnabled && !reduceMotion

internal fun resolveMusicLyricFocusStyle(
    lineIndex: Int,
    currentIndex: Int,
    blurEnabled: Boolean
): MusicLyricFocusStyle {
    val distance = abs(lineIndex - currentIndex)
    val alphaPercent = when (distance) {
        0 -> 100
        1 -> 46
        2 -> 30
        else -> 20
    }
    val blurRadiusDp = if (!blurEnabled) {
        0
    } else {
        when (distance) {
            0 -> 0
            1 -> 2
            2 -> 4
            else -> 7
        }
    }
    return MusicLyricFocusStyle(blurRadiusDp, alphaPercent)
}

internal fun resolveMusicLiquidGlassEnabled(
    sdkInt: Int,
    effectsEnabled: Boolean,
    isAppInBackground: Boolean,
    reduceMotion: Boolean
): Boolean {
    return effectsEnabled &&
        sdkInt >= 33 &&
        !isAppInBackground &&
        !reduceMotion
}
