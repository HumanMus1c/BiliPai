package com.android.purebilibili.navigation3

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.core.ui.motion.SETTINGS_IOS_PUSH_DURATION_MS
import com.android.purebilibili.core.ui.motion.navigationSlideSpring
import com.android.purebilibili.core.ui.motion.resolveBottomBarLikeHorizontalContentTransform
import com.android.purebilibili.core.ui.motion.resolveSettingsIosPushForwardContentTransform
import com.android.purebilibili.core.ui.motion.resolveSettingsIosPushPopContentTransform
import com.android.purebilibili.navigation.resolveBottomPagerNavigationDurationMillis

private const val NAV3_FALLBACK_FADE_MILLIS = 180
private const val NAV3_REDUCED_MOTION_FADE_MILLIS = 140
// Slightly longer so card-disabled enter/exit never reads as a hard cut.
private const val NAV3_DISABLED_VIDEO_DIRECTION_MILLIS = 280
private const val NAV3_DISABLED_VIDEO_RETURN_MILLIS = 260
// 关闭「过渡动画」后的原生横滑：源页仅向对向轻退 1/4 宽（安卓 activity_open/close 同款视差）。
private const val NAV3_DISABLED_VIDEO_NATIVE_PARALLAX = 0.25f
private const val NAV3_SPACE_FORWARD_MILLIS = 220
private const val NAV3_LIGHT_SIBLING_MILLIS = 240
private val NAV3_BOTTOM_BAR_SIBLING_MILLIS =
    resolveBottomPagerNavigationDurationMillis(pageDistance = 1)
internal fun resolveBiliPaiNavContentTransform(
    routeTransition: BiliPaiNavRouteTransition
): ContentTransform {
    return when (routeTransition) {
        BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT ->
            EnterTransition.None togetherWith ExitTransition.None
        BiliPaiNavRouteTransition.REDUCED_MOTION_FADE ->
            fadeIn(animationSpec = tween(NAV3_REDUCED_MOTION_FADE_MILLIS)) togetherWith
                fadeOut(animationSpec = tween(NAV3_REDUCED_MOTION_FADE_MILLIS))
        // 关闭「过渡动画」后首页 ↔ 播放页：安卓原生式左右横滑。LEFT/RIGHT 为历史分类名
        // （记录卡片列方向），关闭后统一为固定「右入 / 右出」原生横滑，见两个 transform 的 KDoc。
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_FORWARD_FROM_LEFT,
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_FORWARD_FROM_RIGHT ->
            disabledVideoDirectionForwardTransform()
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_RETURN_TO_LEFT,
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_RETURN_TO_RIGHT ->
            disabledVideoDirectionReturnTransform()
        BiliPaiNavRouteTransition.SPACE_FORWARD ->
            spaceForwardTransform()
        BiliPaiNavRouteTransition.LIGHT_SIBLING_FORWARD ->
            lightSiblingForwardTransform()
        BiliPaiNavRouteTransition.LIGHT_SIBLING_POP ->
            lightSiblingPopTransform()
        BiliPaiNavRouteTransition.BOTTOM_BAR_SIBLING_FORWARD ->
            bottomBarSiblingForwardTransform()
        BiliPaiNavRouteTransition.BOTTOM_BAR_SIBLING_POP ->
            bottomBarSiblingPopTransform()
        // 设置树 iOS push/pop：只横滑顶层设置页，底层页静止（见
        // resolveSettingsIosPushForwardContentTransform / resolveSettingsIosPushPopContentTransform）。
        BiliPaiNavRouteTransition.SETTINGS_IOS_PUSH_FORWARD ->
            settingsIosPushForwardTransform()
        BiliPaiNavRouteTransition.SETTINGS_IOS_PUSH_POP ->
            settingsIosPushPopTransform()
        BiliPaiNavRouteTransition.CLASSIC_CARD,
        BiliPaiNavRouteTransition.FALLBACK ->
            fadeIn(animationSpec = tween(NAV3_FALLBACK_FADE_MILLIS)) togetherWith
                fadeOut(animationSpec = tween(NAV3_FALLBACK_FADE_MILLIS))
    }
}

/**
 * 设置页 push：只滑顶层设置页，底层页静止；固定时长 tween，不用 spring。
 */
private fun settingsIosPushForwardTransform(): ContentTransform =
    resolveSettingsIosPushForwardContentTransform(durationMillis = SETTINGS_IOS_PUSH_DURATION_MS)

/**
 * 设置页 pop：设置页向右滑出，底层页静止；固定时长 tween 承接预测手势 seek 收尾。
 */
private fun settingsIosPushPopTransform(): ContentTransform =
    resolveSettingsIosPushPopContentTransform(durationMillis = SETTINGS_IOS_PUSH_DURATION_MS)

/**
 * 关闭「过渡动画」时首页 → 播放页：安卓原生式左右滑入。
 *
 * 播放页自右**全宽**滑入（activity_open_enter），首页向左轻退 1/4 宽（activity_open_exit）。
 * 固定 tween + FastOutSlowIn（安卓标准插值器），无 spring、无卡片方向依赖、无缩放。
 */
private fun disabledVideoDirectionForwardTransform(): ContentTransform {
    val spatialSpec: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = NAV3_DISABLED_VIDEO_DIRECTION_MILLIS,
        easing = FastOutSlowInEasing,
    )
    return (
        slideInHorizontally(
            animationSpec = spatialSpec,
            initialOffsetX = { width -> width }
        ) + fadeIn(
            animationSpec = tween(NAV3_DISABLED_VIDEO_DIRECTION_MILLIS)
        )
    ) togetherWith (
        slideOutHorizontally(
            animationSpec = spatialSpec,
            targetOffsetX = { width -> -(width * NAV3_DISABLED_VIDEO_NATIVE_PARALLAX).toInt() }
        ) + fadeOut(animationSpec = tween(NAV3_DISABLED_VIDEO_DIRECTION_MILLIS))
    )
}

private fun spaceForwardTransform(): ContentTransform {
    val spatialSpec = navigationSlideSpring(NAV3_SPACE_FORWARD_MILLIS)
    return (
        slideInHorizontally(
            animationSpec = spatialSpec,
            initialOffsetX = { width -> width / 8 }
        ) + fadeIn(animationSpec = tween(NAV3_SPACE_FORWARD_MILLIS))
    ) togetherWith fadeOut(animationSpec = tween(NAV3_FALLBACK_FADE_MILLIS))
}

private fun lightSiblingForwardTransform(): ContentTransform {
    val spatialSpec = navigationSlideSpring(NAV3_LIGHT_SIBLING_MILLIS)
    return (
        slideInHorizontally(
            animationSpec = spatialSpec,
            initialOffsetX = { width -> width / 8 }
        ) + fadeIn(animationSpec = tween(NAV3_LIGHT_SIBLING_MILLIS, easing = AppMotionEasing.EmphasizedEnter))
    ) togetherWith fadeOut(animationSpec = tween(NAV3_FALLBACK_FADE_MILLIS))
}

private fun lightSiblingPopTransform(): ContentTransform {
    val spatialSpec = navigationSlideSpring(NAV3_LIGHT_SIBLING_MILLIS)
    return EnterTransition.None togetherWith
        (
            slideOutHorizontally(
                animationSpec = spatialSpec,
                targetOffsetX = { width -> width / 8 }
            ) + fadeOut(animationSpec = tween(NAV3_LIGHT_SIBLING_MILLIS, easing = AppMotionEasing.EmphasizedExit))
        )
}

private fun bottomBarSiblingForwardTransform(): ContentTransform =
    resolveBottomBarLikeHorizontalContentTransform(
        durationMillis = NAV3_BOTTOM_BAR_SIBLING_MILLIS,
        forward = true
    )

private fun bottomBarSiblingPopTransform(): ContentTransform =
    resolveBottomBarLikeHorizontalContentTransform(
        durationMillis = NAV3_BOTTOM_BAR_SIBLING_MILLIS,
        forward = false
    )

/**
 * 关闭「过渡动画」时播放页 → 首页：安卓原生式左右滑出（重点路径）。
 *
 * 播放页自右**全宽**滑出（activity_close_exit），首页自左 1/4 宽归位（activity_close_enter）。
 * 方向固定右滑出：与系统预测返回预览（[com.android.purebilibili.navigation3.predictiveback.BiliPaiDefaultPredictiveBackAnimation]
 * 的 ALWAYS_RIGHT）同向，避免「预览右滑、提交左滑」的换向撕裂。
 */
private fun disabledVideoDirectionReturnTransform(): ContentTransform {
    val spatialSpec: FiniteAnimationSpec<IntOffset> = tween(
        durationMillis = NAV3_DISABLED_VIDEO_RETURN_MILLIS,
        easing = FastOutSlowInEasing,
    )
    return (
        slideInHorizontally(
            animationSpec = spatialSpec,
            initialOffsetX = { width -> -(width * NAV3_DISABLED_VIDEO_NATIVE_PARALLAX).toInt() }
        ) + fadeIn(
            animationSpec = tween(NAV3_DISABLED_VIDEO_RETURN_MILLIS)
        )
    ) togetherWith (
        slideOutHorizontally(
            animationSpec = spatialSpec,
            targetOffsetX = { width -> width }
        ) + fadeOut(animationSpec = tween(NAV3_DISABLED_VIDEO_RETURN_MILLIS))
    )
}
