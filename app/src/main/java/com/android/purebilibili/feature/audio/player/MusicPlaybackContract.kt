package com.android.purebilibili.feature.audio.player

internal sealed interface MusicPlaybackSource {
    val stableId: String

    data class AudioSong(val sid: Long) : MusicPlaybackSource {
        override val stableId: String = "au:$sid"
    }

    data class VideoAudio(
        val bvid: String,
        val cid: Long,
        val title: String
    ) : MusicPlaybackSource {
        override val stableId: String = "video:$bvid:$cid"
    }
}

internal data class MusicQueueControlState(
    val hasPrevious: Boolean,
    val hasNext: Boolean,
    val showQueue: Boolean
)

internal fun resolveMusicQueueControlState(
    queueSize: Int,
    currentIndex: Int
): MusicQueueControlState {
    if (queueSize <= 1 || currentIndex !in 0 until queueSize) {
        return MusicQueueControlState(false, false, false)
    }
    return MusicQueueControlState(
        hasPrevious = currentIndex > 0,
        hasNext = currentIndex < queueSize - 1,
        showQueue = true
    )
}

internal fun shouldReleaseMusicPlayerOnScreenExit(
    isManagedByMiniPlayer: Boolean
): Boolean = !isManagedByMiniPlayer
