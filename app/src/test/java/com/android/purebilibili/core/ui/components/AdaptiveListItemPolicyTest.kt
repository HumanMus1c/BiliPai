package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveListItemPolicyTest {

    @Test
    fun `miuix clickable item with chevron routes to arrow preference`() {
        assertEquals(
            IosClickableItemRenderer.MIUIX_ARROW,
            resolveIosClickableItemRenderer(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX,
                onClick = {},
                showChevron = true,
                centered = false
            )
        )
        assertTrue(
            shouldRouteIosClickableItemToMiuixArrowPreference(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX,
                onClick = {},
                showChevron = true,
                centered = false
            )
        )
    }

    @Test
    fun `miuix clickable item without chevron routes to basic component`() {
        assertEquals(
            IosClickableItemRenderer.MIUIX_BASIC,
            resolveIosClickableItemRenderer(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX,
                onClick = {},
                showChevron = false,
                centered = false
            )
        )
        assertFalse(
            shouldRouteIosClickableItemToMiuixArrowPreference(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX,
                onClick = {},
                showChevron = false,
                centered = false
            )
        )
    }

    @Test
    fun `material md3 clickable item routes to basic component`() {
        assertEquals(
            IosClickableItemRenderer.MD3_BASIC,
            resolveIosClickableItemRenderer(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3,
                onClick = {},
                showChevron = true,
                centered = false
            )
        )
    }

    @Test
    fun `ios preset keeps legacy row renderer`() {
        assertEquals(
            IosClickableItemRenderer.IOS_LEGACY,
            resolveIosClickableItemRenderer(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MIUIX,
                onClick = {},
                showChevron = true,
                centered = false
            )
        )
    }

    @Test
    fun `miuix switch item routes to switch preference`() {
        assertTrue(
            shouldRouteIosSwitchItemToMiuixSwitchPreference(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
        assertFalse(
            shouldRouteIosSwitchItemToMiuixSwitchPreference(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
    }

    @Test
    fun `miuix slider preference routes to official slider preference`() {
        assertTrue(
            shouldRouteIosSliderPreferenceToMiuixSliderPreference(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
        assertFalse(
            shouldRouteIosSliderPreferenceToMiuixSliderPreference(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
        assertFalse(
            shouldRouteIosSliderPreferenceToMiuixSliderPreference(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
    }
}
