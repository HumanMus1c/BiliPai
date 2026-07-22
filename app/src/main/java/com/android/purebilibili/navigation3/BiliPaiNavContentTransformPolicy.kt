package com.android.purebilibili.navigation3

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.android.purebilibili.core.ui.motion.AppMotionEasing
import com.android.purebilibili.core.ui.motion.SETTINGS_IOS_PUSH_DURATION_MS
import com.android.purebilibili.core.ui.motion.resolveBottomBarLikeHorizontalContentTransform
import com.android.purebilibili.core.ui.motion.resolveSettingsIosPushForwardContentTransform
import com.android.purebilibili.core.ui.motion.resolveSettingsIosPushPopContentTransform
import com.android.purebilibili.navigation.resolveBottomPagerNavigationDurationMillis

private const val NAV3_FALLBACK_FADE_MILLIS = 180
// Slightly longer so card-disabled enter/exit never reads as a hard cut.
private const val NAV3_DISABLED_VIDEO_DIRECTION_MILLIS = 280
private const val NAV3_DISABLED_VIDEO_RETURN_MILLIS = 260
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
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_FORWARD_FROM_LEFT ->
            disabledVideoDirectionForwardTransform(directionSign = -1)
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_FORWARD_FROM_RIGHT ->
            disabledVideoDirectionForwardTransform(directionSign = 1)
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_RETURN_TO_LEFT ->
            disabledVideoDirectionReturnTransform(directionSign = -1)
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_RETURN_TO_RIGHT ->
            disabledVideoDirectionReturnTransform(directionSign = 1)
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

internal fun resolveBiliPaiNavPopContentTransform(
    routeTransition: BiliPaiNavRouteTransition
): ContentTransform? {
    return when (routeTransition) {
        BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT,
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_RETURN_TO_LEFT,
        BiliPaiNavRouteTransition.CARD_DISABLED_VIDEO_RETURN_TO_RIGHT ->
            resolveBiliPaiNavContentTransform(routeTransition)
        else -> null
    }
}

/**
 * @param directionSign -1 = source left (enter from left / exit to left)
 *                      +1 = source right (enter from right / exit to right)
 */
private fun disabledVideoDirectionForwardTransform(directionSign: Int): ContentTransform {
    return (
        slideInHorizontally(
            animationSpec = tween(
                NAV3_DISABLED_VIDEO_DIRECTION_MILLIS,
                easing = AppMotionEasing.EmphasizedEnter
            ),
            // ~85% width keeps origin readable without a jarring full wipe.
            initialOffsetX = { width -> (directionSign * width * 0.85f).toInt() }
        ) + fadeIn(
            animationSpec = tween(
                NAV3_DISABLED_VIDEO_DIRECTION_MILLIS,
                easing = AppMotionEasing.EmphasizedEnter
            )
        )
    ) togetherWith (
        slideOutHorizontally(
            animationSpec = tween(
                NAV3_DISABLED_VIDEO_DIRECTION_MILLIS,
                easing = AppMotionEasing.EmphasizedExit
            ),
            // Opposite nudge for the list page under the detail.
            targetOffsetX = { width -> (-directionSign * width * 0.18f).toInt() }
        ) + fadeOut(animationSpec = tween(NAV3_DISABLED_VIDEO_DIRECTION_MILLIS))
    )
}

private fun spaceForwardTransform(): ContentTransform {
    return (
        slideInHorizontally(
            animationSpec = tween(NAV3_SPACE_FORWARD_MILLIS, easing = AppMotionEasing.EmphasizedEnter),
            initialOffsetX = { width -> width / 8 }
        ) + fadeIn(animationSpec = tween(NAV3_SPACE_FORWARD_MILLIS))
    ) togetherWith fadeOut(animationSpec = tween(NAV3_FALLBACK_FADE_MILLIS))
}

private fun lightSiblingForwardTransform(): ContentTransform {
    return (
        slideInHorizontally(
            animationSpec = tween(NAV3_LIGHT_SIBLING_MILLIS, easing = AppMotionEasing.EmphasizedEnter),
            initialOffsetX = { width -> width / 8 }
        ) + fadeIn(animationSpec = tween(NAV3_LIGHT_SIBLING_MILLIS, easing = AppMotionEasing.EmphasizedEnter))
    ) togetherWith fadeOut(animationSpec = tween(NAV3_FALLBACK_FADE_MILLIS))
}

private fun lightSiblingPopTransform(): ContentTransform {
    return EnterTransition.None togetherWith
        (
            slideOutHorizontally(
                animationSpec = tween(NAV3_LIGHT_SIBLING_MILLIS, easing = AppMotionEasing.EmphasizedExit),
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

private fun settingsIosPushForwardTransform(): ContentTransform =
    resolveSettingsIosPushForwardContentTransform(durationMillis = SETTINGS_IOS_PUSH_DURATION_MS)

private fun settingsIosPushPopTransform(): ContentTransform =
    resolveSettingsIosPushPopContentTransform(durationMillis = SETTINGS_IOS_PUSH_DURATION_MS)

/**
 * Return to card side:
 * - directionSign -1: detail exits left (left card), list re-enters from the right
 * - directionSign +1: detail exits right (right card), list re-enters from the left
 */
private fun disabledVideoDirectionReturnTransform(directionSign: Int): ContentTransform {
    return (
        slideInHorizontally(
            animationSpec = tween(
                durationMillis = NAV3_DISABLED_VIDEO_RETURN_MILLIS,
                easing = AppMotionEasing.EmphasizedEnter
            ),
            initialOffsetX = { width -> (-directionSign * width * 0.18f).toInt() }
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = NAV3_DISABLED_VIDEO_RETURN_MILLIS,
                easing = AppMotionEasing.EmphasizedEnter
            )
        )
    ) togetherWith (
        slideOutHorizontally(
            animationSpec = tween(
                durationMillis = NAV3_DISABLED_VIDEO_RETURN_MILLIS,
                easing = AppMotionEasing.EmphasizedExit
            ),
            // Exit fully toward the card column (left card → left, right card → right).
            targetOffsetX = { width -> (directionSign * width * 0.9f).toInt() }
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = NAV3_DISABLED_VIDEO_RETURN_MILLIS,
                easing = AppMotionEasing.EmphasizedExit
            )
        )
    )
}
