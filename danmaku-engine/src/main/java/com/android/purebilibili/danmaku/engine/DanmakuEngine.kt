package com.android.purebilibili.danmaku.engine

import android.graphics.PointF
import android.graphics.RectF

interface DanmakuEngine : AutoCloseable {
    val playbackState: DanmakuPlaybackState
    val diagnostics: DanmakuEngineDiagnostics

    fun updateConfig(config: DanmakuRenderConfig)

    fun replaceWindow(window: DanmakuWindow, currentPositionMs: Long = 0L)

    fun replaceMaskFrames(frames: List<DanmakuMaskFrame>, currentPositionMs: Long)

    fun append(items: List<DanmakuItem>)

    /** Drops timeline items older than [positionMs] without restarting rendering. */
    fun trimBefore(positionMs: Long)

    fun addImmediate(item: DanmakuItem)

    fun seekTo(positionMs: Long)

    fun start(positionMs: Long = 0L)

    fun pause()

    fun stop()

    fun clear()

    fun invalidate()

    fun setOnItemClickListener(listener: ((DanmakuItem, RectF, PointF) -> Unit)?)

    override fun close()
}
