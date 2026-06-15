package com.android.purebilibili.feature.home

import com.android.purebilibili.core.store.HomeFeedCardStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeFeedCardStylePolicyTest {

    @Test
    fun currentStyle_usesSixteenByNineAndExistingSpacing() {
        val layout = resolveHomeFeedCardLayout(HomeFeedCardStyle.CURRENT)

        assertEquals(16f / 9f, layout.coverAspectRatio)
        assertEquals(8, layout.outerPaddingDp)
        assertEquals(8, layout.itemSpacingDp)
        assertEquals(8, layout.verticalItemSpacingDp)
        assertEquals(16, layout.storyCardHorizontalPaddingDp)
        assertEquals(false, layout.compactMetadata)
    }

    @Test
    fun officialStyle_usesFourByThreeWithLargerCardsAndCompactSpacing() {
        val layout = resolveHomeFeedCardLayout(HomeFeedCardStyle.OFFICIAL)

        assertEquals(4f / 3f, layout.coverAspectRatio)
        assertEquals(6, layout.outerPaddingDp)
        assertEquals(6, layout.itemSpacingDp)
        assertEquals(6, layout.verticalItemSpacingDp)
        assertEquals(0, layout.storyCardHorizontalPaddingDp)
        assertEquals(true, layout.compactMetadata)
    }
}
