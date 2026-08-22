package com.android.purebilibili.feature.bangumi

internal fun resolveBangumiNavigationTitleFontSizeSp(screenWidthDp: Int): Float {
    return if (screenWidthDp >= 380) 22f else 20f
}

internal fun resolveBangumiTypeTabFontSizeSp(screenWidthDp: Int): Float {
    return if (screenWidthDp >= 380) 16f else 14f
}

internal fun resolveBangumiPlayerTopControlsPaddingTopDp(
    isFullscreen: Boolean,
    statusBarsInsetDp: Float
): Float {
    return 8f
}

internal fun resolveBangumiDanmakuTopInsetDp(
    isFullscreen: Boolean,
    statusBarsInsetDp: Float
): Float {
    return if (isFullscreen) 0f else 52f
}

internal fun resolveBangumiPortraitPlayerContainerTopPaddingDp(
    statusBarsInsetDp: Float
): Float {
    return statusBarsInsetDp.takeIf { it.isFinite() }?.coerceAtLeast(0f) ?: 0f
}

internal data class BangumiEpisodePreviewWindow(
    val startIndex: Int,
    val endExclusive: Int
)

internal fun resolveBangumiEpisodePageCount(
    episodeCount: Int,
    episodesPerPage: Int
): Int {
    if (episodeCount <= 0 || episodesPerPage <= 0) return 0
    return (episodeCount + episodesPerPage - 1) / episodesPerPage
}

internal fun resolveBangumiEpisodePageLabel(
    episodeCount: Int,
    page: Int,
    episodesPerPage: Int,
    descending: Boolean
): String {
    val pageCount = resolveBangumiEpisodePageCount(episodeCount, episodesPerPage)
    if (pageCount == 0) return ""
    val safePage = page.coerceIn(0, pageCount - 1)
    return if (descending) {
        val high = episodeCount - safePage * episodesPerPage
        val low = maxOf(1, high - episodesPerPage + 1)
        "$high-$low"
    } else {
        val low = safePage * episodesPerPage + 1
        val high = minOf(episodeCount, low + episodesPerPage - 1)
        "$low-$high"
    }
}

internal fun <T> orderBangumiEpisodes(episodes: List<T>, descending: Boolean): List<T> {
    return if (descending) episodes.asReversed() else episodes
}

internal fun resolveBangumiEpisodePreviewWindow(
    episodeCount: Int,
    selectedPage: Int,
    episodesPerPage: Int,
    previewCount: Int
): BangumiEpisodePreviewWindow {
    if (episodeCount <= 0 || episodesPerPage <= 0 || previewCount <= 0) {
        return BangumiEpisodePreviewWindow(startIndex = 0, endExclusive = 0)
    }
    val maxPage = (episodeCount - 1) / episodesPerPage
    val safePage = selectedPage.coerceIn(0, maxPage)
    val pageStart = safePage * episodesPerPage
    val pageEnd = minOf(pageStart + episodesPerPage, episodeCount)
    return BangumiEpisodePreviewWindow(
        startIndex = pageStart,
        endExclusive = minOf(pageStart + previewCount, pageEnd)
    )
}
