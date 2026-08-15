package com.android.purebilibili.feature.home.components.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.feedContentTypography

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun HorizontalVideoStatRow(
    playText: String,
    danmakuText: String,
    playIcon: ImageVector,
    danmakuIcon: ImageVector,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        itemVerticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_VIDEO_STAT_ROW_SPACING_DP.dp),
        verticalArrangement = Arrangement.spacedBy(HORIZONTAL_VIDEO_STAT_WRAP_SPACING_DP.dp),
    ) {
        HorizontalVideoStatItem(icon = playIcon, text = playText)
        if (danmakuText.isNotBlank()) {
            HorizontalVideoStatItem(icon = danmakuIcon, text = danmakuText)
        }
    }
}

@Composable
internal fun HorizontalVideoStatItem(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    val contentTypography = feedContentTypography()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(HORIZONTAL_VIDEO_STAT_ICON_TEXT_GAP_DP.dp),
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(HORIZONTAL_VIDEO_STAT_ICON_SIZE_DP.dp),
        )
        AppText(
            text = text,
            style = contentTypography.statistic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Clip,
        )
    }
}
