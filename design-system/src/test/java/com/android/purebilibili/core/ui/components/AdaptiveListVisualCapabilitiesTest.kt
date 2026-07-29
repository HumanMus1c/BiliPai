package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AdaptiveListVisualCapabilitiesTest {

    @Test
    fun `ios capabilities preserve component shape and compact metrics`() {
        val capabilities = resolveAdaptiveListVisualCapabilities(UiPreset.IOS)

        assertTrue(capabilities.useComponentDefaultGroupShape)
        assertTrue(capabilities.showExplicitActionChevron)
        assertEquals(36, capabilities.componentSpec.iconContainerSizeDp)
        assertEquals(16, capabilities.rowSpec.insideHorizontalPaddingDp)
        assertNull(capabilities.adaptGroupShape(RoundedCornerShape(12.dp)))
    }

    @Test
    fun `material capabilities preserve supplied shape and explicit chevron`() {
        val capabilities = resolveAdaptiveListVisualCapabilities(
            uiPreset = UiPreset.MD3,
            androidNativeVariant = AndroidNativeVariant.MATERIAL3,
        )
        val shape = RoundedCornerShape(12.dp)

        assertFalse(capabilities.useComponentDefaultGroupShape)
        assertTrue(capabilities.showExplicitActionChevron)
        assertEquals(40, capabilities.componentSpec.iconContainerSizeDp)
        assertEquals(18, capabilities.rowSpec.insideHorizontalPaddingDp)
        assertSame(shape, capabilities.adaptGroupShape(shape))
    }

    @Test
    fun `miuix capabilities use native action affordance and dense metrics`() {
        val capabilities = resolveAdaptiveListVisualCapabilities(
            uiPreset = UiPreset.MD3,
            androidNativeVariant = AndroidNativeVariant.MIUIX,
        )

        assertFalse(capabilities.useComponentDefaultGroupShape)
        assertFalse(capabilities.showExplicitActionChevron)
        assertEquals(38, capabilities.componentSpec.iconContainerSizeDp)
        assertEquals(16, capabilities.rowSpec.insideHorizontalPaddingDp)
    }
}
