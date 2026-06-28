package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation3.predictiveback.BiliPaiDefaultPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.BiliPaiScalePredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.BiliPaiSharedElementPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.resolveBiliPaiPredictiveBackAnimationHandler
import kotlin.test.Test
import kotlin.test.assertTrue

class BiliPaiPredictiveBackAnimationPolicyTest {

    @Test
    fun sharedElementRoute_usesSharedElementHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT
        )
        assertTrue(handler is BiliPaiSharedElementPredictiveBackAnimation)
    }

    @Test
    fun classicCardRoute_usesScaleHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.CLASSIC_CARD
        )
        assertTrue(handler is BiliPaiScalePredictiveBackAnimation)
    }

    @Test
    fun fallbackRoute_usesDefaultHandler() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.FALLBACK
        )
        assertTrue(handler is BiliPaiDefaultPredictiveBackAnimation)
    }
}