package com.android.purebilibili.feature.audio.screen

/**
 * The source bar is hidden only while the shared-transition host owns the
 * return handoff. A separate landing animation is intentionally not used:
 * the shared morph is the only geometry timeline.
 */
internal fun shouldHideAudioNowPlayingBarForSharedReturn(
    isReturningFromDetail: Boolean,
    targetBvid: String?,
    currentBvid: String,
    isSharedTransitionRunning: Boolean,
    isSharedTransitionSourceOwner: Boolean,
): Boolean {
    if (!isReturningFromDetail || !isSharedTransitionRunning || !isSharedTransitionSourceOwner) {
        return false
    }
    if (currentBvid.isBlank()) return false
    return targetBvid.isNullOrBlank() || targetBvid == currentBvid
}

internal fun canOpenAudioNowPlayingBarSource(
    layoutStable: Boolean,
    imeSettled: Boolean = true,
    sharedTransitionRunning: Boolean = false,
): Boolean = layoutStable && imeSettled && !sharedTransitionRunning
