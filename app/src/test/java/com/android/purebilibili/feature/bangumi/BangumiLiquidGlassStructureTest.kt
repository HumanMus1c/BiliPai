package com.android.purebilibili.feature.bangumi

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class BangumiLiquidGlassStructureTest {

    @Test
    fun `bangumi hub uses adaptive native controls and independent poster ratio`() {
        val screenSource = sourceOf("BangumiScreen.kt")
        val contentSource = sourceOf("BangumiHubContent.kt")
        val tabRowSource = sourceOf("BangumiLiquidTabRow.kt")
        val homeTabSource = sourceOf("HomeBangumiTabPage.kt")

        assertTrue(screenSource.contains("BangumiLiquidAwareTabRow("))
        assertTrue(contentSource.contains("BangumiLiquidAwareTabRow("))
        assertTrue(contentSource.contains("AdaptivePullToRefreshBox("))
        assertTrue(contentSource.contains("AnimatedContent("))
        assertTrue(contentSource.contains("slideInHorizontally("))
        assertTrue(contentSource.contains("slideOutHorizontally("))
        assertTrue(contentSource.contains("BANGUMI_POSTER_ASPECT_RATIO = 0.75f"))
        assertTrue(contentSource.contains("追番时间表"))
        assertTrue(contentSource.contains("AppFilterChip("))
        assertTrue(contentSource.contains("showPgcTimeline"))
        assertTrue(!screenSource.contains("AppNativeTabRow("))
        assertTrue(!contentSource.contains("AppNativeTabRow("))
        assertTrue(contentSource.contains("BangumiLiquidAwareTabRow("))
        assertTrue(tabRowSource.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(tabRowSource.contains("AppChromeSizeTokens.BottomBarMatchedSegmentedControlHeightDp"))
        assertTrue(tabRowSource.contains("AppNativeTabRow("))
        assertTrue(contentSource.contains("rememberLayerBackdrop()"))
        assertTrue(contentSource.contains("miuixBackdrop = followBackdrop"))
        assertTrue(!tabRowSource.contains("forceLiquidChrome = true"))
        assertTrue(!contentSource.contains("HomeFeedCardStyle"))
        assertTrue(!contentSource.contains("SettingsManager"))
        assertTrue(!screenSource.contains("TopAppBarScrollBehavior"))
        assertTrue(homeTabSource.contains("BangumiLiquidAwareTabRow("))
        assertTrue(!homeTabSource.contains("categoryTabsVisible"))
        assertTrue(!homeTabSource.contains("AnimatedVisibility("))
    }

    private fun sourceOf(path: String): String =
        File("src/main/java/com/android/purebilibili/feature/bangumi/$path").readText()
}
