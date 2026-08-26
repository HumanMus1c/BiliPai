package com.android.purebilibili.core.ui

import com.android.purebilibili.core.ui.blur.BlurIntensity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppThemeConfigTest {

    @Test
    fun `defaults keep standalone UI hosts functional`() {
        val config = AppThemeConfig()

        assertEquals(BlurIntensity.THIN, config.blurIntensity)
        assertTrue(config.headerBlurEnabled)
        assertFalse(config.bottomBarBlurEnabled)
        assertTrue(config.hapticFeedbackEnabled)
        assertFalse(config.globalTextTapCopyEnabled)
        assertTrue(config.uiEntranceAnimationEnabled)
        assertTrue(config.runtimeVisualGuardEnabled)
    }
}
