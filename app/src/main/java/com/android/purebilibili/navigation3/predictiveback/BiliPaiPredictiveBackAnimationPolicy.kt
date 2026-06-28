package com.android.purebilibili.navigation3.predictiveback

import com.android.purebilibili.navigation3.BiliPaiNavRouteTransition

internal fun resolveBiliPaiPredictiveBackAnimationHandler(
    routeTransition: BiliPaiNavRouteTransition,
    exitDirection: BiliPaiPredictiveBackExitDirection = BiliPaiPredictiveBackExitDirection.FOLLOW_GESTURE,
): BiliPaiPredictiveBackAnimationHandler {
    return when (routeTransition) {
        BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT ->
            BiliPaiSharedElementPredictiveBackAnimation()
        BiliPaiNavRouteTransition.CLASSIC_CARD ->
            BiliPaiScalePredictiveBackAnimation(exitDirection)
        else ->
            BiliPaiDefaultPredictiveBackAnimation()
    }
}