package com.android.purebilibili.feature.home

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeHeroFlyoutStructureTest {

    @Test
    fun homeScreenNavigatesImmediatelyAndDoesNotRunSourceFlyout() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt")
        val clickWrapperSource = source
            .substringAfter("val wrappedOnVideoClick")
            .substringBefore("val onTodayWatchVideoClick")

        assertFalse(source.contains("pendingHeroFlyoutRequest"))
        assertFalse(source.contains("shouldRunHomeHeroFlyoutBeforeNavigation(request)"))
        assertFalse(source.contains("resolveHomeHeroFlyoutNavigationDelayMillis()"))
        assertTrue(clickWrapperSource.contains("hideTopTabsForForwardDetailNav = true"))
        assertTrue(clickWrapperSource.contains("setBottomBarVisible(false)"))
        assertTrue(clickWrapperSource.contains("isVideoNavigating = true"))
        assertTrue(clickWrapperSource.contains("onVideoClick(request)"))
    }

    @Test
    fun ordinaryHomeVideoCardDoesNotRunSourceFlyout() {
        val pageSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/HomeCategoryPage.kt")
        val cardSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt")

        assertFalse(pageSource.contains("heroFlyoutBvid"))
        assertFalse(pageSource.contains("heroFlyoutActive"))
        assertFalse(cardSource.contains("heroFlyoutActive"))
        assertFalse(cardSource.contains("resolveHomeHeroFlyoutFrame("))
    }

    @Test
    fun ordinaryHomeVideoCardUsesCoverPrimarySharedTransitionPolicy() {
        val cardSource = loadSource("app/src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt")

        assertTrue(cardSource.contains("resolveHomeVideoSharedTransitionMotionSpec("))
        assertTrue(cardSource.contains("resolveHomeVideoSharedTransitionCornerSpec("))
        assertTrue(cardSource.contains("durationMillis = homeSharedTransitionMotionSpec.durationMillis"))
        assertTrue(cardSource.contains("videoCoverSharedElementKey("))
        assertFalse(cardSource.contains("使用 renderInSharedTransitionScopeOverlayOption 控制可见性"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
