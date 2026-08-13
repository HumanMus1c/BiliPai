package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationStyle
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackExitDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BiliPaiPredictiveBackAnimationPolicyTest {
    @Test
    fun `all transition storage values are stable`() {
        assertEquals(
            listOf("none", "aosp", "miuix", "scale", "bilipai_classic"),
            BiliPaiPredictiveBackAnimationStyle.entries.map { it.storageValue },
        )
    }

    @Test
    fun `legacy and unknown transition values migrate to miuix`() {
        assertEquals(
            BiliPaiPredictiveBackAnimationStyle.MIUIX,
            BiliPaiPredictiveBackAnimationStyle.fromStorageValue("default"),
        )
        assertEquals(
            BiliPaiPredictiveBackAnimationStyle.CLASSIC,
            BiliPaiPredictiveBackAnimationStyle.fromStorageValue("classic"),
        )
        assertEquals(
            BiliPaiPredictiveBackAnimationStyle.MIUIX,
            BiliPaiPredictiveBackAnimationStyle.fromStorageValue("unknown"),
        )
    }

    @Test
    fun `scale exit direction defaults to always right`() {
        assertEquals(
            BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT,
            BiliPaiPredictiveBackExitDirection.fromStorageValue(null),
        )
    }

    @Test
    fun `related parent keeps its live surface bound while child playback stays separate`() {
        val parent = BiliPaiNavKey.VideoDetail(
            bvid = "BV_PARENT",
            sourceRoute = "home",
        )
        val child = BiliPaiNavKey.VideoDetail(
            bvid = "BV_CHILD",
            sourceRoute = "video/BV_PARENT",
        )

        assertTrue(
            shouldBindVideoDetailBackPreviewPlayer(
                currentKey = child,
                previewKey = parent,
            )
        )
        assertFalse(
            shouldBindVideoDetailBackPreviewPlayer(
                currentKey = BiliPaiNavKey.VideoDetail(
                    bvid = "BV_OTHER",
                    sourceRoute = "home",
                ),
                previewKey = parent,
            )
        )
    }

    @Test
    fun `committed related return activates retained parent playback before pop`() {
        val parent = BiliPaiNavKey.VideoDetail(bvid = "BV_PARENT")
        val child = BiliPaiNavKey.VideoDetail(
            bvid = "BV_CHILD",
            sourceRoute = "video/BV_PARENT",
        )

        assertFalse(
            shouldActivateVideoDetailPlaybackSession(
                currentKey = child,
                detailKey = parent,
                isImmediateBackPreview = true,
                activateBackPreviewPlayback = false,
            )
        )
        assertTrue(
            shouldActivateVideoDetailPlaybackSession(
                currentKey = child,
                detailKey = parent,
                isImmediateBackPreview = true,
                activateBackPreviewPlayback = true,
            )
        )
    }
}
