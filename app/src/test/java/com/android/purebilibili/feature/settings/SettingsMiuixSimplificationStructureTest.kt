package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsMiuixSimplificationStructureTest {

    @Test
    fun `appearance settings expose one ui style selection while keeping miuix scaffold`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt")

        assertTrue(source.contains("resolveThemeSelectionOptions("))
        assertTrue(source.contains("resolveAppearanceUiPresetDescription("))
        assertTrue(source.contains("onSelectionChange = viewModel::setThemeSelection"))
        assertFalse(source.contains("resolveAndroidNativeVariantSegmentOptions("))
        assertFalse(source.contains("viewModel.setUiPreset("))
        assertFalse(source.contains("viewModel.setAndroidNativeVariant("))
        assertTrue(source.contains("安卓原生液态玻璃"))
        assertTrue(source.contains("toggleAndroidNativeLiquidGlass("))
        assertTrue(source.contains("SettingsPageScaffold("))
        assertFalse(source.contains("MiuixScaffold("))
        assertFalse(source.contains("MiuixSmallTopAppBar("))
    }

    @Test
    fun `animation settings expose independent glass toggles without duplicating global native control`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt")

        assertFalse(source.contains("previewLiquidGlassProgress"))
        assertFalse(source.contains("通透到磨砂"))
        assertTrue(source.contains("顶部标签栏液态玻璃"))
        assertTrue(source.contains("toggleTopBarLiquidGlass("))
        assertTrue(source.contains("首页搜索框液态玻璃"))
        assertTrue(source.contains("toggleHomeSearchLiquidGlass("))
        assertTrue(source.contains("底栏液态玻璃"))
        assertFalse(source.contains("title = \"安卓原生液态玻璃\""))
        assertFalse(source.contains("toggleAndroidNativeLiquidGlass("))
        assertTrue(source.contains("LiquidGlassAdjustmentPanel("))
        assertTrue(source.contains("转场时模糊背景"))
        assertTrue(source.contains("toggleVideoTransitionRealtimeBlur("))
        assertTrue(source.contains("SettingsPageScaffold("))
    }

    @Test
    fun `liquid glass adjustment uses live miuix backdrop and commits on release`() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/settings/LiquidGlassLivePreview.kt"
        )

        assertTrue(source.contains("rememberLayerBackdrop()"))
        assertTrue(source.contains(".layerBackdrop(backdrop)"))
        assertTrue(source.contains(".biliPaiFloatingDockShell("))
        assertTrue(source.contains("onValueChangeFinished = { onProgressCommitted(previewProgress) }"))
        assertFalse(source.contains("import androidx.compose.foundation.layout.weight"))
        assertTrue(source.contains("ActivityResultContracts.OpenDocument()"))
        assertTrue(source.contains("takePersistableUriPermission("))
        assertTrue(source.contains("AsyncImage("))
        assertTrue(source.contains("onPreviewImageChanged(null)"))
        assertTrue(source.contains("LiquidGlassAdvancedPreset.entries"))
        assertFalse(source.contains("AppNativeSegmentedControl("))
        assertTrue(source.contains("contentDescription = \"液态玻璃效果预设\""))
        assertTrue(source.contains("steps = 2"))
        assertTrue(source.contains("title = \"内容可读性\""))
        assertTrue(source.contains("title = \"色散强度\""))
        assertTrue(source.contains("title = \"文字与图标扭曲\""))
        assertTrue(source.contains("preset = LiquidGlassAdvancedPreset.CUSTOM"))
        assertTrue(source.contains("onAdvancedSettingsCommitted(advancedSettings)"))
        assertTrue(source.contains("detectVerticalDragGestures"))
        assertTrue(source.contains("previewPanOffsetPx"))
        assertTrue(source.contains("sliderFollowOffset"))
        assertTrue(source.contains("translationY = previewPanOffsetPx + sliderFollowOffset"))
    }

    @Test
    fun `animation liquid glass section keeps independent top dock and bottom bar toggles`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt")

        assertTrue(
            Regex("""icon = rememberSettingsSemanticIcon\(SettingsIconRole\.TOP_DOCK_GLASS\),\s*title = "顶部标签栏液态玻璃"""")
                .containsMatchIn(source)
        )
        assertTrue(
            Regex("""icon = rememberSettingsSemanticIcon\(SettingsIconRole\.HOME_SEARCH_GLASS\),\s*title = "首页搜索框液态玻璃"""")
                .containsMatchIn(source)
        )
        assertTrue(
            Regex("""icon = rememberSettingsSemanticIcon\(SettingsIconRole\.BOTTOM_BAR_GLASS\),\s*title = "底栏液态玻璃"""")
                .containsMatchIn(source)
        )
    }

    @Test
    fun `settings groups avoid duplicate setting icons`() {
        val paths = listOf(
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/AppearanceSettingsScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/PermissionSettingsScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/screen/PlaybackSettingsScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/share/SettingsShareScreen.kt",
            "app/src/main/java/com/android/purebilibili/feature/settings/webdav/WebDavBackupScreen.kt"
        )

        val duplicateIcons = paths.flatMap { path ->
            findDuplicateSettingIcons(path, loadSource(path))
        }

        assertTrue(
            duplicateIcons.isEmpty(),
            duplicateIcons.joinToString(separator = "\n")
        )
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText().replace("\r\n", "\n")
    }

    private fun findDuplicateSettingIcons(path: String, source: String): List<String> {
        val lines = source.lines()
        val duplicates = mutableListOf<String>()
        var inGroup = false
        var groupStartLine = 0
        var braceDepth = 0
        var pendingIcon: PendingIcon? = null
        val entries = mutableListOf<SettingIconEntry>()

        fun flushGroup() {
            entries
                .groupBy { it.icon }
                .filterValues { it.size > 1 }
                .forEach { (icon, repeatedEntries) ->
                    duplicates += "$path:$groupStartLine repeats $icon for ${
                        repeatedEntries.joinToString { "${it.title}@${it.lineNumber}" }
                    }"
                }
            entries.clear()
            pendingIcon = null
        }

        lines.forEachIndexed { index, line ->
            if (line.contains("IOSGroup") || line.contains("AppPreferenceGroup")) {
                inGroup = true
                groupStartLine = index + 1
                braceDepth = 0
                entries.clear()
                pendingIcon = null
            }

            if (inGroup) {
                braceDepth += line.count { it == '{' }
                braceDepth -= line.count { it == '}' }

                Regex("""icon\s*=\s*([^,]+),?""")
                    .find(line)
                    ?.let { match ->
                        pendingIcon = PendingIcon(
                            icon = match.groupValues[1].trim(),
                            lineNumber = index + 1
                        )
                    }

                Regex("""title\s*=\s*"([^"]+)"""")
                    .find(line)
                    ?.let { match ->
                        val icon = pendingIcon ?: return@let
                        entries += SettingIconEntry(
                            icon = icon.icon,
                            title = match.groupValues[1],
                            lineNumber = icon.lineNumber
                        )
                        pendingIcon = null
                    }

                if (braceDepth <= 0 && index + 1 > groupStartLine) {
                    flushGroup()
                    inGroup = false
                }
            }
        }

        return duplicates
    }

    private data class PendingIcon(
        val icon: String,
        val lineNumber: Int
    )

    private data class SettingIconEntry(
        val icon: String,
        val title: String,
        val lineNumber: Int
    )
}
