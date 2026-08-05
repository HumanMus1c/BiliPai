package com.android.purebilibili.feature.video.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.data.model.response.ViewPoint
import com.android.purebilibili.feature.video.progress.PbpProgressData
import com.android.purebilibili.feature.video.state.VideoPlayerState
import com.android.purebilibili.feature.video.subtitle.SubtitleDisplayMode
import com.android.purebilibili.feature.video.ui.section.VideoPlayerSection
import com.android.purebilibili.feature.video.viewmodel.VideoPlaybackUiState
import kotlin.math.roundToInt

internal data class ContinuousPlayerHostLayout(
    val modifier: Modifier,
    val viewportWidth: Dp,
    val alpha: State<Float>,
    val scale: State<Float>,
    val isFullscreen: Boolean,
    val contentTopInset: Dp = 0.dp,
)

internal data class ContinuousPlayerFullscreenExtras(
    val danmakuComposerVisible: Boolean,
    val onDismissDanmakuComposer: () -> Unit,
    val onSendDanmakuComposer: (String, Int, Int, Int, Boolean) -> Unit,
    val isSendingDanmakuComposer: Boolean,
    val danmakuComposerInitialText: String,
    val danmakuComposerInitialAttentionCommand: Boolean,
    val danmakuComposerInitialColor: Int,
    val danmakuComposerInitialMode: Int,
    val danmakuComposerInitialFontSize: Int,
    val onDanmakuComposerDraftChange: (String, Boolean) -> Unit,
    val onDanmakuComposerSelectionChange: (Int, Int, Int) -> Unit,
    val currentPlayMode: com.android.purebilibili.feature.video.player.PlayMode,
    val onPlayModeClick: () -> Unit,
    val onSaveCover: () -> Unit,
    val onDownloadAudio: () -> Unit,
    val relatedVideos: List<com.android.purebilibili.data.model.response.RelatedVideo>,
    val ugcSeason: com.android.purebilibili.data.model.response.UgcSeason?,
    val isFollowed: Boolean,
    val isLiked: Boolean,
    val isCoined: Boolean,
    val isFavorited: Boolean,
    val onToggleFollow: () -> Unit,
    val onToggleLike: () -> Unit,
    val onDislike: () -> Unit,
    val onCoin: () -> Unit,
    val onToggleFavorite: () -> Unit,
    val onTriple: () -> Unit,
    val onRelatedVideoClick: (String, android.os.Bundle?) -> Unit,
    val onPageSelect: (Int) -> Unit,
    val hasFavoritePlaylist: Boolean,
    val onFavoritePlaylistClick: () -> Unit,
    val onLandscapeCommentClick: () -> Unit,
    val landscapeCommentPanelVisible: Boolean,
    val landscapeCommentPanelOnLeft: Boolean,
)

/**
 * Reads the frame-rate progress only during measurement, keeping the player composition stable
 * while the inline viewport grows into the landscape viewport.
 */
/**
 * @param preferLayoutWidth16x9Inline 横屏 16:9 详情播放器：按**实际布局宽度**算高度，
 * 避免 `configuration.screenWidthDp` 与真机可用宽度不一致时出现左右黑边（vivo 等窄机更常见）。
 * @param inlineTopInset 沉浸状态栏额外高度；只加在 inline 高度上，不参与 16:9 比例本体。
 */
internal fun Modifier.continuousPlayerViewportHeight(
    progressProvider: () -> Float,
    inlineHeight: Dp,
    fullscreenHeight: Dp,
    enabled: Boolean,
    preferLayoutWidth16x9Inline: Boolean = false,
    inlineTopInset: Dp = 0.dp,
): Modifier {
    return layout { measurable, constraints ->
        val layoutWidth = constraints.maxWidth.coerceAtLeast(1)
        val insetPx = inlineTopInset.roundToPx().coerceAtLeast(0)
        val inlinePx = if (preferLayoutWidth16x9Inline) {
            // 纯 16:9 内容区 + 可选顶部 inset，宽度始终吃满 constraints。
            resolveLandscapeDetailPlayerContentHeightPx(layoutWidthPx = layoutWidth) + insetPx
        } else {
            inlineHeight.roundToPx().coerceAtLeast(1)
        }
        val fraction = progressProvider().coerceIn(0f, 1f)
        val fullscreenPx = fullscreenHeight.toPx()
        val height = if (!enabled) {
            inlinePx
        } else {
            (inlinePx + (fullscreenPx - inlinePx) * fraction).roundToInt()
        }.coerceIn(
            minimumValue = constraints.minHeight,
            maximumValue = if (constraints.maxHeight == Constraints.Infinity) {
                Int.MAX_VALUE
            } else {
                constraints.maxHeight
            },
        )
        val placeable = measurable.measure(
            constraints.copy(
                minWidth = layoutWidth,
                maxWidth = layoutWidth,
                minHeight = height,
                maxHeight = height,
            ),
        )
        layout(placeable.width, placeable.height) {
            placeable.placeRelative(0, 0)
        }
    }
}

@Composable
internal fun PortraitInlineVideoPlayerHost(
    modifier: Modifier,
    animatedViewportWidth: Dp,
    contentTopInset: Dp = 0.dp,
    inlinePlayerAlpha: State<Float>,
    inlinePlayerScale: State<Float>,
    isFullscreen: Boolean = false,
    playerState: VideoPlayerState,
    uiState: VideoPlaybackUiState,
    isPipMode: Boolean,
    transitionEnabled: Boolean,
    transitionChromeAlphaProvider: () -> Float,
    onToggleFullscreen: () -> Unit,
    playbackActions: VideoDetailPlaybackActions,
    onDoubleTapLike: () -> Unit,
    onBack: () -> Unit,
    onHomeClick: () -> Unit,
    videoPlayerSectionTarget: VideoPlayerSectionTarget,
    sponsorSegment: com.android.purebilibili.data.model.response.SponsorSegment?,
    showSponsorSkipButton: Boolean,
    sponsorContributionState: com.android.purebilibili.feature.video.viewmodel.SponsorContributionUiState,
    sleepTimerMinutes: Int?,
    viewPoints: List<ViewPoint>,
    pbpProgressData: PbpProgressData?,
    sponsorProgressMarkers: List<com.android.purebilibili.data.model.response.SponsorProgressMarker>,
    isVerticalVideo: Boolean,
    onPortraitFullscreen: () -> Unit,
    isPortraitFullscreen: Boolean,
    onPipClick: () -> Unit,
    codecPreference: String,
    secondCodecPreference: String,
    audioQualityPreference: Int,
    onNavigateToAudioMode: () -> Unit,
    forceCoverOnly: Boolean,
    preserveCurrentFrameOnFullscreenChange: Boolean,
    liveBackPreview: Boolean,
    useTextureSurfaceForNavigation: Boolean,
    predictiveBackCancelRecoveryGeneration: Int,
    allowLivePlayerSharedElement: Boolean,
    sourceRouteForSharedElement: String?,
    preserveSourceCardCornerDuringSharedReturn: Boolean = false,
    suppressSubtitleOverlay: Boolean,
    subtitleDisplayModePreferenceOverride: SubtitleDisplayMode?,
    onSubtitleDisplayModePreferenceOverrideChange: (SubtitleDisplayMode) -> Unit,
    fullscreenExtras: ContinuousPlayerFullscreenExtras? = null,
) {
    val successState = uiState as? VideoPlaybackUiState.Success

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = inlinePlayerAlpha.value
                scaleX = inlinePlayerScale.value
                scaleY = inlinePlayerScale.value
                transformOrigin = TransformOrigin(0.5f, 0f)
            }
    ) {
        VideoPlayerSection(
            playerState = playerState,
            uiState = uiState,
            isFullscreen = isFullscreen,
            isInPipMode = isPipMode,
            contentTopInset = contentTopInset,
            transitionEnabled = transitionEnabled,
            transitionChromeAlphaProvider = transitionChromeAlphaProvider,
            onToggleFullscreen = onToggleFullscreen,
            onQualityChange = { qid -> playbackActions.changeQuality(qid) },
            onBack = onBack,
            onHomeClick = onHomeClick,
            onLandscapeCommentClick = if (isFullscreen) {
                fullscreenExtras?.onLandscapeCommentClick ?: {}
            } else {
                {}
            },
            landscapeCommentPanelVisible = isFullscreen &&
                fullscreenExtras?.landscapeCommentPanelVisible == true,
            landscapeCommentPanelOnLeft = fullscreenExtras?.landscapeCommentPanelOnLeft ?: true,
            onDanmakuInputClick = { playbackActions.showDanmakuSendDialog() },
            danmakuComposerVisible = isFullscreen &&
                fullscreenExtras?.danmakuComposerVisible == true,
            onDismissDanmakuComposer = fullscreenExtras?.onDismissDanmakuComposer ?: {},
            onSendDanmakuComposer = fullscreenExtras?.onSendDanmakuComposer
                ?: { _, _, _, _, _ -> },
            isSendingDanmakuComposer = fullscreenExtras?.isSendingDanmakuComposer == true,
            danmakuComposerInitialText = fullscreenExtras?.danmakuComposerInitialText.orEmpty(),
            danmakuComposerInitialAttentionCommand =
                fullscreenExtras?.danmakuComposerInitialAttentionCommand == true,
            danmakuComposerInitialColor = fullscreenExtras?.danmakuComposerInitialColor ?: 16777215,
            danmakuComposerInitialMode = fullscreenExtras?.danmakuComposerInitialMode ?: 1,
            danmakuComposerInitialFontSize = fullscreenExtras?.danmakuComposerInitialFontSize ?: 25,
            onDanmakuComposerDraftChange = fullscreenExtras?.onDanmakuComposerDraftChange
                ?: { _, _ -> },
            onDanmakuComposerSelectionChange = fullscreenExtras?.onDanmakuComposerSelectionChange
                ?: { _, _, _ -> },
            bvid = videoPlayerSectionTarget.bvid,
            coverUrl = videoPlayerSectionTarget.entryCoverUrl,
            sharedElementBvid = videoPlayerSectionTarget.sharedElementBvid,
            onDoubleTapLike = onDoubleTapLike,
            sponsorSegment = sponsorSegment,
            showSponsorSkipButton = showSponsorSkipButton,
            onSponsorSkip = { playbackActions.skipSponsorSegment() },
            onSponsorDismiss = { playbackActions.dismissSponsorSkipButton() },
            onSponsorVote = { playbackActions.voteSponsorSegment(it) },
            sponsorContributionState = sponsorContributionState,
            onSponsorContributionMarkBoundary = { playbackActions.markSponsorContributionBoundary() },
            onSponsorContributionCategoryChange = { playbackActions.setSponsorContributionCategory(it) },
            onSponsorContributionActionTypeChange = { playbackActions.setSponsorContributionActionType(it) },
            onSponsorContributionSubmit = { playbackActions.submitSponsorContribution() },
            onSponsorContributionCancel = { playbackActions.cancelSponsorContribution() },
            onReloadVideo = { playbackActions.reloadVideo() },
            currentCdnIndex = successState?.currentCdnIndex ?: 0,
            cdnCount = successState?.cdnCount ?: 1,
            cdnLineDiagnostics = successState?.cdnLineDiagnostics.orEmpty(),
            isCdnProbing = successState?.isCdnProbing ?: false,
            onSwitchCdn = { playbackActions.switchCdn() },
            onSwitchCdnTo = { playbackActions.switchCdnTo(it) },
            onProbeCdnCandidates = { playbackActions.probeCdnCandidates() },
            isAudioOnly = false,
            onAudioOnlyToggle = onNavigateToAudioMode,
            sleepTimerMinutes = sleepTimerMinutes,
            onSleepTimerChange = { playbackActions.setSleepTimer(it) },
            videoshotData = successState?.videoshotData,
            viewPoints = viewPoints,
            pbpProgressData = pbpProgressData,
            sponsorMarkers = sponsorProgressMarkers,
            onUserSeek = { position -> playbackActions.notifyExplicitSeek(position) },
            isVerticalVideo = isVerticalVideo,
            onPortraitFullscreen = onPortraitFullscreen,
            isPortraitFullscreen = isPortraitFullscreen,
            viewportWidthDpOverride = animatedViewportWidth.value.roundToInt(),
            onPipClick = onPipClick,
            currentCodec = codecPreference,
            onCodecChange = { playbackActions.setVideoCodec(it) },
            currentSecondCodec = secondCodecPreference,
            onSecondCodecChange = { playbackActions.setVideoSecondCodec(it) },
            currentAudioQuality = audioQualityPreference,
            onAudioQualityChange = { playbackActions.setAudioQuality(it) },
            onPlaybackSpeedChange = { playbackActions.applyPlaybackSpeed(it) },
            onAudioLangChange = { playbackActions.changeAudioLanguage(it) },
            currentPlayMode = fullscreenExtras?.currentPlayMode
                ?: com.android.purebilibili.feature.video.player.PlayMode.SEQUENTIAL,
            onPlayModeClick = fullscreenExtras?.onPlayModeClick ?: {},
            onSaveCover = fullscreenExtras?.onSaveCover ?: { playbackActions.saveCover() },
            onDownloadAudio = fullscreenExtras?.onDownloadAudio ?: { playbackActions.downloadAudio() },
            relatedVideos = fullscreenExtras?.relatedVideos.orEmpty(),
            ugcSeason = fullscreenExtras?.ugcSeason,
            isFollowed = fullscreenExtras?.isFollowed == true,
            isLiked = fullscreenExtras?.isLiked == true,
            isCoined = fullscreenExtras?.isCoined == true,
            isFavorited = fullscreenExtras?.isFavorited == true,
            onToggleFollow = fullscreenExtras?.onToggleFollow ?: {},
            onToggleLike = fullscreenExtras?.onToggleLike ?: {},
            onDislike = fullscreenExtras?.onDislike ?: {},
            onCoin = fullscreenExtras?.onCoin ?: {},
            onToggleFavorite = fullscreenExtras?.onToggleFavorite ?: {},
            onTriple = fullscreenExtras?.onTriple ?: {},
            onRelatedVideoClick = fullscreenExtras?.onRelatedVideoClick ?: { _, _ -> },
            onPageSelect = fullscreenExtras?.onPageSelect ?: {},
            hasFavoritePlaylist = fullscreenExtras?.hasFavoritePlaylist == true,
            onFavoritePlaylistClick = fullscreenExtras?.onFavoritePlaylistClick ?: {},
            forceCoverOnly = forceCoverOnly,
            preserveCurrentFrameOnFullscreenChange = preserveCurrentFrameOnFullscreenChange,
            liveBackPreview = liveBackPreview,
            useTextureSurfaceForNavigation = useTextureSurfaceForNavigation,
            predictiveBackCancelRecoveryGeneration = predictiveBackCancelRecoveryGeneration,
            allowLivePlayerSharedElement = allowLivePlayerSharedElement,
            sourceRouteForSharedElement = sourceRouteForSharedElement,
            preserveSourceCardCornerDuringSharedReturn =
                preserveSourceCardCornerDuringSharedReturn,
            suppressSubtitleOverlay = suppressSubtitleOverlay,
            subtitleDisplayModePreferenceOverride = subtitleDisplayModePreferenceOverride,
            onSubtitleDisplayModePreferenceOverrideChange = onSubtitleDisplayModePreferenceOverrideChange,
            onSubtitleTrackSelected = playbackActions.selectSubtitleTrack
        )
    }
}
