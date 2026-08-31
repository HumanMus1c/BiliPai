package com.android.purebilibili.navigation3.predictiveback

import androidx.compose.ui.graphics.TransformOrigin
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import java.io.File
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.android.purebilibili.core.ui.transition.resolveVideoHeroMotionSpec
import com.android.purebilibili.core.ui.transition.resolveVideoHeroLandingScale
import com.android.purebilibili.core.ui.transition.VideoCardTransitionSettleState
import top.yukonga.miuix.kmp.nav.transition.NavSettleSpec
import top.yukonga.miuix.kmp.nav.transition.NavTransitionScope
import top.yukonga.miuix.kmp.nav.transition.NavSettle
import top.yukonga.miuix.kmp.nav.transition.NavSettlePhase
import top.yukonga.miuix.kmp.nav.transition.NavGesture
import top.yukonga.miuix.kmp.nav.transition.NavSwipeEdge
import top.yukonga.miuix.kmp.nav.transition.NavRole
import top.yukonga.miuix.kmp.nav.runtime.NavChange
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

class MiuixVideoCardNavTransitionTest {
    @Test
    fun heroNavMotionUsesDurationTokensAndVelocityCapableReleaseSpecs() {
        val spec = resolveVideoHeroMotionSpec(360)
        val entering = resolveVideoHeroNavMotion(spec, false)
        val returning = resolveVideoHeroNavMotion(spec, true)
        assertEquals(360, (entering.programmatic as NavSettleSpec.Tween).durationMillis)
        assertEquals(310, (returning.programmatic as NavSettleSpec.Tween).durationMillis)
        assertEquals(spec.commitStiffness, (returning.commit as NavSettleSpec.Spring).stiffness)
        assertEquals(spec.cancelStiffness, (returning.cancel as NavSettleSpec.Spring).stiffness)
        assertEquals(1f, (returning.commit as NavSettleSpec.Spring).dampingRatio)
    }

    @Test
    fun landingCompressionIsBoundedRelativeToFinalSizeOnBothAxes() {
        for (sourceScale in listOf(.05f, .2f, .8f, 1f)) {
            for (i in 0..1000) {
                val depth = i / 1000f
                val scale = resolveMiuixVideoCardOuterScale(sourceScale, depth,
                    resolveVideoHeroLandingScale(depth, true))
                val baseline = resolveMiuixVideoCardOuterScale(sourceScale, depth, 1f)
                assertTrue(abs(scale - baseline) <= .015f * sourceScale)
            }
            assertEquals(sourceScale, resolveMiuixVideoCardOuterScale(sourceScale, 0f, 1f))
            assertEquals(1f, resolveMiuixVideoCardOuterScale(sourceScale, 1f, 1f))
        }
    }

    @Test
    fun retainedGestureMetadataDoesNotMisclassifyCommitOrCancelAsSeek() {
        val scope = object : NavTransitionScope {
            override var relativeDepth = -.4f
            override var role = NavRole.Top
            override val change = NavChange.Pop
            override val layoutSize = IntSize(1080, 2400)
            override val layoutDirection = LayoutDirection.Ltr
            override val density = Density(3f)
            override val gesture = NavGesture(.4f, NavSwipeEdge.Left, 500f)
            override var settle: NavSettle? = null
        }
        val progress = MiuixVideoCardTransitionProgress()
        progress.bind(scope)
        assertTrue(progress.isGestureInProgress())
        assertEquals(.6f, progress.depthOrNull())
        scope.settle = object : NavSettle {
            override val phase = NavSettlePhase.Cancel
            override val releaseVelocity = 0f
            override val elapsedMillis = 0f
        }
        assertEquals(false, progress.isGestureInProgress())
        assertEquals(VideoCardTransitionSettleState.CancelRestore, progress.settleStateOrNull())
        scope.settle = object : NavSettle {
            override val phase = NavSettlePhase.Commit
            override val releaseVelocity = 2f
            override val elapsedMillis = 0f
        }
        scope.role = NavRole.Outgoing
        assertEquals(VideoCardTransitionSettleState.AutoReturn, progress.settleStateOrNull())
        scope.relativeDepth = -1f
        assertEquals(VideoCardTransitionSettleState.Idle, progress.settleStateOrNull())
    }
    @Test
    fun transitionKeepsOneOpaqueFlyingCardWithoutStationaryRevealMask() {
        val source = File(
            "src/main/java/com/android/purebilibili/navigation3/predictiveback/MiuixVideoCardNavTransition.kt"
        ).readText()
        val transform = source.substringAfter("override fun Modifier.transformEntry")

        assertEquals(true, transform.contains("alpha = 1f"))
        assertEquals(false, transform.contains("visibleHeightFraction"))
        assertEquals(false, transform.contains("outgoingClipFraction"))
    }

    @Test
    fun returnDepthClearsBlurInsteadOfReversingIt() {
        assertEquals(
            1f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = 0f),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            0.5f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = -0.5f),
            absoluteTolerance = 0.0001f,
        )
        assertEquals(
            0f,
            resolveMiuixVideoCardDepthProgress(relativeDepth = -1f),
            absoluteTolerance = 0.0001f,
        )
    }

    @Test
    fun cardClipKeepsPhysicalCornerRadiusAcrossNonUniformScale() {
        val radii = resolveMiuixVideoCardClipRadii(
            sourceCornerPx = 12f,
            outerScaleX = 0.5f,
            outerScaleY = 0.25f,
        )

        assertEquals(12f, radii.radiusX * 0.5f, absoluteTolerance = 0.0001f)
        assertEquals(12f, radii.radiusY * 0.25f, absoluteTolerance = 0.0001f)
    }

    @Test
    fun fillWidthTopPreservesAspectRatioAndTopAlignment() {
        val compensation = resolveMiuixVideoCardContentCompensation(
            outerScaleX = 0.5f,
            outerScaleY = 0.25f,
            contentScale = MiuixVideoCardContentScale.FillWidthTop,
        )

        assertEquals(0.5f, 0.5f * compensation.scaleX, absoluteTolerance = 0.0001f)
        assertEquals(0.5f, 0.25f * compensation.scaleY, absoluteTolerance = 0.0001f)
        assertEquals(TransformOrigin(0.5f, 0f), compensation.transformOrigin)
    }

    @Test
    fun cropCenterPreservesAspectRatioUsingCoverScale() {
        val compensation = resolveMiuixVideoCardContentCompensation(
            outerScaleX = 0.35f,
            outerScaleY = 0.6f,
            contentScale = MiuixVideoCardContentScale.CropCenter,
        )

        assertEquals(0.6f, 0.35f * compensation.scaleX, absoluteTolerance = 0.0001f)
        assertEquals(0.6f, 0.6f * compensation.scaleY, absoluteTolerance = 0.0001f)
        assertEquals(TransformOrigin.Center, compensation.transformOrigin)
    }

    @Test
    fun sideBySideAndStackedUseFillWidthTopForLandingAnchors() {
        assertEquals(
            MiuixVideoCardContentScale.FillWidthTop,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.SIDE_BY_SIDE,
            ),
        )
        assertEquals(
            MiuixVideoCardContentScale.FillWidthTop,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.STACKED,
            ),
        )
        assertEquals(
            MiuixVideoCardContentScale.CropCenter,
            resolveMiuixVideoCardContentScaleForSourceLayout(
                sourceLayout = VideoCardSourceLayout.STACKED,
                fullscreen = true,
            ),
        )
    }

    @Test
    fun gestureFollowPeaksMidFlightAndKeepsBothLandingEndpointsExact() {
        val fullscreen = resolveMiuixVideoCardGestureTransform(
            morphProgress = 1f,
            touchY = 900f,
            initialTouchY = 500f,
            widthPx = 1080f,
            heightPx = 2400f,
            isLeftEdge = true,
            maxVerticalTravelPx = 72f,
        )
        val midFlight = resolveMiuixVideoCardGestureTransform(
            morphProgress = 0.5f,
            touchY = 900f,
            initialTouchY = 500f,
            widthPx = 1080f,
            heightPx = 2400f,
            isLeftEdge = true,
            maxVerticalTravelPx = 72f,
        )
        val landed = resolveMiuixVideoCardGestureTransform(
            morphProgress = 0f,
            touchY = 900f,
            initialTouchY = 500f,
            widthPx = 1080f,
            heightPx = 2400f,
            isLeftEdge = true,
            maxVerticalTravelPx = 72f,
        )

        assertEquals(0f, fullscreen.translationX)
        assertEquals(0f, fullscreen.translationY)
        assertEquals(0f, fullscreen.rotationZ)
        assertEquals(0f, landed.translationX)
        assertEquals(0f, landed.translationY)
        assertEquals(0f, landed.rotationZ)
        assertEquals(true, abs(midFlight.translationX) > 0f)
        assertEquals(true, abs(midFlight.translationY) > 0f)
        assertEquals(true, abs(midFlight.rotationZ) > 0f)
    }

    @Test
    fun gestureFollowMirrorsAcrossScreenEdgesWithoutChangingVerticalLanding() {
        val left = resolveMiuixVideoCardGestureTransform(
            morphProgress = 0.5f,
            touchY = 800f,
            initialTouchY = 500f,
            widthPx = 1080f,
            heightPx = 2400f,
            isLeftEdge = true,
            maxVerticalTravelPx = 72f,
        )
        val right = resolveMiuixVideoCardGestureTransform(
            morphProgress = 0.5f,
            touchY = 800f,
            initialTouchY = 500f,
            widthPx = 1080f,
            heightPx = 2400f,
            isLeftEdge = false,
            maxVerticalTravelPx = 72f,
        )

        assertEquals(-left.translationX, right.translationX, absoluteTolerance = 0.001f)
        assertEquals(left.translationY, right.translationY, absoluteTolerance = 0.001f)
        assertEquals(-left.rotationZ, right.rotationZ, absoluteTolerance = 0.001f)
    }
}
