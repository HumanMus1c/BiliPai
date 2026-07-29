package com.android.purebilibili.core.ui

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppChromeNavigationApiStructureTest {

    @Test
    fun neutralChromeApisDelegateToExistingAdaptiveRenderers() {
        val chrome = loadSource("design-system/src/main/java/com/android/purebilibili/core/ui/AdaptiveChrome.kt")
        val navigation = loadSource("app/src/main/java/com/android/purebilibili/core/ui/SplitLayout.kt")

        assertTrue(chrome.contains("fun AppScaffold("))
        assertTrue(chrome.contains(") = AdaptiveScaffold("))
        assertTrue(chrome.contains("fun AppTopBar("))
        assertTrue(chrome.contains(") = AdaptiveTopAppBar("))
        assertTrue(navigation.contains("fun AppSplitLayout("))
        assertTrue(navigation.contains(") = AppAdaptiveSplitLayout("))
    }

    @Test
    fun migratedFeatureCallersDoNotUseLegacyChromeApis() {
        val sourceRoot = listOf(
            File("app/src/main/java/com/android/purebilibili/feature"),
            File("src/main/java/com/android/purebilibili/feature"),
        ).first(File::isDirectory)
        val legacyCalls = Regex(
            """\b(AdaptiveScaffold|AdaptiveTopAppBar|AdaptiveSplitLayout)\s*\("""
        )
        val offenders = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { legacyCalls.containsMatchIn(it.readText()) }
            .toList()

        assertFalse(offenders.isNotEmpty(), "Legacy chrome callers: ${offenders.joinToString { it.path }}")
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/").removePrefix("design-system/")
        return listOf(File(path), File("../$path"), File(normalizedPath))
            .firstOrNull(File::exists)
            ?.readText()
            ?: error("Cannot locate $path from ${File(".").absolutePath}")
    }
}
