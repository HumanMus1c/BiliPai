package com.android.purebilibili.feature.list

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CommonListHistoryFilterTabStructureTest {
    @Test
    fun historySearch_reusesMatchingLiquidDockAsSeparateRow() {
        val source = loadSource("src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt")
        val searchSection = source
            .substringAfter("val searchPlaceholder = when")
            .substringBefore("if (favoriteViewModel != null)")

        assertTrue(searchSection.contains("AppLiquidAwareSearchField("))
        assertTrue(searchSection.contains("backdrop = commonListChromeBackdrop"))
        assertTrue(
            searchSection.contains("primaryGridState.isScrollInProgress"),
            "搜索 Dock 的材质动态应跟随历史列表滚动"
        )
    }

    @Test
    fun historyFilterRow_usesThemeAdaptiveTabsInsteadOfFilterChips() {
        val source = loadSource("src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt")
        val historyFilterSection = source
            .substringAfter("val historyFilterOptions = remember")
            .substringBefore("if (favoriteViewModel != null) {")

        assertTrue(
            source.contains("resolveHistoryFilterTabChromeSpec"),
            "历史筛选行必须走统一的 tab chrome 策略"
        )
        assertTrue(
            historyFilterSection.contains("AppThemeAdaptiveTabRow("),
            "历史筛选必须走主题控件分流：关液态玻璃时 MD3 下划线、开玻璃时液态胶囊"
        )
        assertTrue(historyFilterSection.contains("dragSelectionEnabled = historyFilterChrome.dragSelectionEnabled"))
        assertTrue(historyFilterSection.contains("tapPressRefractionEnabled = true"))
        assertTrue(historyFilterSection.contains("height = historyFilterChrome.heightDp.dp"))
        assertTrue(historyFilterSection.contains("indicatorHeight = historyFilterChrome.indicatorHeightDp.dp"))
        assertTrue(
            historyFilterSection.contains("miuixBackdrop = commonListChromeBackdrop"),
            "液态路径必须绑定与顶栏一致的 backdrop 源"
        )
        assertFalse(
            historyFilterSection.contains("AppFilterChip("),
            "关闭液态玻璃后不得回退到胶囊 FilterChip"
        )
        assertFalse(
            historyFilterSection.contains("BottomBarLiquidSegmentedControl("),
            "历史筛选不得绕过 AppThemeAdaptiveTabRow 直调液态分段控件"
        )
        assertFalse(
            historyFilterSection.contains("forceLiquidChrome"),
            "液态 dock 应由共享组件统一读取全局开关"
        )
        assertFalse(
            historyFilterSection.contains("LazyRow("),
            "历史筛选不应依赖横向 FilterChip 列表"
        )
        assertFalse(
            historyFilterSection.contains("FlowRow("),
            "历史筛选不应再把主筛选放进 FlowRow 胶囊"
        )
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath),
            File("app/$normalizedPath")
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
