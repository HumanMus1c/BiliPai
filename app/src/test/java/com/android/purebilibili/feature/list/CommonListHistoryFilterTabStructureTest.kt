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

        assertTrue(
            searchSection.contains("historyViewModel != null && historyFilterChrome.useLiquidDock"),
            "历史搜索 Dock 必须同时受历史页范围和全局复用开关约束"
        )
        assertTrue(
            searchSection.contains("BottomBarMatchedLiquidDock("),
            "历史搜索应复用与标签相同的底栏材质 Dock"
        )
        assertTrue(
            searchSection.contains("shape = CircleShape"),
            "搜索 Dock 与标签 Dock 必须使用相同的胶囊圆角"
        )
        assertTrue(
            searchSection.contains("height(historyFilterChrome.heightDp.dp)"),
            "搜索与标签两条 Dock 必须保持相同高度"
        )
        assertTrue(
            searchSection.contains("primaryGridState.isScrollInProgress"),
            "搜索 Dock 的材质动态应跟随历史列表滚动"
        )
    }

    @Test
    fun historyFilterRow_centersTabsAndUsesLiquidDockWhenGlobalGlassEnabled() {
        val source = loadSource("src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt")
        val historyFilterSection = source
            .substringAfter("val historyFilterLabels = remember")
            .substringBefore("if (favoriteViewModel != null && subscribedFoldersState.isNotEmpty())")

        assertTrue(
            source.contains("resolveHistoryFilterTabChromeSpec"),
            "历史筛选行必须走统一的 tab chrome 策略"
        )
        assertTrue(
            historyFilterSection.contains("contentAlignment = Alignment.Center"),
            "历史筛选行必须居中布局"
        )
        assertTrue(
            historyFilterSection.contains("BottomBarLiquidSegmentedControl("),
            "开启全局液态玻璃时应复用底栏 dock 分段控件"
        )
        assertTrue(
            historyFilterSection.contains("miuixBackdrop = commonListChromeBackdrop"),
            "液态 dock 必须绑定与顶栏一致的 backdrop 源"
        )
        assertTrue(
            historyFilterSection.contains("forceLiquidChrome = homeSettings.androidNativeLiquidGlassEnabled"),
            "液态 dock 必须跟随全局液态玻璃开关"
        )
        assertTrue(
            historyFilterSection.contains("dragSelectionEnabled = historyFilterChrome.dragSelectionEnabled"),
            "个人列表筛选必须使用点击切换，避免与系统返回手势竞争"
        )
        assertTrue(
            historyFilterSection.contains("modifier = Modifier.fillMaxWidth()"),
            "液态 dock 应铺满可用宽度，避免固定 itemWidth 把指示器压扁"
        )
        assertTrue(
            historyFilterSection.contains("itemWidth = historyFilterChrome.itemWidthDp?.dp"),
            "液态 dock 不应再强制固定 tab 宽度"
        )
        val liquidDockBranch = historyFilterSection
            .substringAfter("if (historyFilterChrome.useLiquidDock) {")
            .substringBefore("} else {")
        assertFalse(
            liquidDockBranch.contains("LazyRow("),
            "液态 dock 分支不应继续依赖横向 FilterChip 列表"
        )
        val fallbackBranch = historyFilterSection.substringAfter("} else {")
        assertTrue(
            fallbackBranch.contains("FlowRow("),
            "非液态模式也应自动换行，不能把主筛选放进横向滚动列表"
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
