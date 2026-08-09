package com.android.purebilibili.feature.home.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeNavigationMiuixStructureTest {

    @Test
    fun `home navigation runtime does not select Cupertino or Material icons`() {
        // 顶栏类别图标是刻意例外：Miuix 图标集（156 个）缺少推荐/关注/直播/番剧/游戏/
        // 知识/科技等类别图标，TopBar 类别解析器沿用 Material（与 BottomBar 同样的文档化
        // 例外）；因此严格检查只覆盖 HomeHeader 与导航图标策略，TopBar 仅禁止 Cupertino
        // 与 fallbackIconFamily 回退机制。
        val strictSources = listOf(
            "HomeHeader.kt",
            "HomeNavigationIconPolicy.kt",
        ).map(::sourceText)

        strictSources.forEach { source ->
            assertFalse(source.contains("CupertinoIcons"))
            assertFalse(source.contains("androidx.compose.material.icons"))
            assertFalse(source.contains("fallbackIconFamily"))
        }

        val topBar = sourceText("TopBar.kt")
        assertFalse(topBar.contains("CupertinoIcons"))
        assertFalse(topBar.contains("fallbackIconFamily"))
    }

    @Test
    fun `bottom bar keeps upstream Cupertino and Material icon pairs`() {
        // 底部导航栏是刻意例外：与上游 miuix 主题保持一致，使用 Cupertino（浮动坞/枚举）
        // 与 Material filled/outlined（停靠栏）成对图标，选中填充、未选中描边。
        val source = sourceText("BottomBar.kt")

        assertTrue(source.contains("enum class BottomNavItem"))
        assertTrue(source.contains("{ AppIcon(CupertinoIcons.Filled.House, contentDescription = null) }"))
        assertTrue(source.contains("{ AppIcon(CupertinoIcons.Outlined.House, contentDescription = null) }"))
        assertTrue(source.contains("internal fun resolveMaterialBottomBarIcon("))
        assertTrue(source.contains("if (selected) Icons.Filled.Home else Icons.Outlined.Home"))
    }

    @Test
    fun `home header actions use Miuix search settings and messages icons`() {
        val source = sourceText("HomeHeader.kt")

        assertTrue(source.contains("val searchIcon = MiuixIcons.Search"))
        assertTrue(source.contains("val settingsIcon = MiuixIcons.Settings"))
        assertTrue(source.contains("val inboxIcon = MiuixIcons.Messages"))
    }

    private fun sourceText(fileName: String): String = listOf(
        File("app/src/main/java/com/android/purebilibili/feature/home/components/$fileName"),
        File("src/main/java/com/android/purebilibili/feature/home/components/$fileName"),
    ).first { it.exists() }.readText()
}
