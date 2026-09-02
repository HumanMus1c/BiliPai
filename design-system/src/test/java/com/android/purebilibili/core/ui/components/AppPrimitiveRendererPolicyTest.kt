package com.android.purebilibili.core.ui.components

import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppPrimitiveRendererPolicyTest {

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

    @Test
    fun actionPrimitivesUseMiuixOnlyWhenLiquidGlassIsOff() {
        assertTrue(shouldUseMiuixNonGlassActionPrimitive(AppUiStyle.MIUIX, liquidGlassEnabled = false))
        assertFalse(shouldUseMiuixNonGlassActionPrimitive(AppUiStyle.MIUIX, liquidGlassEnabled = true))
        assertFalse(shouldUseMiuixNonGlassActionPrimitive(AppUiStyle.MATERIAL3, liquidGlassEnabled = false))
        assertFalse(shouldUseMiuixNonGlassActionPrimitive(AppUiStyle.MATERIAL3, liquidGlassEnabled = true))
    }

    @Test
    fun chipsUsePrimaryToneOnlyWhenSelected() {
        assertEquals(AppMiuixActionTone.PRIMARY, resolveMiuixChipActionTone(selected = true))
        assertEquals(AppMiuixActionTone.SECONDARY, resolveMiuixChipActionTone(selected = false))
    }

    @Test
    fun nonGlassChipMetricsStayCompactInsteadOfOfficialButtonSize() {
        val metrics = resolveMiuixNonGlassChipMetrics()
        assertEquals(36, metrics.minHeightDp)
        assertEquals(52, metrics.minWidthDp)
        assertEquals(10, metrics.cornerRadiusDp)
        assertEquals(12, metrics.horizontalPaddingDp)
        assertEquals(8, metrics.iconGapDp)
    }

    @Test
    fun officialButtonPaddingReplacesOnlyDefaultMaterialPadding() {
        assertTrue(shouldUseOfficialMiuixButtonPadding(usesDefaultMaterialPadding = true))
        assertFalse(shouldUseOfficialMiuixButtonPadding(usesDefaultMaterialPadding = false))
    }

    @Test
    fun fabUsesOfficialSixtyAndSmallStaysAtTouchMinimum() {
        assertEquals(60, resolveMiuixFabMinSizeDp(small = false))
        assertEquals(48, resolveMiuixFabMinSizeDp(small = true))
    }

    @Test
    fun fabDefaultMaterialContainerRemapsToMiuixPrimary() {
        val materialDefault = Color(0xFF123456)
        val custom = Color(0xFFABCDEF)
        val miuixPrimary = Color(0xFF111111)
        assertEquals(
            miuixPrimary,
            resolveMiuixFabContainerColor(
                requested = materialDefault,
                defaultMaterialContainer = materialDefault,
                miuixPrimary = miuixPrimary,
            ),
        )
        assertEquals(
            custom,
            resolveMiuixFabContainerColor(
                requested = custom,
                defaultMaterialContainer = materialDefault,
                miuixPrimary = miuixPrimary,
            ),
        )
    }
}
