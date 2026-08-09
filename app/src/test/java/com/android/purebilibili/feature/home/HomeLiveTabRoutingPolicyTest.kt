package com.android.purebilibili.feature.home

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeLiveTabRoutingPolicyTest {

    @Test
    fun liveTopTabOpensLiveList() {
        assertTrue(shouldOpenLiveListFromHomeTopTab(HomeCategory.LIVE))
    }

    @Test
    fun otherTopTabsStayOnHomePager() {
        assertFalse(shouldOpenLiveListFromHomeTopTab(HomeCategory.RECOMMEND))
        assertFalse(shouldOpenLiveListFromHomeTopTab(HomeCategory.FOLLOW))
        assertFalse(shouldOpenLiveListFromHomeTopTab(HomeCategory.POPULAR))
        assertFalse(shouldOpenLiveListFromHomeTopTab(HomeCategory.ANIME))
    }
}
