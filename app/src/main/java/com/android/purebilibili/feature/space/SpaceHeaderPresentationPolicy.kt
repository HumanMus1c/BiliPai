package com.android.purebilibili.feature.space

import com.android.purebilibili.data.model.response.RelationStatData
import com.android.purebilibili.data.model.response.UpStatData

internal data class SpaceHeaderMetricItem(
    val label: String,
    val value: Long
)

internal fun resolveSpaceHeaderMetricItems(
    relationStat: RelationStatData?,
    upStat: UpStatData?
): List<SpaceHeaderMetricItem> {
    return listOf(
        SpaceHeaderMetricItem("粉丝", relationStat?.follower?.toLong() ?: 0L),
        SpaceHeaderMetricItem("关注", relationStat?.following?.toLong() ?: 0L),
        SpaceHeaderMetricItem("获赞", upStat?.likes ?: 0L)
    )
}

internal const val SPACE_PINNED_TOP_CHROME_FADE_RANGE_PX = 120

/** 0 at rest over the banner, 1 after the header has scrolled under the pinned chrome. */
internal fun resolveSpacePinnedTopChromeScrim(
    firstVisibleItemIndex: Int,
    firstVisibleItemScrollOffset: Int,
    fadeRangePx: Int = SPACE_PINNED_TOP_CHROME_FADE_RANGE_PX,
): Float {
    if (firstVisibleItemIndex > 0) return 1f
    if (fadeRangePx <= 0) return 0f
    return (firstVisibleItemScrollOffset.toFloat() / fadeRangePx).coerceIn(0f, 1f)
}
