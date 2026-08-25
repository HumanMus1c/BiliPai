package com.android.purebilibili.feature.list

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersonalListSelectorStructureTest {

    @Test
    fun favoritePrimarySelectors_areTapFirstAndAlwaysReachable() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt",
        )
        val categorySelector = source
            .substringAfter("if (favoriteViewModel != null) {")
            .substringBefore("if (favoriteViewModel != null && isSearchDestination")

        assertTrue(categorySelector.contains("AppLiquidAwareTabRow("))
        assertFalse(categorySelector.contains("AppNativeTabRow("))
        assertTrue(categorySelector.contains("FavoriteSection.entries.map"))
        assertFalse(categorySelector.contains("LazyRow("))
        assertFalse(source.contains("systemGestureExclusion"))
    }

    @Test
    fun favoriteFolderNavigation_usesSelectorAndProgrammaticPagerOnly() {
        val source = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/list/CommonListScreen.kt",
        )
        val pagerSection = source
            .substringAfter("} else when (favoriteContentMode) {")
            .substringAfter("FavoriteContentMode.PAGER ->")
            .substringBefore("FavoriteContentMode.SINGLE_FOLDER ->")

        assertTrue(source.contains("FavoriteFolderSelector("))
        assertTrue(source.contains("AppDropdownMenu("))
        assertFalse(source.contains("FavoriteFolderSummary("))
        assertFalse(source.contains("selectedValue = favoriteBrowseSection"))
        assertTrue(pagerSection.contains("userScrollEnabled = false"))
        assertFalse(pagerSection.contains("verticalPriorityHorizontalPagerSwipe"))
    }

    @Test
    fun secondaryPersonalFilters_doNotRequireHorizontalDrag() {
        val categorySource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/list/FavoriteCategoryScreen.kt",
        )
        val watchLaterSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/watchlater/WatchLaterScreen.kt",
        )
        val profileSource = loadSource(
            "app/src/main/java/com/android/purebilibili/feature/profile/ProfileScreen.kt",
        )

        assertTrue(categorySource.contains("AppThemeAdaptiveTabRow("))
        assertFalse(categorySource.contains("AppFilterChip("))
        assertTrue(watchLaterSource.contains("AppThemeAdaptiveTabRow("))
        assertFalse(watchLaterSource.contains("AppFilterChip("))
        val profileTabs = profileSource
            .substringAfter("private fun ProfileSpaceTabs(")
            .substringBefore("private fun ProfileSpaceTabBody(")
        assertTrue(profileTabs.contains("AppNativeTabRow("))
        assertFalse(profileTabs.contains("BottomBarLiquidSegmentedControl("))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath),
            File("app/$normalizedPath"),
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
