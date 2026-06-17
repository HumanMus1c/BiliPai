package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRootCategoryContentStructureTest {

    @Test
    fun rootCategoryContent_usesStateAndActionHolders() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText()

        assertTrue(source.contains("internal data class SettingsRootCategoryActions("))
        assertTrue(source.contains("internal data class SettingsRootCategoryState("))
        assertTrue(
            source.contains(
                """
                internal fun SettingsRootCategoryContent(
                    category: SettingsRootCategory,
                    actions: SettingsRootCategoryActions,
                    state: SettingsRootCategoryState
                )
                """.trimIndent()
            )
        )
    }

    @Test
    fun sceneShortcutSection_submitsDetailFocusBeforeOpeningShortcut() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText()

        val sectionBlock = source
            .substringAfter("internal fun SettingsSceneShortcutSection(")
            .substringBefore("internal fun SettingsRootCategoryContent(")

        assertTrue(sectionBlock.contains("resolveSettingsSceneDetailFocus(shortcut.target)?.let"))
        assertTrue(sectionBlock.contains("SettingsSearchFocusController.submit(detailFocus.target, detailFocus.focusId)"))
        assertTrue(sectionBlock.contains("shortcut.onClick()"))
    }

    @Test
    fun sceneShortcutSection_rendersLongDescriptionAsSubtitle() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText()

        val sectionBlock = source
            .substringAfter("internal fun SettingsSceneShortcutSection(")
            .substringBefore("internal fun SettingsRootCategoryContent(")

        assertTrue(sectionBlock.contains("subtitle = shortcut.value"))
    }

    @Test
    fun feedSwitchDescription_allowsWrappingInIosSettings() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText()

        val feedSwitchBlock = source
            .substringAfter("private fun FeedSwitchItem(")
            .substringBefore("@Composable\nprivate fun FeedRefreshCountItem(")

        assertTrue(feedSwitchBlock.contains("text = subtitle"))
        assertTrue(!feedSwitchBlock.contains("maxLines = 1"))
    }

    @Test
    fun mobileSettingsRoot_doesNotRenderDuplicateCategoryHeaders() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt")
        ).first { it.exists() }.readText()

        val rootLoopBlock = source
            .substringAfter("sectionOrder.forEachIndexed { index, section ->")
            .substringBefore("item { Spacer(modifier = Modifier.height(16.dp)) }")

        assertFalse(rootLoopBlock.contains("SettingsCategoryHeader("))
        assertTrue(rootLoopBlock.contains("SettingsRootCategoryContent("))
    }

    @Test
    fun rootCategoryContent_stacksMultipleCardsInsideAnimatedMobileBox() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText()

        val contentBlock = source
            .substringAfter("internal fun SettingsRootCategoryContent(")
            .substringBefore("@Composable\nfun SupportToolsSection(")

        assertTrue(contentBlock.contains("Column {\n        when (category)"))
        assertTrue(contentBlock.contains("SettingsRootCategory.INTERFACE_HOME -> SettingsSceneShortcutSection("))
        assertTrue(contentBlock.contains("SettingsRootCategory.DYNAMIC_RECOMMEND -> {"))
        assertTrue(contentBlock.contains("SettingsRootCategory.DATA_PRIVACY -> {"))
        assertTrue(contentBlock.contains("SettingsRootCategory.EXTENSION_ABOUT -> {"))
    }

    @Test
    fun mobileSettingsRootPinsFollowAuthorSectionAboveCategories() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/SettingsScreen.kt")
        ).first { it.exists() }.readText()

        val rootListBlock = source
            .substringAfter("LazyColumn(")
            .substringBefore("sectionOrder.forEachIndexed")

        assertTrue(rootListBlock.contains("FollowAuthorSection("))
        assertTrue(rootListBlock.indexOf("FollowAuthorSection(") > rootListBlock.indexOf("SettingsSearchBarSection("))
    }

    @Test
    fun tabletSettingsRootPinsFollowAuthorSectionAboveCategoryContent() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/TabletSettingsLayout.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/TabletSettingsLayout.kt")
        ).first { it.exists() }.readText()

        val rootDetailBlock = source
            .substringAfter("// Category Root")
            .substringBefore("Spacer(modifier = Modifier\n                                .windowInsetsBottomHeight")

        assertTrue(rootDetailBlock.contains("FollowAuthorSection("))
        assertTrue(rootDetailBlock.indexOf("FollowAuthorSection(") < rootDetailBlock.indexOf("SettingsRootCategoryContent("))
    }

    @Test
    fun aboutSupport_keepsReleaseChannelBelowAboutDetailsWithoutDuplicateAuthorCard() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText()

        val aboutBlock = source
            .substringAfter("SettingsRootCategory.EXTENSION_ABOUT -> {")
            .substringBefore("SupportToolsSection(")

        assertTrue(aboutBlock.indexOf("AboutSection(") < aboutBlock.indexOf("ReleaseChannelPinnedCard("))
        assertFalse(aboutBlock.contains("FollowAuthorSection("))
    }

    @Test
    fun aboutSection_doesNotRenderDuplicateReleaseChannelDisclaimerEntry() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText()

        val aboutSectionBlock = source
            .substringAfter("fun AboutSection(")
            .substringBefore("@Composable\nfun CheckUpdateSection(")

        assertFalse(aboutSectionBlock.contains("title = \"发布渠道声明\""))
        assertFalse(aboutSectionBlock.contains("SettingsSearchTarget.DISCLAIMER"))
    }

    @Test
    fun releaseChannelPinnedCard_keepsActionsInOneLine() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/ui/SettingsSections.kt")
        ).first { it.exists() }.readText()

        val pinnedCardBlock = source
            .substringAfter("fun ReleaseChannelPinnedCard(")
            .substringBefore("@Composable\nfun SettingsSubpageEntrySection(")

        assertTrue(pinnedCardBlock.contains("modifier = Modifier.fillMaxWidth()"))
        assertTrue(pinnedCardBlock.contains("modifier = Modifier.weight(1f)"))
        assertTrue(pinnedCardBlock.contains("softWrap = false"))
        assertTrue(pinnedCardBlock.contains("maxLines = 1"))
    }
}
