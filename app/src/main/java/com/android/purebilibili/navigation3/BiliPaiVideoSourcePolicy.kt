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

/**
 * Related-detail hops use source route `video/{parentBvid}`.
 * They intentionally use the default Miuix page navigation instead of the whole-card morph.
 */
internal fun isRelatedVideoCardMorphSourceRoute(sourceRoute: String?): Boolean {
    val route = sourceRoute?.substringBefore('?')?.trim().orEmpty()
    return route.startsWith("video/")
}

/**
 * Whole-card Miuix morph gate — **partition SIDE_BY_SIDE cards are the reference path**.
 *
 * Shared contract for recorded list-card sources (home, partition, search, …):
 * 1. Click freezes cardBounds + coverBounds + layout + chrome snapshot
 * 2. Outer entry morphs host ↔ cardBounds (one opaque flying card)
 * 3. Flying entry draws shell + live media + chrome; list stays alpha 0 until IDLE
 * 4. Inverse scale uses Nav host layout width (same as outer morph)
 * 5. Layout-specific landing only:
 *    - STACKED (双列): live media top, info bottom
 *    - SIDE_BY_SIDE (分区横卡): live media left, info right
 *
 * Related-detail sources (`video/{parentBvid}`) bypass this gate and fall back to the default
 * Miuix navigation transition.
 */
internal fun shouldUseMiuixVideoCardMorph(
    cardTransitionEnabled: Boolean,
    reduceMotion: Boolean,
    sourceRoute: String?,
    hasUsableSourceBounds: Boolean,
): Boolean = cardTransitionEnabled &&
    !reduceMotion &&
    !isRelatedVideoCardMorphSourceRoute(sourceRoute) &&
    !sourceRoute?.substringBefore('?').isNullOrBlank() &&
    hasUsableSourceBounds
