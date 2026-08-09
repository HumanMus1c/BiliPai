package com.android.purebilibili.feature.live

/**
 * SuperChat 展示时长策略（对齐 PiliPlus 可过期 SC 卡片）。
 * duration 缺失时用默认值，避免列表永久堆积。
 */
internal const val DEFAULT_LIVE_SUPER_CHAT_DURATION_SEC = 60

internal fun resolveLiveSuperChatDurationSec(durationSec: Int): Int {
    return durationSec.takeIf { it > 0 } ?: DEFAULT_LIVE_SUPER_CHAT_DURATION_SEC
}

internal fun resolveLiveSuperChatRemainingSec(
    durationSec: Int,
    elapsedSec: Int,
): Int {
    val total = resolveLiveSuperChatDurationSec(durationSec)
    return (total - elapsedSec).coerceAtLeast(0)
}

internal fun shouldExpireLiveSuperChat(
    durationSec: Int,
    elapsedSec: Int,
): Boolean = resolveLiveSuperChatRemainingSec(durationSec, elapsedSec) <= 0

internal fun formatLiveSuperChatCountdown(remainingSec: Int): String {
    if (remainingSec <= 0) return "0s"
    val minutes = remainingSec / 60
    val seconds = remainingSec % 60
    return if (minutes > 0) {
        "%d:%02d".format(minutes, seconds)
    } else {
        "${seconds}s"
    }
}
