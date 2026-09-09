package com.android.purebilibili.feature.audio.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AudioNowPlayingBarStructureTest {

    @Test
    fun nowPlayingBarUsesBottomBarCapsuleGlassAndCoverSpin() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/audio/screen/AudioNowPlayingBar.kt"
        )

        assertTrue(source.contains("resolveSharedBottomBarCapsuleShape()"))
        assertTrue(source.contains(".biliPaiFloatingDockShell("))
        assertTrue(source.contains("enabled = glassActive"))
        assertTrue(source.contains("color = if (glassActive) Color.Transparent else containerColor"))
        assertTrue(source.contains("rememberMusicArtworkRotationDegrees("))
        assertTrue(source.contains("shouldRotateMusicArtwork("))
        assertTrue(source.contains("playbackSpeed = state.playbackSpeed"))
        assertTrue(source.contains("consumeNavigationBarsPadding"))
        assertTrue(source.contains("artistAvatarUrl"))
        assertTrue(source.contains("state.artistAvatarUrl"))
        assertFalse(source.contains("enabled = false"))
        assertFalse(source.contains("backdrop = null"))
        assertFalse(source.contains("ContainerLevel.Card"))
    }

    @Test
    fun appNavigationHostsNowPlayingBarBesideBottomBarBackdrop() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/navigation/AppNavigation.kt"
        )
        val overlay = source
            .substringAfter("if (bottomBarCanMount)")
            .substringBefore("MainHostTabBackHandler(")

        assertTrue(overlay.contains("AudioNowPlayingBar("))
        assertTrue(overlay.contains("miuixBackdrop = bottomBarBackdrop"))
        assertTrue(overlay.contains("glassEnabled = effectiveHomeSettings.androidNativeLiquidGlassEnabled"))
        assertTrue(source.contains("getAudioNowPlayingBarEnabled(context)"))
        assertTrue(overlay.contains("BottomBarMatchedDockVisibility("))
        assertTrue(overlay.contains("videoCardTransitionChromeReveal("))
        assertTrue(overlay.contains("showAudioNowPlayingInDock"))
        assertTrue(overlay.contains("isLandscape = isLandscapeNowPlaying"))
        assertTrue(overlay.contains("isPlayerDestination = isPlayerNowPlayingDestination"))
        assertTrue(overlay.contains("consumeNavigationBarsPadding = false"))
        assertTrue(overlay.contains("playbackSpeed = playbackManager.player?.playbackParameters?.speed ?: 1f"))
        assertFalse(overlay.contains("isChromeTransitionRunning"))
        assertFalse(overlay.contains("miuixBackdrop = null"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        return listOf(File(path), File(normalizedPath)).firstOrNull(File::exists)?.readText()
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
