package com.android.purebilibili.feature.watchlater

import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals

class WatchLaterLayoutPolicyTest {
    @Test
    fun listWidth_isCappedForLargeScreens() {
        assertEquals(840.dp, resolveWatchLaterListMaxWidth())
    }

    @Test
    fun coverWidth_isOwnedByWatchLaterLayoutPolicy() {
        assertEquals(140.dp, resolveWatchLaterCoverWidth())
    }
}
