package com.android.purebilibili.feature.space

import com.android.purebilibili.data.model.response.RelationStatData
import com.android.purebilibili.data.model.response.UpStatData
import kotlin.test.Test
import kotlin.test.assertEquals

class SpaceHeaderPresentationPolicyTest {

    @Test
    fun `header metrics keep compact relation and likes set`() {
        val metrics = resolveSpaceHeaderMetricItems(
            relationStat = RelationStatData(following = 24, follower = 1024),
            upStat = UpStatData(likes = 42_000)
        )

        assertEquals(
            listOf("粉丝", "关注", "获赞"),
            metrics.map { it.label }
        )
        assertEquals(
            listOf(1024L, 24L, 42_000L),
            metrics.map { it.value }
        )
    }

    @Test
    fun `header metrics fall back to zero for missing stats`() {
        val metrics = resolveSpaceHeaderMetricItems(
            relationStat = null,
            upStat = null
        )

        assertEquals(listOf(0L, 0L, 0L), metrics.map { it.value })
    }

    @Test
    fun `pinned top chrome stays clear at rest and solid after the header scrolls`() {
        assertEquals(
            0f,
            resolveSpacePinnedTopChromeScrim(
                firstVisibleItemIndex = 0,
                firstVisibleItemScrollOffset = 0,
            )
        )
        assertEquals(
            0.5f,
            resolveSpacePinnedTopChromeScrim(
                firstVisibleItemIndex = 0,
                firstVisibleItemScrollOffset = 60,
            )
        )
        assertEquals(
            1f,
            resolveSpacePinnedTopChromeScrim(
                firstVisibleItemIndex = 1,
                firstVisibleItemScrollOffset = 0,
            )
        )
    }
}
