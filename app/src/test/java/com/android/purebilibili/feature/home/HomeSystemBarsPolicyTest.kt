package com.android.purebilibili.feature.home

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeSystemBarsPolicyTest {

    @Test
    fun systemBars_areOnlyOwnedWhileHomeIsTopLevel() {
        assertTrue(shouldApplyHomeSystemBars(isTopLevelActive = true))
        assertFalse(shouldApplyHomeSystemBars(isTopLevelActive = false))
    }
}
