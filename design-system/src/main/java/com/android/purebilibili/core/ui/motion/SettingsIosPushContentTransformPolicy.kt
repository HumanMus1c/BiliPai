package com.android.purebilibili.core.ui.motion

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset

const val SETTINGS_IOS_PUSH_DURATION_MS = 350
const val SETTINGS_IOS_PUSH_PARALLAX_FACTOR = 0.33f

fun resolveSettingsIosPushTransitionMillis(
    animationEnabled: Boolean,
    reduceMotion: Boolean,
): Int {
    if (!animationEnabled || reduceMotion) return 0
    return SETTINGS_IOS_PUSH_DURATION_MS
}

fun resolveSettingsIosPushForwardContentTransform(
    durationMillis: Int = SETTINGS_IOS_PUSH_DURATION_MS,
): ContentTransform {
    if (durationMillis <= 0) {
        return EnterTransition.None togetherWith ExitTransition.None
    }
    val spec = navigationSlideSpring(durationMillis)
    val parallaxOffset: (Int) -> Int = { width ->
        -(width * SETTINGS_IOS_PUSH_PARALLAX_FACTOR).toInt()
    }
    return slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = spec,
    ) togetherWith slideOutHorizontally(
        targetOffsetX = parallaxOffset,
        animationSpec = spec,
    )
}

fun resolveSettingsIosPushPopContentTransform(
    durationMillis: Int = SETTINGS_IOS_PUSH_DURATION_MS,
): ContentTransform {
    if (durationMillis <= 0) {
        return EnterTransition.None togetherWith ExitTransition.None
    }
    // 返回可能承接预测手势的 seek 进度，保持固定时长 tween，避免松手提交时
    // 从手势曲线突然切到 spring 造成速度跳变。
    val spec = tween<IntOffset>(durationMillis = durationMillis, easing = EaseInOut)
    val parallaxOffset: (Int) -> Int = { width ->
        -(width * SETTINGS_IOS_PUSH_PARALLAX_FACTOR).toInt()
    }
    return slideInHorizontally(
        initialOffsetX = parallaxOffset,
        animationSpec = spec,
    ) togetherWith slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = spec,
    )
}

/**
 * 设置预测式返回专用：目标页保持全屏，只横滑顶页。
 * 若对目标页再套 parallax 入场，手势 seek 时两页之间会露出 windowBackground 灰缝，并显得卡手。
 *
 * 跟手 seek 使用 LinearEasing：预测返回手势的 progress 由系统按手指位移给出，
 * 非线性曲线（如 EaseInOut）会让页面位移滞后于手指（手势 30% 时页面只移动约 10%），
 * 观感粘稠；线性映射才能 1:1 跟手。松手提交走 [resolveSettingsIosPushPopContentTransform]，
 * 从当前 seek 位置平滑收尾，不受本 spec 影响。
 */
fun resolveSettingsIosPredictivePopContentTransform(
    durationMillis: Int = SETTINGS_IOS_PUSH_DURATION_MS,
): ContentTransform {
    if (durationMillis <= 0) {
        return EnterTransition.None togetherWith ExitTransition.None
    }
    return ContentTransform(
        targetContentEnter = EnterTransition.None,
        initialContentExit = slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing),
        ),
    )
}
