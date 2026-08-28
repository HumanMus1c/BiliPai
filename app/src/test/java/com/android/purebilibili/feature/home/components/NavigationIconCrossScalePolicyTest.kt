package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.store.HomeSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationIconCrossScalePolicyTest {

    @Test
    fun `coverage only enlarges old and new navigation icons during transition`() {
        assertEquals(1.212f, resolveNavigationIconCrossScale(true, 0.75f), 0.001f)
        assertEquals(1.3f, resolveNavigationIconCrossScale(true, 0.5f), 0.001f)
        assertEquals(1.212f, resolveNavigationIconCrossScale(true, 0.25f), 0.001f)
    }

    @Test
    fun `cross scale is enabled by default and rests at authored size`() {
        assertTrue(HomeSettings().navigationIconCrossScaleEnabled)
        assertEquals(1f, resolveNavigationIconCrossScale(false, 1f), 0.001f)
        assertEquals(1f, resolveNavigationIconCrossScale(true, 0f), 0.001f)
        assertEquals(1f, resolveNavigationIconCrossScale(true, 1f), 0.001f)
        assertEquals(0f, resolveNavigationIconSelectionLiftDp(1f), 0.001f)
        assertEquals(
            8f,
            resolveNavigationIconSelectionLiftDp(FloatingBottomBarSelectionScale),
            0.001f,
        )
    }
}
