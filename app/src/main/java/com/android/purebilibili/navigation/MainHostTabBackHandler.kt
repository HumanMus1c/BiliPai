package com.android.purebilibili.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.android.purebilibili.core.ui.LocalPredictiveBackGestureEnabled

/**
 * 主页底栏 Tab 二级返回：栈顶为 [com.android.purebilibili.navigation3.BiliPaiNavKey.MainHost]
 * 且当前不在首页 Tab 时，边缘返回手势回到首页 Tab（而非直接退出应用）。
 *
 * 使用 [NavigationBackHandler] 替代 [androidx.activity.compose.BackHandler]。
 * 跟手预览受全局「预测性返回手势」开关控制。
 *
 * 预测返回期间直接用系统进度驱动 [MainBottomPagerState]；不能在松手后才让 Pager
 * 补播整段动画，否则页面既不跟手又会显得粘稠。
 */
@Composable
internal fun MainHostTabBackHandler(
    enabled: Boolean,
    onPredictiveProgress: suspend (Float) -> Unit,
    onPredictiveCancelled: () -> Unit,
    onPredictiveCompleted: () -> Boolean,
    onReturnToHomeTab: () -> Unit,
) {
    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)
    val predictiveBackGestureEnabled = LocalPredictiveBackGestureEnabled.current
    val predictiveProgress = if (predictiveBackGestureEnabled) {
        (navEventState.transitionState as? NavigationEventTransitionState.InProgress)
            ?.latestEvent
            ?.progress
    } else {
        null
    }

    LaunchedEffect(enabled, predictiveProgress) {
        if (enabled && predictiveProgress != null) {
            onPredictiveProgress(predictiveProgress)
        }
    }
    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = enabled,
        reportPredictiveProgress = predictiveBackGestureEnabled,
        onBackCancelled = { commitTransition ->
            onPredictiveCancelled()
            commitTransition()
        },
        onBackCompleted = { commitTransition ->
            if (!onPredictiveCompleted()) {
                onReturnToHomeTab()
            }
            commitTransition()
        },
    )
}
