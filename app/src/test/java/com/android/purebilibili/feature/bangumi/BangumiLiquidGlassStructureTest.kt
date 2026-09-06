package com.android.purebilibili.feature.bangumi

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class BangumiLiquidGlassStructureTest {

    @Test
    fun `bangumi hub uses adaptive native controls and independent poster ratio`() {
        val screenSource = sourceOf("BangumiScreen.kt")
        val contentSource = sourceOf("BangumiHubContent.kt")
        val detailSource = sourceOf("BangumiDetailScreen.kt")
        val tabRowSource = File(
            "src/main/java/com/android/purebilibili/core/ui/components/AppLiquidAwareTabRow.kt",
        ).readText()
        val floatingDockSource = File(
            "src/main/java/com/android/purebilibili/feature/home/components/FloatingBottomBar.kt",
        ).readText()
        val homeTabSource = sourceOf("HomeBangumiTabPage.kt")

        assertTrue(screenSource.contains("AppLiquidAwareTabRow("))
        assertTrue(contentSource.contains("AppLiquidAwareTabRow("))
        assertTrue(contentSource.contains("AdaptivePullToRefreshBox("))
        assertTrue(contentSource.contains("AnimatedContent("))
        assertTrue(contentSource.contains("slideInHorizontally("))
        assertTrue(contentSource.contains("slideOutHorizontally("))
        assertTrue(contentSource.contains("BANGUMI_POSTER_ASPECT_RATIO = 0.75f"))
        assertTrue(contentSource.contains("追番时间表"))
        assertTrue(contentSource.contains("minTabWidth = 112.dp"))
        assertTrue(contentSource.contains("modifier = Modifier.fillMaxWidth()"))
        assertTrue(detailSource.contains("AppStatusBadge("))
        assertTrue(detailSource.contains("emphasized = true"))
        assertTrue(!detailSource.contains("onClick = {},"))
        assertTrue(contentSource.contains("AppThemeAdaptiveTabRow("))
        assertTrue(contentSource.contains("miuixBackdrop = tabBackdrop"))
        assertTrue(screenSource.contains("tabBackdrop = channelBackdrop"))
        assertTrue(homeTabSource.contains("tabBackdrop = channelBackdrop"))
        assertTrue(contentSource.contains("modifier = Modifier.width(56.dp)"))
        assertTrue(contentSource.contains("modifier = Modifier.weight(1f)"))
        assertTrue(contentSource.contains("showPgcTimeline"))
        assertTrue(!screenSource.contains("AppNativeTabRow("))
        assertTrue(contentSource.contains("AppLiquidAwareTabRow("))
        assertTrue(tabRowSource.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(tabRowSource.contains("AppChromeSizeTokens.BottomBarMatchedSegmentedControlHeightDp"))
        assertTrue(tabRowSource.contains("AppNativeTabRow("))
        assertTrue(tabRowSource.contains("Modifier.horizontalScroll(scrollState)"))
        assertTrue(tabRowSource.contains("itemWidth = minTabWidth"))
        assertTrue(tabRowSource.contains("dragSelectionEnabled = resolvedDragSelectionEnabled"))
        assertTrue(floatingDockSource.contains("resolveSharedBottomBarCapsuleShape()"))
        assertTrue(!floatingDockSource.contains("remember { CircleShape }"))
        assertTrue(contentSource.contains("miuixBackdrop = tabBackdrop"))
        assertTrue(!contentSource.contains("followBackdrop"))
        assertTrue(!tabRowSource.contains("forceLiquidChrome = true"))
        assertTrue(!contentSource.contains("HomeFeedCardStyle"))
        assertTrue(!contentSource.contains("SettingsManager"))
        assertTrue(!screenSource.contains("TopAppBarScrollBehavior"))
        assertTrue(homeTabSource.contains("AppLiquidAwareTabRow("))
        assertTrue(!homeTabSource.contains("categoryTabsVisible"))
        assertTrue(!homeTabSource.contains("AnimatedVisibility("))
    }

    private fun sourceOf(path: String): String =
        File("src/main/java/com/android/purebilibili/feature/bangumi/$path").readText()
}
