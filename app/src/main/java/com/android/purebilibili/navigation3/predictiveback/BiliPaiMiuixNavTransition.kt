// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 BiliPai contributors
package com.android.purebilibili.navigation3.predictiveback

import android.graphics.RenderEffect as AndroidRenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RenderEffect as ComposeRenderEffect
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastRoundToInt
import com.android.purebilibili.core.ui.adaptive.MotionTier
import com.android.purebilibili.core.ui.transition.resolvePredictiveBackBlurFrame
import top.yukonga.miuix.kmp.nav.transition.NavMotion
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope

internal fun biliPaiMiuixNavTransition(
    animation: BiliPaiPredictiveBackAnimationStyle,
    exitDirection: BiliPaiPredictiveBackExitDirection,
    isLightBackground: Boolean,
    miuixTransitionBlurEnabled: Boolean = true,
    miuixPredictiveBackMaxProgressPercent: Int =
        MIUIX_PREDICTIVE_BACK_DEFAULT_MAX_PROGRESS_PERCENT,
    miuixPredictiveBackProgressEnabled: Boolean = true,
): NavTransition {
    val progressControlEnabled = shouldUseMiuixPredictiveBackProgress(
        animation = animation,
        enabled = miuixPredictiveBackProgressEnabled,
    )
    val maxPreviewFraction =
        miuixPredictiveBackMaxProgressPercent.coerceIn(0, 100) / 100f
    val baseTransition = when (animation) {
        BiliPaiPredictiveBackAnimationStyle.NONE -> return NoPredictiveBackTransition
        BiliPaiPredictiveBackAnimationStyle.MIUIX -> if (progressControlEnabled) {
            miuixPredictiveBackProgressTransition(maxPreviewFraction)
        } else {
            miuixDepthNavTransition()
        }
        BiliPaiPredictiveBackAnimationStyle.AOSP -> AospNavTransition
        BiliPaiPredictiveBackAnimationStyle.SCALE -> scaleNavTransition(exitDirection)
        BiliPaiPredictiveBackAnimationStyle.CLASSIC -> ClassicNavTransition
    }
    return realtimeCoveredBlurTransition(
        baseTransition = baseTransition,
        isLightBackground = isLightBackground,
        blurEnabled = miuixTransitionBlurEnabled,
        progressControlEnabled = progressControlEnabled,
        maxPreviewFraction = maxPreviewFraction,
    )
}

internal fun shouldUseMiuixPredictiveBackProgress(
    animation: BiliPaiPredictiveBackAnimationStyle,
    enabled: Boolean,
): Boolean = enabled && animation == BiliPaiPredictiveBackAnimationStyle.MIUIX

/**
 * Adds MIUI-style depth blur to the retained page below every animated top entry.
 *
 * [NavTransitionScope.relativeDepth] is the shared Miuix driver for edge swipe, system predictive
 * back, and release settle. Reading it inside [graphicsLayer] keeps the effect draw-only while the
 * covered page moves from fully blurred at depth 1 to clear at depth 0.
 */
private fun miuixDepthNavTransition(): NavTransition {
    return object : NavTransition {
        override fun Modifier.transformEntry(scope: NavTransitionScope): Modifier = graphicsLayer {
            val depth = scope.relativeDepth
            val widthPx = scope.layoutSize.width.toFloat()
            val isRtl = scope.layoutDirection == LayoutDirection.Rtl
            if (depth <= 0f) {
                val direction = if (isRtl) -1f else 1f
                translationX = (direction * (-depth).coerceIn(0f, 1f) * widthPx)
                    .fastRoundToInt()
                    .toFloat()
            } else {
                val coveredDepth = depth.coerceIn(0f, 1f)
                translationX = (if (isRtl) 1f else -1f) * coveredDepth * widthPx * 0.25f
                alpha = 1f - 0.1f * coveredDepth
            }
        }
    }
}

private fun realtimeCoveredBlurTransition(
    baseTransition: NavTransition,
    isLightBackground: Boolean,
    blurEnabled: Boolean,
    progressControlEnabled: Boolean,
    maxPreviewFraction: Float,
): NavTransition {
    return object : NavTransition {
        override val motion: NavMotion
            get() = if (progressControlEnabled) {
                baseTransition.motion
            } else {
                NavMotion.Default
            }

        override fun scrimFraction(scope: NavTransitionScope): Float = if (
            progressControlEnabled
        ) {
            baseTransition.scrimFraction(scope)
        } else {
            scope.relativeDepth.coerceIn(0f, 1f)
        }

        override fun Modifier.transformEntry(scope: NavTransitionScope): Modifier {
            val renderEffectCache = MiuixCoveredBlurRenderEffectCache()
            val transformed = with(baseTransition) {
                this@transformEntry.transformEntry(scope)
            }
            return transformed.graphicsLayer {
                renderEffect = if (blurEnabled) {
                    val blurFrame = resolvePredictiveBackBlurFrame(
                        progress = if (scope.gesture != null || scope.settle != null) {
                            val coveredDepth = if (
                                progressControlEnabled && scope.relativeDepth > 0f
                            ) {
                                resolveMiuixPredictiveBackCoveredDepth(
                                    scope = scope,
                                    maxPreviewFraction = maxPreviewFraction,
                                )
                            } else {
                                scope.relativeDepth
                            }
                            resolveMiuixNavCoveredBlurProgress(coveredDepth)
                        } else {
                            0f
                        },
                        motionTier = MotionTier.Normal,
                        isLightBackground = isLightBackground,
                    )
                    renderEffectCache.resolve(blurFrame.blurRadiusPx)
                } else {
                    null
                }
            }
        }
    }
}

/** Depth 0 is fully revealed; depth 1 is fully covered by the current page. */
internal fun resolveMiuixNavCoveredBlurProgress(relativeDepth: Float): Float {
    val coveredDepth = relativeDepth.coerceIn(0f, 1f)
    return coveredDepth * coveredDepth
}

private class MiuixCoveredBlurRenderEffectCache {
    private var cachedRadiusPx = Float.NaN
    private var cachedEffect: ComposeRenderEffect? = null

    fun resolve(radiusPx: Float): ComposeRenderEffect? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || radiusPx <= 0.01f) {
            cachedRadiusPx = 0f
            cachedEffect = null
            return null
        }
        if (radiusPx != cachedRadiusPx) {
            cachedRadiusPx = radiusPx
            cachedEffect = AndroidRenderEffect.createBlurEffect(
                radiusPx,
                radiusPx,
                Shader.TileMode.CLAMP,
            ).asComposeRenderEffect()
        }
        return cachedEffect
    }
}
