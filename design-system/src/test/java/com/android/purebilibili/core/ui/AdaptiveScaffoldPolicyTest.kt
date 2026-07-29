package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdaptiveScaffoldPolicyTest {

    @Test
    fun miuixVariant_routesToMiuixScaffoldWithPopupHost() {
        assertEquals(
            AdaptiveScaffoldRenderer.MIUIX_SCAFFOLD_WITH_POPUP_HOST,
            resolveAdaptiveScaffoldRenderer(UiPreset.MD3, AndroidNativeVariant.MIUIX)
        )
        assertTrue(
            shouldMountMiuixPopupHostOnAdaptiveScaffold(UiPreset.MD3, AndroidNativeVariant.MIUIX)
        )
    }

    @Test
    fun iosPreset_routesToMaterial3Scaffold() {
        assertEquals(
            AdaptiveScaffoldRenderer.MATERIAL3_SCAFFOLD,
            resolveAdaptiveScaffoldRenderer(UiPreset.IOS, AndroidNativeVariant.MATERIAL3)
        )
        assertFalse(
            shouldMountMiuixPopupHostOnAdaptiveScaffold(UiPreset.IOS, AndroidNativeVariant.MATERIAL3)
        )
    }

    @Test
    fun md3MaterialVariant_routesToMaterial3Scaffold() {
        assertEquals(
            AdaptiveScaffoldRenderer.MATERIAL3_SCAFFOLD,
            resolveAdaptiveScaffoldRenderer(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        )
        assertFalse(
            shouldMountMiuixPopupHostOnAdaptiveScaffold(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        )
    }
}