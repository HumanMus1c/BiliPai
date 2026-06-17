package com.android.purebilibili.feature.settings

import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsRootCategoryPolicyTest {

    @Test
    fun `mobile and tablet settings share scene based root category order`() {
        val expected = listOf(
            SettingsRootCategory.INTERFACE_HOME,
            SettingsRootCategory.DYNAMIC_RECOMMEND,
            SettingsRootCategory.PLAYBACK_INTERACTION,
            SettingsRootCategory.NAVIGATION_GESTURE,
            SettingsRootCategory.DATA_PRIVACY,
            SettingsRootCategory.EXTENSION_ABOUT
        )

        assertEquals(expected, resolveSettingsRootCategoryOrder())
        assertEquals(resolveSettingsRootCategoryOrder(), resolveTabletSettingsRootCategoryOrder())
    }

    @Test
    fun `scene based root categories expose user facing titles`() {
        assertEquals(
            listOf(
                "界面与首页",
                "动态与推荐",
                "播放与互动",
                "导航与手势",
                "数据与隐私",
                "扩展与关于"
            ),
            resolveSettingsRootCategoryOrder().map { it.title }
        )
    }

    @Test
    fun `scene search targets map back to root categories`() {
        assertEquals(
            SettingsRootCategory.DYNAMIC_RECOMMEND,
            resolveSettingsRootCategoryForSearchTarget(SettingsSearchTarget.HOME_FEED)
        )
        assertEquals(
            SettingsRootCategory.NAVIGATION_GESTURE,
            resolveSettingsRootCategoryForSearchTarget(SettingsSearchTarget.FULLSCREEN_GESTURE)
        )
        assertEquals(
            SettingsRootCategory.EXTENSION_ABOUT,
            resolveSettingsRootCategoryForSearchTarget(SettingsSearchTarget.DIAGNOSTICS)
        )
        assertEquals(
            SettingsRootCategory.EXTENSION_ABOUT,
            resolveSettingsRootCategoryForSearchTarget(SettingsSearchTarget.TELEGRAM)
        )
    }

    @Test
    fun `root category list index points to content cards after search bar`() {
        assertEquals(2, resolveSettingsRootCategoryListIndex(SettingsRootCategory.INTERFACE_HOME))
        assertEquals(3, resolveSettingsRootCategoryListIndex(SettingsRootCategory.DYNAMIC_RECOMMEND))
        assertEquals(4, resolveSettingsRootCategoryListIndex(SettingsRootCategory.PLAYBACK_INTERACTION))
        assertEquals(7, resolveSettingsRootCategoryListIndex(SettingsRootCategory.EXTENSION_ABOUT))
    }
}
