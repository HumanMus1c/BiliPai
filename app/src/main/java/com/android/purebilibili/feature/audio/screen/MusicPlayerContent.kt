package com.android.purebilibili.feature.audio.screen

import com.android.purebilibili.navigation.animatePagerSelection

import coil3.request.allowHardware

import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.AdaptiveLoadingIndicator
import com.android.purebilibili.core.ui.components.AppCircularProgressIndicator
import com.android.purebilibili.core.ui.motion.AppMotionTokens
import androidx.compose.material3.ExperimentalMaterial3Api
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.ui.components.AppFilledIconButton
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppIconButtonDefaults
import com.android.purebilibili.core.ui.components.AppLinearProgressIndicator
import com.android.purebilibili.core.ui.components.AppSlider
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.AppModalBottomSheet
import com.android.purebilibili.core.ui.components.AppOutlinedTextField
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.palette.graphics.Palette
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.android.purebilibili.core.lifecycle.BackgroundManager
import com.android.purebilibili.feature.audio.lyrics.LyricLine
import com.android.purebilibili.feature.audio.lyrics.resolveActiveLyricIndex
import com.android.purebilibili.feature.audio.lyrics.resolveLyricFocusScrollOffsetPx
import com.android.purebilibili.feature.audio.player.MusicPlayerUiState
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import com.android.purebilibili.feature.home.components.LiquidGlassTuning
import com.android.purebilibili.feature.home.components.biliPaiFloatingDockShell
import com.android.purebilibili.feature.home.components.resolveLiquidGlassTuning
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import com.android.purebilibili.feature.video.playback.audio.AudioQualityOption
import com.android.purebilibili.feature.video.player.PlayMode
import com.android.purebilibili.feature.video.ui.components.AudioQualitySelectionMenu
import com.android.purebilibili.feature.video.ui.components.DolbyBadge
import com.android.purebilibili.feature.video.ui.components.HiResBadge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.QueueMusic
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.RepeatOne
import androidx.compose.material.icons.outlined.Shuffle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.blur.Backdrop as MiuixBackdrop
import com.android.purebilibili.core.ui.blur.rememberChromeBackdropSource

private val MusicFallbackColor = Color(0xFF342B42)

private val LocalMusicContentColor = staticCompositionLocalOf { Color.White }
private val LocalMusicAccentColor = staticCompositionLocalOf { Color.White }

/** 当前听视频页前景色（随封面色板明暗切换，保证可读）。 */
private val MusicContentColor: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMusicContentColor.current

/** 与视频播放器一致的主题强调色（控件高亮、进度、选中态）。 */
private val MusicAccentColor: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalMusicAccentColor.current

/**
 * 听视频/音乐页正文色：按背景亮度在可读 token 间切换。
 *
 * - 亮底 → [onLightBackground]
 * - 暗底 → [onDarkBackground]（应用层应传高对比色，避免 inverseOnSurface 在自定义主题下发灰）
 */
internal fun resolveMusicPlayerContentColor(
    backgroundColor: Color,
    onLightBackground: Color,
    onDarkBackground: Color,
    lightLuminanceThreshold: Float = 0.45f,
): Color {
    return if (backgroundColor.luminance() >= lightLuminanceThreshold) {
        onLightBackground
    } else {
        onDarkBackground
    }
}

/** Bottom controls inherit the artwork palette while staying on the dark immersive floor. */
internal fun resolveMusicImmersivePanelColor(
    backgroundColor: Color,
    darkOverlayFraction: Float = 0.62f,
): Color = lerp(
    start = backgroundColor,
    stop = Color.Black,
    fraction = darkOverlayFraction.coerceIn(0f, 1f),
)

/** 听视频强调色：直接使用应用主题 primary，与播放器一致。 */
internal fun resolveMusicPlayerAccentColor(primary: Color): Color = primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MusicPlayerContent(
    state: MusicPlayerUiState,
    onBack: () -> Unit,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    onQueueItemSelected: (Int) -> Unit = {},
    onPlayModeChange: (PlayMode) -> Unit = {},
    onShuffleEnabledChange: (Boolean) -> Unit = {},
    onLyricsOffsetChange: (Long) -> Unit = {},
    onLyricsRetry: () -> Unit = {},
    onLyricsSearch: (String) -> Unit = {},
    onLyricsCandidateSelected: (Int) -> Unit = {},
    onVideoModeClick: (() -> Unit)? = null,
    onCollectionClick: (() -> Unit)? = null,
    onSleepTimerClick: (() -> Unit)? = null,
    sleepTimerLabel: String = "定时关闭",
    audioQualityLabel: String = "音质",
    audioQualityOptions: List<AudioQualityOption> = emptyList(),
    requestedAudioQuality: Int = -1,
    isHiResAudioSelected: Boolean = false,
    isDolbyAudioSelected: Boolean = false,
    onAudioQualitySelected: ((Int) -> Unit)? = null,
    onPipClick: (() -> Unit)? = null,
    onToggleOrientation: (() -> Unit)? = null,
    orientationActionLabel: String = "横屏",
    isLiked: Boolean = false,
    onLikeClick: (() -> Unit)? = null,
    onCommentsClick: (() -> Unit)? = null,
    isFavorited: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null,
    onDownloadClick: (() -> Unit)? = null,
    onShareClick: (() -> Unit)? = null,
    onSpeedClick: (() -> Unit)? = null,
    speedLabel: String = "倍速",
    isInPipMode: Boolean = false,
    liquidGlassEffectsEnabled: Boolean = false,
    lyricsBlurEffectsEnabled: Boolean = true,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var paletteColor by remember { mutableStateOf(MusicFallbackColor) }
    var artworkBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var showQueue by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var showAudioQuality by remember { mutableStateOf(false) }
    var showLyricsSearch by remember { mutableStateOf(false) }
    var progressSeekRevision by remember { mutableIntStateOf(0) }
    var lyricsControlsVisible by remember(state.title) { mutableStateOf(true) }
    var lyricSearchText by remember(state.title) { mutableStateOf(state.title) }
    val systemReduceMotion = remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val effectiveReduceMotion = reduceMotion || systemReduceMotion
    val musicBackdropSource = rememberChromeBackdropSource()
    val musicBackdrop = musicBackdropSource.takeIf { it.isReady }?.backdrop
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val liquidGlassTuning = remember(
        homeSettings.liquidGlassProgress,
        homeSettings.liquidGlassAdvancedSettings,
        homeSettings.liquidGlassReadabilityMode,
    ) {
        resolveLiquidGlassTuning(
            progress = homeSettings.liquidGlassProgress,
            advancedSettings = homeSettings.liquidGlassAdvancedSettings,
            readabilityMode = homeSettings.liquidGlassReadabilityMode,
        )
    }

    LaunchedEffect(state.coverUrl) {
        val result = loadMusicArtwork(context.imageLoader, state.coverUrl, context)
        artworkBitmap = result?.first
        paletteColor = result?.second ?: MusicFallbackColor
    }

    val backgroundColor by animateColorAsState(
        targetValue = paletteColor,
        animationSpec = if (effectiveReduceMotion) snap() else AppMotionTokens.emphasizedSpec(),
        label = "music_palette"
    )
    val glassEnabled = resolveMusicLiquidGlassEnabled(
        sdkInt = Build.VERSION.SDK_INT,
        effectsEnabled = liquidGlassEffectsEnabled,
        isAppInBackground = BackgroundManager.isInBackground,
        reduceMotion = effectiveReduceMotion
    )
    val chromeSpec = resolveMusicPlayerChromeSpec(
        uiStyle = LocalAppUiStyle.current,
        glassEnabled = glassEnabled
    )
    val pageBackground = if (chromeSpec.usePaletteImmersiveBackdrop) {
        backgroundColor
    } else {
        MaterialTheme.colorScheme.background
    }
    val resolvedContentColor = if (chromeSpec.usePaletteImmersiveBackdrop) {
        resolveMusicPlayerContentColor(
            backgroundColor = backgroundColor,
            onLightBackground = MaterialTheme.colorScheme.onSurface,
            onDarkBackground = Color.White,
        )
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val resolvedAccentColor = resolveMusicPlayerAccentColor(MaterialTheme.colorScheme.primary)

    CompositionLocalProvider(
        LocalMusicContentColor provides resolvedContentColor,
        LocalMusicAccentColor provides resolvedAccentColor,
    ) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(pageBackground)
    ) {
        val layout = resolveMusicPlayerLayout(maxWidth.value.roundToInt(), isInPipMode)
        val availableWidthDp = maxWidth.value.roundToInt()
        val availableHeightDp = maxHeight.value.roundToInt()
        if (layout != MusicPlayerLayout.PIP_ARTWORK) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(musicBackdropSource.modifier)
                    .background(pageBackground)
            ) {
                MusicArtworkBackground(
                    coverUrl = state.coverUrl,
                    backgroundColor = if (chromeSpec.usePaletteImmersiveBackdrop) {
                        backgroundColor
                    } else {
                        pageBackground
                    },
                    immersive = chromeSpec.usePaletteImmersiveBackdrop
                )
            }
        }
        when (layout) {
            MusicPlayerLayout.PIP_ARTWORK -> MusicArtwork(
                coverUrl = state.coverUrl,
                bitmap = artworkBitmap,
                modifier = Modifier.fillMaxSize(),
                shape = RectangleShape
            )

            MusicPlayerLayout.COMPACT_PAGER -> {
                val pagerState = rememberPagerState(pageCount = { 2 })
                val pagerScope = rememberCoroutineScope()
                Box(modifier = Modifier.fillMaxSize()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        if (page == 0) {
                            PlayerPage(
                                state = state,
                                artworkBitmap = artworkBitmap,
                                artworkSizeDp = resolveMusicArtworkSizeDp(
                                    availableWidthDp,
                                    availableHeightDp,
                                    layout
                                ),
                                chromeSpec = chromeSpec,
                                glassEnabled = glassEnabled,
                                reduceMotion = effectiveReduceMotion,
                                onPlayPause = onPlayPause,
                                onSeek = { positionMs ->
                                    progressSeekRevision += 1
                                    onSeek(positionMs)
                                },
                                onPrevious = onPrevious,
                                onNext = onNext,
                                onPlayModeChange = onPlayModeChange,
                                onShuffleEnabledChange = onShuffleEnabledChange,
                                isLiked = isLiked,
                                onLikeClick = onLikeClick,
                                onCommentsClick = onCommentsClick,
                                onQueueClick = { showQueue = true },
                                miuixBackdrop = musicBackdrop,
                                audioQualityLabel = audioQualityLabel,
                                isHiResAudioSelected = isHiResAudioSelected,
                                isDolbyAudioSelected = isDolbyAudioSelected,
                                onAudioQualityClick = onAudioQualitySelected?.let {
                                    { showAudioQuality = true }
                                },
                                glassTintColor = backgroundColor,
                                modifier = Modifier.padding(bottom = MUSIC_PLAYER_COMPACT_DOCK_BOTTOM_PADDING_DP.dp)
                            )
                        } else {
                            LyricsPage(
                                state = state,
                                glassEnabled = glassEnabled,
                                onPlayPause = onPlayPause,
                                onSeek = { positionMs ->
                                    progressSeekRevision += 1
                                    onSeek(positionMs)
                                },
                                onPrevious = onPrevious,
                                onNext = onNext,
                                onLyricsOffsetChange = onLyricsOffsetChange,
                                onLyricsRetry = onLyricsRetry,
                                onOpenLyricsSearch = { showLyricsSearch = true },
                                blurEffectsEnabled = lyricsBlurEffectsEnabled,
                                reduceMotion = effectiveReduceMotion,
                                glassTintColor = backgroundColor,
                                liquidGlassTuning = liquidGlassTuning,
                                miuixBackdrop = musicBackdrop,
                                progressSeekRevision = progressSeekRevision,
                                controlsVisible = lyricsControlsVisible,
                                onControlsVisibleChange = { lyricsControlsVisible = it },
                                modifier = Modifier.padding(bottom = MUSIC_PLAYER_COMPACT_DOCK_BOTTOM_PADDING_DP.dp)
                            )
                        }
                    }
                    BottomBarLiquidSegmentedControl(
                        items = resolveMusicPlayerPageTabs(),
                        selectedIndex = pagerState.currentPage,
                        onSelected = { page ->
                            pagerScope.launch { animatePagerSelection(pagerState, page) }
                        },
                        itemWidth = 84.dp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(vertical = 8.dp)
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        height = 48.dp,
                        indicatorHeight = 36.dp,
                        containerVerticalPadding = 6.dp,
                        selectedTextColorOverride = MusicContentColor,
                        unselectedTextColorOverride = MusicContentColor.copy(alpha = 0.65f),
                        liquidGlassEffectsEnabled = liquidGlassEffectsEnabled,
                        preferInlineContentStyle = false,
                        miuixBackdrop = musicBackdrop,
                        dragSelectionEnabled = true,
                        tapPressRefractionEnabled = true,
                        isScrollInProgressProvider = { pagerState.isScrollInProgress },
                        indicatorPositionProvider = {
                            resolveMusicPagerIndicatorPosition(
                                currentPage = pagerState.currentPage,
                                currentPageOffsetFraction = pagerState.currentPageOffsetFraction
                            )
                        },
                        externalPagerMotionEffectsEnabled = true,
                    )
                }
            }

            MusicPlayerLayout.EXPANDED_SPLIT -> Row(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(top = 56.dp, start = 32.dp, end = 32.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(36.dp)
            ) {
                PlayerPage(
                    state = state,
                    artworkBitmap = artworkBitmap,
                    artworkSizeDp = resolveMusicArtworkSizeDp(
                        availableWidthDp,
                        availableHeightDp,
                        layout
                    ),
                    chromeSpec = chromeSpec,
                    glassEnabled = glassEnabled,
                    reduceMotion = effectiveReduceMotion,
                    onPlayPause = onPlayPause,
                    onSeek = { positionMs ->
                        progressSeekRevision += 1
                        onSeek(positionMs)
                    },
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onPlayModeChange = onPlayModeChange,
                    onShuffleEnabledChange = onShuffleEnabledChange,
                    isLiked = isLiked,
                    onLikeClick = onLikeClick,
                    onCommentsClick = onCommentsClick,
                    onQueueClick = { showQueue = true },
                    miuixBackdrop = musicBackdrop,
                    audioQualityLabel = audioQualityLabel,
                    isHiResAudioSelected = isHiResAudioSelected,
                    isDolbyAudioSelected = isDolbyAudioSelected,
                    onAudioQualityClick = onAudioQualitySelected?.let {
                        { showAudioQuality = true }
                    },
                    glassTintColor = backgroundColor,
                    modifier = Modifier.weight(1f)
                )
                LyricsPage(
                    state = state,
                    glassEnabled = glassEnabled,
                    onPlayPause = onPlayPause,
                    onSeek = onSeek,
                    onPrevious = onPrevious,
                    onNext = onNext,
                    onLyricsOffsetChange = onLyricsOffsetChange,
                    onLyricsRetry = onLyricsRetry,
                    onOpenLyricsSearch = { showLyricsSearch = true },
                    blurEffectsEnabled = lyricsBlurEffectsEnabled,
                    reduceMotion = effectiveReduceMotion,
                    glassTintColor = backgroundColor,
                    liquidGlassTuning = liquidGlassTuning,
                    miuixBackdrop = musicBackdrop,
                    progressSeekRevision = progressSeekRevision,
                    controlsVisible = lyricsControlsVisible,
                    onControlsVisibleChange = { lyricsControlsVisible = it },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!isInPipMode) {
            MusicTopBar(
                glassEnabled = glassEnabled,
                miuixBackdrop = musicBackdrop,
                liquidGlassTuning = liquidGlassTuning,
                onBack = onBack,
                onMore = { showActions = true },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }

    if (showActions) {
        // 操作 sheet 与封面色板解耦：MD3 会强制主题 surface，必须用 onSurface 才能保证深浅色可读
        val sheetContentColor = MaterialTheme.colorScheme.onSurface
        AppModalBottomSheet(
            onDismissRequest = { showActions = false },
            containerColor = AppSurfaceTokens.surface(),
            contentColor = sheetContentColor
        ) {
            AppText(
                text = "播放器操作",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = sheetContentColor,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            if (state.queueControls.showQueue) {
                MusicActionSheetItem("播放队列", contentColor = sheetContentColor) {
                    showActions = false
                    showQueue = true
                }
            }
            onVideoModeClick?.let { action ->
                MusicActionSheetItem("返回视频", contentColor = sheetContentColor) {
                    showActions = false
                    action()
                }
            }
            onCollectionClick?.let { action ->
                MusicActionSheetItem("选集 / 合集", contentColor = sheetContentColor) {
                    showActions = false
                    action()
                }
            }
            onSpeedClick?.let { action ->
                MusicActionSheetItem(speedLabel, contentColor = sheetContentColor) {
                    showActions = false
                    action()
                }
            }
            onSleepTimerClick?.let { action ->
                MusicActionSheetItem(sleepTimerLabel, contentColor = sheetContentColor) {
                    showActions = false
                    action()
                }
            }
            onFavoriteClick?.let { action ->
                MusicActionSheetItem(
                    if (isFavorited) "已收藏" else "收藏",
                    contentColor = sheetContentColor
                ) {
                    showActions = false
                    action()
                }
            }
            onDownloadClick?.let { action ->
                MusicActionSheetItem("缓存音频", contentColor = sheetContentColor) {
                    showActions = false
                    action()
                }
            }
            onShareClick?.let { action ->
                MusicActionSheetItem("分享", contentColor = sheetContentColor) {
                    showActions = false
                    action()
                }
            }
            onPipClick?.let { action ->
                MusicActionSheetItem("画中画", contentColor = sheetContentColor) {
                    showActions = false
                    action()
                }
            }
            onToggleOrientation?.let { action ->
                MusicActionSheetItem(orientationActionLabel, contentColor = sheetContentColor) {
                    showActions = false
                    action()
                }
            }
            MusicActionSheetItem("搜索歌词", contentColor = sheetContentColor) {
                showActions = false
                showLyricsSearch = true
            }
            Spacer(Modifier.navigationBarsPadding().height(12.dp))
        }
    }

    if (showAudioQuality && onAudioQualitySelected != null) {
        AudioQualitySelectionMenu(
            options = audioQualityOptions,
            requestedAudioQuality = requestedAudioQuality,
            onAudioQualitySelected = { quality ->
                onAudioQualitySelected(quality)
                showAudioQuality = false
            },
            onDismiss = { showAudioQuality = false }
        )
    }

    if (showQueue) {
        AppModalBottomSheet(
            onDismissRequest = { showQueue = false },
            containerColor = AppSurfaceTokens.surface(),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            AppText(
                text = "待播清单",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                itemsIndexed(state.queue, key = { _, item -> item.stableId }) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onQueueItemSelected(index)
                                showQueue = false
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = item.coverUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(AppShapes.container(ContainerLevel.Field)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            AppText(
                                text = item.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (index == state.currentQueueIndex) FontWeight.Bold else FontWeight.Normal
                            )
                            AppText(
                                text = item.artist,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (index == state.currentQueueIndex) {
                            AppIcon(
                                Icons.Outlined.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLyricsSearch) {
        AppModalBottomSheet(
            onDismissRequest = { showLyricsSearch = false },
            containerColor = AppSurfaceTokens.surface(),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            AppText(
                text = "手动匹配歌词",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppOutlinedTextField(
                    value = lyricSearchText,
                    onValueChange = { lyricSearchText = it },
                    label = { AppText("歌名") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                AppTextButton(onClick = { onLyricsSearch(lyricSearchText) }) {
                    AppText("搜索")
                }
            }
            if (state.isLyricsSearching) {
                AppCircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(24.dp)
                )
            } else if (state.lyricCandidates.isEmpty()) {
                AppText(
                    text = "输入歌名后搜索网易云、QQ 音乐与酷狗",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    itemsIndexed(state.lyricCandidates) { index, candidate ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLyricsCandidateSelected(index)
                                    showLyricsSearch = false
                                }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            AppText(
                                candidate.title,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            AppText(
                                text = "${candidate.artist} · ${candidate.sourceLabel}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
    } // CompositionLocalProvider
}

@Composable
private fun MusicArtworkBackground(
    coverUrl: String,
    backgroundColor: Color,
    immersive: Boolean
) {
    Box(Modifier.fillMaxSize()) {
        if (immersive && coverUrl.isNotBlank()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(64.dp),
                contentScale = ContentScale.Crop,
                alpha = 0.52f
            )
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                backgroundColor.copy(alpha = 0.42f),
                                Color.Black.copy(alpha = 0.66f),
                                Color.Black.copy(alpha = 0.88f)
                            )
                        )
                    )
            )
        } else {
            Box(Modifier.fillMaxSize().background(backgroundColor))
        }
    }
}

@Composable
private fun PlayerPage(
    state: MusicPlayerUiState,
    artworkBitmap: ImageBitmap?,
    artworkSizeDp: Int,
    chromeSpec: MusicPlayerChromeSpec,
    glassEnabled: Boolean,
    reduceMotion: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onPlayModeChange: (PlayMode) -> Unit,
    onShuffleEnabledChange: (Boolean) -> Unit,
    isLiked: Boolean,
    onLikeClick: (() -> Unit)?,
    onCommentsClick: (() -> Unit)?,
    onQueueClick: () -> Unit,
    miuixBackdrop: MiuixBackdrop?,
    audioQualityLabel: String,
    isHiResAudioSelected: Boolean,
    isDolbyAudioSelected: Boolean,
    onAudioQualityClick: (() -> Unit)?,
    glassTintColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(
                start = chromeSpec.horizontalPaddingDp.dp,
                top = 76.dp,
                end = chromeSpec.horizontalPaddingDp.dp,
                bottom = 16.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (state.isLoading && state.coverUrl.isBlank()) {
            AdaptiveLoadingIndicator(color = MusicContentColor)
        } else {
            MusicArtwork(
                coverUrl = state.coverUrl,
                bitmap = artworkBitmap,
                modifier = Modifier.size(artworkSizeDp.dp),
                shape = if (chromeSpec.coverShapeIsCircle) CircleShape else AppShapes.container(ContainerLevel.Card),
                rotate = shouldRotateMusicArtwork(
                    isPlaying = state.isPlaying,
                    reduceMotion = reduceMotion
                ),
                playbackSpeed = state.playbackSpeed
            )
        }
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                AppText(
                    text = state.title,
                    color = MusicAccentColor,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                AppText(
                    text = state.artist.ifBlank { "未知艺术家" },
                    color = MusicContentColor.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                state.error?.let {
                    AppText(it, color = Color(0xFFFF9B92), style = MaterialTheme.typography.bodySmall)
                }
            }
            onLikeClick?.let { like ->
                AppIconButton(onClick = like, modifier = Modifier.size(48.dp)) {
                    AppIcon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isLiked) "取消点赞" else "点赞",
                        tint = if (isLiked) MaterialTheme.colorScheme.error else MusicContentColor
                    )
                }
            }
        }
        onAudioQualityClick?.let { action ->
            Spacer(Modifier.height(12.dp))
            MusicAudioQualityControl(
                label = audioQualityLabel,
                isHiResSelected = isHiResAudioSelected,
                isDolbySelected = isDolbyAudioSelected,
                onClick = action
            )
        }
        Spacer(Modifier.height(12.dp))
        MusicProgress(state, onSeek, glassEnabled = chromeSpec.glassEnabled)
        Spacer(Modifier.height(8.dp))
        PlaybackControls(
            state = state,
            playButtonSizeDp = chromeSpec.playButtonSizeDp,
            skipButtonSizeDp = chromeSpec.skipButtonSizeDp,
            onPlayPause = onPlayPause,
            onPrevious = onPrevious,
            onNext = onNext
        )
        Spacer(Modifier.height(12.dp))
        MusicSecondaryControls(
            mode = state.playMode,
            shuffleEnabled = state.shuffleEnabled,
            showQueue = state.queueControls.showQueue,
            onPlayModeChange = onPlayModeChange,
            onShuffleEnabledChange = onShuffleEnabledChange,
            onCommentsClick = onCommentsClick,
            onQueueClick = onQueueClick
        )
    }
}

@Composable
private fun MusicAudioQualityControl(
    label: String,
    isHiResSelected: Boolean,
    isDolbySelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .clip(AppShapes.container(ContainerLevel.Dialog))
            .background(MusicAccentColor.copy(alpha = 0.16f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppText(
            text = "音质",
            color = MusicContentColor.copy(alpha = 0.82f),
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.weight(1f))
        AppText(
            text = label.ifBlank { "音质" },
            color = MusicAccentColor,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        if (isHiResSelected) {
            HiResBadge()
        }
        if (isDolbySelected) {
            DolbyBadge()
        }
    }
}

@Composable
private fun MusicSecondaryControls(
    mode: PlayMode,
    shuffleEnabled: Boolean,
    showQueue: Boolean,
    onPlayModeChange: (PlayMode) -> Unit,
    onShuffleEnabledChange: (Boolean) -> Unit,
    onCommentsClick: (() -> Unit)?,
    onQueueClick: () -> Unit
) {
    val transport = resolveMusicSecondaryTransport(mode, shuffleEnabled)
    val active = MusicAccentColor
    val inactive = MusicContentColor.copy(alpha = 0.62f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppIconButton(
            onClick = { onShuffleEnabledChange(!transport.shuffleEnabled) },
            modifier = Modifier.size(48.dp)
        ) {
            AppIcon(
                Icons.Outlined.Shuffle,
                contentDescription = "随机播放",
                tint = if (transport.shuffleEnabled) active else inactive
            )
        }
        AppIconButton(
            onClick = { onPlayModeChange(resolveRepeatModeAfterToggle(mode)) },
            modifier = Modifier.size(48.dp)
        ) {
            AppIcon(
                imageVector = if (transport.repeatGlyph == MusicRepeatGlyph.ONE) {
                    Icons.Outlined.RepeatOne
                } else {
                    Icons.Outlined.Repeat
                },
                contentDescription = "循环模式",
                tint = if (transport.repeatGlyph == MusicRepeatGlyph.OFF) inactive else active
            )
        }
        AppIconButton(
            onClick = onCommentsClick ?: {},
            enabled = onCommentsClick != null,
            modifier = Modifier.size(48.dp)
        ) {
            AppIcon(
                Icons.AutoMirrored.Outlined.Comment,
                contentDescription = "评论",
                tint = if (onCommentsClick != null) inactive else inactive.copy(alpha = 0.28f)
            )
        }
        AppIconButton(
            onClick = onQueueClick,
            enabled = showQueue,
            modifier = Modifier.size(48.dp)
        ) {
            AppIcon(
                Icons.Outlined.QueueMusic,
                contentDescription = "播放队列",
                tint = if (showQueue) inactive else inactive.copy(alpha = 0.28f)
            )
        }
    }
}

@Composable
private fun MusicArtwork(
    coverUrl: String,
    bitmap: ImageBitmap?,
    modifier: Modifier,
    shape: Shape = CircleShape,
    rotate: Boolean = false,
    playbackSpeed: Float = 1f
) {
    val rotationDegrees = rememberMusicArtworkRotationDegrees(
        active = rotate,
        contentKey = coverUrl,
        playbackSpeed = playbackSpeed
    )
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = rotationDegrees() }
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF615571), Color(0xFF27212F))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> androidx.compose.foundation.Image(
                bitmap = bitmap,
                contentDescription = "专辑封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            coverUrl.isNotBlank() -> AsyncImage(
                model = coverUrl,
                contentDescription = "专辑封面",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            else -> AppIcon(
                Icons.Outlined.MusicNote,
                contentDescription = null,
                tint = MusicContentColor.copy(alpha = 0.78f),
                modifier = Modifier.size(96.dp)
            )
        }
    }
}

@Composable
private fun MusicProgress(
    state: MusicPlayerUiState,
    onSeek: (Long) -> Unit,
    glassEnabled: Boolean
) {
    val duration = state.durationMs.coerceAtLeast(1L)
    var draggedPosition by remember { mutableStateOf<Float?>(null) }
    val context = LocalContext.current
    val reduceMotion = remember(context) {
        android.provider.Settings.Global.getFloat(
            context.contentResolver,
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
    val sliderValue = draggedPosition ?: state.positionMs.coerceIn(0L, duration).toFloat()
    val onSliderChange: (Float) -> Unit = { draggedPosition = it }
    val onSliderChangeFinished = {
        draggedPosition?.let { onSeek(it.toLong()) }
        draggedPosition = null
    }
    val uiStyle = LocalAppUiStyle.current
    if (shouldUseNativeThemeMusicProgress(glassEnabled = glassEnabled, uiStyle = uiStyle)) {
        AppSlider(
            value = sliderValue,
            onValueChange = onSliderChange,
            onValueChangeFinished = onSliderChangeFinished,
            valueRange = 0f..duration.toFloat()
        )
    } else {
        MusicWavySlider(
            value = sliderValue,
            onValueChange = onSliderChange,
            onValueChangeFinished = onSliderChangeFinished,
            valueRange = 0f..duration.toFloat(),
            wavy = shouldUseMusicWavyProgress(
                glassEnabled = glassEnabled,
                uiStyle = uiStyle,
                isPlaying = state.isPlaying,
                isDragging = draggedPosition != null,
                reduceMotion = reduceMotion
            ),
            activeColor = MusicAccentColor,
            inactiveColor = MusicContentColor.copy(alpha = 0.28f),
            thumbColor = MusicAccentColor
        )
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        AppText(formatMusicTime(state.positionMs), color = MusicContentColor.copy(alpha = 0.78f), fontSize = 12.sp)
        AppText("-${formatMusicTime((state.durationMs - state.positionMs).coerceAtLeast(0L))}", color = MusicContentColor.copy(alpha = 0.78f), fontSize = 12.sp)
    }
}

@Composable
private fun PlaybackControls(
    state: MusicPlayerUiState,
    onPlayPause: () -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    modifier: Modifier = Modifier,
    playButtonSizeDp: Int = 72,
    skipButtonSizeDp: Int = 56
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlaybackIconButton(
            icon = Icons.Filled.SkipPrevious,
            description = "上一首",
            enabled = state.queueControls.hasPrevious && onPrevious != null,
            onClick = onPrevious ?: {},
            sizeDp = skipButtonSizeDp
        )
        AppFilledIconButton(
            onClick = onPlayPause,
            modifier = Modifier.size(playButtonSizeDp.dp),
            colors = AppIconButtonDefaults.colors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            if (state.isBuffering) {
                AppCircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size((playButtonSizeDp * 0.45f).dp)
                )
            } else {
                AppIcon(
                    imageVector = if (state.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (state.isPlaying) "暂停" else "播放",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size((playButtonSizeDp * 0.45f).dp)
                )
            }
        }
        PlaybackIconButton(
            icon = Icons.Filled.SkipNext,
            description = "下一首",
            enabled = state.queueControls.hasNext && onNext != null,
            onClick = onNext ?: {},
            sizeDp = skipButtonSizeDp
        )
    }
}

@Composable
private fun PlaybackIconButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
    sizeDp: Int = 56
) {
    AppIconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(sizeDp.dp)) {
        AppIcon(
            imageVector = icon,
            contentDescription = description,
            tint = MusicContentColor.copy(alpha = if (enabled) 1f else 0.28f),
            modifier = Modifier.size(32.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsPage(
    state: MusicPlayerUiState,
    glassEnabled: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onLyricsOffsetChange: (Long) -> Unit,
    onLyricsRetry: () -> Unit,
    onOpenLyricsSearch: () -> Unit,
    blurEffectsEnabled: Boolean,
    reduceMotion: Boolean,
    glassTintColor: Color,
    liquidGlassTuning: LiquidGlassTuning,
    miuixBackdrop: MiuixBackdrop?,
    progressSeekRevision: Int,
    controlsVisible: Boolean,
    onControlsVisibleChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val document = state.lyrics
    val currentIndex = document?.let { resolveActiveLyricIndex(it, state.positionMs) } ?: -1
    val blurEnabled = resolveMusicLyricsBlurEnabled(
        sdkInt = Build.VERSION.SDK_INT,
        effectsEnabled = blurEffectsEnabled,
        reduceMotion = reduceMotion
    )
    val listState = rememberLazyListState()
    val isLyricsDragged by listState.interactionSource.collectIsDraggedAsState()
    var showTranslations by remember { mutableStateOf(true) }
    var showLyricsSettings by remember { mutableStateOf(false) }
    var isAutoFollowPaused by remember(document) { mutableStateOf(false) }
    LaunchedEffect(progressSeekRevision) {
        if (progressSeekRevision > 0) {
            isAutoFollowPaused = false
        }
    }
    LaunchedEffect(isLyricsDragged) {
        if (isLyricsDragged) {
            isAutoFollowPaused = true
        }
    }
    LaunchedEffect(currentIndex, isAutoFollowPaused, reduceMotion) {
        if (currentIndex >= 0 && !isAutoFollowPaused) {
            val focusOffset = resolveLyricFocusScrollOffsetPx(
                listState.layoutInfo.viewportSize.height
            )
            if (reduceMotion) {
                listState.scrollToItem(currentIndex, focusOffset)
            } else {
                listState.animateScrollToItem(currentIndex, focusOffset)
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable { onControlsVisibleChange(!controlsVisible) }
            .padding(top = 72.dp, bottom = 16.dp)
    ) {
        if (document == null || document.lines.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppText(
                    text = when {
                        state.isLyricsSearching -> "正在匹配歌词…"
                        state.lyricsError != null -> "歌词加载失败"
                        else -> "未找到匹配歌词"
                    },
                    color = MusicContentColor.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GlassTextButton(
                        "重新匹配",
                        glassEnabled,
                        miuixBackdrop,
                        onLyricsRetry
                    )
                    GlassTextButton(
                        "手动搜索",
                        glassEnabled,
                        miuixBackdrop,
                        onOpenLyricsSearch
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 28.dp,
                    top = 120.dp,
                    end = 28.dp,
                    bottom = 260.dp
                ),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                itemsIndexed(document.lines, key = { index, line -> "${line.startTimeMs}:$index" }) { index, line ->
                    LyricLineContent(
                        line = line,
                        isCurrent = index == currentIndex,
                        positionMs = state.positionMs - document.offsetMs,
                        showTranslations = showTranslations,
                        focusStyle = resolveMusicLyricFocusStyle(index, currentIndex, blurEnabled),
                        reduceMotion = reduceMotion,
                        onClick = {
                            isAutoFollowPaused = false
                            onSeek(line.startTimeMs + document.offsetMs)
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = controlsVisible,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp),
            enter = if (reduceMotion) EnterTransition.None else fadeIn() + slideInVertically { it / 2 },
            exit = if (reduceMotion) ExitTransition.None else fadeOut() + slideOutVertically { it / 2 }
        ) {
            LyricsPrimaryControls(
                state = state,
                glassEnabled = glassEnabled,
                miuixBackdrop = miuixBackdrop,
                glassTintColor = glassTintColor,
                liquidGlassTuning = liquidGlassTuning,
                onPlayPause = onPlayPause,
                onSeek = onSeek,
                onPrevious = onPrevious,
                onNext = onNext,
                onOpenSettings = { showLyricsSettings = true },
                onHideControls = { onControlsVisibleChange(false) }
            )
        }
        if (!controlsVisible) {
            LyricsImmersiveProgress(
                state = state,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }
        if (isAutoFollowPaused && controlsVisible) {
            GlassTextButton(
                label = "回到当前歌词",
                glassEnabled = glassEnabled,
                miuixBackdrop = miuixBackdrop,
                onClick = { isAutoFollowPaused = false },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
        }
    }

    if (showLyricsSettings) {
        val sheetContentColor = MaterialTheme.colorScheme.onSurface
        val sheetSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
        AppModalBottomSheet(
            onDismissRequest = { showLyricsSettings = false },
            containerColor = AppSurfaceTokens.surface(),
            contentColor = sheetContentColor
        ) {
            LyricsSettingsContent(
                showTranslations = showTranslations,
                lyricsOffsetMs = document?.offsetMs ?: 0L,
                contentColor = sheetContentColor,
                secondaryColor = sheetSecondaryColor,
                onToggleTranslations = { showTranslations = !showTranslations },
                onLyricsOffsetChange = onLyricsOffsetChange,
                onLyricsRetry = onLyricsRetry,
                onOpenLyricsSearch = {
                    showLyricsSettings = false
                    onOpenLyricsSearch()
                }
            )
        }
    }
}

@Composable
private fun LyricsPrimaryControls(
    state: MusicPlayerUiState,
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    glassTintColor: Color,
    liquidGlassTuning: LiquidGlassTuning,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: (() -> Unit)?,
    onNext: (() -> Unit)?,
    onOpenSettings: () -> Unit,
    onHideControls: () -> Unit
) {
    val chromeSpec = resolveMusicPlayerChromeSpec(
        uiStyle = LocalAppUiStyle.current,
        glassEnabled = glassEnabled
    )
    val panelColor = if (glassEnabled) {
        resolveMusicImmersivePanelColor(glassTintColor)
    } else {
        AppSurfaceTokens.surfaceContainer()
    }
    val panelContentColor = if (glassEnabled) {
        resolveMusicPlayerContentColor(
            backgroundColor = panelColor,
            onLightBackground = MaterialTheme.colorScheme.onSurface,
            onDarkBackground = Color.White,
        )
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val panelShape = AppShapes.borderedContainer(ContainerLevel.Card)
    AppSurface(
        modifier = Modifier
            .fillMaxWidth()
            .biliPaiFloatingDockShell(
                backdrop = miuixBackdrop,
                containerColor = panelColor,
                pressProgress = 0f,
                shape = panelShape,
                enabled = glassEnabled,
                liquidGlassTuning = liquidGlassTuning,
            ),
        shape = panelShape,
        color = if (glassEnabled) Color.Transparent else panelColor,
        contentColor = panelContentColor,
        tonalElevation = if (chromeSpec.uiStyle == com.android.purebilibili.core.theme.AppUiStyle.MATERIAL3 && !glassEnabled) {
            1.dp
        } else {
            0.dp
        }
    ) {
        CompositionLocalProvider(LocalMusicContentColor provides panelContentColor) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                MusicProgress(state, onSeek, glassEnabled = glassEnabled)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PlaybackControls(
                        state = state,
                        onPlayPause = onPlayPause,
                        onPrevious = onPrevious,
                        onNext = onNext,
                        modifier = Modifier.weight(1f),
                        playButtonSizeDp = chromeSpec.playButtonSizeDp,
                        skipButtonSizeDp = chromeSpec.skipButtonSizeDp
                    )
                    AppTextButton(onClick = onOpenSettings, modifier = Modifier.height(48.dp)) {
                        AppText("歌词设置", color = MusicContentColor, fontSize = 12.sp)
                    }
                    AppTextButton(onClick = onHideControls, modifier = Modifier.height(48.dp)) {
                        AppText("收起", color = MusicContentColor, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

private fun formatLyricsOffset(offsetMs: Long): String {
    if (offsetMs == 0L) return "校正 0.00s"
    val absoluteMs = kotlin.math.abs(offsetMs)
    val seconds = absoluteMs / 1_000L
    val hundredths = (absoluteMs % 1_000L) / 10L
    val sign = if (offsetMs > 0L) "+" else "-"
    return "校正 $sign$seconds.${hundredths.toString().padStart(2, '0')}s"
}

@Composable
private fun LyricsImmersiveProgress(
    state: MusicPlayerUiState,
    modifier: Modifier = Modifier
) {
    val duration = state.durationMs.coerceAtLeast(1L)
    AppLinearProgressIndicator(
        progress = { state.positionMs.coerceIn(0L, duration).toFloat() / duration.toFloat() },
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp),
        color = MusicContentColor,
        trackColor = MusicContentColor.copy(alpha = 0.22f)
    )
}

@Composable
private fun LyricsSettingsContent(
    showTranslations: Boolean,
    lyricsOffsetMs: Long,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    secondaryColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onToggleTranslations: () -> Unit,
    onLyricsOffsetChange: (Long) -> Unit,
    onLyricsRetry: () -> Unit,
    onOpenLyricsSearch: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppText(
            "歌词设置",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        MusicActionSheetItem(
            if (showTranslations) "隐藏翻译与罗马音" else "显示翻译与罗马音",
            contentColor = contentColor,
            onClick = onToggleTranslations
        )
        AppText(
            "歌词时间校正 · ${formatLyricsOffset(lyricsOffsetMs)}",
            color = secondaryColor
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppTextButton(onClick = { onLyricsOffsetChange(-250L) }, modifier = Modifier.height(48.dp)) {
                AppText("歌词提前 0.25 秒", color = contentColor)
            }
            AppTextButton(onClick = { onLyricsOffsetChange(250L) }, modifier = Modifier.height(48.dp)) {
                AppText("歌词延后 0.25 秒", color = contentColor)
            }
        }
        AppTextButton(onClick = { onLyricsOffsetChange(-lyricsOffsetMs) }, modifier = Modifier.height(48.dp)) {
            AppText("重置歌词时间", color = contentColor)
        }
        MusicActionSheetItem("重新匹配歌词", contentColor = contentColor, onClick = onLyricsRetry)
        MusicActionSheetItem("手动搜索歌词", contentColor = contentColor, onClick = onOpenLyricsSearch)
    }
}

@Composable
private fun LyricLineContent(
    line: LyricLine,
    isCurrent: Boolean,
    positionMs: Long,
    showTranslations: Boolean,
    focusStyle: MusicLyricFocusStyle,
    reduceMotion: Boolean,
    onClick: () -> Unit
) {
    val transition = updateTransition(targetState = focusStyle, label = "lyric_focus")
    val blurRadius = transition.animateDp(
        transitionSpec = { if (reduceMotion) snap() else AppMotionTokens.standardSpec() },
        label = "lyric_blur"
    ) { it.blurRadiusDp.dp }
    val alpha = transition.animateFloat(
        transitionSpec = { if (reduceMotion) snap() else AppMotionTokens.standardSpec() },
        label = "lyric_alpha"
    ) { it.alphaPercent / 100f }
    val focusModifier = if (Build.VERSION.SDK_INT >= 31 && blurRadius.value > 0.dp) {
        Modifier.blur(blurRadius.value, edgeTreatment = BlurredEdgeTreatment.Unbounded)
    } else {
        Modifier
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(focusModifier)
            .graphicsLayer { this.alpha = alpha.value }
            .clickable(onClick = onClick)
    ) {
        AppText(
            text = buildLyricText(line, isCurrent, positionMs, MusicContentColor),
            color = MusicContentColor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp
        )
        line.translations.firstOrNull()?.takeIf { showTranslations && it.isNotBlank() }?.let {
            AppText(
                text = it,
                color = MusicContentColor.copy(alpha = 0.72f),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 5.dp)
            )
        }
        line.romanization?.takeIf { showTranslations && it.isNotBlank() }?.let {
            AppText(
                text = it,
                color = MusicContentColor.copy(alpha = 0.58f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

private fun buildLyricText(
    line: LyricLine,
    isCurrent: Boolean,
    positionMs: Long,
    contentColor: Color,
): AnnotatedString {
    if (!isCurrent || line.spans.isEmpty()) return AnnotatedString(line.text)
    return buildAnnotatedString {
        line.spans.forEach { span ->
            val active = positionMs >= span.startTimeMs
            pushStyle(SpanStyle(color = contentColor.copy(alpha = if (active) 1f else 0.38f)))
            append(span.text)
            pop()
        }
    }
}

@Composable
private fun MusicTopBar(
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    liquidGlassTuning: LiquidGlassTuning,
    onBack: () -> Unit,
    onMore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        GlassIconButton(
            Icons.Outlined.KeyboardArrowDown,
            "返回",
            glassEnabled,
            miuixBackdrop,
            liquidGlassTuning,
            onBack
        )
        GlassIconButton(
            Icons.Outlined.MoreHoriz,
            "更多操作",
            glassEnabled,
            miuixBackdrop,
            liquidGlassTuning,
            onMore
        )
    }
}

@Composable
private fun GlassIconButton(
    icon: ImageVector,
    description: String,
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    liquidGlassTuning: LiquidGlassTuning,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val dragX = remember { Animatable(0f) }
    val dragY = remember { Animatable(0f) }
    val maxDragPx = with(LocalDensity.current) { 36.dp.toPx() }
    val expansionPx = with(LocalDensity.current) { 4.dp.toPx() }
    val releaseSpec = remember {
        spring<Float>(
            dampingRatio = 0.5f,
            stiffness = 300f,
        )
    }

    AppIconButton(
        onClick = onClick,
        modifier = Modifier
            .graphicsLayer {
                val transform = resolveMusicTopControlTransform(
                    dragX = dragX.value,
                    dragY = dragY.value,
                    maxDragPx = maxDragPx,
                    widthPx = size.width,
                    heightPx = size.height,
                    expansionPx = expansionPx,
                )
                scaleX = transform.scaleX
                scaleY = transform.scaleY
                translationX = transform.translationX
                translationY = transform.translationY
            }
            .pointerInput(maxDragPx, releaseSpec) {
                detectDragGestures(
                    onDragCancel = {
                        scope.launch {
                            launch { dragX.animateTo(0f, releaseSpec) }
                            launch { dragY.animateTo(0f, releaseSpec) }
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            launch { dragX.animateTo(0f, releaseSpec) }
                            launch { dragY.animateTo(0f, releaseSpec) }
                        }
                    },
                ) { change, dragAmount ->
                    change.consume()
                    scope.launch {
                        dragX.snapTo((dragX.value + dragAmount.x).coerceIn(-maxDragPx, maxDragPx))
                        dragY.snapTo((dragY.value + dragAmount.y).coerceIn(-maxDragPx, maxDragPx))
                    }
                }
            }
            .biliPaiFloatingDockShell(
                backdrop = miuixBackdrop,
                containerColor = AppSurfaceTokens.cardContainer(),
                pressProgress = 0f,
                shape = CircleShape,
                enabled = glassEnabled,
                liquidGlassTuning = liquidGlassTuning,
            )
    ) {
        AppIcon(
            icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun GlassTextButton(
    label: String,
    glassEnabled: Boolean,
    miuixBackdrop: MiuixBackdrop?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = AppShapes.container(ContainerLevel.Pill)
    Box(
        modifier = modifier
            .height(48.dp)
            .background(AppSurfaceTokens.cardContainer(), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            label,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun MusicActionSheetItem(
    label: String,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        AppText(label, color = contentColor, style = MaterialTheme.typography.bodyLarge)
    }
}

private suspend fun loadMusicArtwork(
    imageLoader: ImageLoader,
    coverUrl: String,
    context: android.content.Context
): Pair<ImageBitmap, Color>? = withContext(Dispatchers.IO) {
    if (coverUrl.isBlank()) return@withContext null
    runCatching {
        val request = ImageRequest.Builder(context)
            .data(coverUrl)
            .allowHardware(false)
            .size(512, 512)
            .build()
        val result = imageLoader.execute(request) as SuccessResult
        val bitmap = (result.image as coil3.BitmapImage).bitmap
        val palette = Palette.from(bitmap).clearFilters().generate()
        val colorInt = palette.mutedSwatch?.rgb
            ?: palette.darkMutedSwatch?.rgb
            ?: palette.dominantSwatch?.rgb
            ?: 0xFF342B42.toInt()
        bitmap.asImageBitmap() to Color(colorInt)
    }.getOrNull()
}

internal fun formatMusicTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / 1000L
    return "%02d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
}
