package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals

class AppTopChromePolicyTest {
    @Test
    fun legacyStylesMapToSemanticTopChromeTreatments() {
        val ios = resolveAppTopChromePolicy(UiPreset.IOS, AndroidNativeVariant.MATERIAL3)
        val material = resolveAppTopChromePolicy(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        val tonal = resolveAppTopChromePolicy(UiPreset.MD3, AndroidNativeVariant.MIUIX)

        assertEquals(AppTopTabPresentation.MOVING_CAPSULE, ios.tabPresentation)
        assertEquals(AppSemanticIconFamily.CUPERTINO, ios.iconFamily)
        assertEquals(AppTopTabPresentation.MATERIAL_UNDERLINE, material.tabPresentation)
        assertEquals(AppSemanticIconFamily.MATERIAL, material.iconFamily)
        assertEquals(AppTopTabPresentation.TONAL_CAPSULE, tonal.tabPresentation)
        assertEquals(AppSemanticIconFamily.MATERIAL, tonal.iconFamily)
    }
}
