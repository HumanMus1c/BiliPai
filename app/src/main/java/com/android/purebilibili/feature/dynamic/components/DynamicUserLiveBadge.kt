package com.android.purebilibili.feature.dynamic.components

import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.android.purebilibili.feature.dynamic.resolveDynamicUserLiveBadgeLabel

@Composable
fun DynamicUserLiveBadge(
    modifier: Modifier = Modifier
) {
    AppText(
        text = resolveDynamicUserLiveBadgeLabel(),
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        softWrap = false
    )
}
