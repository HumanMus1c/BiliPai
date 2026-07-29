package com.android.purebilibili.feature.home.components

import androidx.compose.ui.unit.sp
import kotlin.test.Test
import kotlin.test.assertEquals

class BottomBarTypographySpecTest {
    @Test
    fun skinDockTypography_keepsItsCompactOverlayDensity() {
        assertEquals(12.sp, resolveBottomBarSkinDockLabelFontSize())
        assertEquals(18.sp, resolveBottomBarSkinDockLabelLineHeight())
    }
}
