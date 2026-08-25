// Shared BiliPai liquid-glass dock shell + moving indicator (theme-independent chrome).
// Used by FloatingBottomBar (bottom) and home top tabs so both share one material stack —
// no legacy backdrop / BottomBarGlassMaterialPreset / LiquidIndicator mix-ins.
package com.android.purebilibili.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.android.purebilibili.feature.home.components.liquid.InnerShadow
import com.android.purebilibili.feature.home.components.liquid.innerShadow
import com.android.purebilibili.feature.home.components.liquid.lens
import com.android.purebilibili.feature.home.components.liquid.vibrancy
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.highlight.LightSource
import top.yukonga.miuix.kmp.blur.sensor.rememberDeviceTilt

private val iosIndicatorSpecular: Highlight = Highlight(
    width = 1.dp,
    alpha = 1f,
    style = BloomStroke(
        color = Color.White.copy(alpha = 0.12f),
        innerBlurRadius = 2.0.dp,
        primaryLight = LightSource(
            position = LightPosition(0.5f, -0.3f, -0.05f),
            color = Color.White,
            intensity = 1f,
        ),
        secondaryLight = LightSource(
            position = LightPosition(0.5f, 0.8f, -0.5f),
            color = Color.White,
            intensity = 0.4f,
        ),
        dualPeak = true,
    ),
)

private const val LIGHT_REF_X = 0.5f
private const val LIGHT_REF_Y = 0.7f
private const val GRAVITY_DIR_THRESHOLD_SQ = 0.01f

@Composable
internal fun rememberBiliPaiGravityHighlight(
    extraDegrees: Float = 0f,
): Highlight {
    val base = iosIndicatorSpecular
    val baseStyle = base.style as BloomStroke
    val tilt by rememberDeviceTilt()
    val rotatedPrimary = remember(tilt, baseStyle.primaryLight, extraDegrees) {
        val basePrimary = baseStyle.primaryLight
        val gx = tilt.gravityX
        val gy = tilt.gravityY
        val gMagSq = gx * gx + gy * gy
        val (lx0, ly0) = if (gMagSq > GRAVITY_DIR_THRESHOLD_SQ) {
            val invMag = 1f / sqrt(gMagSq)
            (gx * invMag) to (gy * invMag)
        } else {
            0f to -1f
        }
        val rad = extraDegrees * PI / 180.0
        val c = cos(rad).toFloat()
        val s = sin(rad).toFloat()
        val lx = c * lx0 - s * ly0
        val ly = s * lx0 + c * ly0
        basePrimary.copy(
            position = LightPosition(
                x = LIGHT_REF_X + lx,
                y = LIGHT_REF_Y + ly,
                z = basePrimary.position.z,
            ),
        )
    }
    return remember(base, rotatedPrimary) {
        base.copy(style = baseStyle.copy(primaryLight = rotatedPrimary))
    }
}

/**
 * Outer floating dock shell — BiliPai FloatingBottomBar base layer:
 * dropShadow + tuning-driven vibrancy / blur / lens + gravity highlight.
 */
@Composable
internal fun Modifier.biliPaiFloatingDockShell(
    backdrop: Backdrop?,
    containerColor: Color,
    pressProgress: Float,
    panelOffsetPx: Float = 0f,
    shape: Shape = CircleShape,
    enabled: Boolean = true,
    drawLens: Boolean = true,
    lensIntensity: Float = 1f,
    liquidGlassTuning: LiquidGlassTuning = resolveLiquidGlassTuning(progress = 0.5f),
): Modifier {
    if (!enabled || backdrop == null) {
        return this
            .graphicsLayer { translationX = panelOffsetPx }
            .background(containerColor, shape)
    }
    val isDark = isSystemInDarkTheme()
    val baseHighlight = rememberBiliPaiGravityHighlight(extraDegrees = -45f)
    val surfaceColor = containerColor.copy(alpha = liquidGlassTuning.surfaceAlpha)
    val readabilityScrimColor = if (isDark) Color.Black else Color.White
    val resolvedLensIntensity = lensIntensity.coerceIn(0f, 1f) *
        liquidGlassTuning.contentDistortionScale.coerceIn(0f, 1.8f)
    val shouldDrawLens = drawLens && resolvedLensIntensity > 0.001f
    val refractionHeightDp = liquidGlassTuning.refractionHeight * resolvedLensIntensity
    val refractionAmountDp = liquidGlassTuning.refractionAmount * resolvedLensIntensity
    val effectPaddingDp = resolveFloatingDockEffectPaddingDp(
        refractionAmountDp = refractionAmountDp,
        pressBloomDp = MIUIX_UPSTREAM_DOCK_PRESS_BLOOM_DP,
    )
    return this
        .graphicsLayer { translationX = panelOffsetPx }
        .dropShadow(
            shape = shape,
            shadow = Shadow(
                radius = 10.dp,
                color = Color.Black,
                alpha = if (isDark) 0.2f else 0.1f,
            ),
        )
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                padding = maxOf(padding, effectPaddingDp.dp.toPx())
                vibrancy(liquidGlassTuning.saturation)
                blur(
                    liquidGlassTuning.backdropBlurRadius.dp.toPx(),
                    liquidGlassTuning.backdropBlurRadius.dp.toPx()
                )
                if (shouldDrawLens) {
                    lens(
                        refractionHeight = refractionHeightDp.dp.toPx(),
                        refractionAmount = refractionAmountDp.dp.toPx(),
                        chromaticAberration = liquidGlassTuning.shellChromaticAberrationAmount,
                    )
                }
            },
            // Inline capsules (search/input) disable the shell lens and its rim highlight
            // together; keeping the highlight alone leaves a one-pixel "shrimp line".
            highlight = {
                baseHighlight.copy(
                    alpha = if (shouldDrawLens) {
                        (0.75f * resolvedLensIntensity).coerceIn(0f, 1f)
                    } else {
                        0f
                    }
                )
            },
            layerBlock = {
                val width = size.width.coerceAtLeast(1f)
                val s = lerp(1f, 1f + 16.dp.toPx() / width, pressProgress)
                scaleX = s
                scaleY = s
            },
            onDrawSurface = {
                drawRect(surfaceColor)
                if (liquidGlassTuning.contentReadabilityScrimAlpha > 0f) {
                    drawRect(
                        readabilityScrimColor.copy(
                            alpha = liquidGlassTuning.contentReadabilityScrimAlpha
                        )
                    )
                }
            },
        )
}

/**
 * Hidden capture layer material — same shell glass as BiliPai foreground Row
 * Caller supplies layerBackdrop + alpha(0); material values match the visible shell.
 */
@Composable
internal fun Modifier.biliPaiFloatingDockCaptureSurface(
    backdrop: Backdrop,
    containerColor: Color,
    panelOffsetPx: Float = 0f,
    shape: Shape = CircleShape,
    liquidGlassTuning: LiquidGlassTuning = resolveLiquidGlassTuning(progress = 0.5f),
): Modifier {
    val isDark = isSystemInDarkTheme()
    val surfaceColor = containerColor.copy(alpha = liquidGlassTuning.surfaceAlpha)
    val readabilityScrimColor = if (isDark) Color.Black else Color.White
    val distortionScale = liquidGlassTuning.contentDistortionScale.coerceIn(0f, 1.8f)
    return this
        .graphicsLayer { translationX = panelOffsetPx }
        .drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy(liquidGlassTuning.saturation)
                blur(
                    liquidGlassTuning.backdropBlurRadius.dp.toPx(),
                    liquidGlassTuning.backdropBlurRadius.dp.toPx()
                )
                if (distortionScale > 0.001f) {
                    lens(
                        refractionHeight = liquidGlassTuning.refractionHeight.dp.toPx() *
                            distortionScale,
                        refractionAmount = liquidGlassTuning.refractionAmount.dp.toPx() *
                            distortionScale,
                        chromaticAberration = liquidGlassTuning.shellChromaticAberrationAmount,
                    )
                }
            },
            onDrawSurface = {
                drawRect(surfaceColor)
                if (liquidGlassTuning.contentReadabilityScrimAlpha > 0f) {
                    drawRect(
                        readabilityScrimColor.copy(
                            alpha = liquidGlassTuning.contentReadabilityScrimAlpha
                        )
                    )
                }
            },
        )
}

/**
 * Moving liquid indicator — BiliPai FloatingBottomBar indicator box:
 * combined backdrop + tuning-driven lens + velocity stretch + innerShadow.
 */
@Composable
internal fun BoxScope.BiliPaiFloatingDockIndicator(
    visible: Boolean,
    translationXPx: Float,
    panelOffsetPx: Float,
    width: Dp,
    height: Dp,
    combinedBackdrop: Backdrop?,
    pressProgress: Float,
    scaleX: Float,
    scaleY: Float,
    velocity: Float,
    isDark: Boolean,
    shape: Shape = CircleShape,
    alignment: Alignment = Alignment.CenterStart,
    liquidGlassTuning: LiquidGlassTuning = resolveLiquidGlassTuning(progress = 0.5f),
) {
    if (!visible) return
    val pillHighlight = rememberBiliPaiGravityHighlight(extraDegrees = 90f)
    Box(
        modifier = Modifier
            .align(alignment)
            .graphicsLayer {
                translationX = translationXPx + panelOffsetPx
            }
            .width(width)
            .height(height)
            .zIndex(2f)
            .then(
                if (combinedBackdrop != null) {
                    Modifier
                        .drawBackdrop(
                            backdrop = combinedBackdrop,
                            shape = { shape },
                            effects = {
                                val progress = pressProgress
                                lens(
                                    refractionHeight = 10.dp.toPx() * progress *
                                        liquidGlassTuning.indicatorLensBoost *
                                        liquidGlassTuning.contentDistortionScale,
                                    refractionAmount = 14.dp.toPx() * progress *
                                        liquidGlassTuning.indicatorEdgeWarpBoost *
                                        liquidGlassTuning.contentDistortionScale,
                                    depthEffect = true,
                                    chromaticAberration =
                                        resolveLiquidGlassIndicatorChromaticAberration(
                                            liquidGlassTuning
                                        ),
                                )
                            },
                            highlight = { pillHighlight.copy(alpha = pressProgress) },
                            layerBlock = {
                                this.scaleX = scaleX
                                this.scaleY = scaleY
                                val v = velocity / 10f
                                this.scaleX /= 1f - (v * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                                this.scaleY *= 1f - (v * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                            },
                            onDrawSurface = {
                                val progress = pressProgress
                                drawRect(
                                    color = if (!isDark) {
                                        Color.Black.copy(alpha = 0.1f)
                                    } else {
                                        Color.White.copy(alpha = 0.1f)
                                    },
                                    alpha = 1f - progress,
                                )
                                drawRect(Color.Black.copy(alpha = 0.03f * progress))
                            },
                        )
                        .innerShadow(shape = shape) {
                            InnerShadow(
                                radius = 8.dp * pressProgress,
                                color = Color.Black.copy(alpha = 0.15f),
                                alpha = pressProgress,
                            )
                        }
                } else {
                    Modifier.background(
                        if (!isDark) Color.Black.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.1f),
                        shape,
                    )
                }
            )
    )
}
