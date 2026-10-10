package com.android.purebilibili.feature.dynamic

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DynamicTabDragScrollPolicyTest {
    @Test
    fun dynamicTabsDelegateViewportAndSelectionToSharedRenderer() {
        val path = "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicTopBar.kt"
        val source = listOf(File("app/$path"), File(path)).first { it.exists() }.readText()
        assertTrue(source.contains("AppThemeAdaptiveTabRow("))
        assertTrue(source.contains("scrollable = true"))
        assertFalse(source.contains("resolveDynamicTabVisibilityScrollTarget"))
        assertFalse(source.contains("tabItemWidthPx"))
        assertFalse(source.contains("animateScrollTo("))
    }
}
