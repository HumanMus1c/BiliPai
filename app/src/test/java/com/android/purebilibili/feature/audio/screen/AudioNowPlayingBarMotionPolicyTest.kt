package com.android.purebilibili.feature.audio.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AudioNowPlayingBarMotionPolicyTest {

    @Test
    fun sourceHideRequiresTheRealSharedReturnOwner() {
        assertTrue(
            shouldHideAudioNowPlayingBarForSharedReturn(
                isReturningFromDetail = true,
                targetBvid = "BV123",
                currentBvid = "BV123",
                isSharedTransitionRunning = true,
                isSharedTransitionSourceOwner = true,
            )
        )
        assertFalse(
            shouldHideAudioNowPlayingBarForSharedReturn(
                isReturningFromDetail = true,
                targetBvid = "BV123",
                currentBvid = "BV123",
                isSharedTransitionRunning = false,
                isSharedTransitionSourceOwner = true,
            )
        )
        assertFalse(
            shouldHideAudioNowPlayingBarForSharedReturn(
                isReturningFromDetail = true,
                targetBvid = "BV123",
                currentBvid = "BV123",
                isSharedTransitionRunning = true,
                isSharedTransitionSourceOwner = false,
            )
        )
    }

    @Test
    fun sourceCannotOpenUntilLayoutAndImeAreSettled() {
        assertFalse(canOpenAudioNowPlayingBarSource(layoutStable = false))
        assertFalse(canOpenAudioNowPlayingBarSource(layoutStable = true, imeSettled = false))
        assertFalse(canOpenAudioNowPlayingBarSource(layoutStable = true, sharedTransitionRunning = true))
        assertTrue(canOpenAudioNowPlayingBarSource(layoutStable = true))
    }

    @Test
    fun sourceBarUsesOneSharedGeometryTimeline() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/audio/screen/AudioNowPlayingBar.kt"
        )
        assertTrue(source.contains("onCompactClick"))
        assertTrue(source.contains("isSharedTransitionSourceOwner"))
        assertFalse(source.contains("landingProgress"))
        assertFalse(source.contains("AudioNowPlayingBarLandingEasing"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        return listOf(File(path), File(normalizedPath)).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
