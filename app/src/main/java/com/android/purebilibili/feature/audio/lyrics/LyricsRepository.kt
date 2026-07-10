package com.android.purebilibili.feature.audio.lyrics

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull

internal const val LYRIC_PROVIDER_TIMEOUT_MS = 5_000L
internal const val LYRIC_TOTAL_TIMEOUT_MS = 6_000L

internal data class RawLyrics(
    val primary: String,
    val translation: String? = null,
    val romanization: String? = null
)

internal interface LyricsProvider {
    val source: LyricSource

    suspend fun search(query: LyricQuery): List<LyricCandidate>

    suspend fun fetch(candidate: LyricCandidate): RawLyrics
}

internal interface LyricsCache {
    suspend fun read(key: String): LyricDocument?

    suspend fun write(key: String, document: LyricDocument)
}

internal sealed interface LyricsLoadResult {
    data class Found(val document: LyricDocument) : LyricsLoadResult
    data object NotFound : LyricsLoadResult
}

internal class LyricsRepository(
    private val providers: List<LyricsProvider>,
    private val cache: LyricsCache,
    private val nowMs: () -> Long = System::currentTimeMillis,
    private val providerTimeoutMs: Long = LYRIC_PROVIDER_TIMEOUT_MS,
    private val totalTimeoutMs: Long = LYRIC_TOTAL_TIMEOUT_MS
) {
    suspend fun load(
        cacheKey: String,
        query: LyricQuery,
        bilibiliLyrics: String?,
        forceRefresh: Boolean = false
    ): LyricsLoadResult {
        if (!forceRefresh) {
            cache.read(cacheKey)?.let { return LyricsLoadResult.Found(it) }
        }

        bilibiliLyrics
            ?.takeIf { it.isNotBlank() }
            ?.let { parseSplLyrics(it, source = LyricSource.BILIBILI) }
            ?.takeIf { it.lines.isNotEmpty() }
            ?.let { document ->
                val cached = document.copy(fetchedAtMs = nowMs())
                cache.write(cacheKey, cached)
                return LyricsLoadResult.Found(cached)
            }

        val matched = withTimeoutOrNull(totalTimeoutMs) {
            val candidates = searchAllProviders(query)
            val candidate = selectBestLyricCandidate(query, candidates) ?: return@withTimeoutOrNull null
            val provider = providers.firstOrNull { it.source == candidate.source }
                ?: return@withTimeoutOrNull null
            val raw = runCatching {
                withTimeout(providerTimeoutMs) { provider.fetch(candidate) }
            }.getOrNull() ?: return@withTimeoutOrNull null
            parseSplLyrics(
                primary = raw.primary,
                translation = raw.translation,
                romanization = raw.romanization,
                source = candidate.source
            ).takeIf { it.lines.isNotEmpty() }
                ?.copy(
                    remoteId = candidate.remoteId,
                    fetchedAtMs = nowMs()
                )
        }

        if (matched != null) {
            cache.write(cacheKey, matched)
            return LyricsLoadResult.Found(matched)
        }
        return LyricsLoadResult.NotFound
    }

    suspend fun search(query: LyricQuery): List<LyricCandidate> {
        return withTimeoutOrNull(totalTimeoutMs) { searchAllProviders(query) }.orEmpty()
    }

    suspend fun save(cacheKey: String, document: LyricDocument) {
        cache.write(cacheKey, document)
    }

    suspend fun select(
        cacheKey: String,
        providerCandidate: LyricCandidate
    ): LyricsLoadResult {
        val provider = providers.firstOrNull { it.source == providerCandidate.source }
            ?: return LyricsLoadResult.NotFound
        val raw = runCatching {
            withTimeout(providerTimeoutMs) { provider.fetch(providerCandidate) }
        }.getOrNull() ?: return LyricsLoadResult.NotFound
        val document = parseSplLyrics(
            primary = raw.primary,
            translation = raw.translation,
            romanization = raw.romanization,
            source = providerCandidate.source
        ).copy(
            remoteId = providerCandidate.remoteId,
            manuallySelected = true,
            fetchedAtMs = nowMs()
        )
        if (document.lines.isEmpty()) return LyricsLoadResult.NotFound
        cache.write(cacheKey, document)
        return LyricsLoadResult.Found(document)
    }

    private suspend fun searchAllProviders(query: LyricQuery): List<LyricCandidate> {
        return supervisorScope {
            providers.map { provider ->
                async {
                    runCatching {
                        withTimeout(providerTimeoutMs) { provider.search(query) }
                    }.getOrDefault(emptyList())
                }
            }.awaitAll().flatten()
        }
    }
}
