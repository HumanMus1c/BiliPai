package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AppAdaptiveSplitLayout(
    useSplitLayout: Boolean,
    primaryContent: @Composable () -> Unit,
    secondaryContent: @Composable () -> Unit,
    primaryRatio: Float = 0.65f,
    modifier: Modifier = Modifier,
) {
    val safePrimaryRatio = primaryRatio.coerceIn(0.05f, 0.95f)
    if (!useSplitLayout) {
        Box(modifier = modifier.fillMaxSize()) { primaryContent() }
        return
    }

    Row(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(safePrimaryRatio),
        ) {
            primaryContent()
        }
        Spacer(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f - safePrimaryRatio),
        ) {
            secondaryContent()
        }
    }
}
