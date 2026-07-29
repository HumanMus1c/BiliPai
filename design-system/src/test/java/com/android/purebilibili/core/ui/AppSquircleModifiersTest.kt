package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSquircleModifiersTest {

    @Test
    fun squircleBackground_appliesOnlyOnMiuixVariant() {
        assertTrue(
            shouldApplyMiuixSquircleBackground(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
        assertFalse(
            shouldApplyMiuixSquircleBackground(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
        assertFalse(
            shouldApplyMiuixSquircleBackground(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
    }

    @Test
    fun iosPreset_usesContinuousRoundingPolicy() {
        assertTrue(shouldUseIosContinuousRounding(UiPreset.IOS))
        assertFalse(shouldUseIosContinuousRounding(UiPreset.MD3))
    }
}