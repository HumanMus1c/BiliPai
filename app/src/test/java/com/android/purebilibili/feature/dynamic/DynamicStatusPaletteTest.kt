package com.android.purebilibili.feature.dynamic

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicStatusPaletteTest {
    @Test
    fun likedColor_isAStableDynamicStatusRole() {
        assertEquals(Color(0xFFFF6B81), DynamicStatusPalette.Liked)
    }
}
