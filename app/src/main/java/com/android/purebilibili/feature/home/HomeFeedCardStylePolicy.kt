package com.android.purebilibili.feature.home

import com.android.purebilibili.core.store.HomeFeedCardStyle

internal data class HomeFeedCardLayout(
    val coverAspectRatio: Float,
    val outerPaddingDp: Int,
    val itemSpacingDp: Int,
    val verticalItemSpacingDp: Int = itemSpacingDp,
    val storyCardHorizontalPaddingDp: Int,
    val compactMetadata: Boolean
)

/** 4:3 更高列表框。CDN 16:9 源图会左右裁。 */
internal const val HOME_FEED_OFFICIAL_COVER_ASPECT_RATIO = 4f / 3f

/** 16:10，介于 16:9 与 4:3 之间。 */
internal const val HOME_FEED_BILIPAI_COVER_ASPECT_RATIO = 16f / 10f

/** 与投稿/CDN 源同比例，标准封面几乎不裁。 */
internal const val HOME_FEED_FULL_COVER_ASPECT_RATIO = 16f / 9f

/** @deprecated 使用 [HOME_FEED_FULL_COVER_ASPECT_RATIO] */
internal const val HOME_FEED_CURRENT_COVER_ASPECT_RATIO = HOME_FEED_FULL_COVER_ASPECT_RATIO

/**
 * 解析首页/相关推荐等视频卡封面框比例。
 */
internal fun resolveHomeFeedCoverAspectRatio(
    style: HomeFeedCardStyle,
    gridColumns: Int = 2,
): Float {
    // 单列横卡封面固定为 16:10；双列及以上继续尊重用户选择。
    if (gridColumns <= 1) return HOME_FEED_BILIPAI_COVER_ASPECT_RATIO
    return when (style) {
        HomeFeedCardStyle.CURRENT -> HOME_FEED_FULL_COVER_ASPECT_RATIO
        HomeFeedCardStyle.OFFICIAL -> HOME_FEED_OFFICIAL_COVER_ASPECT_RATIO
        HomeFeedCardStyle.BILIPAI -> HOME_FEED_BILIPAI_COVER_ASPECT_RATIO
    }
}

internal fun resolveHomeFeedCardLayout(
    style: HomeFeedCardStyle,
    gridColumns: Int = 2,
): HomeFeedCardLayout {
    val coverAspectRatio = resolveHomeFeedCoverAspectRatio(
        style = style,
        gridColumns = gridColumns,
    )
    return when (style) {
        HomeFeedCardStyle.CURRENT -> HomeFeedCardLayout(
            coverAspectRatio = coverAspectRatio,
            outerPaddingDp = 6,
            itemSpacingDp = 6,
            verticalItemSpacingDp = 6,
            storyCardHorizontalPaddingDp = 16,
            compactMetadata = false
        )

        HomeFeedCardStyle.OFFICIAL -> HomeFeedCardLayout(
            coverAspectRatio = coverAspectRatio,
            // 统一 6dp 卡间距，垂直与水平一致，卡片墙更紧凑。
            outerPaddingDp = 6,
            itemSpacingDp = 6,
            verticalItemSpacingDp = 6,
            storyCardHorizontalPaddingDp = 0,
            compactMetadata = true
        )

        HomeFeedCardStyle.BILIPAI -> HomeFeedCardLayout(
            // 间距对齐 BiliPai Style.cardSpace / safeSpace 系：6dp 卡间距
            coverAspectRatio = coverAspectRatio,
            outerPaddingDp = 6,
            itemSpacingDp = 6,
            verticalItemSpacingDp = 6,
            storyCardHorizontalPaddingDp = 8,
            compactMetadata = true
        )
    }
}
