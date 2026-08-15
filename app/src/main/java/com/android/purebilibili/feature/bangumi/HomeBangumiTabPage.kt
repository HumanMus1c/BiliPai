package com.android.purebilibili.feature.bangumi

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.feature.download.DownloadManager
import kotlinx.coroutines.launch

/**
 * 首页顶栏「追番」独立页：留在首页 Pager 内，
 * 不切走导航，也不再套一层带返回的番剧二级页。
 */
@Composable
fun HomeBangumiTabPage(
    contentPadding: PaddingValues,
    onBangumiClick: (Long) -> Unit,
    onBangumiEpisodeClick: (Long, Long) -> Unit,
    initialType: Int = 1,
    scrollToTopRequestId: Int = 0,
    viewModel: BangumiHubViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var categoryTabsVisible by remember { mutableStateOf(true) }
    val channelOptions = remember {
        BangumiChannel.entries.map { AppSegmentOption(it, it.label) }
    }

    LaunchedEffect(initialType) { viewModel.initialize(initialType) }
    LaunchedEffect(scrollToTopRequestId) {
        if (scrollToTopRequestId > 0) {
            categoryTabsVisible = true
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = contentPadding.calculateStartPadding(layoutDirection),
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(layoutDirection),
            )
    ) {
        AnimatedVisibility(
            visible = categoryTabsVisible,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            BangumiLiquidAwareTabRow(
                options = channelOptions,
                selectedValue = state.channel,
                onSelectionChange = viewModel::selectChannel,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        BangumiHubContent(
            state = state,
            onBangumiClick = onBangumiClick,
            onEpisodeClick = onBangumiEpisodeClick,
            onRefreshHome = { viewModel.refreshHome() },
            onLoadMoreHomeRecommendations = viewModel::loadMoreHomeRecommendations,
            onLoadMoreHomeFollows = viewModel::loadMoreHomeFollows,
            onRetryTimeline = viewModel::retryTimeline,
            onOpenIndex = viewModel::openIndex,
            onOpenFollow = viewModel::openFollowManager,
            onIndexCategorySelected = viewModel::selectIndexCategory,
            onIndexFilterSelected = viewModel::selectIndexFilter,
            onToggleFiltersExpanded = viewModel::toggleIndexFiltersExpanded,
            onRetryIndexConditions = viewModel::retryIndexConditions,
            onRetryIndexResults = viewModel::retryIndexResults,
            onLoadMoreIndexResults = viewModel::loadMoreIndexResults,
            onFollowStatusSelected = viewModel::selectFollowStatus,
            onRefreshFollow = viewModel::refreshFollowManager,
            onLoadMoreFollow = viewModel::loadMoreFollowManager,
            onToggleFollowSelection = viewModel::toggleFollowSelection,
            onSelectAllFollow = viewModel::selectAllFollowItems,
            onClearFollowSelection = viewModel::clearFollowSelection,
            onMoveSelectedFollow = viewModel::moveSelectedFollowItems,
            onMoveSingleFollow = viewModel::updateSingleFollowItem,
            onUnfollowSingle = viewModel::unfollowSingleItem,
            onLoadMoreSearch = viewModel::loadMoreSearch,
            onSaveCover = { url, title ->
                scope.launch {
                    DownloadManager.saveImageToGallery(context, url, title)
                }
            },
            onHomeScrollChanged = { firstVisibleIndex, scrollOffset ->
                categoryTabsVisible = firstVisibleIndex <= 0 && scrollOffset <= 0
            },
            scrollToTopRequestId = scrollToTopRequestId,
            listBottomPadding = contentPadding.calculateBottomPadding(),
        )
    }
}
