package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.store.LiquidGlassAdvancedSettings
import com.android.purebilibili.core.store.LiquidGlassMode
import com.android.purebilibili.core.store.LiquidGlassStyle
import com.android.purebilibili.core.store.normalizeLiquidGlassProgress
import com.android.purebilibili.core.store.normalizeLiquidGlassStrength
import com.android.purebilibili.core.store.resolveLiquidGlassStrengthFromProgress
import com.android.purebilibili.core.store.resolveLegacyLiquidGlassProgress
import com.android.purebilibili.core.store.resolveLegacyLiquidGlassMode

data class LiquidGlassTuning(
    val mode: LiquidGlassMode,
    val progress: Float,
    val strength: Float,
    val backdropBlurRadius: Float,
    val surfaceAlpha: Float,
    val whiteOverlayAlpha: Float,
    val saturation: Float,
    val refractIntensity: Float,
    val refractionAmount: Float,
    val refractionHeight: Float,
    val indicatorTintAlpha: Float,
    val indicatorLensBoost: Float,
    val indicatorEdgeWarpBoost: Float,
    val indicatorChromaticBoost: Float,
    val contentReadabilityBoost: Float,
    val contentReadabilityScrimAlpha: Float,
    val contentDistortionScale: Float,
    val chromaticAberrationEnabled: Boolean,
    val chromaticAberrationAmount: Float,
    val scrollCoupledRefraction: Boolean,
    val scrollCoupledRefractionAmount: Float,
    val useNeutralIndicatorTint: Boolean,
    val neutralIndicatorTintAmount: Float,
    val depthEffectEnabled: Boolean,
    val depthEffectAmount: Float
)

internal fun resolveLiquidGlassTuning(
    progress: Float,
    advancedSettings: LiquidGlassAdvancedSettings = LiquidGlassAdvancedSettings(),
): LiquidGlassTuning {
    val normalizedProgress = normalizeLiquidGlassProgress(progress)
    val mode = when {
        normalizedProgress < 0.34f -> LiquidGlassMode.CLEAR
        normalizedProgress < 0.68f -> LiquidGlassMode.BALANCED
        else -> LiquidGlassMode.FROSTED
    }
    val frostWeight = normalizedProgress
    val clearReadabilityUrgency = (
        (0.36f - normalizedProgress).coerceAtLeast(0f) / 0.36f
    ).coerceIn(0f, 1f)
    val configuredReadability = advancedSettings.contentReadability.coerceIn(0f, 1f)
    val contentReadabilityBoost = clearReadabilityUrgency * configuredReadability
    val contentReadabilityScrimAlpha = contentReadabilityBoost *
        (0.12f + configuredReadability * 0.22f)
    // Miuix lens accepts a useful 0..0.5 range. Keep the user setting independent from
    // transparency so a reused indicator has the same visible dispersion as the home dock.
    val chromaticAmount = advancedSettings.chromaticAberration.coerceIn(0f, 1f) * 0.5f
    val contentDistortionScale = (
        advancedSettings.contentDistortion.coerceIn(0f, 1f) / 0.45f
    ).coerceIn(0f, 1.8f)
    val scrollCouplingAmount = midpointLerp(1f, 0f, 0f, normalizedProgress)
    val neutralTintAmount = midpointLerp(1f, 0f, 0f, normalizedProgress)
    val depthEffectAmount = midpointLerp(1f, 1f, 0f, normalizedProgress)
    return LiquidGlassTuning(
        mode = mode,
        progress = normalizedProgress,
        strength = resolveLiquidGlassStrengthFromProgress(normalizedProgress),
        // Keep 0.5 visually aligned with the previous fixed BiliPai material while allowing
        // both endpoints to move far enough that the difference remains obvious on busy feeds.
        backdropBlurRadius = midpointLerp(3f, 4f, 24f, normalizedProgress),
        surfaceAlpha = midpointLerp(0.12f, 0.40f, 0.54f, normalizedProgress),
        whiteOverlayAlpha = midpointLerp(0.012f, 0.04f, 0.14f, normalizedProgress),
        saturation = midpointLerp(1.65f, 1.5f, 1.24f, normalizedProgress),
        refractIntensity = midpointLerp(0.5f, 0.28f, 0.14f, normalizedProgress),
        refractionAmount = midpointLerp(26f, 24f, 8f, normalizedProgress),
        refractionHeight = midpointLerp(24f, 24f, 8f, normalizedProgress),
        indicatorTintAlpha = midpointLerp(0.20f, 0.28f, 0.38f, normalizedProgress),
        indicatorLensBoost = midpointLerp(1.35f, 1f, 0.78f, frostWeight),
        indicatorEdgeWarpBoost = midpointLerp(1.40f, 1f, 0.82f, frostWeight),
        indicatorChromaticBoost = midpointLerp(1.20f, 1f, 0.70f, frostWeight),
        contentReadabilityBoost = contentReadabilityBoost,
        contentReadabilityScrimAlpha = contentReadabilityScrimAlpha,
        contentDistortionScale = contentDistortionScale,
        chromaticAberrationEnabled = chromaticAmount > 0.01f,
        chromaticAberrationAmount = chromaticAmount,
        scrollCoupledRefraction = scrollCouplingAmount > 0.01f,
        scrollCoupledRefractionAmount = scrollCouplingAmount,
        useNeutralIndicatorTint = neutralTintAmount > 0.5f,
        neutralIndicatorTintAmount = neutralTintAmount,
        depthEffectEnabled = depthEffectAmount > 0.08f,
        depthEffectAmount = depthEffectAmount
    )
}

internal fun resolveLiquidGlassIndicatorChromaticAberration(
    tuning: LiquidGlassTuning,
): Float = if (tuning.chromaticAberrationEnabled) {
    (tuning.chromaticAberrationAmount * tuning.indicatorChromaticBoost).coerceIn(0f, 0.5f)
} else {
    0f
}

internal fun resolveLiquidGlassTuning(
    mode: LiquidGlassMode,
    strength: Float
): LiquidGlassTuning {
    return resolveLiquidGlassTuning(
        progress = resolveLegacyLiquidGlassProgress(
            mode = mode,
            strength = normalizeLiquidGlassStrength(strength)
        )
    )
}

internal fun resolveLiquidGlassTuning(style: LiquidGlassStyle): LiquidGlassTuning {
    return when (style) {
        LiquidGlassStyle.SUKISU -> sukisuLiquidGlassTuning()
        else -> resolveLiquidGlassTuning(progress = resolveLegacyLiquidGlassProgress(style))
    }
}

private fun sukisuLiquidGlassTuning(): LiquidGlassTuning = resolveLiquidGlassTuning(progress = 0.5f)

private fun midpointLerp(
    start: Float,
    midpoint: Float,
    stop: Float,
    fraction: Float
): Float = if (fraction <= 0.5f) {
    lerp(start, midpoint, fraction * 2f)
} else {
    lerp(midpoint, stop, (fraction - 0.5f) * 2f)
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction
}
