package com.android.purebilibili.feature.home.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class BottomBarFloatingSegmentedControlBackdropStructureTest {

    @Test
    fun `external backdrop stays singular and local sampling is fallback only`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarFloatingSegmentedControl.kt"
        )

        assertTrue(source.contains("val localBackdrop = rememberLayerBackdrop()"))
        assertTrue(source.contains("miuixBackdrop ?: localBackdrop"))
        assertTrue(source.contains("effectiveBackdrop != null && miuixBackdrop == null"))
        assertTrue(!source.contains("rememberCombinedBackdrop(localBackdrop, miuixBackdrop)"))
        assertTrue(source.contains(".matchParentSize()"))
        assertTrue(source.contains(".bottomBarMatchedCaptureOverflow("))
        assertTrue(source.contains("horizontalInset = captureInsets.horizontalDp.dp"))
        assertTrue(source.contains("verticalInset = captureInsets.verticalDp.dp"))
        assertTrue(source.contains(".layerBackdrop(localBackdrop)"))
        assertTrue(source.contains(".background(AppSurfaceTokens.background())"))
        assertTrue(source.contains("backdrop = effectiveBackdrop"))
    }

    @Test
    fun `reused liquid dock applies caller sizing to its direct layout root`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/home/components/BottomBarFloatingSegmentedControl.kt"
        )

        assertTrue(source.contains("modifier = rootModifier.height(height)"))
        assertTrue(source.contains("modifier = Modifier.matchParentSize()"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(File(path), File(normalizedPath)).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
