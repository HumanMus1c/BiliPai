package com.android.purebilibili.feature.video.ui.section

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoPlayerDanmakuLoadPolicyTest {

    @Test
    fun immediateLoad_startsAsSoonAsCidIsAvailable_evenWithoutDurationHint() {
        val policy = resolveVideoPlayerDanmakuLoadPolicy(
            cid = 10086L,
            danmakuEnabled = true,
            durationHintMs = 0L
        )

        assertTrue(policy.shouldEnable)
        assertTrue(policy.shouldLoadImmediately)
        assertEquals(0L, policy.durationHintMs)
    }

    @Test
    fun immediateLoad_keepsDurationHintWhenPlayerAlreadyKnowsIt() {
        val policy = resolveVideoPlayerDanmakuLoadPolicy(
            cid = 10010L,
            danmakuEnabled = true,
            durationHintMs = 360_000L
        )

        assertTrue(policy.shouldLoadImmediately)
        assertEquals(360_000L, policy.durationHintMs)
    }

    @Test
    fun immediateLoad_staysDisabledWhenCidInvalidOrDanmakuOff() {
        assertFalse(
            resolveVideoPlayerDanmakuLoadPolicy(
                cid = 0L,
                danmakuEnabled = true,
                durationHintMs = 0L
            ).shouldLoadImmediately
        )

        assertFalse(
            resolveVideoPlayerDanmakuLoadPolicy(
                cid = 10010L,
                danmakuEnabled = false,
                durationHintMs = 0L
            ).shouldLoadImmediately
        )
    }

    @Test
    fun engineSync_disablesAndClearsWhenSettingTurnsOff() {
        assertEquals(
            VideoPlayerDanmakuEngineSyncAction.DisableAndClear,
            resolveVideoPlayerDanmakuEngineSyncAction(
                danmakuEnabled = false,
                cid = 10010L
            )
        )
    }

    @Test
    fun engineSync_enablesOnlyWhenCidIsValidAndSettingIsOn() {
        assertEquals(
            VideoPlayerDanmakuEngineSyncAction.Enable,
            resolveVideoPlayerDanmakuEngineSyncAction(
                danmakuEnabled = true,
                cid = 10010L
            )
        )
        assertEquals(
            VideoPlayerDanmakuEngineSyncAction.DisableAndClear,
            resolveVideoPlayerDanmakuEngineSyncAction(
                danmakuEnabled = true,
                cid = 0L
            )
        )
    }

    @Test
    fun detailAndPlayerToggle_syncEngineImmediatelyAndReleaseDanmakuView() {
        val playerSource = java.io.File(
            "src/main/java/com/android/purebilibili/feature/video/ui/section/VideoPlayerSection.kt"
        ).readText()
        val detailSource = java.io.File(
            "src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailPhoneContent.kt"
        ).readText()

        assertTrue(
            playerSource.contains("resolveVideoPlayerDanmakuEngineSyncAction(") &&
                playerSource.contains("onRelease") &&
                playerSource.contains("danmakuManager.hide()") &&
                playerSource.contains("danmakuManager.clear()") &&
                playerSource.contains("danmakuManager.detachView()"),
            "VideoPlayerSection must sync/clear the engine on toggle and release DanmakuView on dispose."
        )
        assertTrue(
            detailSource.contains("danmakuManager.isEnabled = newValue") ||
                detailSource.contains("rememberDanmakuManager()"),
            "Detail tab danmaku toggle must update the shared DanmakuManager immediately, not only DataStore."
        )
        assertTrue(
            playerSource.contains("danmakuManager.isEnabled = newState"),
            "Fullscreen bottom-bar danmaku toggle must update DanmakuManager immediately."
        )
    }
}
