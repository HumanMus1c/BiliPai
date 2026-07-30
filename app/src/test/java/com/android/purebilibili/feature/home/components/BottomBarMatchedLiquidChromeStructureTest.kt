package com.android.purebilibili.feature.home.components

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BottomBarMatchedLiquidChromeStructureTest {

    @Test
    fun `shared chrome owns dock indicator state orientation and visibility contracts`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarMatchedLiquidChrome.kt"
        )

        assertTrue(source.contains("internal class BottomBarMatchedLiquidChromeState"))
        assertTrue(source.contains("internal fun rememberBottomBarMatchedLiquidChromeState("))
        assertTrue(source.contains("internal fun BottomBarMatchedLiquidDock("))
        assertTrue(source.contains("internal fun BoxScope.BottomBarMatchedLiquidIndicator("))
        assertTrue(source.contains("internal enum class BottomBarLiquidOrientation"))
        assertTrue(source.contains("BottomBarLiquidOrientation.VERTICAL"))
        assertTrue(source.contains("swapMotionAxes = orientation == BottomBarLiquidOrientation.VERTICAL"))
        assertTrue(source.contains("internal fun BottomBarMatchedDockVisibility("))
        assertTrue(source.contains("internal enum class BottomBarMatchedDockEdge"))
        assertTrue(source.contains("    TOP,"))
        assertTrue(source.contains("    BOTTOM"))
        assertTrue(source.contains("resolveBottomBarMaterialScrollAnimationDurationMillis(isScrolling)"))
        assertTrue(source.contains("KernelSuMiuixBottomBarIndicatorLayer("))
        assertTrue(source.contains("KernelSuBottomBarIndicatorLayer("))
        assertTrue(source.contains("rememberCombinedBackdrop(localBackdrop, backdrop)"))
        assertTrue(source.contains("bottomBarMatchedCaptureOverflow(captureSafeInset)"))
    }

    @Test
    fun `bottom top and segmented chrome all delegate to shared implementation`() {
        val bottomBar = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt"
        )
        val topBar = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/TopBar.kt"
        )
        val segmented = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarLiquidSegmentedControl.kt"
        )
        val sharedChrome = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarMatchedLiquidChrome.kt"
        )

        listOf(bottomBar, topBar, segmented).forEach { source ->
            assertTrue(source.contains("rememberBottomBarMatchedLiquidChromeState("))
            assertTrue(source.contains("BottomBarMatchedLiquidIndicator("))
        }
        assertTrue(bottomBar.contains("BottomBarMatchedLiquidDock("))
        assertTrue(topBar.contains(".bottomBarMatchedLiquidDockSurface("))
        assertFalse(topBar.contains(".kernelSuFloatingDockSurface("))
        assertFalse(topBar.contains(".kernelSuMiuixFloatingDockSurface("))
        assertTrue(segmented.contains("BottomBarMatchedLiquidDock("))
        assertTrue(segmented.contains("drawShellLens = false"))
        assertTrue(sharedChrome.contains("drawShellLens = drawShellLens"))
        assertFalse(segmented.contains(".kernelSuFloatingDockSurface("))
        assertFalse(segmented.contains(".kernelSuMiuixFloatingDockSurface("))
        assertFalse(segmented.contains("KernelSuBottomBarIndicatorLayer("))
        assertFalse(segmented.contains("KernelSuMiuixBottomBarIndicatorLayer("))
        assertTrue(segmented.contains("rememberMiuixCombinedBackdrop("))
        assertTrue(segmented.contains(".miuixLayerBackdrop(localPageMiuixBackdrop)"))
    }

    @Test
    fun `dynamic search detail and partition chrome use shared entry points`() {
        val dynamicTopBar = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicTopBar.kt"
        )
        val dynamicScreen = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/dynamic/DynamicScreen.kt"
        )
        val search = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/search/SearchScreen.kt"
        )
        val bottomInput = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/video/ui/components/BottomInputBar.kt"
        )
        val partition = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/partition/PartitionScreen.kt"
        )
        val musicPlayer = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/audio/screen/MusicPlayerContent.kt"
        )

        assertTrue(dynamicTopBar.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(dynamicTopBar.contains("indicatorPositionProvider = indicatorPositionProvider"))
        assertTrue(dynamicTopBar.contains("isScrollInProgressProvider = isScrollInProgressProvider"))
        assertTrue(dynamicScreen.contains("BottomBarMatchedDockVisibility("))
        assertTrue(dynamicScreen.contains("edge = BottomBarMatchedDockEdge.TOP"))
        assertTrue(search.contains("BottomBarMatchedReusableLiquidDock("))
        assertTrue(bottomInput.contains("BottomBarMatchedReusableLiquidDock("))
        assertFalse(bottomInput.contains("BottomBarMatchedLiquidDock("))
        assertFalse(bottomInput.contains(".kernelSuFloatingDockSurface("))
        assertTrue(partition.contains("rememberBottomBarMatchedLiquidChromeState("))
        assertTrue(partition.contains("BottomBarMatchedLiquidIndicator("))
        assertTrue(partition.contains("orientation = BottomBarLiquidOrientation.VERTICAL"))
        assertFalse(partition.contains("KernelSuBottomBarIndicatorLayer("))
        assertTrue(musicPlayer.contains("BottomBarMatchedReusableLiquidDock("))
        assertFalse(musicPlayer.contains("bottomBarMatchedLiquidDockSurface("))
        assertFalse(musicPlayer.contains("kernelSuMiuixFloatingDockSurface("))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(File(path), File(normalizedPath)).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
