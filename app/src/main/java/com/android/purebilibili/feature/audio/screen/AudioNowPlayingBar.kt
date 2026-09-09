package com.android.purebilibili.feature.audio.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.QueueMusic
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.theme.LocalSettingsLiquidGlassEnabled
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.motion.rememberSystemReduceMotion
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.android.purebilibili.feature.home.components.LiquidGlassTuning
import com.android.purebilibili.feature.home.components.LocalLiquidGlassRenderConfig
import com.android.purebilibili.feature.home.components.biliPaiFloatingDockShell
import com.android.purebilibili.feature.home.components.resolveSharedBottomBarCapsuleShape
import kotlin.math.abs
import top.yukonga.miuix.kmp.blur.Backdrop as MiuixBackdrop

internal data class AudioNowPlayingBarState(
    val title: String,
    val artist: String,
    val artistAvatarUrl: String = "",
    val coverUrl: String,
    val isPlaying: Boolean,
    val playbackSpeed: Float = 1f
)

@Composable
internal fun AudioNowPlayingBar(
    state: AudioNowPlayingBarState,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onDismiss: () -> Unit,
    glassEnabled: Boolean = LocalSettingsLiquidGlassEnabled.current,
    miuixBackdrop: MiuixBackdrop? = null,
    liquidGlassTuning: LiquidGlassTuning = LocalLiquidGlassRenderConfig.current.tuning,
    liftAboveBottomBar: Boolean = true,
    consumeNavigationBarsPadding: Boolean = true,
    modifier: Modifier = Modifier
) {
    val chrome = resolveMusicPlayerChromeSpec(
        uiStyle = LocalAppUiStyle.current,
        glassEnabled = glassEnabled
    )
    val shape = resolveSharedBottomBarCapsuleShape()
    val containerColor = AppSurfaceTokens.surfaceContainer()
    val glassActive = glassEnabled && miuixBackdrop != null
    val reduceMotion = rememberSystemReduceMotion()
    val coverRotationDegrees = rememberMusicArtworkRotationDegrees(
        active = shouldRotateMusicArtwork(
            isPlaying = state.isPlaying,
            reduceMotion = reduceMotion
        ),
        contentKey = state.coverUrl,
        playbackSpeed = state.playbackSpeed
    )
    AppSurface(
        modifier = modifier
            .fillMaxWidth()
            .then(if (consumeNavigationBarsPadding) Modifier.navigationBarsPadding() else Modifier)
            .padding(
                start = chrome.horizontalPaddingDp.dp,
                end = chrome.horizontalPaddingDp.dp,
                bottom = when {
                    liftAboveBottomBar -> 72.dp
                    !glassActive && chrome.uiStyle == com.android.purebilibili.core.theme.AppUiStyle.MATERIAL3 -> 16.dp
                    else -> 8.dp
                }
            )
            .biliPaiFloatingDockShell(
                backdrop = miuixBackdrop,
                containerColor = containerColor,
                pressProgress = 0f,
                shape = shape,
                enabled = glassActive,
                liquidGlassTuning = liquidGlassTuning,
            )
            .clickable(onClick = onExpand)
            .audioNowPlayingSkipGesture(
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious
            ),
        shape = shape,
        color = if (glassActive) Color.Transparent else containerColor,
        tonalElevation = if (chrome.uiStyle == com.android.purebilibili.core.theme.AppUiStyle.MATERIAL3 && !glassActive) {
            3.dp
        } else {
            0.dp
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = state.coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .graphicsLayer { rotationZ = coverRotationDegrees() }
                    .clip(if (chrome.coverShapeIsCircle) CircleShape else AppShapes.container(ContainerLevel.Field)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                AppText(
                    text = state.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (state.artistAvatarUrl.isNotBlank()) {
                        AsyncImage(
                            model = state.artistAvatarUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    AppText(
                        text = state.artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            AppIconButton(onClick = onPlayPause, modifier = Modifier.size(44.dp)) {
                AppIcon(
                    imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "暂停" else "播放"
                )
            }
            AppIconButton(onClick = onExpand, modifier = Modifier.size(44.dp)) {
                AppIcon(Icons.Outlined.QueueMusic, contentDescription = "正在播放")
            }
            AppIconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                AppIcon(Icons.Filled.Close, contentDescription = "关闭听视频条")
            }
        }
    }
}

private fun Modifier.audioNowPlayingSkipGesture(
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit
): Modifier = pointerInput(onSkipNext, onSkipPrevious) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragEnd = {
            if (abs(totalDrag) > 64f) {
                if (totalDrag < 0f) onSkipNext() else onSkipPrevious()
            }
            totalDrag = 0f
        },
        onDragCancel = { totalDrag = 0f }
    ) { _, dragAmount ->
        totalDrag += dragAmount
    }
}
