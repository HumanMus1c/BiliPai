package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

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

    @Test
    fun `shared entry points route through adaptive loading`() {
        val adaptive = loadSource(
            "src/main/java/com/android/purebilibili/core/ui/AdaptiveLoadingIndicator.kt",
        )
        val lottie = loadSource(
            "src/main/java/com/android/purebilibili/core/ui/LottieComponents.kt",
        )

        assertTrue(adaptive.contains("LoadingIndicator("))
        assertTrue(adaptive.contains("MiuixInfiniteProgressIndicator("))
        assertTrue(adaptive.contains("MiuixCircularProgressIndicator("))
        assertTrue(adaptive.contains("IosCutePersonLoadingIndicator("))
        assertTrue(lottie.contains("AdaptiveLoadingIndicator("))
        assertTrue(lottie.contains("fun CutePersonLoadingIndicator("))
        assertTrue(lottie.contains("internal fun IosCutePersonLoadingIndicator("))
    }

    private fun loadSource(path: String): String = File(path).readText()
}
