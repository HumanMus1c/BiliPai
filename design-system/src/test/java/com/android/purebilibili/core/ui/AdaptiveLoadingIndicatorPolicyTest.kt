package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveLoadingIndicatorPolicyTest {

    @Test
    fun `ios preset keeps cute person for page and compact`() {
        assertEquals(
            AdaptiveLoadingVisual.IOS_CUTE_PERSON,
            resolveAdaptiveLoadingVisual(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3,
                density = AdaptiveLoadingDensity.PAGE,
            ),
        )
        assertEquals(
            AdaptiveLoadingVisual.IOS_CUTE_PERSON,
            resolveAdaptiveLoadingVisual(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MIUIX,
                density = AdaptiveLoadingDensity.COMPACT,
            ),
        )
    }

    @Test
    fun `material3 page uses official loading indicator`() {
        assertEquals(
            AdaptiveLoadingVisual.MATERIAL3_LOADING_INDICATOR,
            resolveAdaptiveLoadingVisual(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3,
                density = AdaptiveLoadingDensity.PAGE,
            ),
        )
    }

    @Test
    fun `material3 compact uses circular progress`() {
        assertEquals(
            AdaptiveLoadingVisual.MATERIAL3_CIRCULAR,
            resolveAdaptiveLoadingVisual(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3,
                density = AdaptiveLoadingDensity.COMPACT,
            ),
        )
    }

    @Test
    fun `miuix page uses infinite orbit indicator`() {
        assertEquals(
            AdaptiveLoadingVisual.MIUIX_INFINITE,
            resolveAdaptiveLoadingVisual(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX,
                density = AdaptiveLoadingDensity.PAGE,
            ),
        )
    }

    @Test
    fun `miuix compact uses circular progress`() {
        assertEquals(
            AdaptiveLoadingVisual.MIUIX_CIRCULAR,
            resolveAdaptiveLoadingVisual(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX,
                density = AdaptiveLoadingDensity.COMPACT,
            ),
        )
    }

    @Test
    fun `size heuristic maps compact threshold`() {
        assertEquals(AdaptiveLoadingDensity.PAGE, resolveAdaptiveLoadingDensity(null))
        assertEquals(AdaptiveLoadingDensity.PAGE, resolveAdaptiveLoadingDensity(80f))
        assertEquals(AdaptiveLoadingDensity.PAGE, resolveAdaptiveLoadingDensity(33f))
        assertEquals(AdaptiveLoadingDensity.COMPACT, resolveAdaptiveLoadingDensity(32f))
        assertEquals(AdaptiveLoadingDensity.COMPACT, resolveAdaptiveLoadingDensity(24f))
    }

}
