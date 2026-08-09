package com.android.purebilibili.core.store

import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeTopTabSettingsMappingPolicyTest {

    @Test
    fun emptyPreferences_useExpectedTopTabDefaults() {
        val prefs = mutablePreferencesOf()

        val result = mapHomeTopTabSettingsFromPreferences(prefs)

        assertEquals(5, SettingsManager.MAX_TOP_TABS)
        assertEquals(
            listOf("RECOMMEND", "FOLLOW", "POPULAR", "LIVE", "GAME"),
            result.orderIds
        )
        assertEquals(
            setOf("RECOMMEND", "FOLLOW", "POPULAR", "LIVE", "GAME"),
            result.visibleIds
        )
    }

    @Test
    fun populatedPreferences_mapTopTabOrderAndVisibility() {
        val prefs = mutablePreferencesOf(
            stringPreferencesKey("top_tab_order") to "POPULAR,LIVE,RECOMMEND,FOLLOW",
            stringPreferencesKey("top_tab_visible_tabs") to "POPULAR,RECOMMEND"
        )

        val result = mapHomeTopTabSettingsFromPreferences(prefs)

        assertEquals(listOf("POPULAR", "LIVE", "RECOMMEND", "FOLLOW"), result.orderIds)
        assertEquals(setOf("POPULAR", "RECOMMEND"), result.visibleIds)
    }

    @Test
    fun overLimitVisibleTabs_areCappedToMaxKeepingUserOrder() {
        val prefs = mutablePreferencesOf(
            stringPreferencesKey("top_tab_order") to
                "RECOMMEND,FOLLOW,POPULAR,LIVE,ANIME,GAME,KNOWLEDGE,TECH,PARTITION",
            stringPreferencesKey("top_tab_visible_tabs") to
                "RECOMMEND,FOLLOW,POPULAR,LIVE,ANIME,GAME,KNOWLEDGE,TECH,PARTITION"
        )

        val result = mapHomeTopTabSettingsFromPreferences(prefs)

        // 保留用户顺序的前 MAX_TOP_TABS 个可见项
        assertEquals(
            setOf("RECOMMEND", "FOLLOW", "POPULAR", "LIVE", "ANIME"),
            result.visibleIds
        )
    }
}
