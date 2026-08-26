package com.android.purebilibili.feature.home.components.cards

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HorizontalVideoCardLayoutPolicyTest {

    @Test
    fun coverUsesSixteenByTen() {
        assertEquals(16f / 10f, HORIZONTAL_VIDEO_CARD_COVER_ASPECT_RATIO)
        assertEquals(87.5f, resolveHorizontalVideoCoverHeightDp(140f, 16f / 10f))
    }

    @Test
    fun coverHeightNeverUsesSubUnityAspect() {
        assertEquals(140f, resolveHorizontalVideoCoverHeightDp(140f, 0.5f))
    }

    @Test
    fun horizontalCardDoesNotLockInfoColumnToCoverHeight() {
        val source = java.io.File(
            "src/main/java/com/android/purebilibili/feature/home/components/cards/HomeStyleSingleColumnVideoCard.kt",
        ).let { file ->
            listOf(file, java.io.File("app/${file.path}")).first { it.exists() }.readText()
        }
        assertTrue(source.contains("HORIZONTAL_VIDEO_CARD_COVER_ASPECT_RATIO"))
        assertTrue(source.contains("HorizontalVideoStatRow("))
        assertTrue(source.contains("modifier = Modifier.fillMaxWidth()"))
        assertTrue(!source.contains(".height(coverHeight)"))
    }

    @Test
    fun gridCardOnlyTruncatesTitleWhileKeepingEnabledMetadataComplete() {
        val source = java.io.File(
            "src/main/java/com/android/purebilibili/feature/home/components/cards/VideoCard.kt",
        ).let { file ->
            listOf(file, java.io.File("app/${file.path}")).first { it.exists() }.readText()
        }

        assertTrue(
            source.contains(
                "durationText = durationText.takeIf { showDurationOutside }.orEmpty()"
            )
        )
        assertFalse(
            source.substringAfter("if (scrollLitePolicy.showSecondaryStatsRow)")
                .substringBefore("VideoCardOwnerMetadata(")
                .contains("overflow = TextOverflow.Ellipsis")
        )
        assertTrue(source.contains("maxLines = Int.MAX_VALUE"))
        assertTrue(source.contains("metaMaxLines = Int.MAX_VALUE"))
        assertTrue(
            source.substringAfter("text = highlightedTitle ?: AnnotatedString(video.title)")
                .substringBefore("style = contentTypography.title")
                .contains("overflow = videoCardTitleOverflow()")
        )
        assertTrue(source.contains("maxLines = videoCardTitleMaxLines(titleMaxLines)"))
        assertTrue(
            source.substringAfter("internal fun VideoCardDurationPublishRow(")
                .substringBefore("private fun VideoCardPublishTime(")
                .contains("FlowRow(")
        )
    }

    @Test
    fun statRowWrapsInsteadOfClippingTrailingUnits() {
        val source = java.io.File(
            "src/main/java/com/android/purebilibili/feature/home/components/cards/HorizontalVideoCardStats.kt",
        ).let { file ->
            listOf(file, java.io.File("app/${file.path}")).first { it.exists() }.readText()
        }

        assertTrue(source.contains("FlowRow("))
        assertTrue(source.contains("HORIZONTAL_VIDEO_STAT_WRAP_SPACING_DP"))
        assertTrue(source.contains("Icons.Outlined.PlayCircleOutline"))
        assertTrue(source.contains("Icons.Outlined.Subtitles"))
    }

    @Test
    fun videoCardSurfacesReuseTheRelatedVideoStatRow() {
        val paths = listOf(
            "feature/home/components/cards/VideoCard.kt",
            "feature/home/components/cards/HomeStyleSingleColumnVideoCard.kt",
            "feature/home/components/cards/StoryVideoCard.kt",
            "feature/home/components/cards/CinematicVideoCard.kt",
            "feature/home/components/cards/GlassVideoCard.kt",
            "feature/video/ui/components/RelatedVideoItem.kt",
            "feature/space/SpaceScreen.kt",
            "feature/search/SearchScreen.kt",
            "feature/dynamic/components/VideoCards.kt",
            "feature/list/FavoritePersonalCard.kt",
            "feature/watchlater/WatchLaterScreen.kt",
        )

        paths.forEach { relativePath ->
            val source = java.io.File("src/main/java/com/android/purebilibili/$relativePath")
                .let { file ->
                    listOf(file, java.io.File("app/${file.path}")).first { it.exists() }.readText()
                }
            assertTrue(
                source.contains("HorizontalVideoStatRow("),
                "$relativePath must reuse the related-video stat row",
            )
        }
    }
}
