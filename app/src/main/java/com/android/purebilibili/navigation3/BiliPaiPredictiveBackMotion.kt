package com.android.purebilibili.navigation3

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.navigation3.scene.Scene
import androidx.navigation3.ui.defaultPredictivePopTransitionSpec as navigation3DefaultPredictivePopTransitionSpec
import androidx.navigationevent.NavigationEvent

/**
 * BiliPai 默认预测返回效果：开启时使用 Navigation3 原生 MIUIX/平台同步路径，
 * 共享元素回程则由 entry metadata 返回 NO_OP，避免路由层和元素层抢动画。
 */
internal object BiliPaiPredictiveBackMotion {
    fun <T : Any> defaultPredictivePopTransitionSpec():
        AnimatedContentTransitionScope<Scene<T>>.(@NavigationEvent.SwipeEdge Int) -> ContentTransform {
        return navigation3DefaultPredictivePopTransitionSpec()
    }
}
