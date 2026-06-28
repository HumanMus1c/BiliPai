package com.android.purebilibili.navigation3.predictiveback

import com.android.purebilibili.navigation3.BiliPaiNavRouteTransition

internal fun resolveBiliPaiPredictiveBackAnimationHandler(
    routeTransition: BiliPaiNavRouteTransition,
    style: BiliPaiPredictiveBackAnimationStyle = BiliPaiPredictiveBackAnimationStyle.SCALE,
    exitDirection: BiliPaiPredictiveBackExitDirection = BiliPaiPredictiveBackExitDirection.FOLLOW_GESTURE,
): BiliPaiPredictiveBackAnimationHandler {
    if (routeTransition == BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT) {
        return BiliPaiSharedElementPredictiveBackAnimation()
    }

    return when (routeTransition) {
        BiliPaiNavRouteTransition.CLASSIC_CARD -> resolveClassicCardPredictiveBackHandler(
            style = style,
            exitDirection = exitDirection,
        )
        else -> BiliPaiDefaultPredictiveBackAnimation()
    }
}

private fun resolveClassicCardPredictiveBackHandler(
    style: BiliPaiPredictiveBackAnimationStyle,
    exitDirection: BiliPaiPredictiveBackExitDirection,
): BiliPaiPredictiveBackAnimationHandler {
    return when (style) {
        BiliPaiPredictiveBackAnimationStyle.DEFAULT,
        BiliPaiPredictiveBackAnimationStyle.SCALE ->
            BiliPaiScalePredictiveBackAnimation(exitDirection)
        BiliPaiPredictiveBackAnimationStyle.AOSP ->
            BiliPaiAospCrossActivityPredictiveBackAnimation(exitDirection)
        BiliPaiPredictiveBackAnimationStyle.CLASSIC ->
            BiliPaiClassicPredictiveBackAnimation()
    }
}