package com.android.purebilibili.feature.personal

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.skeleton.ContentSkeletonBlock
import com.android.purebilibili.core.ui.skeleton.rememberContentSkeletonBlockColor
import com.android.purebilibili.core.ui.skeleton.rememberContentSkeletonPulse

/**
 * Shared visual frame for personal-list media rows.
 *
 * The frame owns geometry and selection chrome only. History, favorites and
 * watch-later cards provide their own badges, metadata and actions through slots.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PersonalMediaCardFrame(
    headlineContent: @Composable () -> Unit,
    coverContent: @Composable BoxScope.() -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverModifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    coverAspectRatio: Float = PERSONAL_LIST_HORIZONTAL_COVER_ASPECT_RATIO,
    coverWidth: Dp? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    overlineContent: (@Composable () -> Unit)? = null,
    coverOverlayContent: (@Composable BoxScope.() -> Unit)? = null,
    trailingContent: (@Composable RowScope.() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
) {
    val fontScale = LocalDensity.current.fontScale
    val minimumHeight = resolvePersonalMediaCardMinHeightDp(fontScale).dp
    val cardShape = AppShapes.container(ContainerLevel.Card)

    AppSurface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minimumHeight)
            .clip(cardShape)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick,
            ),
        shape = cardShape,
        color = AppSurfaceTokens.cardContainer(),
    ) {
        Box {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) {
                val resolvedCoverWidth = coverWidth ?: (minimumHeight * coverAspectRatio)
                Box(
                    modifier = coverModifier
                        .width(resolvedCoverWidth)
                        .aspectRatio(coverAspectRatio)
                        .clip(cardShape),
                ) {
                    coverContent()
                    coverOverlayContent?.invoke(this)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(AppSpacingTokens.Medium),
                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
                ) {
                    overlineContent?.invoke()
                    headlineContent()
                    supportingContent?.invoke()
                }

                trailingContent?.let { content ->
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = AppSpacingTokens.Small),
                        verticalAlignment = Alignment.CenterVertically,
                        content = content,
                    )
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = cardShape,
                        )
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f),
                            shape = cardShape,
                        ),
                )
            }
        }
    }
}

internal object PersonalMediaCardDefaults {
    val selectionOverlayColor: Color
        @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
}

@Composable
internal fun PersonalMediaCardSkeleton(
    modifier: Modifier = Modifier,
    blockColor: Color? = null,
) {
    val pulse = if (blockColor == null) rememberContentSkeletonPulse() else 0f
    val color = blockColor ?: rememberContentSkeletonBlockColor(pulse)
    val coverHeight = PERSONAL_LIST_BASE_MIN_HEIGHT_DP.dp
    val coverWidth = coverHeight * PERSONAL_LIST_HORIZONTAL_COVER_ASPECT_RATIO
    val cardShape = AppShapes.container(ContainerLevel.Card)

    AppSurface(
        modifier = modifier
            .fillMaxWidth()
            .height(coverHeight),
        shape = cardShape,
        color = AppSurfaceTokens.cardContainer(),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            ContentSkeletonBlock(
                color = color,
                shape = cardShape,
                modifier = Modifier
                    .width(coverWidth)
                    .fillMaxHeight(),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(AppSpacingTokens.Medium),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
            ) {
                ContentSkeletonBlock(
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .height(16.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                ContentSkeletonBlock(
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(12.dp),
                )
                ContentSkeletonBlock(
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(12.dp),
                )
            }
            ContentSkeletonBlock(
                color = color,
                shape = CircleShape,
                modifier = Modifier
                    .padding(end = AppSpacingTokens.Small)
                    .size(24.dp)
                    .align(Alignment.CenterVertically),
            )
        }
    }
}
