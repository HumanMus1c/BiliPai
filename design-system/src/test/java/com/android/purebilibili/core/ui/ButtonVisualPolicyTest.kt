package com.android.purebilibili.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.ACCESSIBLE_TEXT_MIN_CONTRAST
import com.android.purebilibili.core.theme.calculateContrastRatio
import com.android.purebilibili.core.theme.opaqueCompositeOver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ButtonVisualPolicyTest {

    @Test
    fun `light follow status button rejects light primary text`() {
        val scheme = lightColorScheme(surface = Color.White, onSurface = Color.Black)
        val resolved = resolveButtonContentColor(Color.White, Color(0xFFF8F6FF), scheme)
        assertTrue(calculateContrastRatio(resolved, Color.White) >= ACCESSIBLE_TEXT_MIN_CONTRAST)
    }

    @Test
    fun `dark and translucent button backgrounds keep readable text`() {
        val scheme = darkColorScheme(surface = Color(0xFF121212), onSurface = Color.White)
        val container = Color.White.copy(alpha = 0.2f)
        val resolved = resolveButtonContentColor(container, Color.Black, scheme)
        assertTrue(calculateContrastRatio(resolved, opaqueCompositeOver(container, scheme.surface)) >= ACCESSIBLE_TEXT_MIN_CONTRAST)
    }

    @Test
    fun `readable custom foreground is preserved`() {
        val foreground = Color(0xFF245A20)
        assertEquals(foreground, resolveButtonContentColor(Color.White, foreground, lightColorScheme()))
    }

    @Test
    fun `filled button uses primary on dark theme and primaryContainer on light theme`() {
        val light = lightColorScheme(primary = Color(0xFF0F2A6A), primaryContainer = Color(0xFFD9E2FF))
        val dark = darkColorScheme(primary = Color(0xFFAEC6FF), primaryContainer = Color(0xFF1E3A8A))

        assertFalse(isColorSchemeDark(light))
        assertTrue(isColorSchemeDark(dark))

        assertEquals(light.primaryContainer, resolveFilledButtonContainerColor(light))
        assertEquals(light.onPrimaryContainer, resolveFilledButtonContentColor(light))
        assertEquals(dark.primary, resolveFilledButtonContainerColor(dark))
        assertEquals(dark.onPrimary, resolveFilledButtonContentColor(dark))
    }

    @Test
    fun `md3 buttons compact default padding while explicit padding is preserved`() {
        val defaultPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
        val custom = PaddingValues(horizontal = 12.dp, vertical = 4.dp)

        // MD3:默认内边距收紧为紧凑密度,显式定制保持原样。
        assertEquals(
            AppCompactButtonContentPadding,
            resolveAppButtonContentPadding(AppUiStyle.MATERIAL3, defaultPadding, defaultPadding),
        )
        assertEquals(
            custom,
            resolveAppButtonContentPadding(AppUiStyle.MATERIAL3, custom, defaultPadding),
        )
        // MIUIX:原样透传,由 AppMiuixButton 的 insideMargin 管线控制。
        assertEquals(
            defaultPadding,
            resolveAppButtonContentPadding(AppUiStyle.MIUIX, defaultPadding, defaultPadding),
        )
    }
}
