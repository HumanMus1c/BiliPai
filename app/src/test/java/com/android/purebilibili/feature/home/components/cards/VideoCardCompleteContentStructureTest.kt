package com.android.purebilibili.feature.home.components.cards

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoCardCompleteContentStructureTest {

    private val completeContentCardSources = listOf(
        "feature/home/components/cards/VideoCard.kt",
        "feature/home/components/cards/StoryVideoCard.kt",
        "feature/home/components/cards/GlassVideoCard.kt",
        "feature/home/components/cards/CinematicVideoCard.kt",
        "feature/home/components/cards/HomeStyleSingleColumnVideoCard.kt",
        "feature/dynamic/components/VideoCards.kt",
        "feature/list/FavoritePersonalCard.kt",
        "feature/list/HistoryPersonalCard.kt",
        "feature/video/ui/components/RelatedVideoItem.kt",
        "feature/video/ui/components/UpPreviewSheet.kt",
        "feature/watchlater/WatchLaterScreen.kt",
    )

    @Test
    fun `video card implementations do not ellipsize visible content`() {
        completeContentCardSources.forEach { relativePath ->
            val source = loadSource(relativePath)
            assertFalse(
                source.contains("TextOverflow.Ellipsis"),
                "$relativePath must wrap or expand visible video-card content",
            )
        }
    }

    @Test
    fun `grid search and space cards expand their primary text`() {
        val searchCard = loadSource("feature/search/SearchScreen.kt")
            .substringAfter("fun SearchResultCard(")
            .substringBefore("internal fun UpSearchResultCard(")
        val spaceCards = loadSource("feature/space/SpaceScreen.kt")
            .substringAfter("private fun SpaceHomeVideoCard(")
            .substringBefore("private fun SpaceNoticeCard(")

        assertFalse(searchCard.contains("TextOverflow.Ellipsis"))
        assertTrue(searchCard.contains("FlowRow("))
        assertFalse(spaceCards.contains("TextOverflow.Ellipsis"))
    }

    private fun loadSource(relativePath: String): String {
        val path = "src/main/java/com/android/purebilibili/$relativePath"
        return listOf(File(path), File("app/$path")).first(File::exists).readText()
    }
}
