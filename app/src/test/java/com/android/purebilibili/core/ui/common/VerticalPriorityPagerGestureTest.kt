package com.android.purebilibili.core.ui.common

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VerticalPriorityPagerGestureTest {

    @Test
    fun `movement below extended touch slop stays undecided`() {
        assertEquals(
            PagerGestureDirection.UNDECIDED,
            resolveVerticalPriorityPagerGestureDirection(
                totalX = 9f,
                totalY = 9f,
                touchSlop = 10f,
            ),
        )
    }

    @Test
    fun `mostly vertical drag locks vertical`() {
        assertEquals(
            PagerGestureDirection.VERTICAL,
            resolveVerticalPriorityPagerGestureDirection(
                totalX = 8f,
                totalY = 20f,
                touchSlop = 8f,
            ),
        )
    }

    @Test
    fun `ambiguous diagonal drag prefers vertical content`() {
        assertEquals(
            PagerGestureDirection.VERTICAL,
            resolveVerticalPriorityPagerGestureDirection(
                totalX = 18f,
                totalY = 15f,
                touchSlop = 8f,
            ),
        )
    }

    @Test
    fun `clearly horizontal drag locks pager`() {
        assertEquals(
            PagerGestureDirection.HORIZONTAL,
            resolveVerticalPriorityPagerGestureDirection(
                totalX = 24f,
                totalY = 8f,
                touchSlop = 8f,
            ),
        )
    }

    @Test
    fun `home and phone comments use vertical priority pager input`() {
        val homeSource = File(
            "src/main/java/com/android/purebilibili/feature/home/HomeScreen.kt"
        ).readText()
        val videoContentSource = File(
            "src/main/java/com/android/purebilibili/feature/video/screen/VideoContentSection.kt"
        ).readText()

        val homePager = homeSource
            .substringAfter("val homeTopPagerSwipeEnabled")
            .substringAfter("HorizontalPager(")
            .substringBefore(") { page ->")
        assertTrue(homePager.contains("userScrollEnabled = false"))
        assertTrue(homePager.contains(".verticalPriorityHorizontalPagerSwipe("))

        val commentPager = videoContentSource
            .substringAfter("HorizontalPager(")
            .substringBefore(") { page ->")
        assertTrue(commentPager.contains("userScrollEnabled = false"))
        assertTrue(commentPager.contains(".verticalPriorityHorizontalPagerSwipe("))
    }

    @Test
    fun `other paged vertical lists use the shared direction gate`() {
        val expectedGateCounts = mapOf(
            "feature/search/SearchScreen.kt" to 1,
            "feature/dynamic/DynamicScreen.kt" to 2,
            "feature/list/CommonListScreen.kt" to 1,
            "feature/live/LiveAreaScreen.kt" to 1,
            "feature/bangumi/ui/player/BangumiPlayerContent.kt" to 1,
            "feature/video/screen/TabletVideoLayout.kt" to 1,
            "feature/video/screen/TabletCinemaLayout.kt" to 1,
        )

        expectedGateCounts.forEach { (relativePath, expectedCount) ->
            val source = File("src/main/java/com/android/purebilibili/$relativePath").readText()
            assertTrue(
                source.countOccurrences(".verticalPriorityHorizontalPagerSwipe(") >= expectedCount,
                "$relativePath should use the shared pager direction gate",
            )
            assertTrue(
                source.countOccurrences("userScrollEnabled = false") >= expectedCount,
                "$relativePath should disable the pager's competing built-in drag detector",
            )
        }
    }

    private fun String.countOccurrences(value: String): Int = split(value).size - 1
}
