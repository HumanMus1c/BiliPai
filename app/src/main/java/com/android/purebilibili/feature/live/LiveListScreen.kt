package com.android.purebilibili.feature.live

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import android.widget.Toast
import com.android.purebilibili.core.ui.components.AppBadge
import com.android.purebilibili.core.ui.components.AppIcon
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.components.AppOutlinedButton
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.LocalBottomBarContentPadding
import com.android.purebilibili.core.ui.rememberAppTopChromePolicy
import com.android.purebilibili.core.util.LocalWindowSizeClass
import com.android.purebilibili.core.util.responsiveContentWidth
import com.android.purebilibili.data.model.response.LiveAreaParent
import com.android.purebilibili.data.repository.LiveRepository
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import dev.chrisbanes.haze.HazeState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveRoomItem(
    val roomId: Long,
    val title: String,
    val cover: String,
    val uname: String,
    val face: String,
    val online: Int,
    val areaName: String,
    val liveStatus: Int = 1
)

data class LiveListUiState(
    val recommendItems: List<LiveRoomItem> = emptyList(),
    val followItems: List<LiveRoomItem> = emptyList(),
    val areaList: List<LiveAreaParent> = emptyList(),
    val selectedAreaId: Int = 0,
    val areaItems: List<LiveRoomItem> = emptyList(),
    val isLoading: Boolean = false,
    val isAreaLoading: Boolean = false,
    val error: String? = null,
    val currentTab: Int = 0,
    val livingCount: Int = 0
)

class LiveListViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(LiveListUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val recommendJob = launch { loadRecommendLive() }
                val areaJob = launch { loadAreaList() }
                val followJob = launch { loadFollowLive() }

                recommendJob.join()
                areaJob.join()
                followJob.join()

                _uiState.value = _uiState.value.copy(isLoading = false)
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载失败"
                )
            }
        }
    }

    private suspend fun loadRecommendLive() {
        try {
            LiveRepository.getRecommendedLiveRooms().onSuccess { rooms ->
                val items = rooms.map { room ->
                    LiveRoomItem(
                        roomId = room.roomid,
                        title = room.title,
                        cover = room.displayCover(),
                        uname = room.uname,
                        face = room.face,
                        online = room.viewerCount(),
                        areaName = room.areaName
                    )
                }
                _uiState.value = _uiState.value.copy(recommendItems = items)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadAreaList() {
        try {
            val response = NetworkModule.api.getLiveAreaList()
            if (response.code == 0 && response.data != null) {
                _uiState.value = _uiState.value.copy(areaList = response.data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadFollowLive() {
        viewModelScope.launch {
            try {
                LiveRepository.getFollowedLive(page = 1).onSuccess { rooms ->
                    val items = rooms.map { room ->
                        LiveRoomItem(
                            roomId = room.roomid,
                            title = room.title,
                            cover = room.displayCover(),
                            uname = room.uname,
                            face = room.face,
                            online = room.viewerCount(),
                            areaName = room.areaName,
                            liveStatus = 1
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        followItems = items,
                        livingCount = items.size
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadAreaLive(parentAreaId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAreaLoading = true,
                selectedAreaId = parentAreaId
            )
            try {
                val response = NetworkModule.api.getLiveList(
                    parentAreaId = parentAreaId,
                    page = 1,
                    pageSize = 30
                )
                if (response.code == 0 && response.data != null) {
                    val items = response.data.getAllRooms().map { room ->
                        LiveRoomItem(
                            roomId = room.roomid,
                            title = room.title,
                            cover = room.displayCover(),
                            uname = room.uname,
                            face = room.face,
                            online = room.viewerCount(),
                            areaName = room.areaName
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        areaItems = items,
                        isAreaLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isAreaLoading = false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isAreaLoading = false)
            }
        }
    }

    fun openArea(parentAreaId: Int) {
        _uiState.value = _uiState.value.copy(currentTab = 1)
        loadAreaLive(parentAreaId)
    }

    fun selectHomeArea(areaId: Int) {
        if (areaId == 0) {
            _uiState.value = _uiState.value.copy(selectedAreaId = 0, areaItems = emptyList())
            return
        }
        loadAreaLive(areaId)
    }

    fun setTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(currentTab = tabIndex)
        if (tabIndex == 2 && _uiState.value.followItems.isEmpty()) {
            loadFollowLive()
        }
        if (tabIndex == 1 && _uiState.value.areaList.isNotEmpty() && _uiState.value.selectedAreaId == 0) {
            loadAreaLive(_uiState.value.areaList.first().id)
        }
    }

    fun refresh() {
        loadInitialData()
        if (_uiState.value.currentTab == 1 && _uiState.value.selectedAreaId != 0) {
            loadAreaLive(_uiState.value.selectedAreaId)
        }
    }
}

@Composable
fun LiveListScreen(
    onBack: () -> Unit,
    onLiveClick: (Long, String, String) -> Unit,
    onSearchClick: () -> Unit,
    onAreaListClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onAreaDetailClick: (Int, Int, String) -> Unit,
    onMatchClick: () -> Unit = {},
    viewModel: LiveListViewModel = viewModel(),
    globalHazeState: HazeState? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val palette = rememberLiveChromePalette()
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        val originalNavBarColor = window?.navigationBarColor
        if (window != null) {
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
        onDispose {
            if (window != null && originalNavBarColor != null) {
                window.navigationBarColor = originalNavBarColor
            }
        }
    }

    val windowSizeClass = LocalWindowSizeClass.current
    val topChromePolicy = rememberAppTopChromePolicy()
    val visualSpec = remember(topChromePolicy.tabPresentation) {
        resolveLiveVisualSpec(topChromePolicy.tabPresentation)
    }
    val metrics = visualSpec.homeMetrics
    val contentWidth = if (windowSizeClass.isExpandedScreen) {
        minOf(windowSizeClass.widthDp, visualSpec.maxContentWidthDp.dp)
    } else {
        windowSizeClass.widthDp
    }
    val gridColumns = remember(contentWidth, windowSizeClass.isTablet) {
        resolveLivePiliPlusGridColumns(contentWidth.value.toInt(), windowSizeClass.isTablet)
    }
    val gridBottomPadding = LocalBottomBarContentPadding.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.backgroundBrush())
    ) {
        Column(
            modifier = Modifier
                .responsiveContentWidth(maxWidth = visualSpec.maxContentWidthDp.dp)
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LiveListHeader(
                metrics = metrics,
                livingCount = state.livingCount,
                primaryFace = state.followItems.firstOrNull()?.face.orEmpty(),
                onBack = onBack,
                onSearchClick = onSearchClick,
                onInboxClick = onFollowingClick,
                onAvatarClick = onAreaListClick
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = AppSpacingTokens.ExtraSmall)
            ) {
                when {
                    state.isLoading -> {
                        LiveListLoadingState()
                    }
                    state.error != null -> {
                        LiveListErrorState(
                            message = state.error ?: "未知错误",
                            onRetry = viewModel::refresh
                        )
                    }
                    else -> {
                        LiveHomeContent(
                            recommendItems = state.recommendItems,
                            followItems = state.followItems,
                            areaList = state.areaList,
                            selectedAreaId = state.selectedAreaId,
                            areaItems = state.areaItems,
                            livingCount = state.livingCount,
                            isAreaLoading = state.isAreaLoading,
                            gridColumns = gridColumns,
                            bottomPadding = gridBottomPadding,
                            metrics = metrics,
                            visualSpec = visualSpec,
                            onLiveClick = onLiveClick,
                            onAreaSelected = viewModel::selectHomeArea,
                            onAreaDetailClick = onAreaDetailClick,
                            onAreaListClick = onAreaListClick,
                            onFollowingClick = onFollowingClick,
                            onMatchClick = onMatchClick,
                            onLongPressCard = { card ->
                                coroutineScope.launch {
                                    val success = com.android.purebilibili.feature.download.DownloadManager
                                        .saveImageToGallery(
                                            context = context,
                                            url = card.coverUrl,
                                            title = card.title
                                        )
                                    Toast.makeText(
                                        context,
                                        if (success) "封面已保存到相册" else "封面保存失败，请稍后重试",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveHomeContent(
    recommendItems: List<LiveRoomItem>,
    followItems: List<LiveRoomItem>,
    areaList: List<LiveAreaParent>,
    selectedAreaId: Int,
    areaItems: List<LiveRoomItem>,
    livingCount: Int,
    isAreaLoading: Boolean,
    gridColumns: Int,
    bottomPadding: androidx.compose.ui.unit.Dp,
    metrics: LivePiliPlusHomeMetrics,
    visualSpec: LiveVisualSpec,
    onLiveClick: (Long, String, String) -> Unit,
    onAreaSelected: (Int) -> Unit,
    onAreaDetailClick: (Int, Int, String) -> Unit,
    onAreaListClick: () -> Unit,
    onFollowingClick: () -> Unit,
    onMatchClick: () -> Unit = {},
    onLongPressCard: (LiveRoomCardUiModel) -> Unit = {}
) {
    val selectedArea = areaList.firstOrNull { it.id == selectedAreaId }
    val contentItems = if (selectedAreaId == 0) recommendItems else areaItems

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridColumns),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = metrics.safeSpaceDp.dp,
            end = metrics.safeSpaceDp.dp,
            top = metrics.cardSpaceDp.dp,
            bottom = bottomPadding
        ),
        horizontalArrangement = Arrangement.spacedBy(metrics.cardSpaceDp.dp),
        verticalArrangement = Arrangement.spacedBy(metrics.cardSpaceDp.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            LiveMatchEntry(onClick = onMatchClick)
        }
        if (followItems.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiveFollowHeader(
                    livingCount = livingCount,
                    onActionClick = onFollowingClick
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiveFollowAvatarRow(
                    items = followItems.take(10),
                    metrics = metrics,
                    onLiveClick = onLiveClick
                )
            }
        }
        if (areaList.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LiveAreaHomeChipRow(
                    areaList = areaList,
                    selectedAreaId = selectedAreaId,
                    onAreaSelected = onAreaSelected
                )
            }
            if (!selectedArea?.list.isNullOrEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LiveAreaChildChipRow(
                        items = selectedArea.list.orEmpty(),
                        parentAreaId = selectedAreaId,
                        onAreaDetailClick = onAreaDetailClick
                    )
                }
            }
        }
        when {
            isAreaLoading -> item(span = { GridItemSpan(maxLineSpan) }) { LiveListLoadingState() }
            contentItems.isEmpty() -> item(span = { GridItemSpan(maxLineSpan) }) {
                EmptyState("暂无直播内容", visualSpec)
            }
            else -> items(contentItems, key = { it.roomId }) { item ->
                LiveRoomCard(
                    model = item.toLiveRoomCardUiModel(),
                    enableSharedCoverTransition = true,
                    onClick = { onLiveClick(item.roomId, item.title, item.uname) },
                    onLongPress = { onLongPressCard(item.toLiveRoomCardUiModel()) }
                )
            }
        }
    }
}

@Composable
private fun LiveListHeader(
    metrics: LivePiliPlusHomeMetrics,
    livingCount: Int,
    primaryFace: String,
    onBack: () -> Unit,
    onSearchClick: () -> Unit,
    onInboxClick: () -> Unit,
    onAvatarClick: () -> Unit
) {
    val palette = rememberLiveChromePalette()
    val compactChrome = rememberAppTopChromePolicy().compactChromeSpec
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = metrics.safeSpaceDp.dp,
                vertical = AppSpacingTokens.Small,
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)
        ) {
            AppSurface(
                onClick = onBack,
                color = Color.Transparent,
                shape = CircleShape,
                modifier = Modifier.size(AppSpacingTokens.TripleExtraLarge)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    AppIcon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "返回",
                        tint = palette.primaryText
                    )
                }
            }
            AppSurface(
                onClick = onSearchClick,
                color = palette.searchField,
                shape = AppShapes.container(ContainerLevel.Pill),
                modifier = Modifier
                    .weight(1f)
                    .height(compactChrome.primaryHeightDp.dp)
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = compactChrome.inputHorizontalPaddingDp.dp,
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = palette.secondaryText
                    )
                    Spacer(Modifier.width(AppSpacingTokens.Medium))
                    AppText(
                        text = "搜索直播间 / 主播",
                        color = palette.secondaryText,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            Box {
                AppSurface(
                    onClick = onInboxClick,
                    color = Color.Transparent,
                    shape = CircleShape,
                    modifier = Modifier.size(AppSpacingTokens.TripleExtraLarge)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        AppIcon(
                            imageVector = Icons.Outlined.NotificationsNone,
                            contentDescription = "开播提醒",
                            tint = palette.primaryText
                        )
                    }
                }
                if (livingCount > 0) {
                    AppBadge(
                        containerColor = palette.accentStrong,
                        contentColor = palette.onAccent,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        AppText(
                            text = if (livingCount > 99) "99+" else livingCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(AppSpacingTokens.TripleExtraLarge)
                    .clickable(onClick = onAvatarClick)
                    .semantics { contentDescription = "全部直播分区" },
                contentAlignment = Alignment.Center,
            ) {
                AppSurface(
                    color = palette.surfaceMuted,
                    shape = CircleShape,
                    modifier = Modifier.size(compactChrome.secondaryButtonSizeDp.dp)
                ) {
                    if (primaryFace.isNotBlank()) {
                        AsyncImage(
                            model = primaryFace,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            AppText(
                                text = "LIVE",
                                color = palette.primaryText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveFollowHeader(
    livingCount: Int,
    onActionClick: () -> Unit
) {
    val palette = rememberLiveChromePalette()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.Bottom) {
            AppText(
                text = "我的关注  ",
                color = palette.primaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            AppText(
                text = livingCount.toString(),
                color = palette.accentStrong,
                style = MaterialTheme.typography.bodySmall,
            )
            AppText(
                text = " 人正在直播",
                color = palette.secondaryText,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Row(
            modifier = Modifier.clickable(onClick = onActionClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppText(
                text = "查看更多",
                color = palette.secondaryText,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.width(AppSpacingTokens.ExtraSmall))
            AppText(
                text = ">",
                color = palette.secondaryText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun LiveFollowAvatarRow(
    items: List<LiveRoomItem>,
    metrics: LivePiliPlusHomeMetrics,
    onLiveClick: (Long, String, String) -> Unit
) {
    val palette = rememberLiveChromePalette()
    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
        items(items, key = { it.roomId }) { item ->
            Column(
                modifier = Modifier
                    .width(metrics.followItemExtentDp.dp)
                    .clickable { onLiveClick(item.roomId, item.title, item.uname) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size((metrics.followAvatarSizeDp + 5).dp)
                            .clip(CircleShape)
                            .background(palette.accentStrong)
                            .padding(AppSpacingTokens.Micro)
                    ) {
                        AsyncImage(
                            model = item.face.ifBlank { item.cover },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(palette.surface)
                        )
                    }
                }
                Spacer(Modifier.height(AppSpacingTokens.Small))
                AppText(
                    text = item.uname,
                    color = palette.primaryText,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LiveAreaHomeChipRow(
    areaList: List<LiveAreaParent>,
    selectedAreaId: Int,
    onAreaSelected: (Int) -> Unit
) {
    val categoryItems = remember(areaList) {
        listOf(0 to "推荐") + areaList.map { it.id to it.name }
    }
    val selectedIndex = remember(selectedAreaId, areaList) {
        resolveLiveHomeCategorySelectedIndex(
            selectedAreaId = selectedAreaId,
            areaIds = areaList.map { it.id }
        )
    }
    val compactChrome = rememberAppTopChromePolicy().compactChromeSpec
    val segmentedSpec = remember(compactChrome) {
        resolveLiveHomeCategorySegmentedControlSpec(compactChrome)
    }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current
    val itemWidthPx = with(density) { (segmentedSpec.itemWidthDp ?: 0).dp.toPx() }
    val scrollEdgeBufferPx = with(density) { segmentedSpec.edgeBufferDp.dp.toPx() }
    var indicatorPosition by remember { mutableFloatStateOf(selectedIndex.toFloat()) }

    LaunchedEffect(selectedIndex) {
        indicatorPosition = selectedIndex.toFloat()
    }

    LaunchedEffect(indicatorPosition, categoryItems.size, scrollState.maxValue, itemWidthPx) {
        if (itemWidthPx <= 0f || scrollState.maxValue <= 0) return@LaunchedEffect
        val contentWidthPx = itemWidthPx * categoryItems.size +
            with(density) { (segmentedSpec.containerHorizontalPaddingDp * 2).dp.toPx() }
        val viewportWidthPx = (contentWidthPx - scrollState.maxValue).coerceAtLeast(1f)
        val targetScroll = resolveLiveHomeCategoryFollowScrollTarget(
            indicatorPosition = indicatorPosition,
            itemWidthPx = itemWidthPx,
            itemCount = categoryItems.size,
            viewportWidthPx = viewportWidthPx,
            currentScrollPx = scrollState.value.toFloat(),
            maxScrollPx = scrollState.maxValue.toFloat(),
            edgeBufferPx = scrollEdgeBufferPx
        )

        if (kotlin.math.abs(targetScroll - scrollState.value) > 1) {
            scrollState.scrollTo(targetScroll)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(segmentedSpec.heightDp.dp)
            .horizontalScroll(scrollState, enabled = false),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomBarLiquidSegmentedControl(
            items = categoryItems.map { it.second },
            selectedIndex = selectedIndex,
            onSelected = { index ->
                categoryItems.getOrNull(index)?.let { onAreaSelected(it.first) }
            },
            itemWidth = segmentedSpec.itemWidthDp?.dp,
            height = segmentedSpec.heightDp.dp,
            indicatorHeight = segmentedSpec.indicatorHeightDp.dp,
            labelFontSize = segmentedSpec.labelFontSizeSp.sp,
            containerHorizontalPadding = segmentedSpec.containerHorizontalPaddingDp.dp,
            containerVerticalPadding = segmentedSpec.containerVerticalPaddingDp.dp,
            onIndicatorPositionChanged = { indicatorPosition = it }
        )
    }
}

@Composable
private fun LiveAreaChildChipRow(
    items: List<com.android.purebilibili.data.model.response.LiveAreaChild>,
    parentAreaId: Int,
    onAreaDetailClick: (Int, Int, String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val chipColors = resolveLivePiliPlusChipColors(
        selectedContainer = colorScheme.secondaryContainer,
        selectedContent = colorScheme.onSecondaryContainer,
        unselectedContent = colorScheme.onSurfaceVariant
    )
    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium)) {
        items(items, key = { it.id }) { child ->
            AppSurface(
                onClick = {
                    onAreaDetailClick(
                        parentAreaId,
                        child.id.toIntOrNull() ?: 0,
                        child.name
                    )
                },
                color = chipColors.unselectedContainerColor,
                shape = AppShapes.container(ContainerLevel.Pill),
                border = null
            ) {
                AppText(
                    text = child.name,
                    color = chipColors.unselectedContentColor,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(
                        horizontal = AppSpacingTokens.Small,
                        vertical = AppSpacingTokens.ExtraSmall,
                    )
                )
            }
        }
    }
}

@Composable
private fun LiveListLoadingState() {
    val palette = rememberLiveChromePalette()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacingTokens.ExtraLarge),
        contentAlignment = Alignment.Center
    ) {
        AppText(
            text = "直播内容加载中…",
            color = palette.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LiveListErrorState(
    message: String,
    onRetry: () -> Unit
) {
    val palette = rememberLiveChromePalette()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppText(
            text = message,
            color = palette.primaryText,
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(AppSpacingTokens.Medium))
        AppOutlinedButton(onClick = onRetry) {
            AppText("重试")
        }
    }
}

@Composable
private fun EmptyState(
    message: String,
    visualSpec: LiveVisualSpec,
) {
    val palette = rememberLiveChromePalette()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacingTokens.DoubleExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium)
    ) {
        Box(
            modifier = Modifier
                .size(visualSpec.emptyStateContainerSizeDp.dp)
                .clip(CircleShape)
                .background(palette.surfaceMuted),
            contentAlignment = Alignment.Center
        ) {
            AppIcon(
                imageVector = Icons.Outlined.NotificationsNone,
                contentDescription = null,
                tint = palette.secondaryText,
                modifier = Modifier.size(visualSpec.emptyStateIconSizeDp.dp)
            )
        }
        AppText(
            text = message,
            color = palette.secondaryText,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

private fun LiveRoomItem.toLiveRoomCardUiModel() = LiveRoomCardUiModel(
    roomId = roomId,
    title = title,
    coverUrl = cover.ifBlank { face },
    hostName = uname,
    viewerCount = online,
    areaName = areaName,
)

/**
 * 赛事入口（打开官方比赛中心 Web 页）
 */
@Composable
private fun LiveMatchEntry(
    onClick: () -> Unit
) {
    val palette = rememberLiveChromePalette()
    AppSurface(
        onClick = onClick,
        shape = AppShapes.borderedContainer(ContainerLevel.Card),
        color = AppSurfaceTokens.cardContainer(),
        border = BorderStroke(AppSurfaceTokens.OutlineWidth, palette.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AppSpacingTokens.Large,
                vertical = AppSpacingTokens.Medium
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(
                imageVector = Icons.Outlined.EmojiEvents,
                contentDescription = null,
                tint = palette.accentStrong,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(AppSpacingTokens.Medium))
            Column(modifier = Modifier.weight(1f)) {
                AppText(
                    text = "电竞赛事",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                AppText(
                    text = "热门赛事直播聚合",
                    color = palette.secondaryText,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            AppIcon(
                imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = palette.secondaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
