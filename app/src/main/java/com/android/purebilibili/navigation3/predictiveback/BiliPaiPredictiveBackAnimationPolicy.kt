package com.android.purebilibili.navigation3.predictiveback

import com.android.purebilibili.navigation3.BiliPaiNavRouteTransition

internal fun resolveBiliPaiPredictiveBackAnimationHandler(
    routeTransition: BiliPaiNavRouteTransition,
    predictiveBackEnabled: Boolean = true,
    // Legacy 存储值:SCALE/AOSP/CLASSIC 不再改变 handler 选择,统一走 routeTransition 分发,
    // 避免重蹈「SCALE 预览 vs 提交动画分裂」的历史问题(见 CHANGELOG 预测返回统一条目)。
    @Suppress("UNUSED_PARAMETER")
    style: BiliPaiPredictiveBackAnimationStyle = BiliPaiPredictiveBackAnimationStyle.SCALE,
    exitDirection: BiliPaiPredictiveBackExitDirection = BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT,
): BiliPaiPredictiveBackAnimationHandler {
    if (!predictiveBackEnabled) {
        return BiliPaiDisabledPredictiveBackAnimation()
    }
    if (routeTransition == BiliPaiNavRouteTransition.NO_OP_SHARED_ELEMENT) {
        return BiliPaiSharedElementPredictiveBackAnimation()
    }
    if (routeTransition == BiliPaiNavRouteTransition.SETTINGS_IOS_PUSH_POP) {
        return BiliPaiSettingsIosPredictiveBackAnimation()
    }
    return BiliPaiDefaultPredictiveBackAnimation(exitDirection = exitDirection)
}
