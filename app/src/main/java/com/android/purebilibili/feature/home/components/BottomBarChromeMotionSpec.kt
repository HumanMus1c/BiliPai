package com.android.purebilibili.feature.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import com.android.purebilibili.core.ui.motion.AppMotionEasing

internal fun <T> bottomBarDockWidthMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 260,
    easing = AppMotionEasing.Continuity,
)

internal fun <T> bottomBarChromeHeightMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 220,
    easing = AppMotionEasing.Continuity,
)

internal fun <T> bottomBarSearchGapMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 240,
    easing = AppMotionEasing.Continuity,
)

internal fun <T> bottomBarContentVisibilityMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 180,
    easing = AppMotionEasing.Continuity,
)

internal fun <T> bottomBarClickPulseMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 240,
    easing = LinearEasing,
)

internal fun <T> bottomBarTapReleaseMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 240,
    easing = FastOutSlowInEasing,
)

internal fun <T> bottomBarSettleReboundMotionSpec(): TweenSpec<T> = tween(
    durationMillis = 260,
    easing = LinearEasing,
)

internal fun <T> bottomBarSearchHoldMotionSpec(): SpringSpec<T> = spring(
    dampingRatio = 0.62f,
    stiffness = 560f,
)
