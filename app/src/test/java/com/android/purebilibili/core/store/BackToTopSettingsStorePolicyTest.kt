package com.android.purebilibili.core.store

import kotlin.test.Test
import kotlin.test.assertTrue

class BackToTopSettingsStorePolicyTest {
    @Test
    fun backToTopButton_isEnabledByDefault() {
        assertTrue(DEFAULT_BACK_TO_TOP_BUTTON_ENABLED)
    }
}
