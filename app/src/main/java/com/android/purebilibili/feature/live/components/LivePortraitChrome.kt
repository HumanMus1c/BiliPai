package com.android.purebilibili.feature.live.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppModalBottomSheet
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppIconButtonDefaults
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppWindowAction
import com.android.purebilibili.core.ui.rememberAppCommentIcon
import com.android.purebilibili.feature.live.LiveDanmakuItem
import com.android.purebilibili.feature.live.LiveStatusPalette

/** A small, non-scrolling preview. Tap opens history, so it never competes with video drags. */
@Composable
internal fun LivePortraitChatPreview(
    messages: List<LiveDanmakuItem>,
    maxMessages: Int,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(AppShapes.container(ContainerLevel.Card))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        LiveStatusPalette.MediaScrim.copy(alpha = 0.72f),
                        LiveStatusPalette.MediaScrim.copy(alpha = 0.56f),
                    )
                )
            )
            .clickable(role = Role.Button, onClickLabel = "展开完整聊天", onClick = onOpenHistory)
            .heightIn(min = 48.dp)
            .padding(AppSpacingTokens.Small),
        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
    ) {
        if (messages.isEmpty()) {
            AppText(
                text = "暂无消息，点击查看聊天",
                color = LiveStatusPalette.MediaContent,
                style = MaterialTheme.typography.bodySmall,
            )
        } else {
            messages.takeLast(maxMessages).forEach { message ->
                AppText(
                    text = buildString {
                        append(message.uname.ifBlank { "直播观众" })
                        append("：")
                        append(message.text.ifBlank { "[表情]" })
                    },
                    color = LiveStatusPalette.MediaContent,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** App primitives dispatch to Miuix/MD3; media contrast remains independent of page theme. */
@Composable
internal fun LivePortraitBottomBar(
    chatVisible: Boolean,
    onOpenSend: () -> Unit,
    onToggleChat: () -> Unit,
    onOpenMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mediaColors = AppIconButtonDefaults.colors(
        containerColor = LiveStatusPalette.MediaScrim.copy(alpha = 0.56f),
        contentColor = LiveStatusPalette.MediaContent,
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppSurface(
            onClick = onOpenSend,
            shape = AppShapes.container(ContainerLevel.Pill),
            color = LiveStatusPalette.MediaScrim.copy(alpha = 0.56f),
            contentColor = LiveStatusPalette.MediaContent,
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .semantics { contentDescription = "发送弹幕" },
        ) {
            Box(
                modifier = Modifier.padding(horizontal = AppSpacingTokens.Large),
                contentAlignment = Alignment.CenterStart,
            ) {
                AppText(
                    text = "说点什么…",
                    color = LiveStatusPalette.MediaContent,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        AppIconButton(
            onClick = onToggleChat,
            colors = mediaColors,
            modifier = Modifier
                .semantics { stateDescription = if (chatVisible) "聊天已显示" else "聊天已隐藏" },
        ) {
            AppIcon(
                imageVector = rememberAppCommentIcon(),
                contentDescription = if (chatVisible) "隐藏聊天" else "显示聊天",
                tint = LiveStatusPalette.MediaContent.copy(alpha = if (chatVisible) 1f else 0.65f),
            )
        }
        AppIconButton(onClick = onOpenMore, colors = mediaColors) {
            AppIcon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = "更多直播操作",
                tint = LiveStatusPalette.MediaContent,
            )
        }
    }
}

/** Reuses the theme-aware sheet, shape, surface and typography contracts for both UI styles. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LivePortraitMoreSheet(
    actions: List<AppWindowAction>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
            contentPadding = PaddingValues(
                horizontal = AppSpacingTokens.Large,
                vertical = AppSpacingTokens.Small,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
        ) {
            item {
                AppText(
                    text = "直播操作",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = AppSpacingTokens.Small),
                )
                AppText(
                    text = "轻点画面清屏或恢复，长按打开操作",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            items(actions, key = { it.label }) { action ->
                AppSurface(
                    onClick = {
                        onDismiss()
                        action.onClick?.invoke()
                    },
                    enabled = action.enabled,
                    shape = AppShapes.container(ContainerLevel.Card),
                    color = AppSurfaceTokens.cardContainer(),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AppText(
                        text = action.label,
                        color = if (action.enabled) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .padding(AppSpacingTokens.Medium),
                    )
                }
            }
        }
    }
}
