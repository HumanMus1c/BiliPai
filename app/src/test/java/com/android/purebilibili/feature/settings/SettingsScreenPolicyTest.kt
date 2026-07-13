package com.android.purebilibili.feature.settings

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsScreenPolicyTest {

    @Test
    fun `settings back target chooses the topmost overlay`() {
        assertEquals(
            SettingsBackTarget.BLOCKED_LIST,
            resolveSettingsBackTarget(
                showBlockedList = true,
                showCacheDialog = true,
                showCacheAnimation = true,
            )
        )
        assertEquals(
            SettingsBackTarget.CACHE_ANIMATION,
            resolveSettingsBackTarget(
                showCacheDialog = true,
                showCacheAnimation = true,
            )
        )
    }

    @Test
    fun `settings back target is none without a local overlay`() {
        assertEquals(SettingsBackTarget.NONE, resolveSettingsBackTarget())
    }

    @Test
    fun topLevelSettings_bottomPaddingIncludesVisibleBottomBarHeight() {
        val padding = resolveSettingsContentBottomPadding(
            navigationBarsBottom = 16.dp,
            bottomBarVisible = true,
            isBottomBarFloating = true,
            bottomBarLabelMode = 0,
            isTablet = false
        )

        assertEquals(142.dp, padding)
    }

    @Test
    fun secondarySettingsLayout_keepsLegacyBottomPaddingWhenBottomBarHidden() {
        val padding = resolveSettingsContentBottomPadding(
            navigationBarsBottom = 16.dp,
            bottomBarVisible = false,
            isBottomBarFloating = true,
            bottomBarLabelMode = 0,
            isTablet = false
        )

        assertEquals(44.dp, padding)
    }
}
