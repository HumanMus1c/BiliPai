package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.ui.components.AppAdaptiveSplitLayout
import com.android.purebilibili.core.util.LocalWindowSizeClass

@Composable
fun AppSplitLayout(
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit,
    primaryRatio: Float = 0.65f,
    modifier: Modifier = Modifier,
) = AppAdaptiveSplitLayout(
    useSplitLayout = LocalWindowSizeClass.current.shouldUseSplitLayout,
    primaryContent = primaryContent,
    secondaryContent = secondaryContent,
    primaryRatio = primaryRatio,
    modifier = modifier,
)
