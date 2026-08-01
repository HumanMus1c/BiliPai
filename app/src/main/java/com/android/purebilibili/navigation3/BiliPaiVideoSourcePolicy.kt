package com.android.purebilibili.navigation3

internal data class BiliPaiVideoSource(
    val route: String?,
    val key: String?
)

internal fun resolveBiliPaiVideoSource(
    bvid: String,
    explicitSourceRoute: String?,
    currentKey: BiliPaiNavKey?,
    previousSourceRoute: String?
): BiliPaiVideoSource {
    val route = normalizeBiliPaiVideoSourceRoute(
        explicitSourceRoute ?: when (currentKey) {
            is BiliPaiNavKey.VideoDetail -> {
                // Prefer explicit related host `video/{parent}` when provided by callers.
                // Without explicit: keep list origin (home/search/…) so multi-hop returns
                // still land on the original card, not an intermediate detail.
                previousSourceRoute
                    ?.takeIf { it.isNotBlank() }
                    ?: currentKey.sourceRoute
                    ?: "video/${currentKey.bvid}"
            }
            null -> previousSourceRoute
            else -> currentKey.toLegacyRoute()
        }
    )
    return BiliPaiVideoSource(
        route = route,
        key = route?.takeIf { bvid.isNotBlank() }?.let { "$it:$bvid" }
    )
}

internal fun normalizeBiliPaiVideoSourceRoute(route: String?): String? {
    val normalized = route?.trim()?.takeIf { it.isNotBlank() } ?: return null
    return if (normalized.startsWith("home?category=")) {
        normalized
    } else {
        normalized.substringBefore("?")
    }
}
