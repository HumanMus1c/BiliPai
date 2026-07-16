package com.android.purebilibili.feature.bangumi

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class BangumiLiquidGlassStructureTest {

    @Test
    fun `bangumi segmented controls reuse home liquid indicator`() {
        val screenSource = sourceOf("BangumiScreen.kt")
        val filterSource = sourceOf("ui/components/BangumiFilterComponents.kt")
        val followSource = sourceOf("MyBangumiScreen.kt")

        assertTrue(screenSource.contains("fun BangumiTypeTabs("))
        assertTrue(screenSource.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(filterSource.contains("fun BangumiModeTabs("))
        assertTrue(filterSource.contains("fun BangumiIndexFilterRows("))
        assertTrue(filterSource.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(followSource.contains("fun MyFollowTypeTabs("))
        assertTrue(followSource.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(!filterSource.contains("Modifier.unifiedBlur("))
        assertTrue(!followSource.contains("Modifier.unifiedBlur("))
    }

    private fun sourceOf(path: String): String =
        File("src/main/java/com/android/purebilibili/feature/bangumi/$path").readText()
}
