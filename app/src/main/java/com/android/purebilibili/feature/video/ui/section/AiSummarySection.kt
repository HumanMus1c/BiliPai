package com.android.purebilibili.feature.video.ui.section
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppHorizontalDivider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AdaptiveLoadingIndicator
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppContentCard
import com.android.purebilibili.core.ui.components.AppOutlinedButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppTextButton
import com.android.purebilibili.data.model.response.AiSummaryData
import com.android.purebilibili.feature.video.ui.VideoDetailShapes
import com.android.purebilibili.feature.video.viewmodel.AiSummaryPromptState
import com.android.purebilibili.feature.video.viewmodel.AiSummaryPromptTone
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*

/**
 * AI Video Summary Card — adaptive native card (M3 Card / Miuix Card).
 */
@Composable
fun AiSummaryCard(
    aiSummary: AiSummaryData?,
    onTimestampClick: ((Long) -> Unit)? = null,
    onCreateNoteDraftClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (!hasAiSummaryContent(aiSummary)) return

    val modelResult = requireNotNull(aiSummary?.modelResult)
    val collapsedPreview = remember(modelResult.summary, modelResult.outline) {
        modelResult.summary.takeIf { it.isNotBlank() } ?: "查看分段总结和时间点"
    }
    var expanded by remember { mutableStateOf(false) }
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow

    AppContentCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .animateContentSize(),
        containerColor = containerColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LeadingIconBadge(
                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            ) {
                AppIcon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = "AI 总结",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                AppText(
                    text = collapsedPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (expanded) 2 else 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AppIcon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (expanded) "收起" else "展开",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
                AppHorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(bottom = 12.dp),
                )

                if (modelResult.summary.isNotBlank()) {
                    AppText(
                        text = modelResult.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(
                            bottom = if (modelResult.outline.isNotEmpty()) 12.dp else 0.dp
                        ),
                    )
                }

                if (modelResult.outline.isNotEmpty()) {
                    modelResult.outline.forEach { outlineItem ->
                        OutlineItemRow(
                            title = outlineItem.title,
                            timestamp = outlineItem.timestamp,
                            onClick = { onTimestampClick?.invoke(outlineItem.timestamp * 1000L) },
                        )
                        outlineItem.partOutline.forEach { part ->
                            OutlineItemRow(
                                title = part.content,
                                timestamp = part.timestamp,
                                isSubItem = true,
                                onClick = { onTimestampClick?.invoke(part.timestamp * 1000L) },
                            )
                        }
                    }
                }

                if (onCreateNoteDraftClick != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    AppOutlinedButton(
                        onClick = onCreateNoteDraftClick,
                        modifier = Modifier.align(Alignment.End),
                    ) {
                        AppIcon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        AppText("生成笔记草稿")
                    }
                }
            }
        }
    }
}

@Composable
fun AiSummaryPromptCard(
    promptState: AiSummaryPromptState,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val containerColor = when (promptState.tone) {
        AiSummaryPromptTone.INFO -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        AiSummaryPromptTone.MUTED -> MaterialTheme.colorScheme.surfaceContainerLow
        AiSummaryPromptTone.WARNING -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.55f)
    }
    val accentColor = when (promptState.tone) {
        AiSummaryPromptTone.INFO -> MaterialTheme.colorScheme.primary
        AiSummaryPromptTone.MUTED -> MaterialTheme.colorScheme.onSurfaceVariant
        AiSummaryPromptTone.WARNING -> MaterialTheme.colorScheme.error
    }

    AppContentCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        containerColor = containerColor,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LeadingIconBadge(containerColor = accentColor.copy(alpha = 0.12f)) {
                if (promptState.tone == AiSummaryPromptTone.INFO) {
                    AdaptiveLoadingIndicator(
                        size = 16.dp,
                        strokeWidth = 2.dp,
                        color = accentColor,
                    )
                } else {
                    AppIcon(
                        imageVector = if (promptState.tone == AiSummaryPromptTone.WARNING) {
                            Icons.Outlined.ErrorOutline
                        } else {
                            Icons.Outlined.Info
                        },
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = promptState.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(2.dp))
                AppText(
                    text = promptState.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (!promptState.actionLabel.isNullOrBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.height(8.dp))
            AppTextButton(
                onClick = onActionClick,
                modifier = Modifier.align(Alignment.End),
            ) {
                AppText(promptState.actionLabel)
            }
        }
    }
}

@Composable
private fun LeadingIconBadge(
    containerColor: Color,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(VideoDetailShapes.leadingIcon())
            .background(containerColor),
        contentAlignment = Alignment.Center,
        content = { content() },
    )
}

/** 主条目圆点 + 间距；子条目与标题列左缘对齐。 */
internal val OutlineBulletSlotWidth = 18.dp
/**
 * 时间戳列固定宽。芯片必须 [fillMaxWidth]，否则比例数字会让时钟图标左右参差
 * （右缘对齐时左缘仍不齐）。
 */
internal val OutlineTimestampColumnWidth = 76.dp

@Composable
private fun OutlineItemRow(
    title: String,
    timestamp: Long,
    isSubItem: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // 固定前导槽：主条目标圆点，子条目留白，标题列左缘一致。
        Box(
            modifier = Modifier
                .width(OutlineBulletSlotWidth)
                .padding(top = 6.dp),
            contentAlignment = Alignment.TopStart,
        ) {
            if (!isSubItem) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                )
            }
        }

        AppText(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
        )

        // 固定列宽 + 满宽芯片 + 表内居中，时钟图标与 mm:ss 形成垂直列。
        Box(
            modifier = Modifier
                .width(OutlineTimestampColumnWidth)
                .padding(top = 2.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            AppSurface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = AppShapes.container(ContainerLevel.Tag),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    AppIcon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    AppText(
                        text = formatAiSummaryTimestamp(timestamp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFeatureSettings = "tnum",
                        ),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/** mm:ss，秒级时间点；使用等宽数字特性减少视觉漂移。 */
internal fun formatAiSummaryTimestamp(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0L)
    val m = safe / 60
    val s = safe % 60
    return "%02d:%02d".format(m, s)
}
