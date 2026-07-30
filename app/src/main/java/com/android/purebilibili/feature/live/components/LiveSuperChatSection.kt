package com.android.purebilibili.feature.live.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.feature.live.resolveLiveSuperChatColor
import com.android.purebilibili.feature.live.LiveDanmakuItem
import com.android.purebilibili.feature.live.rememberLiveChromePalette

@Composable
fun LiveSuperChatSection(
    items: List<LiveDanmakuItem>,
    modifier: Modifier = Modifier
) {
    val palette = rememberLiveChromePalette()
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(AppSpacingTokens.Large),
        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium)
    ) {
        items(items, key = { "${it.superChatId}_${it.uid}_${it.text}_${it.superChatPrice}" }) { item ->
            AppSurface(
                shape = AppShapes.borderedContainer(ContainerLevel.Card),
                color = palette.surfaceElevated,
            border = androidx.compose.foundation.BorderStroke(AppSpacingTokens.Micro / 2, palette.border)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            resolveLiveSuperChatColor(item.superChatBackgroundColor).copy(alpha = 0.12f)
                        )
                        .padding(AppSpacingTokens.Large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        AppText(
                            text = item.uname.ifBlank { "醒目留言" },
                            color = palette.primaryText,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        AppText(
                            text = item.superChatPrice.ifBlank { "SC" },
                            color = palette.accentStrong,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    AppText(
                        text = item.text,
                        color = palette.primaryText,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
