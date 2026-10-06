package com.android.purebilibili.feature.live.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.feature.live.LiveDanmakuItem
import com.android.purebilibili.feature.live.formatLiveSuperChatCountdown
import com.android.purebilibili.feature.live.resolveLiveSuperChatColor
import com.android.purebilibili.feature.live.remainingLiveSuperChatSeconds
import com.android.purebilibili.feature.live.resolveLiveSuperChatFlashEndTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

/**
 * SC 左下角非侵入式悬浮卡片（借鉴 PiliPlus fsSC 体验）
 *
 * 仅响应实时到达的新 SC（[flashFlow]）：
 * 在画面左下角以 SC 主题色展示 用户名 + 价格 + 内容 + 实时倒计时，
 * 右上角提供独立关闭按钮。不阻塞全屏画面触控与视线，按倒计时自动消失。
 */
@Composable
fun LiveSuperChatFlashOverlay(
    flashFlow: SharedFlow<LiveDanmakuItem>,
    modifier: Modifier = Modifier,
    onUserClick: (Long) -> Unit = {},
    persistUntilDismiss: Boolean = false,
    maxWidthDp: Int = 360,
) {
    var current by remember { mutableStateOf<LiveDanmakuItem?>(null) }
    var remainingSec by remember { mutableIntStateOf(0) }
    var flashEndTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(flashFlow) {
        flashFlow.collect { item ->
            val now = System.currentTimeMillis() / 1_000L
            flashEndTime = resolveLiveSuperChatFlashEndTime(item.superChatEndTime, now)
            remainingSec = remainingLiveSuperChatSeconds(flashEndTime, now)
            current = item
        }
    }

    // 倒计时与自动消失
    LaunchedEffect(current, flashEndTime, persistUntilDismiss) {
        if (current == null || persistUntilDismiss) return@LaunchedEffect
        remainingSec = remainingLiveSuperChatSeconds(flashEndTime, System.currentTimeMillis() / 1_000L)
        while (remainingSec > 0) {
            delay(1_000L)
            remainingSec = remainingLiveSuperChatSeconds(flashEndTime, System.currentTimeMillis() / 1_000L)
        }
        current = null
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomStart
    ) {
        AnimatedVisibility(
            visible = current != null && (persistUntilDismiss || remainingSec > 0),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .padding(start = 20.dp, bottom = 72.dp)
                .widthIn(min = 260.dp, max = maxWidthDp.coerceIn(260, 640).dp)
        ) {
            val item = current ?: return@AnimatedVisibility
            val accent = resolveLiveSuperChatColor(item.superChatBackgroundColor)
            val shape = RoundedCornerShape(12.dp)

            AppSurface(
                shape = shape,
                color = accent,
                modifier = Modifier
                    .clip(shape)
                    .shadow(elevation = 8.dp, shape = shape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        if (item.uid > 0L) {
                            onUserClick(item.uid)
                        }
                    }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(accent, accent.copy(alpha = 0.85f))
                                )
                            )
                            .padding(
                                horizontal = AppSpacingTokens.Medium,
                                vertical = AppSpacingTokens.Small
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            AppText(
                                text = item.uname.ifBlank { "醒目留言" },
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (item.superChatPrice.isNotBlank()) {
                                Spacer(Modifier.width(AppSpacingTokens.Small))
                                AppText(
                                    text = item.superChatPrice,
                                    color = Color.White.copy(alpha = 0.95f),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall)
                        ) {
                            AppText(
                                text = if (persistUntilDismiss) "常驻" else formatLiveSuperChatCountdown(remainingSec),
                                color = Color.White.copy(alpha = 0.80f),
                                style = MaterialTheme.typography.labelSmall
                            )
                            // 独立关闭按钮
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.24f))
                                    .clickable { current = null },
                                contentAlignment = Alignment.Center
                            ) {
                                AppIcon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "关闭",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Content row
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(accent.copy(alpha = 0.70f))
                            .padding(
                                horizontal = AppSpacingTokens.Medium,
                                vertical = AppSpacingTokens.Small
                            )
                    ) {
                        AppText(
                            text = item.text,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Medium,

                            ),
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
