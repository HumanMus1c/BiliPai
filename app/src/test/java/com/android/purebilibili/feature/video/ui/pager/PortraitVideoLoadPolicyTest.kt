package com.android.purebilibili.feature.video.ui.pager

import com.android.purebilibili.data.model.response.Dash
import com.android.purebilibili.data.model.response.DashAudio
import com.android.purebilibili.data.model.response.DashVideo
import com.android.purebilibili.data.model.response.Durl
import com.android.purebilibili.data.model.response.PlayUrlData
import com.android.purebilibili.data.model.response.RelatedVideo
import com.android.purebilibili.data.model.response.ViewInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PortraitVideoLoadPolicyTest {

    @Test
    fun parallelBootstrap_enablesWhenFeedProvidesCid() {
        assertTrue(
            shouldUsePortraitParallelPlaybackBootstrap(
                bvid = "BV1test",
                requestedCid = 12345L
            )
        )
    }

    @Test
    fun parallelBootstrap_disablesWhenCidMissing() {
        assertFalse(
            shouldUsePortraitParallelPlaybackBootstrap(
                bvid = "BV1test",
                requestedCid = 0L
            )
        )
    }

    @Test
    fun pagePlaybackIdentity_readsCidFromRelatedVideo() {
        val identity = resolvePortraitPagePlaybackIdentity(
            RelatedVideo(
                bvid = "BV1related",
                aid = 42L,
                cid = 99L
            )
        )

        assertEquals("BV1related", identity?.bvid)
        assertEquals(42L, identity?.aid)
        assertEquals(99L, identity?.cid)
    }

    @Test
    fun pagePlaybackIdentity_readsCidFromViewInfo() {
        val identity = resolvePortraitPagePlaybackIdentity(
            ViewInfo(
                bvid = "BV1view",
                aid = 7L,
                cid = 8L
            )
        )

        assertEquals("BV1view", identity?.bvid)
        assertEquals(7L, identity?.aid)
        assertEquals(8L, identity?.cid)
    }

    @Test
    fun playbackStreamUrls_prefersBestDashTrackOverFirstTrack() {
        val playData = PlayUrlData(
            dash = Dash(
                video = listOf(
                    DashVideo(
                        id = 64,
                        baseUrl = "https://cdn.example/64.m4s",
                        codecs = "avc1.64001E"
                    ),
                    DashVideo(
                        id = 80,
                        baseUrl = "https://cdn.example/80.m4s",
                        codecs = "hev1.1.6.L120.90"
                    )
                ),
                audio = listOf(
                    DashAudio(
                        id = 30232,
                        baseUrl = "https://cdn.example/audio.m4s"
                    )
                )
            )
        )

        val urls = resolvePortraitPlaybackStreamUrls(
            playData = playData,
            targetQuality = 64,
            isHevcSupported = true,
            isAv1Supported = false
        )

        assertEquals("https://cdn.example/64.m4s", urls?.videoUrl)
        assertEquals("https://cdn.example/audio.m4s", urls?.audioUrl)
    }

    @Test
    fun playbackStreamUrls_fallsBackToProgressiveUrl() {
        val urls = resolvePortraitPlaybackStreamUrls(
            playData = PlayUrlData(
                durl = listOf(Durl(url = "https://cdn.example/progressive.mp4"))
            )
        )

        assertEquals("https://cdn.example/progressive.mp4", urls?.videoUrl)
        assertNull(urls?.audioUrl)
    }

    @Test
    fun playUrlPreloadCount_defaultsToOneOnWifiWithoutExperimentalFlag() {
        assertEquals(
            1,
            resolvePortraitPlayUrlPreloadCount(
                prefetchVideoEnabled = false,
                isWifi = true,
                availableTargets = 3
            )
        )
    }

    @Test
    fun playUrlPreloadCount_expandsToTwoWhenExperimentalFlagEnabled() {
        assertEquals(
            2,
            resolvePortraitPlayUrlPreloadCount(
                prefetchVideoEnabled = true,
                isWifi = true,
                availableTargets = 5
            )
        )
    }

    @Test
    fun playUrlPreloadCount_skipsCellularAndEmptyTargets() {
        assertEquals(
            0,
            resolvePortraitPlayUrlPreloadCount(
                prefetchVideoEnabled = true,
                isWifi = false,
                availableTargets = 3
            )
        )
        assertEquals(
            0,
            resolvePortraitPlayUrlPreloadCount(
                prefetchVideoEnabled = true,
                isWifi = true,
                availableTargets = 0
            )
        )
    }

    @Test
    fun playUrlPreloadTargets_collectsUpcomingPagesWithoutDuplicates() {
        val targets = resolvePortraitPlayUrlPreloadTargets(
            committedPage = 0,
            pageItems = listOf(
                RelatedVideo(bvid = "BV1", cid = 1L),
                RelatedVideo(bvid = "BV2", cid = 2L),
                RelatedVideo(bvid = "BV2", cid = 2L),
                RelatedVideo(bvid = "BV3", cid = 3L)
            ),
            preloadCount = 2
        )

        assertEquals(2, targets.size)
        assertEquals("BV2", targets[0].bvid)
        assertEquals(2L, targets[0].cid)
        assertEquals("BV3", targets[1].bvid)
    }
}