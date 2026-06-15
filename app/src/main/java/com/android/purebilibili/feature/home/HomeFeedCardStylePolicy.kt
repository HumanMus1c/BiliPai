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

internal fun resolveHomeFeedCardLayout(style: HomeFeedCardStyle): HomeFeedCardLayout {
    return when (style) {
        HomeFeedCardStyle.CURRENT -> HomeFeedCardLayout(
            coverAspectRatio = 16f / 9f,
            outerPaddingDp = 8,
            itemSpacingDp = 8,
            verticalItemSpacingDp = 8,
            storyCardHorizontalPaddingDp = 16,
            compactMetadata = false
        )

        HomeFeedCardStyle.OFFICIAL -> HomeFeedCardLayout(
            coverAspectRatio = 4f / 3f,
            outerPaddingDp = 6,
            itemSpacingDp = 6,
            verticalItemSpacingDp = 6,
            storyCardHorizontalPaddingDp = 0,
            compactMetadata = true
        )
    }
}
