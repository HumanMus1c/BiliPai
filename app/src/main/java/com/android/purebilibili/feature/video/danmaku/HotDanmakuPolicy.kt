package com.android.purebilibili.feature.video.danmaku

import com.android.purebilibili.danmaku.engine.DanmakuItem

/** Only recent comments are eligible: never reveal a later scene's comments early. */
internal fun selectHotDanmaku(
    items: List<DanmakuItem>,
    positionMs: Long,
): List<DanmakuItem> = items.asSequence()
    .filter { it.danmakuId > 0L && !it.text.isNullOrBlank() && it.likeCount >= 10L }
    .filter { it.showAtTime in (positionMs.coerceAtLeast(0L) - 15_000L).coerceAtLeast(0L)..positionMs.coerceAtLeast(0L) }
    .sortedWith(compareByDescending<DanmakuItem> { it.likeCount }.thenBy { it.danmakuId })
    .distinctBy { it.danmakuId }
    .distinctBy { it.text }
    .take(3)
    .toList()
