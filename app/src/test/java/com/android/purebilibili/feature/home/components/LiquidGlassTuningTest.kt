package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.store.LiquidGlassMode
import com.android.purebilibili.core.store.LiquidGlassAdvancedPreset
import com.android.purebilibili.core.store.LiquidGlassAdvancedSettings
import com.android.purebilibili.core.store.LiquidGlassStyle
import com.android.purebilibili.core.store.LiquidGlassReadabilityMode
import com.android.purebilibili.core.store.resolveLiquidGlassAdvancedPreset
import com.android.purebilibili.core.store.resolveLegacyLiquidGlassProgress
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LiquidGlassTuningTest {

    @Test
    fun `stable readability is default and adaptive mode is explicit`() {
        assertEquals(
            LiquidGlassReadabilityMode.STABLE,
            resolveLiquidGlassTuning(progress = 0.5f).readabilityMode,
        )
        assertEquals(
            LiquidGlassReadabilityMode.ADAPTIVE,
            resolveLiquidGlassTuning(
                progress = 0.5f,
                readabilityMode = LiquidGlassReadabilityMode.ADAPTIVE,
            ).readabilityMode,
        )
    }

    @Test
    fun `clear progress stays more transparent than frosted progress`() {
        val clear = resolveLiquidGlassTuning(progress = 0f)
        val frosted = resolveLiquidGlassTuning(progress = 1f)

        assertTrue(clear.backdropBlurRadius < frosted.backdropBlurRadius)
        assertTrue(clear.surfaceAlpha < frosted.surfaceAlpha)
        assertTrue(clear.refractionAmount > frosted.refractionAmount)
        assertTrue(clear.saturation > frosted.saturation)
    }

    @Test
    fun `progress is clamped into safe range`() {
        val low = resolveLiquidGlassTuning(progress = -1f)
        val high = resolveLiquidGlassTuning(progress = 3f)

        assertEquals(0f, low.progress, 0.0001f)
        assertEquals(1f, high.progress, 0.0001f)
        assertTrue(high.backdropBlurRadius >= low.backdropBlurRadius)
    }

    @Test
    fun `progress continuously shifts optical emphasis from lens to frost`() {
        val clear = resolveLiquidGlassTuning(progress = 0f)
        val middle = resolveLiquidGlassTuning(progress = 0.5f)
        val frosted = resolveLiquidGlassTuning(progress = 1f)

        assertTrue(clear.indicatorLensBoost > middle.indicatorLensBoost)
        assertTrue(middle.indicatorLensBoost > frosted.indicatorLensBoost)
        assertTrue(
            clear.shellChromaticAberrationAmount >= middle.shellChromaticAberrationAmount
        )
        assertTrue(
            middle.shellChromaticAberrationAmount >= frosted.shellChromaticAberrationAmount
        )
        assertTrue(clear.scrollCoupledRefractionAmount >= middle.scrollCoupledRefractionAmount)
        assertTrue(middle.scrollCoupledRefractionAmount >= frosted.scrollCoupledRefractionAmount)
    }

    @Test
    fun `clear progress preserves the crystal dynamic dock material`() {
        val clear = resolveLiquidGlassTuning(progress = 0f)

        assertEquals(0f, clear.backdropBlurRadius, 0.0001f)
        assertEquals(0.40f, clear.surfaceAlpha, 0.0001f)
        assertEquals(0.04f, clear.whiteOverlayAlpha, 0.0001f)
        assertEquals(1.5f, clear.saturation, 0.0001f)
        assertEquals(24f, clear.refractionAmount, 0.0001f)
    }

    @Test
    fun `shell refraction height stays in a capsule-safe range`() {
        val clear = resolveLiquidGlassTuning(progress = 0f)
        val frosted = resolveLiquidGlassTuning(progress = 1f)

        assertTrue(clear.refractionHeight <= 24f)
        assertTrue(frosted.refractionHeight <= clear.refractionHeight)
    }

    @Test
    fun `readability protection stays active across the full material slider`() {
        val readablePreset = resolveLiquidGlassAdvancedPreset(LiquidGlassAdvancedPreset.READABLE)
        val clear = resolveLiquidGlassTuning(
            progress = 0f,
            advancedSettings = readablePreset,
        )
        val balanced = resolveLiquidGlassTuning(
            progress = 0.5f,
            advancedSettings = readablePreset,
        )
        val frosted = resolveLiquidGlassTuning(
            progress = 1f,
            advancedSettings = readablePreset,
        )

        assertEquals(1f, clear.contentReadabilityBoost, 0.0001f)
        assertTrue(clear.contentReadabilityBoost > balanced.contentReadabilityBoost)
        assertTrue(balanced.contentReadabilityBoost > frosted.contentReadabilityBoost)
        assertTrue(frosted.contentReadabilityBoost > 0f)
        assertTrue(clear.contentReadabilityScrimAlpha > balanced.contentReadabilityScrimAlpha)
        assertTrue(balanced.contentReadabilityScrimAlpha > frosted.contentReadabilityScrimAlpha)
        assertTrue(frosted.contentReadabilityScrimAlpha > 0f)
    }

    @Test
    fun `prism preset adds more chromatic separation and distortion than readable preset`() {
        val readable = resolveLiquidGlassTuning(
            progress = 0f,
            advancedSettings = resolveLiquidGlassAdvancedPreset(
                LiquidGlassAdvancedPreset.READABLE
            ),
        )
        val prism = resolveLiquidGlassTuning(
            progress = 0f,
            advancedSettings = resolveLiquidGlassAdvancedPreset(
                LiquidGlassAdvancedPreset.PRISM
            ),
        )

        assertTrue(
            prism.shellChromaticAberrationAmount > readable.shellChromaticAberrationAmount
        )
        assertTrue(prism.contentDistortionScale > readable.contentDistortionScale)
        assertEquals(0f, readable.contentDistortionScale, 0.0001f)
        assertTrue(prism.contentReadabilityBoost > 0f)
    }

    @Test
    fun `zero content distortion disables content warping completely`() {
        val tuning = resolveLiquidGlassTuning(
            progress = 0f,
            advancedSettings = LiquidGlassAdvancedSettings(contentDistortion = 0f),
        )

        assertEquals(0f, tuning.contentDistortionScale, 0.0001f)
    }

    @Test
    fun `legacy mode and strength map into ordered continuous progress`() {
        val clear = resolveLegacyLiquidGlassProgress(
            mode = LiquidGlassMode.CLEAR,
            strength = 0.42f
        )
        val balanced = resolveLegacyLiquidGlassProgress(
            mode = LiquidGlassMode.BALANCED,
            strength = 0.52f
        )
        val frosted = resolveLegacyLiquidGlassProgress(
            mode = LiquidGlassMode.FROSTED,
            strength = 0.62f
        )

        assertTrue(clear < balanced)
        assertTrue(balanced < frosted)
    }

    @Test
    fun `sukisu style keeps the original value one slot`() {
        assertEquals(LiquidGlassStyle.SUKISU, LiquidGlassStyle.fromValue(1))
    }

    @Test
    fun `sukisu style uses floating bottom bar glass recipe`() {
        val tuning = resolveLiquidGlassTuning(LiquidGlassStyle.SUKISU)

        assertEquals(LiquidGlassMode.BALANCED, tuning.mode)
        assertEquals(0.5f, tuning.progress, 0.0001f)
        assertEquals(4f, tuning.backdropBlurRadius, 0.0001f)
        assertEquals(0.40f, tuning.surfaceAlpha, 0.0001f)
        assertEquals(1.5f, tuning.saturation, 0.0001f)
        assertEquals(24f, tuning.refractionAmount, 0.0001f)
        assertEquals(24f, tuning.refractionHeight, 0.0001f)
        assertEquals(0.28f, tuning.indicatorTintAlpha, 0.0001f)
        assertEquals(0f, tuning.contentReadabilityScrimAlpha, 0.0001f)
        assertEquals(0f, tuning.shellChromaticAberrationAmount, 0.0001f)
        assertEquals(0.5f, tuning.indicatorChromaticAberrationAmount, 0.0001f)
    }

    @Test
    fun `shared indicator chromatic policy is identical at equal tuning`() {
        val tuning = resolveLiquidGlassTuning(
            progress = 0.5f,
            advancedSettings = resolveLiquidGlassAdvancedPreset(
                LiquidGlassAdvancedPreset.BALANCED
            ),
        )

        assertEquals(
            0.5f,
            resolveLiquidGlassIndicatorChromaticAberration(tuning),
            0.0001f,
        )
    }
}
