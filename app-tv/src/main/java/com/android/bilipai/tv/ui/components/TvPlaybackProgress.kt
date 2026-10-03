package com.android.bilipai.tv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.android.bilipai.tv.ui.TvUiTokens
import com.android.purebilibili.core.player.resolveProgressFraction
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.drawMediaProgressTrack
import com.android.purebilibili.core.util.FormatUtils

/** TV input/labels around the same track geometry used by the mobile player. */
@Composable
internal fun TvPlaybackProgress(
    positionMsProvider: () -> Long,
    bufferedPositionMsProvider: () -> Long,
    durationMs: Long,
    previewPositionMs: Long?,
    canSeek: Boolean,
    onStartPreview: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    val trackColor = if (focused) colors.onPrimary else colors.onSurface
    val trackHeight = if (focused || previewPositionMs != null) TvUiTokens.focusedProgressTrackHeight
        else TvUiTokens.progressTrackHeight
    val actualTime = FormatUtils.formatDuration(positionMsProvider())
    val targetTime = previewPositionMs?.let { FormatUtils.formatDuration(it.coerceIn(0, durationMs.coerceAtLeast(0))) }
    TvAppButton(
        onClick = onStartPreview,
        enabled = canSeek && durationMs > 0,
        modifier = modifier
            .onFocusChanged { focused = it.isFocused }
            .semantics {
                if (durationMs > 0) progressBarRangeInfo = ProgressBarRangeInfo(
                    resolveProgressFraction(positionMsProvider(), durationMs), 0f..1f,
                )
                stateDescription = if (targetTime != null) "准备跳转至 $targetTime，尚未确认"
                    else if (canSeek && durationMs > 0) "播放位置 $actualTime"
                    else "当前无法调整进度"
            }
            .testTag("tv-seek"),
    ) {
        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
            Text(
                if (durationMs > 0) "播放位置 $actualTime / ${FormatUtils.formatDuration(durationMs)}"
                else "播放位置 $actualTime · 时长未知",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (durationMs > 0) {
                Spacer(Modifier.fillMaxWidth().height(AppSpacingTokens.ExtraLarge).drawBehind {
                    val actualFraction = resolveProgressFraction(positionMsProvider(), durationMs)
                    drawMediaProgressTrack(
                        progressFraction = actualFraction,
                        bufferedFraction = resolveProgressFraction(bufferedPositionMsProvider(), durationMs),
                        trackHeightPx = trackHeight.toPx(),
                        activeColor = trackColor,
                        bufferedColor = trackColor.copy(alpha = 0.5f),
                        inactiveColor = trackColor.copy(alpha = 0.22f),
                    )
                    val radius = TvUiTokens.progressThumbSize.toPx() / 2f
                    val fraction = previewPositionMs?.let { resolveProgressFraction(it, durationMs) } ?: actualFraction
                    val x = (size.width * fraction).coerceIn(radius, (size.width - radius).coerceAtLeast(radius))
                    if (previewPositionMs != null) {
                        val actualX = size.width * actualFraction
                        drawLine(colors.onSurface, Offset(actualX, size.height / 2f - radius),
                            Offset(actualX, size.height / 2f + radius), TvUiTokens.focusBorderWidth.toPx())
                        drawCircle(colors.onSurface, radius + TvUiTokens.focusBorderWidth.toPx(), Offset(x, size.height / 2f))
                    }
                    drawCircle(trackColor, radius, Offset(x, size.height / 2f))
                })
            }
            Text(
                when {
                    targetTime != null -> "跳转到 $targetTime · 确认跳转 · 返回取消"
                    canSeek && durationMs > 0 -> "左右调整 · 确认开始预览"
                    durationMs <= 0 -> "时长尚未确定，暂不能跳转"
                    else -> "当前内容暂不支持跳转"
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
