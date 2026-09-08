package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsPageBlurPreferenceStructureTest {
    @Test
    fun settingsPageChromeUsesHomeProgressiveBlurWithoutReadabilityScrim() {
        val source = locate(
            "src/main/java/com/android/purebilibili/feature/settings/ui/SettingsPageScaffold.kt"
        ).readText()

        assertTrue(source.contains("topBarBlurEnabled: Boolean? = null"))
        assertTrue(source.contains("val effectiveTopBarBlurEnabled = topBarBlurEnabled ?: globalTopBarBlurEnabled"))
        assertTrue(source.contains("enabled = effectiveTopBarBlurEnabled"))
        assertTrue(source.contains(".biliPaiProgressiveTopBlur("))
        assertTrue(source.contains("Modifier.layerBackdrop(backdrop)"))
        assertFalse(source.contains("TopReadabilityChrome"))
        assertTrue(source.contains("top = padding.calculateTopPadding()"))
        assertTrue(source.contains("if (progressiveBlurEnabled) rememberLayerBackdrop()"))
        assertFalse(source.contains(".fillMaxSize()\n                .hazeSourceCompat(state = hazeState)"))
    }

    @Test
    fun nonGlassMiuixSettingsRootUsesCollapsibleLargeTitleAndSolidFallback() {
        val scaffold = locate(
            "src/main/java/com/android/purebilibili/feature/settings/ui/SettingsPageScaffold.kt"
        ).readText()
        val settings = locate(
            "src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt"
        ).readText()

        assertTrue(scaffold.contains("topBarStyle: AppTopBarStyle = AppTopBarStyle.SMALL"))
        assertTrue(scaffold.contains("rememberAppTopBarCollapseBehavior()"))
        assertTrue(scaffold.contains("modifier.appTopBarNestedScroll(collapseBehavior)"))
        assertTrue(scaffold.contains("!progressiveBlurEnabled"))
        assertTrue(settings.contains("destination == SettingsNavDestination.Home"))
        assertTrue(settings.contains("AppTopBarStyle.LARGE"))
    }

    private fun locate(path: String): File {
        return listOf(File(path), File("app/$path"))
            .firstOrNull { it.exists() }
            ?: error("Cannot locate $path from cwd")
    }
}
