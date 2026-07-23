package com.android.purebilibili.feature.dynamic

import com.android.purebilibili.core.util.appendDistinctByKey
import com.android.purebilibili.core.util.prependDistinctByKey
import com.android.purebilibili.data.model.response.DynamicItem
import kotlinx.collections.immutable.toImmutableList

private const val DynamicTopBarReservedHeightDp = 60
private const val DynamicHorizontalExpandedHeaderReservedHeightDp = 184

internal data class DynamicPagePresentation(
    val items: List<DynamicItem>,
    val isLoading: Boolean,
    val error: String?,
    val hasMore: Boolean,
    val isSelectedUserFeed: Boolean,
    val incrementalRefreshBoundaryKey: String?,
    val incrementalPrependedCount: Int
)

internal fun resolveDynamicPagePresentation(
    state: DynamicUiState,
    logicalTab: Int,
    selectedUserId: Long?
): DynamicPagePresentation {
    val isSelectedUserFeed = shouldUseSelectedUserDynamicFeed(logicalTab, selectedUserId)
    if (logicalTab == 4) {
        if (!isSelectedUserFeed) {
            return DynamicPagePresentation(emptyList(), false, null, false, false, null, 0)
        }
        val items = resolveSelectedUserVisibleItems(
            timelineItems = state.timelinePage("all").items,
            remoteUserItems = state.userItems,
            selectedUid = selectedUserId
        ).distinctBy { it.id_str }
        return DynamicPagePresentation(
            items = items,
            isLoading = state.userIsLoading,
            error = state.userError,
            hasMore = state.hasUserMore && (
                state.userItems.isNotEmpty() || state.userIsLoading || !state.userError.isNullOrBlank()
            ),
            isSelectedUserFeed = true,
            incrementalRefreshBoundaryKey = null,
            incrementalPrependedCount = 0
        )
    }

    val page = state.timelinePage(resolveDynamicFeedRequestType(logicalTab))
    val items = when (logicalTab) {
        1 -> page.items.filter(::shouldIncludeDynamicItemInVideoTab)
        2 -> page.items.filter(::shouldIncludeDynamicItemInPgcTab)
        3 -> page.items.filter(::shouldIncludeDynamicItemInArticleTab)
        else -> page.items
    }.distinctBy { it.id_str }
    return DynamicPagePresentation(
        items = items,
        isLoading = page.isLoading,
        error = page.error,
        hasMore = page.hasMore,
        isSelectedUserFeed = false,
        incrementalRefreshBoundaryKey = page.incrementalRefreshBoundaryKey,
        incrementalPrependedCount = page.incrementalPrependedCount
    )
}

internal fun resolveDynamicListTopPaddingExtraDp(
    isHorizontalMode: Boolean,
    isHorizontalUserListCollapsed: Boolean = false,
    shouldShowHorizontalUserList: Boolean = true,
    isTopBarCollapsed: Boolean = false
): Int {
    return when {
        isTopBarCollapsed -> 0
        // 横向关注列表展开时，头像下方可能同时有直播标记和 UP 名称两行。
        isHorizontalMode && shouldShowHorizontalUserList && !isHorizontalUserListCollapsed -> DynamicHorizontalExpandedHeaderReservedHeightDp
        isHorizontalMode -> DynamicTopBarReservedHeightDp
        else -> DynamicTopBarReservedHeightDp
    }
}

internal fun shouldShowDynamicHorizontalUserList(
    isHorizontalMode: Boolean,
    selectedTab: Int,
    allTabHorizontalUserListVisible: Boolean
): Boolean {
    if (!isHorizontalMode) return false
    return selectedTab == 4 || allTabHorizontalUserListVisible
}

internal fun shouldCollapseDynamicHorizontalUserList(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    topTolerancePx: Int = 8
): Boolean {
    return firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > topTolerancePx
}

/**
 * Tab top-bar collapse is optional. When [collapseOnScrollEnabled] is false the bar stays pinned;
 * the horizontal UP list still uses [shouldCollapseDynamicHorizontalUserList] independently.
 */
internal fun shouldCollapseDynamicTopBar(
    collapseOnScrollEnabled: Boolean,
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    topTolerancePx: Int = 8
): Boolean {
    if (!collapseOnScrollEnabled) return false
    return shouldCollapseDynamicHorizontalUserList(
        firstVisibleItemIndex = firstVisibleItemIndex,
        firstVisibleItemScrollOffset = firstVisibleItemScrollOffset,
        topTolerancePx = topTolerancePx
    )
}

internal fun resolveDynamicSelectedUserIdAfterClick(
    selectedUserId: Long?,
    clickedUserId: Long?
): Long? {
    if (clickedUserId == null) return null
    return if (selectedUserId == clickedUserId) null else clickedUserId
}

internal fun shouldUseSelectedUserDynamicFeed(
    selectedTab: Int,
    selectedUserId: Long?
): Boolean {
    return selectedTab == 4 && selectedUserId != null
}

internal fun resolveDynamicSelectedUserForTab(
    selectedTab: Int,
    selectedUserId: Long?
): Long? {
    return selectedUserId.takeIf { selectedTab == 4 }
}

internal fun shouldResetDynamicFeedScrollOnSourceChange(
    previousTab: Int,
    nextTab: Int,
    previousSelectedUserId: Long?,
    nextSelectedUserId: Long?
): Boolean {
    return previousTab != nextTab || previousSelectedUserId != nextSelectedUserId
}

internal fun resolveDynamicTabAfterUserSelection(
    selectedUserId: Long?,
    clickedUserId: Long?,
    currentTab: Int
): Int {
    val nextUserId = resolveDynamicSelectedUserIdAfterClick(selectedUserId, clickedUserId)
    return when {
        nextUserId != null -> 4
        currentTab == 4 -> 0
        else -> currentTab
    }
}

internal fun resolveDynamicSelectedTab(
    savedTab: Int?,
    tabCount: Int
): Int {
    if (tabCount <= 0) return 0
    return savedTab?.takeIf { it in 0 until tabCount } ?: 0
}

internal fun resolveDynamicSwipeTargetTab(
    currentTab: Int,
    tabCount: Int,
    dragDistancePx: Float,
    thresholdPx: Float = 96f
): Int? {
    if (tabCount <= 0 || currentTab !in 0 until tabCount) return null
    if (kotlin.math.abs(dragDistancePx) < thresholdPx) return null
    val target = if (dragDistancePx < 0f) currentTab + 1 else currentTab - 1
    return target.takeIf { it in 0 until tabCount && it != currentTab }
}

internal fun resolveDynamicFeedRequestType(selectedTab: Int): String {
    return when (selectedTab) {
        1 -> "video"
        2 -> "pgc"
        3 -> "article"
        else -> "all"
    }
}

internal fun shouldUseServerFilteredDynamicFeed(selectedTab: Int): Boolean {
    return selectedTab in 1..3
}

internal fun resolveHorizontalUserListVerticalPaddingDp(): Int {
    return 4
}

internal fun shouldShowDynamicErrorOverlay(
    error: String?,
    activeItemsCount: Int
): Boolean {
    return !error.isNullOrBlank() && activeItemsCount == 0
}

internal fun shouldShowDynamicLoadingFooter(
    isLoading: Boolean,
    activeItemsCount: Int
): Boolean {
    return isLoading && activeItemsCount > 0
}

internal fun shouldShowDynamicNoMoreFooter(
    hasMore: Boolean,
    activeItemsCount: Int
): Boolean {
    return !hasMore && activeItemsCount > 0
}

internal fun shouldRevealDynamicBottomBarForStaticContent(
    activeItemsCount: Int,
    isLoading: Boolean
): Boolean {
    return activeItemsCount == 0 && !isLoading
}

internal fun shouldShowDynamicCommentSheet(selectedDynamicId: String?): Boolean {
    return !selectedDynamicId.isNullOrBlank()
}

/**
 * Detail page should open comments once on entry so users who already
 * tapped into a dynamic do not need a second "评论" tap to read replies.
 * Keep false after the first open so dismiss is respected.
 */
internal fun shouldAutoOpenCommentsOnDynamicDetailEntry(
    hasAlreadyOpened: Boolean
): Boolean = !hasAlreadyOpened

internal fun resolveDynamicCommentSheetTotalCount(
    liveCount: Int,
    fallbackCount: Int
): Int {
    return if (liveCount > 0) liveCount else fallbackCount.coerceAtLeast(0)
}

internal fun resolveDynamicStateAfterAuthorUnfollow(
    currentState: DynamicUiState,
    authorMid: Long
): DynamicUiState {
    if (authorMid <= 0L) return currentState
    return mapDynamicTimelineItems(currentState) { items ->
        items.filterNot { it.modules.module_author?.mid == authorMid }
    }.copy(
        userItems = currentState.userItems.filterNot { it.modules.module_author?.mid == authorMid }.toImmutableList()
    )
}

internal fun resolveFollowedUsersAfterAuthorUnfollow(
    users: List<SidebarUser>,
    authorMid: Long
): List<SidebarUser> {
    if (authorMid <= 0L) return users
    return users.filterNot { it.uid == authorMid }
}

internal fun shouldResetFollowedUserListToTopOnRefresh(
    boundaryKey: String?,
    prependedCount: Int,
    selectedUserId: Long?,
    handledBoundaryKey: String?
): Boolean {
    if (boundaryKey.isNullOrBlank()) return false
    if (prependedCount <= 0) return false
    if (selectedUserId != null) return false
    return boundaryKey != handledBoundaryKey
}

enum class DynamicFeedErrorSource {
    NONE,
    INITIAL_LOAD,
    REFRESH,
    APPEND
}

internal fun DynamicUiState.timelinePage(requestType: String): DynamicTimelinePageState {
    timelinePages[requestType]?.let { return it }
    if (timelineRequestType != requestType) return DynamicTimelinePageState()
    return DynamicTimelinePageState(
        items = items,
        isLoading = isLoading,
        error = error,
        hasMore = hasMore,
        incrementalRefreshBoundaryKey = incrementalRefreshBoundaryKey,
        incrementalPrependedCount = incrementalPrependedCount,
        errorSource = errorSource
    )
}

internal fun updateDynamicTimelinePage(
    currentState: DynamicUiState,
    requestType: String,
    transform: (DynamicTimelinePageState) -> DynamicTimelinePageState
): DynamicUiState {
    val updatedPage = transform(currentState.timelinePage(requestType))
    val updatedState = currentState.copy(
        timelinePages = currentState.timelinePages.put(requestType, updatedPage)
    )
    return if (currentState.timelineRequestType == requestType) {
        updatedState.copyActiveTimelinePage(requestType, updatedPage)
    } else {
        updatedState
    }
}

internal fun DynamicUiState.selectTimelinePage(requestType: String): DynamicUiState {
    return copyActiveTimelinePage(requestType, timelinePage(requestType))
}

internal fun mapDynamicTimelineItems(
    currentState: DynamicUiState,
    transform: (List<DynamicItem>) -> List<DynamicItem>
): DynamicUiState {
    val requestTypes = currentState.timelinePages.keys + currentState.timelineRequestType
    return requestTypes.fold(currentState) { state, requestType ->
        updateDynamicTimelinePage(state, requestType) { page ->
            page.copy(items = transform(page.items).toImmutableList())
        }
    }
}

private fun DynamicUiState.copyActiveTimelinePage(
    requestType: String,
    page: DynamicTimelinePageState
): DynamicUiState {
    return copy(
        items = page.items,
        timelineRequestType = requestType,
        isLoading = page.isLoading,
        error = page.error,
        hasMore = page.hasMore,
        incrementalRefreshBoundaryKey = page.incrementalRefreshBoundaryKey,
        incrementalPrependedCount = page.incrementalPrependedCount,
        errorSource = page.errorSource
    )
}

internal fun resolveDynamicTimelinePageForLoadStart(
    currentPage: DynamicTimelinePageState,
    refresh: Boolean,
    showLoading: Boolean
): DynamicTimelinePageState {
    val basePage = currentPage.copy(
        error = null,
        errorSource = DynamicFeedErrorSource.NONE
    )
    return when {
        refresh && showLoading -> basePage.copy(isLoading = true)
        !refresh -> basePage.copy(isLoading = true)
        else -> basePage
    }
}

internal fun resolveDynamicTimelinePageAfterSuccess(
    currentPage: DynamicTimelinePageState,
    incomingItems: List<DynamicItem>,
    isRefresh: Boolean,
    incrementalRefreshEnabled: Boolean,
    hasMore: Boolean
): DynamicTimelinePageState {
    val currentItems = currentPage.items
    val canUseIncrementalRefresh = isRefresh && incrementalRefreshEnabled
    val mergedItems = when {
        canUseIncrementalRefresh -> sortDynamicTimelineItemsByPublishTime(
            prependDistinctByKey(
                existing = currentItems,
                incoming = incomingItems,
                keySelector = ::dynamicFeedItemKey
            )
        )
        isRefresh -> sortDynamicTimelineItemsByPublishTime(incomingItems)
        else -> appendDistinctByKey(
            existing = currentItems,
            incoming = incomingItems,
            keySelector = ::dynamicFeedItemKey
        )
    }
    val boundary = when {
        canUseIncrementalRefresh -> resolveIncrementalRefreshBoundary(
            existingKeys = currentItems.map(::dynamicFeedItemKey),
            mergedKeys = mergedItems.map(::dynamicFeedItemKey)
        )
        isRefresh -> IncrementalRefreshBoundary(null, 0)
        else -> IncrementalRefreshBoundary(
            currentPage.incrementalRefreshBoundaryKey,
            currentPage.incrementalPrependedCount
        )
    }
    return currentPage.copy(
        items = mergedItems.toImmutableList(),
        isLoading = false,
        error = null,
        hasMore = hasMore,
        incrementalRefreshBoundaryKey = boundary.boundaryKey,
        incrementalPrependedCount = boundary.prependedCount,
        errorSource = DynamicFeedErrorSource.NONE
    )
}

internal fun resolveDynamicTimelinePageAfterFailure(
    currentPage: DynamicTimelinePageState,
    errorMessage: String,
    refresh: Boolean
): DynamicTimelinePageState {
    val source = when {
        currentPage.items.isEmpty() -> DynamicFeedErrorSource.INITIAL_LOAD
        refresh -> DynamicFeedErrorSource.REFRESH
        else -> DynamicFeedErrorSource.APPEND
    }
    return currentPage.copy(
        isLoading = false,
        error = errorMessage,
        errorSource = source
    )
}

internal fun resolveDynamicActiveLoadingState(
    currentState: DynamicUiState,
    selectedUserId: Long?
): Boolean {
    return if (selectedUserId != null) currentState.userIsLoading else currentState.isLoading
}

internal fun resolveDynamicActiveError(
    currentState: DynamicUiState,
    selectedUserId: Long?
): String? {
    return if (selectedUserId != null) currentState.userError else currentState.error
}

internal fun resolveDynamicFeedStateForLoadStart(
    currentState: DynamicUiState,
    refresh: Boolean,
    showLoading: Boolean
): DynamicUiState {
    val baseState = currentState.copy(
        error = null,
        errorSource = DynamicFeedErrorSource.NONE
    )
    return when {
        refresh && showLoading -> baseState.copy(isLoading = true)
        !refresh -> baseState.copy(isLoading = true)
        else -> baseState
    }
}

internal fun resolveDynamicFeedStateAfterSuccess(
    currentState: DynamicUiState,
    incomingItems: List<DynamicItem>,
    isRefresh: Boolean,
    requestType: String,
    incrementalRefreshEnabled: Boolean,
    hasMore: Boolean
): DynamicUiState {
    val currentItems = currentState.items
    val canUseIncrementalRefresh = isRefresh &&
        incrementalRefreshEnabled &&
        currentState.timelineRequestType == requestType
    val mergedItems = when {
        canUseIncrementalRefresh -> sortDynamicTimelineItemsByPublishTime(
            prependDistinctByKey(
                existing = currentItems,
                incoming = incomingItems,
                keySelector = ::dynamicFeedItemKey
            )
        )
        isRefresh -> sortDynamicTimelineItemsByPublishTime(incomingItems)
        else -> appendDistinctByKey(
            existing = currentItems,
            incoming = incomingItems,
            keySelector = ::dynamicFeedItemKey
        )
    }
    val boundary = when {
        canUseIncrementalRefresh -> resolveIncrementalRefreshBoundary(
            existingKeys = currentItems.map(::dynamicFeedItemKey),
            mergedKeys = mergedItems.map(::dynamicFeedItemKey)
        )
        isRefresh -> IncrementalRefreshBoundary(
            boundaryKey = null,
            prependedCount = 0
        )
        else -> IncrementalRefreshBoundary(
            boundaryKey = currentState.incrementalRefreshBoundaryKey,
            prependedCount = currentState.incrementalPrependedCount
        )
    }
    return currentState.copy(
        items = mergedItems.toImmutableList(),
        isLoading = false,
        error = null,
        errorSource = DynamicFeedErrorSource.NONE,
        hasMore = hasMore,
        timelineRequestType = requestType,
        incrementalRefreshBoundaryKey = boundary.boundaryKey,
        incrementalPrependedCount = boundary.prependedCount
    )
}

internal fun sortDynamicTimelineItemsByPublishTime(items: List<DynamicItem>): List<DynamicItem> {
    if (items.size <= 1) return items
    return items
        .mapIndexed { index, item -> index to item }
        .sortedWith(
            compareByDescending<Pair<Int, DynamicItem>> { (_, item) ->
                item.modules.module_author?.pub_ts ?: 0L
            }.thenBy { (index, _) -> index }
        )
        .map { (_, item) -> item }
}

internal fun resolveDynamicFeedStateAfterFailure(
    currentState: DynamicUiState,
    errorMessage: String,
    refresh: Boolean
): DynamicUiState {
    val source = when {
        currentState.items.isEmpty() -> DynamicFeedErrorSource.INITIAL_LOAD
        refresh -> DynamicFeedErrorSource.REFRESH
        else -> DynamicFeedErrorSource.APPEND
    }
    return currentState.copy(
        isLoading = false,
        error = errorMessage,
        errorSource = source
    )
}
