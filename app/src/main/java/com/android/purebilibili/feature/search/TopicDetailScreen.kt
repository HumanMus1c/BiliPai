package com.android.purebilibili.feature.search

import coil3.request.crossfade

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import com.android.purebilibili.core.ui.AdaptiveLoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.imageLoader
import coil3.request.ImageRequest
import com.android.purebilibili.core.ui.AppScaffold
import com.android.purebilibili.core.ui.AppTopBar
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppIconButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.globalWallpaperAwareBackground
import com.android.purebilibili.core.ui.skeleton.ContentSkeletonBlock
import com.android.purebilibili.core.ui.skeleton.rememberContentSkeletonBlockColor
import com.android.purebilibili.core.ui.rememberAppBackIcon
import com.android.purebilibili.core.ui.resolveBottomSafeAreaPadding
import com.android.purebilibili.core.util.FormatUtils
import com.android.purebilibili.data.model.response.TopicTopDetails
import com.android.purebilibili.data.model.response.normalizeSearchImageUrl
import com.android.purebilibili.feature.dynamic.components.DynamicCardV2
import com.android.purebilibili.feature.dynamic.components.DynamicFeedSkeletonCard
import com.android.purebilibili.feature.dynamic.components.rememberDynamicFeedSkeletonPulse
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.R
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.rememberAppChromeLiquidGlassEnabled
import com.android.purebilibili.core.util.responsiveContentWidth
import com.android.purebilibili.data.model.response.DynamicPublishTopic
import com.android.purebilibili.feature.dynamic.components.DynamicAdaptiveSegmentedControl
import com.android.purebilibili.feature.dynamic.components.DynamicPublishComposer
import com.android.purebilibili.feature.home.components.BottomBarMatchedReusableLiquidDock
import com.android.purebilibili.feature.home.components.resolveFloatingDockGeometryScale
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop

@Composable
fun TopicDetailScreen(
    topicId: Long,
    viewModel: TopicDetailViewModel = viewModel(),
    onBack: () -> Unit,
    onVideoClick: (String) -> Unit,
    onBangumiClick: (Long, Long) -> Unit,
    onUserClick: (Long) -> Unit,
    onTopicClick: (Long) -> Unit,
    onLiveClick: (Long, String, String) -> Unit,
    onMusicClick: (Long) -> Unit = {},
    onCollectionClick: (Long, Long, String, String) -> Unit = { _, _, _, _ -> },
    onCourseClick: (String, String) -> Unit = { _, _ -> },
    onArticleClick: (Long, String) -> Unit,
    onDynamicDetailClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val homeSettings by SettingsManager.getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val liquidGlassEnabled = rememberAppChromeLiquidGlassEnabled(
        androidNativeEnabled = homeSettings.androidNativeLiquidGlassEnabled,
    )
    val topicBackdrop = rememberLayerBackdrop()
    var showPublishComposer by remember { mutableStateOf(false) }

    LaunchedEffect(topicId) {
        viewModel.load(topicId)
    }

    AppScaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            AppTopBar(
                title = state.details?.topicItem?.name.orEmpty().ifBlank { "话题" },
                navigationIcon = {
                    AppIconButton(onClick = onBack) {
                        AppIcon(
                            imageVector = rememberAppBackIcon(),
                            contentDescription = "返回",
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            TopicParticipateButton(
                liquidGlassEnabled = liquidGlassEnabled,
                backdrop = topicBackdrop,
                onClick = { showPublishComposer = true },
            )
        },
    ) { padding ->
        val showInitialSkeleton = shouldShowTopicInitialSkeleton(
            isLoading = state.isLoading,
            hasDetails = state.details != null,
            itemCount = state.items.size,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .globalWallpaperAwareBackground()
                .layerBackdrop(topicBackdrop)
                .padding(padding)
        ) {
            when {
                showInitialSkeleton -> {
                    TopicDetailLoadingSkeleton(modifier = Modifier.fillMaxSize())
                }
                state.error != null && state.details == null && state.items.isEmpty() -> {
                    AppText(
                        text = state.error ?: "话题加载失败",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = resolveBottomSafeAreaPadding(
                                navigationBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                                extraBottomPadding = 16.dp
                            )
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .responsiveContentWidth(maxWidth = 960.dp)
                            .fillMaxSize()
                    ) {
                        item {
                            TopicHeaderCard(
                                details = state.details,
                                onUserClick = onUserClick,
                            )
                        }
                        if (state.sortOptions.isNotEmpty()) {
                            item(key = "topic_sort") {
                                TopicSortControl(
                                    options = state.sortOptions.map { it.sortName.ifBlank { "动态" } },
                                    selectedIndex = state.sortOptions
                                        .indexOfFirst { it.sortBy == state.selectedSortBy }
                                        .coerceAtLeast(0),
                                    switching = state.isSwitchingSort,
                                    onSelected = { index ->
                                        state.sortOptions.getOrNull(index)?.let { option ->
                                            viewModel.selectSort(option.sortBy)
                                        }
                                    },
                                )
                            }
                        }
                        itemsIndexed(state.items, key = { _, item -> item.id_str }) { index, item ->
                            DynamicCardV2(
                                item = item,
                                onVideoClick = onVideoClick,
                                onBangumiClick = onBangumiClick,
                                onUserClick = onUserClick,
                                onTopicClick = onTopicClick,
                                onLiveClick = onLiveClick,
                                onMusicClick = onMusicClick,
                                onCollectionClick = onCollectionClick,
                                onCourseClick = onCourseClick,
                                onArticleClick = onArticleClick,
                                onDynamicDetailClick = onDynamicDetailClick,
                                onCommentClick = onDynamicDetailClick,
                                gifImageLoader = context.imageLoader
                            )
                            if (index == state.items.size - 3 && state.hasMore && !state.isLoadingMore) {
                                LaunchedEffect(state.offset) {
                                    viewModel.loadMore()
                                }
                            }
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AdaptiveLoadingIndicator(
                                        size = 24.dp,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showPublishComposer) {
        val topic = state.details?.topicItem
        DynamicPublishComposer(
            initialDraft = com.android.purebilibili.data.model.response.DynamicPublishDraft(
                text = "",
                topic = topic?.takeIf { it.id > 0L }?.let {
                    DynamicPublishTopic(id = it.id, name = it.name)
                },
            ),
            isEditing = false,
            submitting = state.isPublishing,
            errorMessage = state.publishError,
            onDismiss = { showPublishComposer = false },
            onSubmit = { draft ->
                viewModel.publish(context, draft) { success, message ->
                    android.widget.Toast.makeText(
                        context,
                        message,
                        android.widget.Toast.LENGTH_SHORT,
                    ).show()
                    if (success) showPublishComposer = false
                }
            },
        )
    }
}

@Composable
private fun TopicDetailLoadingSkeleton(modifier: Modifier = Modifier) {
    val dynamicPulse = rememberDynamicFeedSkeletonPulse()
    val headerBlockColor = rememberContentSkeletonBlockColor(dynamicPulse)
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
        modifier = modifier,
    ) {
        item {
            AppSurface(
                shape = AppShapes.container(ContainerLevel.Chip),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ContentSkeletonBlock(
                        color = headerBlockColor,
                        shape = AppShapes.container(ContainerLevel.Chip),
                        modifier = Modifier.size(58.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ContentSkeletonBlock(
                            color = headerBlockColor,
                            modifier = Modifier
                                .fillMaxWidth(0.62f)
                                .height(18.dp),
                        )
                        ContentSkeletonBlock(
                            color = headerBlockColor,
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .height(13.dp),
                        )
                        ContentSkeletonBlock(
                            color = headerBlockColor,
                            modifier = Modifier
                                .fillMaxWidth(0.56f)
                                .height(12.dp),
                        )
                    }
                    ContentSkeletonBlock(
                        color = headerBlockColor,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp),
                    )
                }
            }
        }
        items(6) {
            DynamicFeedSkeletonCard(pulse = dynamicPulse)
        }
    }
}

@Composable
private fun TopicSortControl(
    options: List<String>,
    selectedIndex: Int,
    switching: Boolean,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemWidth = TOPIC_SORT_ITEM_WIDTH_DP.dp
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DynamicAdaptiveSegmentedControl(
            items = options,
            selectedIndex = selectedIndex,
            onSelected = onSelected,
            itemWidth = itemWidth,
            height = 40.dp,
            indicatorHeight = 34.dp,
            labelFontSize = 13.sp,
            // This control is inside the page source captured by topicBackdrop. Reusing that
            // same source here would make the liquid lens sample an ancestor that contains
            // the lens itself, producing a cyclic RenderNode graph and RenderThread overflow.
            // Let the segmented control own its isolated local backdrop instead.
            backdrop = null,
            modifier = Modifier.width(resolveTopicSortControlWidthDp(options.size).dp),
        )
        if (switching) {
            Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
            AdaptiveLoadingIndicator(size = 18.dp, strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun TopicParticipateButton(
    liquidGlassEnabled: Boolean,
    backdrop: top.yukonga.miuix.kmp.blur.Backdrop,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonModifier = modifier
        .width(TOPIC_PARTICIPATE_BUTTON_WIDTH_DP.dp)
        .height(52.dp)
    val topicIcon = painterResource(R.drawable.ms_tag_24)

    when (
        resolveTopicParticipateChrome(
            uiStyle = LocalAppUiStyle.current,
            liquidGlassEnabled = liquidGlassEnabled,
        )
    ) {
        TopicParticipateChrome.LIQUID_GLASS_DOCK -> BottomBarMatchedReusableLiquidDock(
            shape = AppShapes.container(ContainerLevel.Pill),
            modifier = buttonModifier,
            backdrop = backdrop,
            reuseEnabled = true,
            useNeutralLiquidContainer = true,
            drawShellLens = true,
            shellLensIntensity = resolveFloatingDockGeometryScale(52f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(AppShapes.container(ContainerLevel.Pill))
                    .clickable(onClick = onClick)
                    .padding(horizontal = AppSpacingTokens.Medium),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppIcon(
                    painter = topicIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
                AppText(
                    text = "参与话题",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        TopicParticipateChrome.MIUIX_COMPACT_BUTTON -> MiuixButton(
            onClick = onClick,
            modifier = buttonModifier,
            insideMargin = PaddingValues(horizontal = AppSpacingTokens.Medium),
        ) {
            AppIcon(
                painter = topicIcon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(AppSpacingTokens.Small))
            AppText("参与话题", fontWeight = FontWeight.SemiBold)
        }

        TopicParticipateChrome.MATERIAL_EXTENDED_FAB ->
            androidx.compose.material3.ExtendedFloatingActionButton(
            onClick = onClick,
            icon = {
                AppIcon(
                    painter = topicIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            text = { AppText("参与话题", fontWeight = FontWeight.SemiBold) },
            modifier = buttonModifier,
        )
    }
}

@Composable
private fun TopicHeaderCard(
    details: TopicTopDetails?,
    onUserClick: (Long) -> Unit,
) {
    val topic = details?.topicItem
    val creator = details?.topicCreator
    val topicDescription = topic?.description
    val creatorName = creator?.name
    val creatorFace = creator?.face
    AppSurface(
        shape = AppShapes.container(ContainerLevel.Chip),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(normalizeSearchImageUrl(topic?.sharePic.orEmpty()))
                    .crossfade(true)
                    .build(),
                contentDescription = topic?.name,
                modifier = Modifier
                    .size(58.dp)
                    .clip(AppShapes.container(ContainerLevel.Chip))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = topic?.name.orEmpty().ifBlank { "话题" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!topicDescription.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AppText(
                        text = topicDescription,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                AppText(
                    text = buildString {
                        append("浏览 ${FormatUtils.formatStat(topic?.view ?: 0)}")
                        append(" · 讨论 ${FormatUtils.formatStat(topic?.discuss ?: 0)}")
                        if (!creatorName.isNullOrBlank()) {
                            append(" · $creatorName")
                        }
                    },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )
            }
            if (!creatorFace.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(normalizeSearchImageUrl(creatorFace))
                        .crossfade(true)
                        .build(),
                    contentDescription = creatorName,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(enabled = (creator?.uid ?: 0L) > 0L) {
                            onUserClick(creator?.uid ?: 0L)
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
