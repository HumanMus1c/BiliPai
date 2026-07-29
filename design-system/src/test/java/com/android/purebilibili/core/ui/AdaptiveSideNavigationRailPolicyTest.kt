package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveSideNavigationRailPolicyTest {

    @Test
    fun miuixVariantUsesMiuixRailRenderer() {
        assertEquals(
            AdaptiveSideNavigationRailRenderer.MIUIX,
            resolveAdaptiveSideNavigationRailRenderer(UiPreset.MD3, AndroidNativeVariant.MIUIX),
        )
    }

    @Test
    fun materialKeepsMaterial3RailRenderer() {
        assertEquals(
            AdaptiveSideNavigationRailRenderer.MATERIAL3,
            resolveAdaptiveSideNavigationRailRenderer(UiPreset.MD3, AndroidNativeVariant.MATERIAL3),
        )
    }

    @Test
    fun expandableOnlyWhenExpandedWidthClass() {
        assertTrue(shouldUseExpandableMiuixNavigationRail(isExpandedWidthClass = true))
        assertFalse(shouldUseExpandableMiuixNavigationRail(isExpandedWidthClass = false))
    }
}
