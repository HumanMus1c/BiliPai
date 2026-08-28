package com.android.purebilibili.feature.space

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SpaceScreenStructureTest {

    @Test
    fun `space chrome uses liquid tab rows and piliplus actions`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")

        assertTrue(source.contains("AppNativeTabRow("))
        assertTrue(source.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(source.contains("AppThemeAdaptiveTabRow("))
        assertTrue(source.contains("scrollable = spec.scrollable"))
        assertFalse(source.contains("forceLiquidChrome = true"))
        assertTrue(source.contains("SpaceSecondarySwitchRow("))
        assertTrue(source.contains("resolveSpacePrimaryTab(selectedMainTab)"))
        assertTrue(source.contains("showTabRail = false"))
        assertTrue(source.contains("onFollowingClick"))
        assertTrue(source.contains("onFansClick"))
        assertTrue(source.contains("Intent.ACTION_SEND"))
        assertFalse(source.contains("暂不支持私信"))
        assertFalse(source.contains("AppFilterChip("))
    }

    @Test
    fun `contribution videos render as grid cards instead of full width rows`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")

        assertTrue(source.contains("columns = GridCells.Fixed("))
        assertTrue(source.contains("resolveSpaceContentGridColumnCount("))
        assertTrue(source.contains("SpaceContributionVideoLayoutMode.GRID"))
        assertTrue(source.contains("SpaceHomeVideoCard("))
        assertTrue(source.contains("resolveSpaceContributionVideoItemKey("))
        assertFalse(source.contains("SpaceVideoListItemRow("))
    }

    @Test
    fun `space grid video card follows the home cover overlay treatment`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")
        val cardSource = source
            .substringAfter("private fun SpaceHomeVideoCard(")
            .substringBefore("private fun SpaceAggregateMediaCard(")

        assertTrue(cardSource.contains("Brush.verticalGradient"))
        assertTrue(cardSource.contains("resolveVideoCardCoverOverlayTextShadow"))
        assertTrue(cardSource.contains("HorizontalVideoStatRow("))
        assertFalse(cardSource.contains("color = Color.Black.copy(alpha = 0.72f)"))
        assertTrue(source.contains("VideoCardCoverDurationText("))
        assertFalse(source.contains("color = Color.Black.copy(alpha = 0.72f)"))
    }

    @Test
    fun `contribution videos switch layout without dual placing lazy grid content`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")
        val contributionVideoItems = source
            .substringAfter("items(\n                            items = state.videos")
            .substringBefore("if (state.isLoadingMore)")

        assertTrue(source.contains("showContributionVideoMenuActions"))
        assertTrue(source.contains("toggleSpaceContributionVideoLayoutMode"))
        assertTrue(source.contains("resolveSpaceContributionVideoGridSpan("))
        assertTrue(source.contains("resolveSpaceContributionVideoItemKey("))
        assertTrue(source.contains("SpaceContributionVideoLayoutMode.SINGLE_COLUMN"))
        assertTrue(source.contains("SpaceArchiveListItemRow("))
        assertFalse(contributionVideoItems.contains("Modifier.animateItem()"))
        assertFalse(contributionVideoItems.contains("AnimatedContent("))
        assertFalse(contributionVideoItems.contains("SizeTransform("))
    }

    @Test
    fun `space high frequency video covers join shared element transition`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")
        val horizontalArchiveCard = source
            .substringAfter("private fun SpaceArchiveListItemRow(")
            .substringBefore("private fun SpaceAudioListItem(")

        assertTrue(source.contains("sharedTransitionKey = resolveSpaceArchiveSharedTransitionKey(video.bvid)"))
        assertTrue(source.contains("sharedTransitionKey = resolveSpaceArchiveSharedTransitionKey(topVideo.bvid)"))
        assertTrue(source.contains("sharedTransitionKey = resolveSpaceArchiveSharedTransitionKey(item.bvid)"))
        assertTrue(source.contains("CardPositionManager.recordVideoCardPosition("))
        assertTrue(source.contains("videoCoverSharedElementKey("))
        assertTrue(source.contains("clipInOverlayDuringTransition = OverlayClip(coverShape)"))
        assertTrue(horizontalArchiveCard.contains("videoCardShellSharedBoundsOrEmpty("))
        assertTrue(horizontalArchiveCard.contains("crossfadeSourceContent = true"))
        assertFalse(horizontalArchiveCard.contains("videoTitleSharedElementKey("))
    }

    @Test
    fun `contribution video actions live in the top overflow menu`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")

        assertTrue(source.contains("AppWindowActionMenu("))
        assertTrue(source.contains("label = \"播放全部\""))
        assertTrue(source.contains("\"切换为双列\""))
        assertTrue(source.contains("\"切换为单列\""))
        assertTrue(source.contains("\"排序：${'$'}{resolveSpaceVideoSortCompactLabel"))
        assertTrue(source.contains("children = VideoSortOrder.entries.map"))
        assertTrue(source.contains("selected = currentSuccessState?.sortOrder == order"))
        assertFalse(source.contains("showVideoSortMenu"))
        assertFalse(source.contains("SpaceContributionToolbar("))
        assertFalse(source.contains("SpaceContributionVideoToolbarActions("))
        assertFalse(source.contains("SpaceContributionTabRow("))
        assertFalse(source.contains("SpaceContributionVideoActions("))
    }

    @Test
    fun `secondary contribution switch uses responsive liquid glass rail`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")
        val secondaryRow = source
            .substringAfter("private fun SpaceSecondarySwitchRow(")
            .substringBefore("private fun SpaceMainTabRow(")

        assertTrue(source.contains("SpaceSecondarySwitchRow("))
        assertTrue(secondaryRow.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(secondaryRow.contains("shouldScrollSpaceSecondarySwitch("))
        assertTrue(secondaryRow.contains("resolveSpaceSecondarySwitchAdaptiveItemWidthDp("))
        assertTrue(secondaryRow.contains("itemWidthDp = preferredItemWidthDp"))
        assertTrue(secondaryRow.contains(".clip(CircleShape)"))
        assertTrue(secondaryRow.contains(".horizontalScroll(scrollState)"))
        assertTrue(secondaryRow.contains("dragSelectionEnabled = spec.dragSelectionEnabled && !useScrollableRail"))
        assertTrue(secondaryRow.contains("longPressDragSelectionEnabled = useScrollableRail"))
        assertTrue(secondaryRow.contains("scrollState.dispatchRawDelta("))
        assertTrue(secondaryRow.contains("resolveSpaceSecondarySwitchDragScrollDeltaPx("))
        assertTrue(secondaryRow.contains("onIndicatorPositionChanged = { position ->"))
        assertFalse(secondaryRow.contains("AppFilterChip("))
        assertTrue(secondaryRow.contains("AppNativeTabRow("))
        assertTrue(secondaryRow.contains("homeSettings.androidNativeLiquidGlassEnabled"))
        assertFalse(source.contains("rememberTextMeasurer()"))
    }

    @Test
    fun `space media library uses card semantic corners`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")

        assertTrue(source.contains("AppShapes.borderedContainer(ContainerLevel.Card)"))
        assertTrue(source.contains("AppShapes.containerCornerDp(ContainerLevel.Card)"))
        assertFalse(source.contains("RoundedCornerShape("))
        assertFalse(source.contains("ContainerLevel.Dialog"))
        assertFalse(source.contains("sourceCornerDp = 12"))
        assertFalse(source.contains("sourceCornerDp = 14"))
    }

    @Test
    fun `space search action scrolls to focused search bar and dynamic body opens comments`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")

        assertTrue(source.contains("resolveSpaceSearchBarGridItemIndex("))
        assertTrue(source.contains("resolveSpaceSearchBarRevealScrollOffsetPx("))
        assertTrue(source.contains("scrollOffset = searchBarRevealScrollOffsetPx"))
        assertTrue(source.contains("val searchFocusRequester = remember { FocusRequester() }"))
        assertTrue(source.contains(".focusRequester(searchFocusRequester)"))
        assertTrue(source.contains("SpaceSearchEntryChip("))
        assertTrue(source.contains("onSearchEntryClick = { viewModel.setSearchMode(true) }"))
        // bordered Field shape avoids iOS continuous-corner + BorderStroke chamfer
        assertTrue(source.contains("AppShapes.borderedContainer(ContainerLevel.Field)"))
        assertTrue(source.contains("onPrimaryClickOverride = { onSpaceDynamicCommentClick(dynamic) }"))
    }

    @Test
    fun `space profile exposes copy actions for its identifying text`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")

        assertTrue(source.contains("copyOnLongPress(userInfo.name, \"UP主名称\")"))
        assertTrue(source.contains("copyOnLongPress(userInfo.sign, \"UP主简介\")"))
        assertTrue(source.contains("copyOnLongPress(userInfo.mid.toString(), \"UID\")"))
        assertTrue(source.contains("label = \"复制空间链接\""))
        assertTrue(source.contains("label = \"复制 UID\""))
        assertTrue(source.contains("label = \"分享\""))
        assertTrue(source.contains("label = \"举报\""))
        assertTrue(source.contains("https://space.bilibili.com/${'$'}mid"))
    }

    @Test
    fun `space official verify follows piliplus multiline badge presentation`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")
        val officialTag = source
            .substringAfter("private fun SpaceOfficialTag(")
            .substringBefore("private fun SpaceBadgeChip(")

        assertTrue(source.contains("userInfo.official.spliceTitle.ifBlank"))
        assertTrue(officialTag.contains("Icons.Outlined.Bolt"))
        assertTrue(officialTag.contains("Color(0xFFFFCC00)"))
        assertTrue(officialTag.contains("fontSize = 12.sp"))
        assertFalse(officialTag.contains("maxLines = 1"))
        assertFalse(officialTag.contains("TextOverflow.Ellipsis"))
        assertFalse(officialTag.contains("widthIn("))
    }

    @Test
    fun `space follow actions share the name and level row`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")

        assertTrue(source.contains("SpaceHeaderRelationActions("))
        assertTrue(source.contains("contentDescription = \"私信\""))
        assertTrue(source.contains("onMessageClick = onMessageClick"))
        assertTrue(source.contains("if (!isOwner)"))
        assertTrue(
            source.contains("名字 + 等级 + 私信/关注同一行垂直居中对齐"),
            "relation actions should align on the name/level row"
        )
        assertFalse(source.contains("resolveSpaceHeaderActionTopPaddingDp("))
        assertFalse(source.contains("topChromeInset = scaffoldPadding.calculateTopPadding()"))
    }

    @Test
    fun `space back button and up name stay in the pinned top bar`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")
        val header = source
            .substringAfter("private fun SpaceHeader(")
            .substringBefore("private fun SpaceMainTabRow(")

        assertTrue(source.contains("resolveSpacePinnedTopChromeScrim("))
        assertFalse(source.contains("nestedScroll(scrollBehavior.nestedScrollConnection)"))
        assertFalse(source.contains("scrollBehavior = scrollBehavior"))
        assertFalse(
            header.contains(".alpha(contentAlpha)"),
            "header name must not fade/slide with the collapsing banner"
        )
        assertFalse(
            header.contains("IntOffset(0, -translateYPx)"),
            "header must not translate the name row under the pinned chrome"
        )
    }

    @Test
    fun `played video locate prompt is configurable and scoped to each space visit`() {
        val source = loadSource("app/src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt")

        assertTrue(source.contains("getSpacePlayedVideoLocatePromptEnabled(context)"))
        assertTrue(source.contains("var playedVideoLocatePromptHandled by remember(mid, playedVideoBvid)"))
        assertFalse(source.contains("var playedVideoLocatePromptHandled by rememberSaveable"))
    }

    private fun loadSource(path: String): String {
        val normalizedPath = path.removePrefix("app/")
        val sourceFile = listOf(
            File(path),
            File(normalizedPath)
        ).firstOrNull { it.exists() }
        require(sourceFile != null) { "Cannot locate $path from ${File(".").absolutePath}" }
        return sourceFile.readText()
    }
}
