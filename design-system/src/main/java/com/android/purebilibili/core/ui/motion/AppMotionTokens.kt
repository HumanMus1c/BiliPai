package com.android.purebilibili.core.ui.motion

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens

object AppMotionEasing {
    val EmphasizedEnter: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)
    val EmphasizedExit: Easing = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)
    val Continuity: Easing = CubicBezierEasing(0.20f, 0.90f, 0.22f, 1.00f)
    val GentleEnter: Easing = CubicBezierEasing(0.18f, 0.80f, 0.20f, 1.00f)
    /** 景深返回清晰：ease-in 向 0，先留住模糊再柔化，避免 Continuity 在 1→0 时过早掐清。 */
    val SoftClear: Easing = CubicBezierEasing(0.40f, 0.00f, 0.55f, 0.30f)
}

fun <T> emphasizedEnterTween(durationMillis: Int): TweenSpec<T> =
    tween(durationMillis = durationMillis, easing = AppMotionEasing.EmphasizedEnter)

fun <T> emphasizedExitTween(durationMillis: Int): TweenSpec<T> =
    tween(durationMillis = durationMillis, easing = AppMotionEasing.EmphasizedExit)

fun <T> continuityTween(durationMillis: Int): TweenSpec<T> =
    tween(durationMillis = durationMillis, easing = AppMotionEasing.Continuity)

internal fun <T> gentleEnterTween(durationMillis: Int): TweenSpec<T> =
    tween(durationMillis = durationMillis, easing = AppMotionEasing.GentleEnter)

fun <T> softLandingSpring(): SpringSpec<T> =
    spring(
        dampingRatio = 0.86f,
        stiffness = Spring.StiffnessMediumLow
    )

fun interactiveSnapSpring(): SpringSpec<Float> =
    spring(
        dampingRatio = 0.78f,
        stiffness = 420f
    )

fun pullRefreshReleaseSpring(): SpringSpec<Float> =
    spring(
        dampingRatio = 0.96f,
        stiffness = 520f
    )

internal fun expressiveSnapSpring(): SpringSpec<Float> =
    spring(
        dampingRatio = 0.72f,
        stiffness = 520f
    )

internal fun pressFeedbackSpring(): SpringSpec<Float> =
    spring(
        dampingRatio = 1f,
        stiffness = 1000f,
        visibilityThreshold = 0.001f
    )

internal fun selectionSpring(): SpringSpec<Float> =
    spring(
        dampingRatio = 0.82f,
        stiffness = 500f
    )

fun indicatorSpring(): SpringSpec<Float> =
    spring(
        dampingRatio = 0.7f,
        stiffness = Spring.StiffnessMedium
    )

/**
 * 两值主题的运动 tokens。Screens should call the @Composable accessors
 * (e.g. [AppMotionTokens.standardSpec]) instead of writing literal `tween(...)`
 * or `spring(...)` calls. MIUIX resolves to denser tween durations; MATERIAL3
 * resolves to the standard Material tween durations.
 */
object AppMotionTokens {

    fun <T> resolveStandardSpec(
        uiStyle: AppUiStyle
    ): FiniteAnimationSpec<T> = when (uiStyle) {
        AppUiStyle.MIUIX -> tween(
            durationMillis = 180,
            easing = AppMotionEasing.Continuity
        )
        AppUiStyle.MATERIAL3 -> tween(
            durationMillis = 200,
            easing = AppMotionEasing.Continuity
        )
    }

    fun <T> resolveEmphasizedSpec(
        uiStyle: AppUiStyle
    ): FiniteAnimationSpec<T> = when (uiStyle) {
        AppUiStyle.MIUIX -> tween(
            durationMillis = 240,
            easing = AppMotionEasing.EmphasizedEnter
        )
        AppUiStyle.MATERIAL3 -> tween(
            durationMillis = 300,
            easing = AppMotionEasing.EmphasizedEnter
        )
    }

    fun <T> resolveExpressiveSpec(
        uiStyle: AppUiStyle
    ): FiniteAnimationSpec<T> = when (uiStyle) {
        AppUiStyle.MIUIX -> tween(
            durationMillis = 150,
            easing = AppMotionEasing.EmphasizedExit
        )
        AppUiStyle.MATERIAL3 -> tween(
            durationMillis = 180,
            easing = AppMotionEasing.EmphasizedExit
        )
    }

    fun <T> resolveSpatialSpec(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.82f,
        stiffness = 380f
    )

    @Composable
    fun <T> standardSpec(): FiniteAnimationSpec<T> = resolveStandardSpec(
        uiStyle = LocalAppUiStyle.current
    )

    @Composable
    fun <T> emphasizedSpec(): FiniteAnimationSpec<T> = resolveEmphasizedSpec(
        uiStyle = LocalAppUiStyle.current
    )

    @Composable
    fun <T> expressiveSpec(): FiniteAnimationSpec<T> = resolveExpressiveSpec(
        uiStyle = LocalAppUiStyle.current
    )

    fun <T> spatialSpec(): FiniteAnimationSpec<T> = resolveSpatialSpec()

    fun <T> resolveBottomSheetSlideSpec(
        uiStyle: AppUiStyle
    ): FiniteAnimationSpec<T> {
        val tokens = resolveAndroidNativeChromeTokens(uiStyle)
        return continuityTween(tokens.motionStandardMillis)
    }

    fun <T> resolveBottomSheetFadeEnterSpec(
        uiStyle: AppUiStyle
    ): FiniteAnimationSpec<T> = resolveEmphasizedSpec(uiStyle)

    fun <T> resolveBottomSheetFadeExitSpec(
        uiStyle: AppUiStyle
    ): FiniteAnimationSpec<T> = resolveExpressiveSpec(uiStyle)

    fun resolveBottomSheetSlideExitSpec(
        uiStyle: AppUiStyle
    ): FiniteAnimationSpec<IntOffset> {
        val tokens = resolveAndroidNativeChromeTokens(uiStyle)
        return emphasizedExitTween(tokens.expressiveMotionDurationMillis)
    }
}
