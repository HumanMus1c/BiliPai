package com.android.purebilibili.feature.video.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.MediaContrastPalette
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.VideoStatRow
import com.android.purebilibili.core.ui.feedContentTypography
import com.android.purebilibili.core.ui.videoCardTitleMaxLines
import com.android.purebilibili.core.ui.videoCardTitleOverflow
import com.android.purebilibili.core.ui.transition.LocalMiuixVideoCardTransitionState
import com.android.purebilibili.core.ui.transition.VideoCardSourceChromeSnapshot
import com.android.purebilibili.core.ui.transition.VideoCardSourceCoverPresentation
import com.android.purebilibili.core.ui.transition.VideoCardSourceInfoPresentation
import com.android.purebilibili.core.ui.transition.VideoCardSourceLayout
import com.android.purebilibili.core.ui.transition.VideoCardTransitionBackgroundPhase
import com.android.purebilibili.core.ui.transition.resolveVideoCardDetailChromeAlpha
import com.android.purebilibili.core.ui.transition.resolveVideoCardSourceChromeVisualFrame
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.core.theme.BiliPink
import com.android.purebilibili.data.model.response.ViewInfo
import com.android.purebilibili.feature.home.resolveHomeCardInfoSurfaceAppearance
import com.android.purebilibili.feature.home.components.cards.HorizontalVideoStatRow
import com.android.purebilibili.feature.home.components.cards.resolveVideoCardCoverOverlayTextShadow
import com.android.purebilibili.feature.home.components.cards.resolveVideoCardPrimaryStatBadgeMinWidthDp
import com.android.purebilibili.feature.home.components.cards.resolveVideoCardSecondaryStatBadgeMinWidthDp

/** Click-time card text rendered inside the Miuix flying detail entry. */
internal data class VideoDetailReturnSourceCardChromeModel(
    val title: String,
    val ownerName: String,
    val viewText: String = "",
    val danmakuText: String = "",
    val durationText: String = "",
    val followed: Boolean = false,
    val infoPresentation: VideoCardSourceInfoPresentation = VideoCardSourceInfoPresentation(),
    val coverPresentation: VideoCardSourceCoverPresentation =
        VideoCardSourceCoverPresentation(),
)

internal fun resolveVideoDetailReturnSourceCardChromeModel(
    info: ViewInfo?,
    snapshot: VideoCardSourceChromeSnapshot?,
): VideoDetailReturnSourceCardChromeModel? {
    if (snapshot != null) {
        return VideoDetailReturnSourceCardChromeModel(
            title = snapshot.title.ifBlank { info?.title.orEmpty() },
            ownerName = snapshot.ownerName.ifBlank { info?.owner?.name.orEmpty() },
            viewText = snapshot.viewText,
            danmakuText = snapshot.danmakuText,
            durationText = snapshot.durationText,
            followed = snapshot.followed,
            infoPresentation = snapshot.infoPresentation,
            coverPresentation = snapshot.coverPresentation,
        ).takeIf { it.title.isNotBlank() || it.ownerName.isNotBlank() }
    }
    if (info == null) return null
    return VideoDetailReturnSourceCardChromeModel(
        title = info.title,
        ownerName = info.owner.name,
        infoPresentation = VideoCardSourceInfoPresentation(
            publishTimeText = if (info.pubdate > 0L) {
                FormatUtils.formatPublishTime(info.pubdate)
            } else {
                ""
            },
            showStatsInInfo = false,
        ),
    )
}

internal data class VideoDetailReturnInfoSurfaceSpec(
    val useTintedSurface: Boolean,
    val containerColor: Color,
    val borderColor: Color,
    val borderWidth: Dp,
)

internal fun resolveVideoDetailReturnInfoSurfaceSpec(
    useTintedInfoSurface: Boolean,
    isDarkTheme: Boolean,
    baseContainerColor: Color,
): VideoDetailReturnInfoSurfaceSpec {
    if (!useTintedInfoSurface) {
        return VideoDetailReturnInfoSurfaceSpec(
            useTintedSurface = false,
            containerColor = baseContainerColor,
            borderColor = Color.Transparent,
            borderWidth = 0.dp,
        )
    }
    val appearance = resolveHomeCardInfoSurfaceAppearance(
        wallpaperTintEnabled = true,
        isDarkTheme = isDarkTheme,
        isDataSaverActive = false,
        hasWallpaperHazeState = false,
        hasLayerBackdrop = false,
        blurEnabled = true,
    )
    return VideoDetailReturnInfoSurfaceSpec(
        useTintedSurface = true,
        containerColor = baseContainerColor.copy(alpha = appearance.containerAlpha),
        borderColor = MediaContrastPalette.Foreground.copy(alpha = appearance.borderAlpha),
        borderWidth = AppSpacingTokens.Micro * 0.4f,
    )
}

internal fun resolveVideoDetailFlyingSourceChromeAlpha(
    morphDepthProgress: Float,
    phase: VideoCardTransitionBackgroundPhase,
    isReturnGestureInProgress: Boolean,
    sourceLayout: VideoCardSourceLayout,
): Float {
    if (phase == VideoCardTransitionBackgroundPhase.OPENING) {
        return 1f - resolveVideoCardDetailChromeAlpha(
            morphDepthProgress = morphDepthProgress,
            phase = phase,
            isReturnGestureInProgress = isReturnGestureInProgress,
        )
    }
    return resolveVideoCardSourceChromeVisualFrame(
        morphDepthProgress = morphDepthProgress,
        phase = phase,
        isReturnGestureInProgress = isReturnGestureInProgress,
        sourceLayout = sourceLayout,
    ).alpha
}

/**
 * Reconstructs the source card's information region in the same entry that owns the flying media.
 * The retained list card is layout-only until the navigation transition reaches IDLE.
 */
@Composable
internal fun BoxScope.VideoDetailReturnSourceCardChrome(
    sourceBounds: Rect?,
    sourceCoverBounds: Rect?,
    morphDepthProgressProvider: () -> Float,
    modifier: Modifier = Modifier,
    sourceLayout: VideoCardSourceLayout? = null,
    info: ViewInfo? = null,
    sourceChromeSnapshot: VideoCardSourceChromeSnapshot? = null,
    phaseProvider: () -> VideoCardTransitionBackgroundPhase = {
        VideoCardTransitionBackgroundPhase.RETURNING
    },
    isReturnGestureInProgressProvider: () -> Boolean = { true },
) {
    val model = resolveVideoDetailReturnSourceCardChromeModel(info, sourceChromeSnapshot) ?: return
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val miuixHost = LocalMiuixVideoCardTransitionState.current
    val viewportWidthPx = miuixHost.layoutWidthProvider().takeIf { it > 1f }
        ?: with(density) { configuration.screenWidthDp.dp.toPx() }
    val layout = resolveVideoDetailReturnSourceCardLayout(
        viewportWidthPx = viewportWidthPx,
        sourceBounds = sourceBounds,
        sourceCoverBounds = sourceCoverBounds,
        sourceLayout = sourceLayout ?: miuixHost.sourceLayout,
    )
    if (!layout.canRender) return

    val cardWidth = with(density) { layout.cardWidthPx.toDp() }
    val cardHeight = with(density) { layout.cardHeightPx.toDp() }
    val cardAnchorX = with(density) { layout.cardAnchorXInViewportPx.toDp() }
    val cardAnchorY = with(density) { layout.cardAnchorYInViewportPx.toDp() }
    val infoWidth = with(density) { layout.infoWidthPx.toDp() }
    val infoHeight = with(density) { layout.infoHeightPx.toDp() }
    val infoAnchorX = with(density) { layout.infoAnchorXInViewportPx.toDp() }
    val infoAnchorY = with(density) { layout.infoAnchorYInViewportPx.toDp() }
    val inverseScale = 1f / layout.sourceScale

    fun Modifier.landingLayer(): Modifier = graphicsLayer {
        val phase = phaseProvider()
        val isReturnGestureInProgress = isReturnGestureInProgressProvider()
        val morphDepthProgress = morphDepthProgressProvider()
        val frame = resolveVideoCardSourceChromeVisualFrame(
            morphDepthProgress = morphDepthProgress,
            phase = phase,
            isReturnGestureInProgress = isReturnGestureInProgress,
            sourceLayout = layout.layout,
        )
        scaleX = inverseScale * frame.layoutScaleMultiplier
        scaleY = inverseScale * frame.layoutScaleMultiplier
        transformOrigin = TransformOrigin(0f, 0f)
        alpha = resolveVideoDetailFlyingSourceChromeAlpha(
            morphDepthProgress = morphDepthProgress,
            phase = phase,
            isReturnGestureInProgress = isReturnGestureInProgress,
            sourceLayout = layout.layout,
        )
    }

    val baseContainer = AppSurfaceTokens.cardContainer()
    val isDarkTheme = AppSurfaceTokens.chromeBackground().luminance() < 0.5f
    val surface = remember(
        model.infoPresentation.useTintedInfoSurface,
        isDarkTheme,
        baseContainer,
    ) {
        resolveVideoDetailReturnInfoSurfaceSpec(
            useTintedInfoSurface = model.infoPresentation.useTintedInfoSurface,
            isDarkTheme = isDarkTheme,
            baseContainerColor = baseContainer,
        )
    }

    fun Modifier.infoSurface(shape: Shape): Modifier {
        return clip(shape)
            .background(surface.containerColor, shape)
            .then(
                if (surface.useTintedSurface) {
                    Modifier.border(surface.borderWidth, surface.borderColor, shape)
                } else {
                    Modifier
                },
            )
    }

    when (layout.layout) {
        VideoCardSourceLayout.SIDE_BY_SIDE -> {
            Box(
                modifier = modifier
                    .zIndex(-1f)
                    .align(Alignment.TopStart)
                    .offset(x = cardAnchorX, y = cardAnchorY)
                    .width(cardWidth)
                    .height(cardHeight)
                    .landingLayer()
                    .background(baseContainer),
            )
            Box(
                modifier = modifier
                    .zIndex(1f)
                    .align(Alignment.TopStart)
                    .offset(x = infoAnchorX, y = cardAnchorY)
                    .width(infoWidth)
                    .height(cardHeight)
                    .landingLayer()
                    .infoSurface(AppShapes.container(ContainerLevel.Field))
                    .padding(
                        start = AppSpacingTokens.Medium,
                        end = AppSpacingTokens.Small,
                        top = AppSpacingTokens.Small,
                        // The measured source height already contains the horizontal card's
                        // outer inset. Reserving another bottom inset here shortens the flying
                        // info column and clips the statistics row at the landing boundary.
                        bottom = AppSpacingTokens.None,
                    ),
                contentAlignment = Alignment.CenterStart,
            ) {
                LandingInfoContent(
                    model = model,
                    sourceLayout = layout.layout,
                    modifier = Modifier,
                    constrainedHeight = (cardHeight - AppSpacingTokens.Small)
                        .coerceAtLeast(AppSpacingTokens.None),
                )
            }
        }

        VideoCardSourceLayout.STACKED -> Box(
            modifier = modifier
                .zIndex(1f)
                .align(Alignment.TopStart)
                .offset(x = infoAnchorX, y = infoAnchorY)
                .width(infoWidth)
                .height(infoHeight)
                .landingLayer()
                .infoSurface(AppShapes.bottomRounded(AppSpacingTokens.Small))
                .padding(
                    horizontal = AppSpacingTokens.Small + AppSpacingTokens.Micro,
                    vertical = AppSpacingTokens.Small,
                ),
        ) {
            LandingInfoContent(
                model = model,
                sourceLayout = layout.layout,
            )
        }

        VideoCardSourceLayout.COVER_ONLY -> Unit
    }
}

private fun VideoCardSourceCoverPresentation.hasVisibleChrome(): Boolean =
    showGradientMask ||
        showStatsOnCover ||
        showDurationOnCover ||
        showDurationAsStat ||
        premiumBadgeText.isNotBlank() ||
        showHistoryProgressBar

/**
 * Rebuilds only the click-time cover chrome beside the resident cover/player layer.
 * The caller owns the media geometry so these pixels travel with the same cover crop.
 */
@Composable
internal fun VideoDetailReturnCoverChrome(
    sourceChromeSnapshot: VideoCardSourceChromeSnapshot?,
    sourceScale: Float,
    modifier: Modifier = Modifier,
) {
    val snapshot = sourceChromeSnapshot ?: return
    if (!snapshot.coverPresentation.hasVisibleChrome()) return
    val model = resolveVideoDetailReturnSourceCardChromeModel(
        info = null,
        snapshot = snapshot,
    ) ?: return
    val baseDensity = LocalDensity.current
    val densityScale = resolveVideoDetailReturnCoverChromeDensityScale(sourceScale)
    val compensatedDensity = remember(
        baseDensity.density,
        baseDensity.fontScale,
        densityScale,
    ) {
        Density(
            density = baseDensity.density * densityScale,
            fontScale = baseDensity.fontScale,
        )
    }
    Box(modifier = modifier) {
        CompositionLocalProvider(LocalDensity provides compensatedDensity) {
            Box(modifier = Modifier.fillMaxSize()) {
                LandingCoverChrome(model = model)
            }
        }
    }
}

/** Inverse source scaling keeps cover text and icons at their stationary-card size. */
internal fun resolveVideoDetailReturnCoverChromeDensityScale(sourceScale: Float): Float =
    1f / sourceScale.coerceIn(0.01f, 1f)

@Composable
private fun BoxScope.LandingCoverChrome(
    model: VideoDetailReturnSourceCardChromeModel,
) {
    val presentation = model.coverPresentation
    val overlayStyle = remember { TextStyle(shadow = resolveVideoCardCoverOverlayTextShadow()) }

    if (presentation.showGradientMask) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(AppSpacingTokens.TripleExtraLarge + AppSpacingTokens.Small)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            MediaContrastPalette.Scrim.copy(alpha = 0.3f),
                            MediaContrastPalette.Scrim.copy(alpha = 0.78f),
                        ),
                    ),
                ),
        )
    }

    if (presentation.premiumBadgeText.isNotBlank()) {
        AppText(
            text = presentation.premiumBadgeText,
            color = MediaContrastPalette.Foreground,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(AppSpacingTokens.Small)
                .clip(AppShapes.container(ContainerLevel.Tag))
                .background(BiliPink.copy(alpha = 0.88f))
                .padding(
                    horizontal = AppSpacingTokens.ExtraSmall,
                    vertical = AppSpacingTokens.Micro,
                ),
        )
    }

    val progressBottomPadding = if (presentation.showHistoryProgressBar) {
        AppSpacingTokens.ExtraSmall
    } else {
        AppSpacingTokens.None
    }
    if (presentation.showHistoryProgressBar) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(2.dp)
                .background(MediaContrastPalette.Foreground.copy(alpha = 0.24f)),
        )
        if (presentation.historyProgressFraction > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth(presentation.historyProgressFraction.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }

    if (presentation.showStatsOnCover) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = AppSpacingTokens.Small,
                    end = AppSpacingTokens.Small,
                    bottom = AppSpacingTokens.ExtraSmall + progressBottomPadding,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        end = if (presentation.showDurationOnCover) {
                            AppSpacingTokens.TripleExtraLarge
                        } else {
                            AppSpacingTokens.None
                        },
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro,
                ),
            ) {
                LandingCoverStat(
                    icon = Icons.Outlined.PlayCircle,
                    text = model.viewText,
                    minWidth = resolveVideoCardPrimaryStatBadgeMinWidthDp(model.viewText).dp,
                    useGlass = presentation.useGlassStats,
                    textStyle = overlayStyle,
                )
                if (presentation.showSecondaryStatOnCover && model.danmakuText.isNotBlank()) {
                    LandingCoverStat(
                        icon = Icons.Outlined.Subtitles,
                        text = model.danmakuText,
                        minWidth = resolveVideoCardSecondaryStatBadgeMinWidthDp(
                            model.danmakuText,
                        ).dp,
                        useGlass = presentation.useGlassStats,
                        textStyle = overlayStyle,
                    )
                }
                if (presentation.showOnlineCountOnCover) {
                    LandingCoverStat(
                        icon = Icons.Outlined.Visibility,
                        text = presentation.onlineCountText,
                        useGlass = presentation.useGlassStats,
                        textStyle = overlayStyle,
                    )
                }
                if (presentation.showDurationAsStat && model.durationText.isNotBlank()) {
                    LandingCoverStat(
                        icon = Icons.Outlined.Alarm,
                        text = model.durationText,
                        useGlass = presentation.useGlassStats,
                        textStyle = overlayStyle,
                    )
                }
            }
            if (presentation.showDurationOnCover && model.durationText.isNotBlank()) {
                AppText(
                    text = model.durationText,
                    color = MediaContrastPalette.Foreground,
                    style = MaterialTheme.typography.labelSmall.merge(overlayStyle),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.BottomEnd),
                    maxLines = 1,
                )
            }
        }
    } else if (presentation.showDurationOnCover && model.durationText.isNotBlank()) {
        AppText(
            text = model.durationText,
            color = MediaContrastPalette.Foreground,
            style = MaterialTheme.typography.labelSmall.merge(overlayStyle),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = AppSpacingTokens.Small + AppSpacingTokens.Micro,
                    bottom = AppSpacingTokens.Small + progressBottomPadding,
                ),
        )
    }
}

@Composable
private fun LandingCoverStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    useGlass: Boolean,
    textStyle: TextStyle,
    modifier: Modifier = Modifier,
    minWidth: Dp = 0.dp,
) {
    if (text.isBlank()) return
    val pillShape = AppShapes.container(ContainerLevel.Pill)
    val pillDecoration = if (useGlass) {
        Modifier
            .clip(pillShape)
            .background(MediaContrastPalette.Scrim.copy(alpha = 0.46f))
            .border(
                width = AppSpacingTokens.Micro * 0.4f,
                color = MediaContrastPalette.Foreground.copy(alpha = 0.22f),
                shape = pillShape,
            )
            .padding(
                horizontal = AppSpacingTokens.ExtraSmall + AppSpacingTokens.Micro,
                vertical = AppSpacingTokens.ExtraSmall - AppSpacingTokens.Micro / 2,
            )
    } else {
        Modifier
    }
    Row(
        modifier = modifier
            .widthIn(min = minWidth)
            .then(pillDecoration),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Micro),
    ) {
        AppIcon(
            imageVector = icon,
            contentDescription = null,
            tint = MediaContrastPalette.Foreground.copy(alpha = 0.92f),
            modifier = Modifier.size(AppSpacingTokens.Small + AppSpacingTokens.Micro),
        )
        AppText(
            text = text,
            color = MediaContrastPalette.Foreground.copy(alpha = 0.94f),
            style = MaterialTheme.typography.labelSmall.merge(textStyle),
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Visible,
        )
    }
}

@Composable
private fun BoxScope.LandingInfoContent(
    model: VideoDetailReturnSourceCardChromeModel,
    sourceLayout: VideoCardSourceLayout,
    modifier: Modifier = Modifier,
    constrainedHeight: Dp? = null,
) {
    Column(
        modifier = (if (constrainedHeight == null) {
            modifier.fillMaxSize()
        } else {
            modifier.fillMaxWidth().height(constrainedHeight)
        }).padding(
            end = if (model.infoPresentation.showOverflowMenu) {
                AppSpacingTokens.Large
            } else {
                AppSpacingTokens.None
            },
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
    ) {
        LandingInfoTexts(model = model, sourceLayout = sourceLayout)
    }
    if (model.infoPresentation.showOverflowMenu) {
        AppText(
            text = "⋮",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = AppSpacingTokens.ExtraSmall,
                    bottom = AppSpacingTokens.ExtraSmall,
                ),
        )
    }
}

@Composable
private fun LandingInfoTexts(
    model: VideoDetailReturnSourceCardChromeModel,
    sourceLayout: VideoCardSourceLayout,
) {
    val contentTypography = feedContentTypography()
    AppText(
        text = model.title,
        modifier = Modifier.fillMaxWidth(),
        style = contentTypography.title,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Medium,
        maxLines = if (sourceLayout == VideoCardSourceLayout.SIDE_BY_SIDE) {
            2
        } else {
            videoCardTitleMaxLines()
        },
        minLines = 1,
        overflow = if (sourceLayout == VideoCardSourceLayout.SIDE_BY_SIDE) {
            TextOverflow.Ellipsis
        } else {
            videoCardTitleOverflow()
        },
    )

    if (sourceLayout == VideoCardSourceLayout.SIDE_BY_SIDE) {
        LandingSideBySideMetadata(model)
        if (model.infoPresentation.showStatsInInfo) {
            // Match the source horizontal card's 13dp icons. The generic row uses 16dp
            // icons, which can extend below the measured source bounds during landing.
            HorizontalVideoStatRow(
                playText = model.viewText,
                danmakuText = model.danmakuText,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        return
    }

    if (model.infoPresentation.showStatsInInfo) {
        VideoStatRow(
            playText = model.viewText,
            danmakuText = model.danmakuText,
            modifier = Modifier.fillMaxWidth(),
        )
    }
    LandingOwnerMetadata(model, Modifier.fillMaxWidth())
    val footer = buildList {
        if (model.infoPresentation.showDurationInInfo && model.durationText.isNotBlank()) {
            add(model.durationText)
        }
        if (model.infoPresentation.publishTimeText.isNotBlank()) {
            add(model.infoPresentation.publishTimeText)
        }
    }.joinToString("  ·  ")
    if (footer.isNotBlank()) {
        LandingPublishMetadata(footer, Modifier.fillMaxWidth())
    }
}

@Composable
private fun LandingSideBySideMetadata(model: VideoDetailReturnSourceCardChromeModel) {
    val publishTime = model.infoPresentation.publishTimeText
    val hasOwner = model.ownerName.isNotBlank() || model.followed
    if (publishTime.isBlank() && !hasOwner) return

    if (model.infoPresentation.ownerBeforePublish) {
        if (hasOwner) LandingOwnerMetadata(model, Modifier.fillMaxWidth())
        if (publishTime.isNotBlank()) {
            LandingPublishMetadata(publishTime, Modifier.fillMaxWidth())
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small),
        ) {
            if (publishTime.isNotBlank()) {
                LandingPublishMetadata(
                    text = publishTime,
                    modifier = Modifier.weight(1f, fill = false),
                )
            }
            if (hasOwner) {
                LandingOwnerMetadata(model, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun LandingPublishMetadata(
    text: String,
    modifier: Modifier = Modifier,
) {
    AppText(
        text = text,
        modifier = modifier,
        style = feedContentTypography().statistic,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun LandingOwnerMetadata(
    model: VideoDetailReturnSourceCardChromeModel,
    modifier: Modifier = Modifier,
) {
    if (model.ownerName.isBlank() && !model.followed) return
    val contentTypography = feedContentTypography()
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.ExtraSmall),
    ) {
        if (model.ownerName.isNotBlank()) {
            AppText(
                text = model.ownerName,
                style = contentTypography.author,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        if (model.followed) {
            AppText(
                text = "已关注",
                style = contentTypography.coverBadge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}
