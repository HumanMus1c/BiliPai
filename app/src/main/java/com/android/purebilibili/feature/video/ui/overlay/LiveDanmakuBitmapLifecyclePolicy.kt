package com.android.purebilibili.feature.video.ui.overlay

internal enum class LiveDanmakuBitmapOwnership {
    APP_QUEUE_ONLY,
    CONTROLLER_ATTACHED,
    RELEASED_FROM_ENGINE,
}

internal fun shouldManuallyRecycleLiveDanmakuBitmap(
    ownership: LiveDanmakuBitmapOwnership
): Boolean {
    return ownership != LiveDanmakuBitmapOwnership.CONTROLLER_ATTACHED
}
