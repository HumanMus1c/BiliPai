package com.android.purebilibili.feature.home

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class HomeEmbeddedTopTabPageStructureTest {

    @Test
    fun homePagerEmbedsLiveAndBangumiIndependentPages() {
        val source = File("src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt").readText()
        assertTrue(source.contains("shouldEmbedLivePageInHomeTopTab(category)"))
        assertTrue(source.contains("LiveListScreen("))
        assertTrue(source.contains("embeddedInHome = true"))
        assertTrue(source.contains("shouldEmbedBangumiPageInHomeTopTab(category)"))
        assertTrue(source.contains("HomeBangumiTabPage("))
        assertTrue(source.contains("scrollToTopRequestId = liveScrollToTopRequestId"))
        assertTrue(source.contains("scrollToTopRequestId = bangumiScrollToTopRequestId"))
        assertTrue(source.contains("scrollToTopRequestId = partitionScrollToTopRequestId"))
        assertTrue(source.contains("resolveHomeTopTabScrollTarget(entry)"))
        assertTrue(!source.contains("onLiveListClick()\n                    return@onCategorySelected"))
    }

    @Test
    fun liveListScreenSupportsHomeEmbeddedChrome() {
        val source = File("src/main/java/com/android/purebilibili/feature/live/LiveListScreen.kt").readText()
        assertTrue(source.contains("embeddedInHome: Boolean = false"))
        assertTrue(source.contains("if (!embeddedInHome)"))
        assertTrue(source.contains("label = \"已关注\""))
        assertTrue(source.contains("onAreaSelected(LIVE_HOME_FOLLOWED_INDEX)"))
    }

    @Test
    fun bangumiHomePageCollapsesCategoryTabsOnScroll() {
        val source = File("src/main/java/com/android/purebilibili/feature/bangumi/HomeBangumiTabPage.kt").readText()
        assertTrue(source.contains("AnimatedVisibility("))
        assertTrue(source.contains("onHomeScrollChanged"))
        assertTrue(source.contains("categoryTabsVisible"))
        assertTrue(source.contains("scrollToTopRequestId"))
        assertTrue(source.contains("listBottomPadding = contentPadding.calculateBottomPadding()"))
        assertTrue(!source.contains(".padding(contentPadding)"))
    }

    @Test
    fun bangumiPageBackdropIsASiblingSharedByAllTabRows() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/bangumi/HomeBangumiTabPage.kt"
        ).readText()

        assertTrue(source.contains("miuixBackdrop = channelBackdrop"))
        assertTrue(source.contains(".layerBackdrop(channelBackdrop)"))
        assertTrue(source.contains(".background(MaterialTheme.colorScheme.background)"))
        assertTrue(source.contains("tabBackdrop = channelBackdrop"))
        assertTrue(
            source.indexOf(".layerBackdrop(channelBackdrop)") <
                source.indexOf("Column(modifier = Modifier.fillMaxSize())")
        )
    }
}
