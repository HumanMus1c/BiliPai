package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.store.HomeSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationIconCrossScalePolicyTest {

    @Test
    fun `coverage continuously cross scales old and new navigation icons`() {
        assertEquals(1.075f, resolveNavigationIconCrossScale(true, 0.75f), 0.001f)
        assertEquals(1.025f, resolveNavigationIconCrossScale(true, 0.25f), 0.001f)
    }

    @Test
    fun `cross scale is disabled by default and selected endpoint is one point ten`() {
        assertFalse(HomeSettings().navigationIconCrossScaleEnabled)
        assertEquals(1f, resolveNavigationIconCrossScale(false, 1f), 0.001f)
        assertEquals(1f, resolveNavigationIconCrossScale(true, 0f), 0.001f)
        assertEquals(1.1f, resolveNavigationIconCrossScale(true, 1f), 0.001f)
    }
}
