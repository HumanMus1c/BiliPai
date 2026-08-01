package com.android.purebilibili.navigation3

import kotlin.test.Test
import kotlin.test.assertEquals

class BiliPaiVideoSourcePolicyTest {

    @Test
    fun searchVideoUsesSearchSourceRouteAndIndependentSourceKey() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV1",
            explicitSourceRoute = null,
            currentKey = BiliPaiNavKey.Search,
            previousSourceRoute = null
        )

        assertEquals("search", source.route)
        assertEquals("search:BV1", source.key)
    }

    @Test
    fun videoToVideoNavigationKeepsPreviousListSourceInsteadOfVideoRoute() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV2",
            explicitSourceRoute = null,
            currentKey = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home"),
            previousSourceRoute = "home"
        )

        assertEquals("home", source.route)
        assertEquals("home:BV2", source.key)
    }

    @Test
    fun relatedVideoNavigationUsesExplicitVideoSourceRouteForDetailToDetailSharedElement() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV2",
            explicitSourceRoute = "video/BV1",
            currentKey = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = "home"),
            previousSourceRoute = "home"
        )

        assertEquals("video/BV1", source.route)
        assertEquals("video/BV1:BV2", source.key)
    }

    @Test
    fun videoToVideoWithoutPreviousSourceFallsBackToParentVideoHost() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV2",
            explicitSourceRoute = null,
            currentKey = BiliPaiNavKey.VideoDetail(bvid = "BV1", sourceRoute = null),
            previousSourceRoute = null
        )

        assertEquals("video/BV1", source.route)
        assertEquals("video/BV1:BV2", source.key)
    }

    @Test
    fun explicitSourceRouteIsNormalizedBeforeKeyGeneration() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV1",
            explicitSourceRoute = "search?keyword=test",
            currentKey = BiliPaiNavKey.Home,
            previousSourceRoute = null
        )

        assertEquals("search", source.route)
        assertEquals("search:BV1", source.key)
    }

    @Test
    fun homeTopTabSourceRouteKeepsQueryForIndependentSourceKey() {
        val source = resolveBiliPaiVideoSource(
            bvid = "BV1",
            explicitSourceRoute = "home?category=POPULAR",
            currentKey = BiliPaiNavKey.Home,
            previousSourceRoute = null
        )

        assertEquals("home?category=POPULAR", source.route)
        assertEquals("home?category=POPULAR:BV1", source.key)
    }
}
