package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation3.predictiveback.BiliPaiDisabledPredictiveBackAnimation
import com.android.purebilibili.navigation3.predictiveback.resolveBiliPaiPredictiveBackAnimationHandler
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoDetailNoAnimationPolicyTest {
    private val video = BiliPaiNavKey.VideoDetail(
        bvid = "BV1noanimation",
        sourceRoute = "home",
    )

    @Test
    fun disabledVideoDetailMotion_removesForwardAndReturnTransforms() {
        val transitions = resolveBiliPaiNavEntryRouteTransitions(
            key = video,
            cardTransitionEnabled = false,
            videoDetailTransitionsEnabled = false,
            sourceMetadata = BiliPaiNavSourceMetadata(
                sourceRoute = "home",
                cardSourceDirection = BiliPaiNavCardSourceDirection.SOURCE_LEFT,
            ),
        )

        assertEquals(BiliPaiNavRouteTransition.VIDEO_DETAIL_NO_ANIMATION, transitions.forward)
        assertEquals(BiliPaiNavRouteTransition.VIDEO_DETAIL_NO_ANIMATION, transitions.pop)
        assertEquals(BiliPaiNavRouteTransition.VIDEO_DETAIL_NO_ANIMATION, transitions.predictivePop)
    }

    @Test
    fun disabledVideoDetailMotion_removesDisplayLevelReturnTransform() {
        val transition = resolveBiliPaiNavDisplayPopRouteTransition(
            cardTransitionEnabled = false,
            videoDetailTransitionsEnabled = false,
            sourceMetadata = BiliPaiNavSourceMetadata(),
            fromKey = video,
            toKey = BiliPaiNavKey.MainHost,
        )

        assertEquals(BiliPaiNavRouteTransition.VIDEO_DETAIL_NO_ANIMATION, transition)
    }

    @Test
    fun disabledVideoDetailMotion_disablesPredictiveDecorator() {
        val handler = resolveBiliPaiPredictiveBackAnimationHandler(
            routeTransition = BiliPaiNavRouteTransition.VIDEO_DETAIL_NO_ANIMATION,
        )

        assertTrue(handler is BiliPaiDisabledPredictiveBackAnimation)
    }
}
