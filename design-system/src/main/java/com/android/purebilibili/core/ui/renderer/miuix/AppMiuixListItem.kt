package com.android.purebilibili.core.ui.renderer.miuix

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.BasicComponent

@Composable
internal fun AppMiuixListItem(
    headlineContent: @Composable () -> Unit,
    modifier: Modifier,
    overlineContent: (@Composable () -> Unit)?,
    supportingContent: (@Composable () -> Unit)?,
    leadingContent: (@Composable () -> Unit)?,
    trailingContent: (@Composable () -> Unit)?,
) = BasicComponent(
    modifier = modifier,
    startAction = leadingContent,
    endActions = trailingContent?.let { content ->
        { content() }
    },
) {
    overlineContent?.invoke()
    headlineContent()
    supportingContent?.invoke()
}
