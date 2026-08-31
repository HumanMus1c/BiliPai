package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.VideoHeroMotionSpec
import com.android.purebilibili.core.ui.transition.VideoHeroMotionTokens
import com.android.purebilibili.core.ui.transition.VideoCardTransitionSettleState
import com.android.purebilibili.core.ui.transition.resolveVideoHeroMotionSpec
import com.android.purebilibili.core.ui.transition.resolveVideoHeroLandingScale
import top.yukonga.miuix.kmp.nav.transition.NavMotion
import top.yukonga.miuix.kmp.nav.transition.NavRole
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavSwipeEdge
import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope
import top.yukonga.miuix.kmp.nav.transition.NavSettlePhase

internal enum class MiuixVideoCardContentScale {
    /** Home dual-column / stacked cards: media fills card width, pinned to top. */
    FillWidthTop,
    /** Fullscreen, side-by-side, or square-ish morphs: cover scale about center. */
    CropCenter,
}

/**
 * Pick entry content compensation from the frozen source layout.
 * Side-by-side related/home single-column rows avoid FillWidthTop's vertical stretch
 * (which reads as "whole page crushed into a thin strip").
 */
internal fun resolveMiuixVideoCardContentScaleForSourceLayout(
    sourceLayout: VideoCardSourceLayout,
    fullscreen: Boolean = false,
): MiuixVideoCardContentScale {
    if (fullscreen) return MiuixVideoCardContentScale.CropCenter
    // FillWidthTop for both STACKED and SIDE_BY_SIDE so inverse-scale landing
    // (1/sourceScale) + outer non-uniform scale maps to the frozen card bounds.
    // CropCenter shifts top-left landing anchors and turns horizontal cards into black strips.
    return when (sourceLayout) {
        VideoCardSourceLayout.SIDE_BY_SIDE,
        VideoCardSourceLayout.STACKED,
        VideoCardSourceLayout.COVER_ONLY,
        -> MiuixVideoCardContentScale.FillWidthTop
    }
}

internal data class MiuixVideoCardContentCompensation(
    val scaleX: Float,
    val scaleY: Float,
    val transformOrigin: TransformOrigin,
)

internal data class MiuixVideoCardClipRadii(
    val radiusX: Float,
    val radiusY: Float,
)

internal data class MiuixVideoCardGestureTransform(
    val translationX: Float,
    val translationY: Float,
    val rotationZ: Float,
    val transformOrigin: TransformOrigin,
)

internal fun resolveMiuixVideoCardGestureTransform(
    morphProgress: Float,
    touchY: Float,
    initialTouchY: Float,
    widthPx: Float,
    heightPx: Float,
    isLeftEdge: Boolean,
    maxVerticalTravelPx: Float,
): MiuixVideoCardGestureTransform {
    val morph = morphProgress.coerceIn(0f, 1f)
    // Fullscreen (1) and exact card landing (0) must both be identity transforms.
    val gestureWeight = 4f * morph * (1f - morph)
    val edgeSign = if (isLeftEdge) 1f else -1f
    val verticalDelta = touchY - initialTouchY
    val normalizedVerticalDelta = if (heightPx > 0f) {
        (verticalDelta / (heightPx * 0.5f)).coerceIn(-1f, 1f)
    } else {
        0f
    }

    return MiuixVideoCardGestureTransform(
        translationX = edgeSign * widthPx.coerceAtLeast(0f) * 0.035f * gestureWeight,
        translationY = (verticalDelta * 0.1f)
            .coerceIn(-maxVerticalTravelPx.coerceAtLeast(0f), maxVerticalTravelPx.coerceAtLeast(0f)) *
            gestureWeight,
        rotationZ = edgeSign * normalizedVerticalDelta * 1.8f * gestureWeight,
        transformOrigin = TransformOrigin(
            pivotFractionX = if (isLeftEdge) 0.82f else 0.18f,
            pivotFractionY = if (heightPx > 0f) {
                (touchY / heightPx).coerceIn(0.1f, 0.9f)
            } else {
                0.5f
            },
        ),
    )
}

/** Top entry depth is 0 at rest and moves toward -1 while returning. */
internal fun resolveMiuixVideoCardDepthProgress(relativeDepth: Float): Float =
    topProgress(relativeDepth)

internal fun resolveMiuixVideoCardOuterScale(sourceScale: Float, depth: Float, landingScale: Float): Float {
    val source = sourceScale.coerceIn(0.05f, 1f)
    return source + (1f - source) * depth.coerceIn(0f, 1f) - source * (1f - landingScale)
}

/**
 * Keeps the corner circular in screen space while the outer card layer scales non-uniformly.
 * A regular RoundedCornerShape is scaled together with the layer and becomes too small on the
 * compressed axis, which exposes the retained source card near the end of a card return.
 */
internal fun resolveMiuixVideoCardClipRadii(
    sourceCornerPx: Float,
    outerScaleX: Float,
    outerScaleY: Float,
): MiuixVideoCardClipRadii {
    val physicalRadius = sourceCornerPx.coerceAtLeast(0f)
    return MiuixVideoCardClipRadii(
        radiusX = physicalRadius / outerScaleX.coerceAtLeast(0.01f),
        radiusY = physicalRadius / outerScaleY.coerceAtLeast(0.01f),
    )
}

private data class MiuixVideoCardClipShape(
    val radiusX: Float,
    val radiusY: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        return Outline.Rounded(
            RoundRect(
                rect = Rect(0f, 0f, size.width, size.height),
                cornerRadius = CornerRadius(
                    x = radiusX.coerceIn(0f, size.width / 2f),
                    y = radiusY.coerceIn(0f, size.height / 2f),
                ),
            ),
        )
    }
}

internal fun resolveMiuixVideoCardContentCompensation(
    outerScaleX: Float,
    outerScaleY: Float,
    contentScale: MiuixVideoCardContentScale,
): MiuixVideoCardContentCompensation {
    val safeOuterScaleX = outerScaleX.coerceAtLeast(0.01f)
    val safeOuterScaleY = outerScaleY.coerceAtLeast(0.01f)
    val uniformScale = when (contentScale) {
        MiuixVideoCardContentScale.FillWidthTop -> safeOuterScaleX
        MiuixVideoCardContentScale.CropCenter -> maxOf(safeOuterScaleX, safeOuterScaleY)
    }
    return MiuixVideoCardContentCompensation(
        scaleX = uniformScale / safeOuterScaleX,
        scaleY = uniformScale / safeOuterScaleY,
        transformOrigin = when (contentScale) {
            MiuixVideoCardContentScale.FillWidthTop -> TransformOrigin(0.5f, 0f)
            MiuixVideoCardContentScale.CropCenter -> TransformOrigin.Center
        },
    )
}

/** Deferred bridge to the top video entry's live Miuix driver. */
internal class MiuixVideoCardTransitionProgress {
    private var topScope: NavTransitionScope? by mutableStateOf(null)

    fun bind(scope: NavTransitionScope) {
        when (scope.role) {
            NavRole.Incoming,
            NavRole.Outgoing,
            -> topScope = scope
            NavRole.Top -> if (topScope == null || topScope?.role == NavRole.Covered) {
                topScope = scope
            }
            NavRole.Covered -> Unit
        }
    }

    fun depthOr(fallback: Float): Float = topScope
        ?.let { resolveMiuixVideoCardDepthProgress(it.relativeDepth) }
        ?: fallback.coerceIn(0f, 1f)

    // Miuix retains gesture metadata while settling. It is NOT still direct manipulation.
    fun isGestureInProgress(): Boolean = topScope?.let {
        it.gesture != null && it.settle == null
    } == true

    fun depthOrNull(): Float? = topScope?.let {
        resolveMiuixVideoCardDepthProgress(it.relativeDepth)
    }

    fun releaseVelocity(): Float = topScope?.settle?.releaseVelocity ?: 0f

    fun settleStateOrNull(): VideoCardTransitionSettleState? = topScope?.let { scope ->
        when {
            scope.settle?.phase == NavSettlePhase.Cancel -> VideoCardTransitionSettleState.CancelRestore
            isGestureInProgress() -> VideoCardTransitionSettleState.InteractiveSeek
            scope.role == NavRole.Outgoing && scope.relativeDepth <= -1f -> VideoCardTransitionSettleState.Idle
            scope.settle?.phase == NavSettlePhase.Commit || scope.role == NavRole.Outgoing ->
                VideoCardTransitionSettleState.AutoReturn
            scope.role == NavRole.Incoming -> VideoCardTransitionSettleState.AutoEnter
            else -> VideoCardTransitionSettleState.Held
        }
    }

    /**
     * Host layout width used by outer morph (`bounds.width / layoutSize.width`).
     * Landing inverse scale must use the same width or land size drifts from the list card.
     */
    fun layoutWidthOr(fallback: Float): Float {
        val w = topScope?.layoutSize?.width?.toFloat() ?: return fallback.coerceAtLeast(1f)
        return w.coerceAtLeast(1f)
    }

    /**
     * 预测返回手势进度（0=开始 → 1=完全提交），无手势时为 null。
     * 供预测返回背景模糊（predictiveBackBackgroundEffect）随手势映射，恢复 0.2.2 链路。
     */
    fun gestureBackProgress(): Float? = topScope?.gesture?.progress?.takeIf { isGestureInProgress() }
}

internal fun resolveVideoHeroNavMotion(spec: VideoHeroMotionSpec, returning: Boolean): NavMotion = NavMotion(
    // Unlike Tween, Spring consumes the existing driver's release velocity and partial position.
    // No generic fallback spring is now needed for interrupted programmatic transitions.
    commit = NavSettleSpec.Spring(
        dampingRatio = VideoHeroMotionTokens.SPRING_DAMPING,
        stiffness = spec.commitStiffness,
    ),
    cancel = NavSettleSpec.Spring(
        dampingRatio = VideoHeroMotionTokens.SPRING_DAMPING,
        stiffness = spec.cancelStiffness,
    ),
    programmatic = NavSettleSpec.Tween(
        durationMillis = if (returning) spec.returnDurationMillis else spec.enterDurationMillis,
        easing = if (returning) spec.returnSpatialSpec else spec.enterSpatialSpec,
    ),
)

/**
 * Video-card morph authored directly against Miuix's shared navigation driver.
 *
 * The video entry is transformed from the click-time card rectangle to the navigation host. The
 * same [NavTransitionScope.relativeDepth] drives push, programmatic pop, predictive back, commit,
 * and cancellation, so there is no AndroidX Navigation3 or AnimatedVisibility compatibility path.
 */
internal fun miuixVideoCardNavTransition(
    sourceBounds: Rect?,
    sourceCornerDp: Int?,
    durationMillis: Int,
    fallback: NavTransition,
    progress: MiuixVideoCardTransitionProgress,
    contentScale: MiuixVideoCardContentScale = MiuixVideoCardContentScale.FillWidthTop,
    gestureFollowEnabled: Boolean = true,
    heroMotionSpec: VideoHeroMotionSpec = resolveVideoHeroMotionSpec(durationMillis),
    returningProvider: () -> Boolean = { false },
): NavTransition {
    val bounds = sourceBounds?.takeIf { it.width > 1f && it.height > 1f }
        ?: return fallback
    val enterMotion = resolveVideoHeroNavMotion(heroMotionSpec, returning = false)
    val returnMotion = resolveVideoHeroNavMotion(heroMotionSpec, returning = true)
    val corner = sourceCornerDp?.coerceAtLeast(0) ?: 16

    return object : NavTransition {
        override val opaqueDepth: Float = fallback.opaqueDepth
        override val motion: NavMotion get() = if (returningProvider()) returnMotion else enterMotion

        // Source-page scrim and blur are rendered by the existing depth layer from this same
        // transition's deferred progress. Do not add Miuix's generic dim on top of it.
        override fun scrimFraction(scope: NavTransitionScope): Float = 0f

        override fun Modifier.transformEntry(scope: NavTransitionScope): Modifier {
            progress.bind(scope)
            val gestureModifier = if (gestureFollowEnabled) {
                graphicsLayer {
                    val depth = scope.relativeDepth
                    val gesture = scope.gesture
                    if (depth <= 0f && gesture != null) {
                        val height = scope.layoutSize.height.toFloat().coerceAtLeast(1f)
                        val transform = resolveMiuixVideoCardGestureTransform(
                            morphProgress = resolveMiuixVideoCardDepthProgress(depth),
                            touchY = gesture.touchY,
                            initialTouchY = gesture.initialTouchY,
                            widthPx = scope.layoutSize.width.toFloat().coerceAtLeast(1f),
                            heightPx = height,
                            isLeftEdge = gesture.swipeEdge == NavSwipeEdge.Left,
                            maxVerticalTravelPx = 24.dp.toPx(),
                        )
                        translationX = transform.translationX
                        translationY = transform.translationY
                        rotationZ = transform.rotationZ
                        transformOrigin = transform.transformOrigin
                    }
                }
            } else {
                this
            }
            return gestureModifier.graphicsLayer {
                val width = scope.layoutSize.width.toFloat().coerceAtLeast(1f)
                val height = scope.layoutSize.height.toFloat().coerceAtLeast(1f)
                val depth = scope.relativeDepth
                if (depth <= 0f) {
                    val morph = resolveMiuixVideoCardDepthProgress(depth)
                    val sourceScaleX = (bounds.width / width).coerceIn(0.05f, 1f)
                    val sourceScaleY = (bounds.height / height).coerceIn(0.05f, 1f)
                    val landingScale = resolveVideoHeroLandingScale(
                        depth = morph,
                        autoReturning = !heroMotionSpec.reducedMotion &&
                            scope.settle != null && scope.settle?.phase != NavSettlePhase.Cancel &&
                            scope.role == NavRole.Outgoing,
                    )
                    val outerScaleX = resolveMiuixVideoCardOuterScale(sourceScaleX, morph, landingScale)
                    val outerScaleY = resolveMiuixVideoCardOuterScale(sourceScaleY, morph, landingScale)
                    scaleX = outerScaleX
                    scaleY = outerScaleY
                    transformOrigin = TransformOrigin(0f, 0f)
                    translationX = bounds.left.coerceIn(-width, width) * (1f - morph)
                    translationY = bounds.top.coerceIn(-height, height) * (1f - morph)
                    // Keep the complete flying entry opaque. The source card and the detail entry
                    // already share the same geometry driver; an entry-level alpha handoff would
                    // expose the player's black Surface frame at landing.
                    alpha = 1f
                    clip = morph < 0.999f
                    val clipRadii = resolveMiuixVideoCardClipRadii(
                        sourceCornerPx = corner.dp.toPx(),
                        outerScaleX = outerScaleX,
                        outerScaleY = outerScaleY,
                    )
                    shape = MiuixVideoCardClipShape(
                        radiusX = clipRadii.radiusX,
                        radiusY = clipRadii.radiusY,
                    )
                }
            }.graphicsLayer {
                val depth = scope.relativeDepth
                if (depth <= 0f) {
                    val width = scope.layoutSize.width.toFloat().coerceAtLeast(1f)
                    val height = scope.layoutSize.height.toFloat().coerceAtLeast(1f)
                    val morph = resolveMiuixVideoCardDepthProgress(depth)
                    val landingScale = resolveVideoHeroLandingScale(
                        depth = morph,
                        autoReturning = !heroMotionSpec.reducedMotion &&
                            scope.settle != null && scope.settle?.phase != NavSettlePhase.Cancel &&
                            scope.role == NavRole.Outgoing,
                    )
                    val outerScaleX = resolveMiuixVideoCardOuterScale(bounds.width / width, morph, landingScale)
                    val outerScaleY = resolveMiuixVideoCardOuterScale(bounds.height / height, morph, landingScale)
                    val compensation = resolveMiuixVideoCardContentCompensation(
                        outerScaleX = outerScaleX,
                        outerScaleY = outerScaleY,
                        contentScale = contentScale,
                    )
                    scaleX = compensation.scaleX
                    scaleY = compensation.scaleY
                    transformOrigin = compensation.transformOrigin
                }
            }.zIndex(1f)
        }
    }
}
