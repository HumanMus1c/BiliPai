package com.android.purebilibili.feature.home.components

import com.android.purebilibili.core.store.LiquidGlassAdvancedSettings
import com.android.purebilibili.core.store.LiquidGlassMode
import com.android.purebilibili.core.store.LiquidGlassReadabilityMode
import com.android.purebilibili.core.store.LiquidGlassStyle
import com.android.purebilibili.core.store.normalizeLiquidGlassProgress
import com.android.purebilibili.core.store.normalizeLiquidGlassStrength
import com.android.purebilibili.core.store.resolveLiquidGlassStrengthFromProgress
import com.android.purebilibili.core.store.resolveLegacyLiquidGlassProgress
import com.android.purebilibili.core.store.resolveLegacyLiquidGlassMode

data class LiquidGlassTuning(
    val readabilityMode: LiquidGlassReadabilityMode,
    val mode: LiquidGlassMode,
    val progress: Float,
    val strength: Float,
    val backdropBlurRadius: Float,
    val progressiveBlurRadius: Float,
    val progressiveBlurEndFraction: Float,
    val progressiveBlurCurve: Float,
    val surfaceAlpha: Float,
    val whiteOverlayAlpha: Float,
    val saturation: Float,
    val refractionAmount: Float,
    val refractionHeight: Float,
    val indicatorTintAlpha: Float,
    val indicatorLensBoost: Float,
    val indicatorEdgeWarpBoost: Float,
    val indicatorChromaticBoost: Float,
    val contentReadabilityBoost: Float,
    val contentReadabilityScrimAlpha: Float,
    val contentDistortionScale: Float,
    val shellChromaticAberrationAmount: Float,
    val indicatorChromaticAberrationAmount: Float,
    val scrollCoupledRefractionAmount: Float,
)

private const val UPSTREAM_BALANCED_READABILITY = 0.62f
private const val UPSTREAM_BALANCED_CHROMATIC_CONTROL = 0.56f
private const val UPSTREAM_INDICATOR_CHROMATIC_ABERRATION = 0.5f

internal fun resolveLiquidGlassTuning(
    progress: Float,
    advancedSettings: LiquidGlassAdvancedSettings = LiquidGlassAdvancedSettings(),
    readabilityMode: LiquidGlassReadabilityMode = LiquidGlassReadabilityMode.STABLE,
): LiquidGlassTuning {
    val normalizedProgress = normalizeLiquidGlassProgress(progress)
    val mode = when {
        normalizedProgress < 0.34f -> LiquidGlassMode.CLEAR
        normalizedProgress < 0.68f -> LiquidGlassMode.BALANCED
        else -> LiquidGlassMode.FROSTED
    }
    val frostWeight = normalizedProgress
    val configuredReadability = advancedSettings.contentReadability.coerceIn(0f, 1f)
    val readabilityProtection = (
        (configuredReadability - UPSTREAM_BALANCED_READABILITY) /
            (1f - UPSTREAM_BALANCED_READABILITY)
    ).coerceIn(0f, 1f)
    // Readability protection must remain active across the full slider. It is strongest for
    // transparent glass, but a user-selected 100% must not become a no-op around 50%.
    val readabilityWeight = midpointLerp(1f, 0.6f, 0.25f, normalizedProgress)
    val contentReadabilityBoost = readabilityWeight * readabilityProtection
    val contentReadabilityScrimAlpha = contentReadabilityBoost *
        (0.12f + readabilityProtection * 0.22f)
    val chromaticControl = advancedSettings.chromaticAberration.coerceIn(0f, 1f)
    // Miuix keeps the 24dp shell achromatic and applies 0.5 dispersion to the moving
    // 10dp/14dp indicator. The balanced anchor reproduces that split exactly.
    val shellChromaticAmount = if (chromaticControl > UPSTREAM_BALANCED_CHROMATIC_CONTROL) {
        (chromaticControl - UPSTREAM_BALANCED_CHROMATIC_CONTROL) /
            (1f - UPSTREAM_BALANCED_CHROMATIC_CONTROL) *
            UPSTREAM_INDICATOR_CHROMATIC_ABERRATION
    } else {
        0f
    }
    val indicatorChromaticAmount = (
        chromaticControl / UPSTREAM_BALANCED_CHROMATIC_CONTROL
    ).coerceIn(0f, 1f) * UPSTREAM_INDICATOR_CHROMATIC_ABERRATION
    val contentDistortionScale = (
        advancedSettings.contentDistortion.coerceIn(0f, 1f) / 0.45f
    ).coerceIn(0f, 1.8f)
    val scrollCouplingAmount = midpointLerp(1f, 0f, 0f, normalizedProgress)
    return LiquidGlassTuning(
        readabilityMode = readabilityMode,
        mode = mode,
        progress = normalizedProgress,
        strength = resolveLiquidGlassStrengthFromProgress(normalizedProgress),
        // The clear endpoint intentionally preserves the dynamic dock's formerly accidental
        // crystal-glass recipe: no backdrop blur, but enough tint, saturation and refraction
        // to keep the capsule legible over moving content. The midpoint remains the original
        // BiliPai material and the frosted endpoint retains its stronger diffusion.
        backdropBlurRadius = midpointLerp(0f, 4f, 24f, normalizedProgress),
        progressiveBlurRadius = advancedSettings.progressiveBlurRadius.coerceIn(0f, 1f) * 40f,
        progressiveBlurEndFraction = 0.25f +
            advancedSettings.progressiveBlurExtent.coerceIn(0f, 1f) * 0.75f,
        progressiveBlurCurve = 0.35f +
            advancedSettings.progressiveBlurCurve.coerceIn(0f, 1f) * 1.65f,
        surfaceAlpha = midpointLerp(0.40f, 0.40f, 0.54f, normalizedProgress),
        whiteOverlayAlpha = midpointLerp(0.04f, 0.04f, 0.14f, normalizedProgress),
        saturation = midpointLerp(1.5f, 1.5f, 1.24f, normalizedProgress),
        refractionAmount = midpointLerp(24f, 24f, 8f, normalizedProgress),
        refractionHeight = midpointLerp(24f, 24f, 8f, normalizedProgress),
        indicatorTintAlpha = midpointLerp(0.20f, 0.28f, 0.38f, normalizedProgress),
        indicatorLensBoost = midpointLerp(1.35f, 1f, 0.78f, frostWeight),
        indicatorEdgeWarpBoost = midpointLerp(1.40f, 1f, 0.82f, frostWeight),
        indicatorChromaticBoost = midpointLerp(1.20f, 1f, 0.70f, frostWeight),
        contentReadabilityBoost = contentReadabilityBoost,
        contentReadabilityScrimAlpha = contentReadabilityScrimAlpha,
        contentDistortionScale = contentDistortionScale,
        shellChromaticAberrationAmount = shellChromaticAmount,
        indicatorChromaticAberrationAmount = indicatorChromaticAmount,
        scrollCoupledRefractionAmount = scrollCouplingAmount,
    )
}

internal fun resolveLiquidGlassIndicatorChromaticAberration(
    tuning: LiquidGlassTuning,
): Float = if (tuning.indicatorChromaticAberrationAmount > 0.01f) {
    (tuning.indicatorChromaticAberrationAmount * tuning.indicatorChromaticBoost)
        .coerceIn(0f, UPSTREAM_INDICATOR_CHROMATIC_ABERRATION)
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
