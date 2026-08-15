package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppPrimitiveRendererPolicyTest {

    @Test
    fun sliderUsesMiuixOnMiuixStyleAndMaterialOnMd3() {
        assertEquals(
            AppPrimitiveRenderer.MIUIX,
            resolveAppSliderRenderer(AppUiStyle.MIUIX),
        )
        assertEquals(
            AppPrimitiveRenderer.MATERIAL,
            resolveAppSliderRenderer(AppUiStyle.MATERIAL3),
        )
    }

    @Test
    fun outlinedTextFieldUsesMiuixUnlessPrefixOrSuffixIsPresent() {
        assertTrue(
            shouldUseMiuixOutlinedTextField(
                uiStyle = AppUiStyle.MIUIX,
                hasPrefix = false,
                hasSuffix = false,
            )
        )
        assertFalse(
            shouldUseMiuixOutlinedTextField(
                uiStyle = AppUiStyle.MIUIX,
                hasPrefix = true,
                hasSuffix = false,
            )
        )
        assertFalse(
            shouldUseMiuixOutlinedTextField(
                uiStyle = AppUiStyle.MATERIAL3,
                hasPrefix = false,
                hasSuffix = false,
            )
        )
    }
}
