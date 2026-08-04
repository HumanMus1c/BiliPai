package com.android.purebilibili.feature.live.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.feature.live.LiveDanmakuItem
import com.android.purebilibili.feature.live.resolveLiveSuperChatColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

/** SC 浮层默认展示时长（秒），WS 未提供 duration 时使用 */
private const val SUPER_CHAT_FLASH_DEFAULT_DURATION_SEC = 5L
/** SC 浮层最长展示时长（秒），防止异常数据长时间遮挡画面 */
private const val SUPER_CHAT_FLASH_MAX_DURATION_SEC = 30L

/**
 * SC 全屏大字浮层
 *
 * 仅响应实时到达的新 SC（[flashFlow]，由 ViewModel 的 EmitSuperChat 分支驱动）：
 * 在画面中央以 SC 主题色大字展示 用户名 + 价格 + 内容，按 WS 提供的 duration
 * 自动消失（点击可立即关闭）。
 */
@Composable
fun LiveSuperChatFlashOverlay(
    flashFlow: SharedFlow<LiveDanmakuItem>,
    modifier: Modifier = Modifier
) {
    var current by remember { mutableStateOf<LiveDanmakuItem?>(null) }

    LaunchedEffect(flashFlow) {
        flashFlow.collect { item ->
            current = item
        }
    }

    // 按 SC 时长自动消失
    LaunchedEffect(current) {
        val item = current ?: return@LaunchedEffect
        val durationSec = item.superChatDuration
            .takeIf { it > 0 }
            ?.toLong()
            ?: SUPER_CHAT_FLASH_DEFAULT_DURATION_SEC
        val durationMs = durationSec.coerceAtMost(SUPER_CHAT_FLASH_MAX_DURATION_SEC) * 1000L
        delay(durationMs)
        current = null
    }

    AnimatedVisibility(
        visible = current != null,
        enter = fadeIn() + scaleIn(initialScale = 0.92f),
        exit = fadeOut() + scaleOut(targetScale = 0.96f),
        modifier = modifier
    ) {
        val item = current ?: return@AnimatedVisibility
        val accent = resolveLiveSuperChatColor(item.superChatBackgroundColor)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.30f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    current = null
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .padding(horizontal = AppSpacingTokens.ExtraLarge)
            ) {
                AppSurface(
                    shape = RoundedCornerShape(AppSpacingTokens.Large),
                    color = accent.copy(alpha = 0.92f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AppSpacingTokens.Large))
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    accent,
                                    accent.copy(alpha = 0.78f)
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(
                            horizontal = AppSpacingTokens.ExtraLarge,
                            vertical = AppSpacingTokens.ExtraLarge
                        )
                    ) {
                        AppText(
                            text = item.uname.ifBlank { "醒目留言" },
                            color = Color.White.copy(alpha = 0.92f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (item.superChatPrice.isNotBlank()) {
                            Spacer(Modifier.height(AppSpacingTokens.Small))
                            AppText(
                                text = item.superChatPrice,
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(Modifier.height(AppSpacingTokens.Medium))
                        AppText(
                            text = item.text,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                lineHeight = 34.sp
                            ),
                            maxLines = 5,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
