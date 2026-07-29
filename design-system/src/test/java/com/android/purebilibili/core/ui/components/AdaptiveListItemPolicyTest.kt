package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveListItemPolicyTest {

    @Test
    fun `miuix clickable item with chevron routes to arrow preference`() {
        assertEquals(
            AppClickableItemRenderer.MIUIX_ARROW,
            resolveAppClickableItemRenderer(
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
            AppClickableItemRenderer.MIUIX_BASIC,
            resolveAppClickableItemRenderer(
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
            AppClickableItemRenderer.MD3_BASIC,
            resolveAppClickableItemRenderer(
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
            AppClickableItemRenderer.CUPERTINO,
            resolveAppClickableItemRenderer(
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
