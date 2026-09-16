package com.android.purebilibili.feature.video.screen

import android.content.res.Configuration
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope.OverlayClip
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.LocalAnimatedVisibilityScope
import com.android.purebilibili.core.ui.LocalSharedTransitionScope
import com.android.purebilibili.core.ui.motion.AppMotionTokens
import com.android.purebilibili.core.ui.transition.LocalVideoCardSharedElementSourceRoute
import com.android.purebilibili.core.ui.transition.resolveVideoSharedTransitionSourceCornerDp
import com.android.purebilibili.core.ui.transition.videoCoverSharedElementKey
import com.android.purebilibili.data.model.response.BgmInfo
import com.android.purebilibili.data.model.response.ViewPoint
import com.android.purebilibili.feature.video.state.VideoPlayerState
import com.android.purebilibili.feature.video.ui.section.VideoPlayerSection
import com.android.purebilibili.feature.video.ui.section.resolveAllowLivePlayerSharedElementForMorph
import com.android.purebilibili.feature.video.ui.section.resolveNavigationLiveSurfaceTextureEnabled
import com.android.purebilibili.feature.video.viewmodel.CommentUiState
import com.android.purebilibili.feature.video.viewmodel.SubReplyUiState
import com.android.purebilibili.feature.video.viewmodel.VideoEngagementUiState
import com.android.purebilibili.feature.video.viewmodel.VideoPlaybackUiState

private enum class LargeScreenPaneVisibility {
    BOTH,
    PRIMARY_COLLAPSED,
    SECONDARY_COLLAPSED,
}

@Composable
private fun LargeScreenPaneToggleRail(
    onCollapsePrimary: () -> Unit,
    onCollapseSecondary: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        TabletSecondaryPaneToggleButton(
            isSecondaryPaneVisible = false,
            onClick = onCollapsePrimary,
            contentDescription = "收起左侧内容",
        )
        TabletSecondaryPaneToggleButton(
            isSecondaryPaneVisible = true,
            onClick = onCollapseSecondary,
            contentDescription = "收起右侧内容",
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun LargeScreenVideoLayout(
    playerState: VideoPlayerState,
    uiState: VideoPlaybackUiState,
    commentState: CommentUiState,
    engagementState: VideoEngagementUiState,
    subReplyState: SubReplyUiState,
    downloadProgress: Float,
    commentMemberDecorationsEnabled: Boolean,
    playbackActions: VideoDetailPlaybackActions,
    engagementActions: VideoDetailEngagementActions,
    commentActions: VideoDetailCommentActions,
    @Suppress("UNUSED_PARAMETER")
    configuration: Configuration,
    isVerticalVideo: Boolean,
    sleepTimerMinutes: Int?,
    viewPoints: List<ViewPoint>,
    bvid: String,
    coverUrl: String = "",
    onBack: () -> Unit,
    onUpClick: (Long) -> Unit,
    onBgmClick: (BgmInfo) -> Unit = {},
    onNavigateToAudioMode: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onPortraitFullscreen: () -> Unit,
    isInPipMode: Boolean,
    onPipClick: () -> Unit,
    isPortraitFullscreen: Boolean = false,
    onHomeClick: () -> Unit,
    currentCodec: String = "hev1",
    onCodecChange: (String) -> Unit = {},
    currentSecondCodec: String = "avc1",
    onSecondCodecChange: (String) -> Unit = {},
    currentAudioQuality: Int = -1,
    onAudioQualityChange: (Int) -> Unit = {},
    transitionEnabled: Boolean = false,
    onRelatedVideoClick: (String, android.os.Bundle?) -> Unit,
    showUpBadge: Boolean = true,
    onSearchKeywordClick: (String) -> Unit = {},
    onOpenBilibiliLink: ((String) -> Unit)? = null,
    currentPlayMode: com.android.purebilibili.feature.video.player.PlayMode =
        com.android.purebilibili.feature.video.player.PlayMode.SEQUENTIAL,
    onPlayModeClick: () -> Unit = {},
    forceCoverOnlyOnReturn: Boolean = false,
    predictiveBackCancelRecoveryGeneration: Int = 0,
    liveSurfaceCardTransitionEnabled: Boolean = true,
    paneControlsEnabled: Boolean = true,
) {
    val pageColor = AppSurfaceTokens.chromeBackground()
    val danmakuChrome = rememberTabletDanmakuChromeState(bvid)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(pageColor),
    ) {
        val windowWidthDp = maxWidth.value
        val windowHeightDp = maxHeight.value
        val metrics = remember(windowWidthDp, windowHeightDp, isVerticalVideo) {
            resolveLargeScreenVideoMetrics(
                windowWidthDp = windowWidthDp,
                windowHeightDp = windowHeightDp,
                isVerticalVideo = isVerticalVideo,
            )
        }
        val applySideStatusBarPadding =
            metrics.mode != LargeScreenVideoLayoutMode.AlmostSquare
        val showRelatedInIntro = false
        val relatedTabFirst = false
        val includeRelatedTab = true
        val success = uiState as? VideoPlaybackUiState.Success
        var sidePaneCollapsedRequested by rememberSaveable(bvid) { mutableStateOf(false) }
        var primaryPaneCollapsedRequested by rememberSaveable(bvid) { mutableStateOf(false) }
        val canCollapseSidePane = metrics.mode == LargeScreenVideoLayoutMode.Split ||
            metrics.mode == LargeScreenVideoLayoutMode.Landscape ||
            metrics.mode == LargeScreenVideoLayoutMode.AlmostSquare
        val paneVisibility = when {
            canCollapseSidePane && primaryPaneCollapsedRequested ->
                LargeScreenPaneVisibility.PRIMARY_COLLAPSED
            canCollapseSidePane && sidePaneCollapsedRequested ->
                LargeScreenPaneVisibility.SECONDARY_COLLAPSED
            else -> LargeScreenPaneVisibility.BOTH
        }
        val paneTransition = updateTransition(
            targetState = paneVisibility,
            label = "large-screen-secondary-pane",
        )
        val animatedPrimaryPaneWidth by paneTransition.animateDp(
            transitionSpec = { tween(durationMillis = 320) },
            label = "primary-pane-width",
        ) { visibility ->
            when (visibility) {
                LargeScreenPaneVisibility.PRIMARY_COLLAPSED -> 0.dp
                LargeScreenPaneVisibility.BOTH -> if (
                    metrics.mode == LargeScreenVideoLayoutMode.AlmostSquare
                ) {
                    maxWidth / 2f
                } else {
                    metrics.playerWidthDp.dp
                }
                LargeScreenPaneVisibility.SECONDARY_COLLAPSED -> maxWidth
            }
        }
        val animatedSidePaneWidth by paneTransition.animateDp(
            transitionSpec = { tween(durationMillis = 320) },
            label = "secondary-pane-width",
        ) { visibility ->
            when (visibility) {
                LargeScreenPaneVisibility.PRIMARY_COLLAPSED -> maxWidth
                LargeScreenPaneVisibility.BOTH -> if (
                    metrics.mode == LargeScreenVideoLayoutMode.AlmostSquare
                ) {
                    maxWidth / 2f
                } else {
                    metrics.sidePaneWidthDp.dp
                }
                LargeScreenPaneVisibility.SECONDARY_COLLAPSED -> 0.dp
            }
        }
        val sidePaneCollapsed = paneVisibility == LargeScreenPaneVisibility.SECONDARY_COLLAPSED
        val primaryPaneCollapsed = paneVisibility == LargeScreenPaneVisibility.PRIMARY_COLLAPSED
        val paneControlsVisible = paneControlsEnabled && success != null
        val player: @Composable (Modifier) -> Unit = { modifier ->
            LargeScreenPlayerHost(
                modifier = modifier,
                playerState = playerState,
                uiState = uiState,
                bvid = bvid,
                coverUrl = coverUrl,
                isVerticalVideo = isVerticalVideo,
                isInPipMode = isInPipMode,
                isPortraitFullscreen = isPortraitFullscreen,
                sleepTimerMinutes = sleepTimerMinutes,
                viewPoints = viewPoints,
                currentCodec = currentCodec,
                currentSecondCodec = currentSecondCodec,
                currentAudioQuality = currentAudioQuality,
                currentPlayMode = currentPlayMode,
                transitionEnabled = transitionEnabled,
                forceCoverOnlyOnReturn = forceCoverOnlyOnReturn,
                predictiveBackCancelRecoveryGeneration = predictiveBackCancelRecoveryGeneration,
                liveSurfaceCardTransitionEnabled = liveSurfaceCardTransitionEnabled,
                playbackActions = playbackActions,
                engagementActions = engagementActions,
                onBack = onBack,
                onHomeClick = onHomeClick,
                onToggleFullscreen = onToggleFullscreen,
                onPortraitFullscreen = onPortraitFullscreen,
                onPipClick = onPipClick,
                onNavigateToAudioMode = onNavigateToAudioMode,
                onCodecChange = onCodecChange,
                onSecondCodecChange = onSecondCodecChange,
                onAudioQualityChange = onAudioQualityChange,
                onPlayModeClick = onPlayModeClick,
            )
        }
        val intro: @Composable (Modifier) -> Unit = { modifier ->
            if (success != null) {
                TabletVideoInfoPane(
                    success = success,
                    engagementState = engagementState,
                    downloadProgress = downloadProgress,
                    playbackActions = playbackActions,
                    engagementActions = engagementActions,
                    onBgmClick = onBgmClick,
                    onRelatedVideoClick = onRelatedVideoClick,
                    onOpenBilibiliLink = onOpenBilibiliLink,
                    danmakuEnabled = danmakuChrome.enabled,
                    onDanmakuSendClick = playbackActions.showDanmakuSendDialog,
                    onDanmakuToggle = danmakuChrome.onToggle,
                    onOwnerUploadsClick = {
                        success.info.owner.mid.takeIf { it > 0L }?.let(onUpClick)
                    },
                    modifier = modifier,
                    showRelatedVideos = showRelatedInIntro,
                )
            }
        }
        val side: @Composable (Boolean) -> Unit = { includeIntro ->
            if (success != null) {
                TabletSecondaryContent(
                    success = success,
                    commentState = commentState,
                    subReplyState = subReplyState,
                    playbackActions = playbackActions,
                    engagementState = engagementState,
                    engagementActions = engagementActions,
                    commentActions = commentActions,
                    playerState = playerState,
                    onUpClick = onUpClick,
                    paneMode = TabletSecondaryPaneMode.EXPANDED,
                    onPaneModeChange = {},
                    onRelatedVideoClick = onRelatedVideoClick,
                    onSearchKeywordClick = onSearchKeywordClick,
                    showUpBadge = showUpBadge,
                    showIdentityDecorations = commentMemberDecorationsEnabled,
                    onOpenBilibiliLink = onOpenBilibiliLink,
                    requestedTabName = null,
                    onRequestedTabConsumed = {},
                    introContent = if (includeIntro) {
                        { intro(Modifier.fillMaxSize()) }
                    } else {
                        null
                    },
                    applyStatusBarPadding = applySideStatusBarPadding,
                    includeRelatedTab = includeRelatedTab,
                    includeOwnerUploadsTab = true,
                    relatedTabFirst = relatedTabFirst,
                )
            }
        }
        when (metrics.mode) {
            LargeScreenVideoLayoutMode.VerticalThreePane -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .width(metrics.sidePaneWidthDp.dp)
                            .fillMaxHeight()
                            .statusBarsPadding(),
                    ) {
                        intro(Modifier.fillMaxSize())
                    }
                    player(
                        Modifier
                            .width(metrics.playerWidthDp.dp)
                            .height(metrics.playerHeightDp.dp)
                            .background(Color.Black),
                    )
                    Column(
                        modifier = Modifier
                            .width(metrics.sidePaneWidthDp.dp)
                            .fillMaxHeight(),
                    ) {
                        side(false)
                    }
                }
            }
            LargeScreenVideoLayoutMode.Split -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .width(animatedPrimaryPaneWidth)
                                .fillMaxHeight(),
                        ) {
                            player(
                                Modifier
                                    .fillMaxWidth()
                                    .height(metrics.playerHeightDp.dp)
                                    .background(Color.Black),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(animatedSidePaneWidth)
                                .fillMaxHeight(),
                        ) {
                            side(true)
                        }
                    }
                    if (paneControlsVisible && paneVisibility == LargeScreenPaneVisibility.BOTH) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .width(animatedPrimaryPaneWidth)
                                    .fillMaxHeight(),
                            ) {
                                LargeScreenPaneToggleRail(
                                    onCollapsePrimary = {
                                        primaryPaneCollapsedRequested = true
                                        sidePaneCollapsedRequested = false
                                    },
                                    onCollapseSecondary = {
                                        sidePaneCollapsedRequested = true
                                        primaryPaneCollapsedRequested = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .offset(x = 20.dp),
                                )
                            }
                        }
                    }
                    if (paneControlsVisible && primaryPaneCollapsed) {
                        TabletSecondaryPaneToggleButton(
                            isSecondaryPaneVisible = true,
                            onClick = { primaryPaneCollapsedRequested = false },
                            contentDescription = "展开左侧内容",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 4.dp),
                        )
                    } else if (paneControlsVisible && sidePaneCollapsed) {
                        TabletSecondaryPaneToggleButton(
                            isSecondaryPaneVisible = false,
                            onClick = { sidePaneCollapsedRequested = false },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 0.dp),
                        )
                    }
                }
            }
            LargeScreenVideoLayoutMode.AlmostSquare -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        player(
                            Modifier
                                .fillMaxWidth()
                                .height(metrics.playerHeightDp.dp)
                                .background(Color.Black),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(animatedPrimaryPaneWidth)
                                    .fillMaxHeight(),
                            ) {
                                intro(Modifier.fillMaxSize())
                            }
                            Box(
                                modifier = Modifier
                                    .width(animatedSidePaneWidth)
                                    .fillMaxHeight(),
                            ) {
                                side(false)
                            }
                        }
                    }
                    if (paneControlsVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = metrics.playerHeightDp.dp),
                        ) {
                            if (paneVisibility == LargeScreenPaneVisibility.BOTH) {
                                LargeScreenPaneToggleRail(
                                    onCollapsePrimary = {
                                        primaryPaneCollapsedRequested = true
                                        sidePaneCollapsedRequested = false
                                    },
                                    onCollapseSecondary = {
                                        sidePaneCollapsedRequested = true
                                        primaryPaneCollapsedRequested = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .offset(x = 20.dp),
                                )
                            } else if (primaryPaneCollapsed) {
                                TabletSecondaryPaneToggleButton(
                                    isSecondaryPaneVisible = true,
                                    onClick = { primaryPaneCollapsedRequested = false },
                                    contentDescription = "展开左侧内容",
                                    modifier = Modifier.align(Alignment.CenterStart),
                                )
                            } else if (sidePaneCollapsed) {
                                TabletSecondaryPaneToggleButton(
                                    isSecondaryPaneVisible = false,
                                    onClick = { sidePaneCollapsedRequested = false },
                                    modifier = Modifier.align(Alignment.CenterEnd),
                                )
                            }
                        }
                    }
                }
            }
            LargeScreenVideoLayoutMode.Landscape -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .width(animatedPrimaryPaneWidth)
                                .fillMaxHeight(),
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                player(
                                    Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(LARGE_SCREEN_VIDEO_ASPECT_16_9)
                                        .background(Color.Black),
                                )
                                if (metrics.introBelowPlayer) {
                                    intro(
                                        Modifier
                                            .fillMaxWidth()
                                            .weight(1f),
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .width(animatedSidePaneWidth)
                                .fillMaxHeight(),
                        ) {
                            side(!metrics.introBelowPlayer)
                        }
                    }
                    if (paneControlsVisible && paneVisibility == LargeScreenPaneVisibility.BOTH) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            Box(
                                modifier = Modifier
                                    .width(animatedPrimaryPaneWidth)
                                    .fillMaxHeight(),
                            ) {
                                LargeScreenPaneToggleRail(
                                    onCollapsePrimary = {
                                        primaryPaneCollapsedRequested = true
                                        sidePaneCollapsedRequested = false
                                    },
                                    onCollapseSecondary = {
                                        sidePaneCollapsedRequested = true
                                        primaryPaneCollapsedRequested = false
                                    },
                                    modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .offset(x = 20.dp),
                                )
                            }
                        }
                    }
                    if (paneControlsVisible && primaryPaneCollapsed) {
                        TabletSecondaryPaneToggleButton(
                            isSecondaryPaneVisible = true,
                            onClick = { primaryPaneCollapsedRequested = false },
                            contentDescription = "展开左侧内容",
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 4.dp),
                        )
                    } else if (paneControlsVisible && sidePaneCollapsed) {
                        TabletSecondaryPaneToggleButton(
                            isSecondaryPaneVisible = false,
                            onClick = { sidePaneCollapsedRequested = false },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 0.dp),
                        )
                    }
                }
            }
            LargeScreenVideoLayoutMode.Phone -> {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .width(metrics.playerWidthDp.dp)
                            .fillMaxHeight(),
                    ) {
                        player(
                            Modifier
                                .fillMaxWidth()
                                .height(metrics.playerHeightDp.dp)
                                .background(Color.Black),
                        )
                        if (metrics.introBelowPlayer) {
                            intro(
                                Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            )
                        }
                    }
                    Column(
                        modifier = Modifier
                            .width(metrics.sidePaneWidthDp.dp)
                            .fillMaxHeight(),
                    ) {
                        side(!metrics.introBelowPlayer)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun LargeScreenPlayerHost(
    modifier: Modifier,
    playerState: VideoPlayerState,
    uiState: VideoPlaybackUiState,
    bvid: String,
    coverUrl: String,
    isVerticalVideo: Boolean,
    isInPipMode: Boolean,
    isPortraitFullscreen: Boolean,
    sleepTimerMinutes: Int?,
    viewPoints: List<ViewPoint>,
    currentCodec: String,
    currentSecondCodec: String,
    currentAudioQuality: Int,
    currentPlayMode: com.android.purebilibili.feature.video.player.PlayMode,
    transitionEnabled: Boolean,
    forceCoverOnlyOnReturn: Boolean,
    predictiveBackCancelRecoveryGeneration: Int,
    liveSurfaceCardTransitionEnabled: Boolean,
    playbackActions: VideoDetailPlaybackActions,
    engagementActions: VideoDetailEngagementActions,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onPortraitFullscreen: () -> Unit,
    onPipClick: () -> Unit,
    onNavigateToAudioMode: () -> Unit,
    onCodecChange: (String) -> Unit,
    onSecondCodecChange: (String) -> Unit,
    onAudioQualityChange: (Int) -> Unit,
    onPlayModeClick: () -> Unit,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    val sourceRoute = LocalVideoCardSharedElementSourceRoute.current
    val sharedCoverShape = remember(sourceRoute) {
        RoundedCornerShape(resolveVideoSharedTransitionSourceCornerDp(sourceRoute).dp)
    }
    val playerContainerModifier = if (
        transitionEnabled &&
        sharedTransitionScope != null &&
        animatedVisibilityScope != null &&
        !forceCoverOnlyOnReturn
    ) {
        with(sharedTransitionScope) {
            modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(
                    key = videoCoverSharedElementKey(bvid),
                ),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ -> AppMotionTokens.spatialSpec() },
                clipInOverlayDuringTransition = OverlayClip(sharedCoverShape),
            )
        }
    } else {
        modifier
    }
    Box(modifier = playerContainerModifier) {
        VideoPlayerSection(
            playerState = playerState,
            uiState = uiState,
            isFullscreen = false,
            isInPipMode = isInPipMode,
            useTextureSurfaceForNavigation = resolveNavigationLiveSurfaceTextureEnabled(
                cardTransitionEnabled = transitionEnabled,
                liveSurfaceCardTransitionEnabled = liveSurfaceCardTransitionEnabled,
            ),
            allowLivePlayerSharedElement = resolveAllowLivePlayerSharedElementForMorph(
                cardTransitionEnabled = transitionEnabled,
                liveSurfaceCardTransitionEnabled = liveSurfaceCardTransitionEnabled,
            ),
            predictiveBackCancelRecoveryGeneration = predictiveBackCancelRecoveryGeneration,
            onToggleFullscreen = onToggleFullscreen,
            onQualityChange = playbackActions.changeQuality,
            onBack = onBack,
            onHomeClick = onHomeClick,
            bvid = bvid,
            coverUrl = coverUrl,
            onDoubleTapLike = engagementActions.toggleLike,
            onReloadVideo = playbackActions.reloadVideo,
            cdnCount = (uiState as? VideoPlaybackUiState.Success)?.cdnCount ?: 1,
            cdnLineDiagnostics = (uiState as? VideoPlaybackUiState.Success)?.cdnLineDiagnostics.orEmpty(),
            isCdnProbing = (uiState as? VideoPlaybackUiState.Success)?.isCdnProbing ?: false,
            onSwitchCdn = playbackActions.switchCdn,
            onSwitchCdnTo = playbackActions.switchCdnTo,
            onProbeCdnCandidates = playbackActions.probeCdnCandidates,
            isAudioOnly = false,
            onAudioOnlyToggle = {
                playbackActions.setAudioMode(true)
                onNavigateToAudioMode()
            },
            sleepTimerMinutes = sleepTimerMinutes,
            onSleepTimerChange = playbackActions.setSleepTimer,
            videoshotData = (uiState as? VideoPlaybackUiState.Success)?.videoshotData,
            viewPoints = viewPoints,
            isVerticalVideo = isVerticalVideo,
            onPortraitFullscreen = onPortraitFullscreen,
            isPortraitFullscreen = isPortraitFullscreen,
            onPipClick = onPipClick,
            currentCodec = currentCodec,
            onCodecChange = onCodecChange,
            currentSecondCodec = currentSecondCodec,
            onSecondCodecChange = onSecondCodecChange,
            currentAudioQuality = currentAudioQuality,
            onAudioQualityChange = onAudioQualityChange,
            onPlaybackSpeedChange = playbackActions.applyPlaybackSpeed,
            onSaveCover = playbackActions.saveCover,
            onDownloadAudio = playbackActions.downloadAudio,
            currentPlayMode = currentPlayMode,
            onPlayModeClick = onPlayModeClick,
            onSubtitleTrackSelected = playbackActions.selectSubtitleTrack,
            onDanmakuInputClick = playbackActions.showDanmakuSendDialog,
        )
    }
}
