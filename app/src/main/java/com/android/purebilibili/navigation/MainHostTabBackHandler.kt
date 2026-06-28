package com.android.purebilibili.navigation

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

/**
 * 主页底栏 Tab 二级返回：栈顶为 [com.android.purebilibili.navigation3.BiliPaiNavKey.MainHost]
 * 且当前不在首页 Tab 时，边缘返回手势回到首页 Tab（而非直接退出应用）。
 *
 * 使用 [NavigationBackHandler] 替代 [androidx.activity.compose.BackHandler]，
 * 以保留系统预测性返回手势预览。
 */
@Composable
internal fun MainHostTabBackHandler(
    enabled: Boolean,
    onReturnToHomeTab: () -> Unit,
) {
    val navEventState = rememberNavigationEventState(NavigationEventInfo.None)
    NavigationBackHandler(
        state = navEventState,
        isBackEnabled = enabled,
        onBackCompleted = onReturnToHomeTab,
    )
}