package com.android.purebilibili.feature.audio.lyrics

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LyricsRepositoryTest {

    @Test
    fun `manual cache wins without contacting providers`() = runTest {
        val cached = parseSplLyrics("[00:01.00]Manual", source = LyricSource.MANUAL)
            .copy(manuallySelected = true)
        val cache = FakeLyricsCache(cached)
        val provider = FakeLyricsProvider(LyricSource.NETEASE)
        val repository = LyricsRepository(listOf(provider), cache)

        val result = repository.load("au:1", query(), bilibiliLyrics = "[00:01.00]Bilibili")

        assertEquals(cached, assertIs<LyricsLoadResult.Found>(result).document)
        assertEquals(0, provider.searchCalls)
    }

    @Test
    fun `timed bilibili lyrics win before external providers`() = runTest {
        val cache = FakeLyricsCache()
        val provider = FakeLyricsProvider(LyricSource.NETEASE)
        val repository = LyricsRepository(listOf(provider), cache)

        val result = repository.load("au:1", query(), bilibiliLyrics = "[00:01.00]Bilibili")

        val document = assertIs<LyricsLoadResult.Found>(result).document
        assertEquals("Bilibili", document.lines.single().text)
        assertEquals(LyricSource.BILIBILI, document.source)
        assertEquals(0, provider.searchCalls)
        assertEquals(document, cache.saved)
    }

    @Test
    fun `external providers are searched and best candidate is fetched`() = runTest {
        val wrong = FakeLyricsProvider(
            source = LyricSource.KUGOU,
            candidates = listOf(candidate(LyricSource.KUGOU, "wrong", artist = "Someone")),
            rawLyrics = RawLyrics("[00:01.00]Wrong")
        )
        val best = FakeLyricsProvider(
            source = LyricSource.QQ_MUSIC,
            candidates = listOf(candidate(LyricSource.QQ_MUSIC, "best")),
            rawLyrics = RawLyrics("[00:01.00]Matched", translation = "[00:01.00]匹配")
        )
        val repository = LyricsRepository(listOf(wrong, best), FakeLyricsCache())

        val result = repository.load("video:BV1:2", query(), bilibiliLyrics = null)

        val document = assertIs<LyricsLoadResult.Found>(result).document
        assertEquals("Matched", document.lines.single().text)
        assertEquals(listOf("匹配"), document.lines.single().translations)
        assertEquals(LyricSource.QQ_MUSIC, document.source)
        assertEquals(0, wrong.fetchCalls)
        assertEquals(1, best.fetchCalls)
    }

    @Test
    fun `provider failures degrade to not found`() = runTest {
        val provider = FakeLyricsProvider(
            source = LyricSource.NETEASE,
            failure = IllegalStateException("offline")
        )
        val repository = LyricsRepository(listOf(provider), FakeLyricsCache())

        val result = repository.load("video:BV1:2", query(), bilibiliLyrics = null)

        assertIs<LyricsLoadResult.NotFound>(result)
    }

    @Test
    fun `adjusted lyric offset is persisted without losing manual selection`() = runTest {
        val cache = FakeLyricsCache()
        val repository = LyricsRepository(emptyList(), cache)
        val adjusted = parseSplLyrics("[00:01.00]Manual", source = LyricSource.MANUAL)
            .copy(manuallySelected = true)
            .withOffset(2_000L)

        repository.save("au:1", adjusted)

        assertEquals(adjusted, cache.saved)
        assertEquals(true, cache.saved?.manuallySelected)
        assertEquals(2_000L, cache.saved?.offsetMs)
    }

    private fun query() = LyricQuery("Song", "Artist", 180_000L)

    private fun candidate(
        source: LyricSource,
        id: String,
        artist: String = "Artist"
    ) = LyricCandidate(source, id, "Song", artist, 180_000L)
}

private class FakeLyricsCache(
    private val initial: LyricDocument? = null
) : LyricsCache {
    var saved: LyricDocument? = null

    override suspend fun read(key: String): LyricDocument? = initial

    override suspend fun write(key: String, document: LyricDocument) {
        saved = document
    }
}

private class FakeLyricsProvider(
    override val source: LyricSource,
    private val candidates: List<LyricCandidate> = emptyList(),
    private val rawLyrics: RawLyrics = RawLyrics(""),
    private val failure: Throwable? = null
) : LyricsProvider {
    var searchCalls = 0
    var fetchCalls = 0

    override suspend fun search(query: LyricQuery): List<LyricCandidate> {
        searchCalls += 1
        failure?.let { throw it }
        return candidates
    }

    override suspend fun fetch(candidate: LyricCandidate): RawLyrics {
        fetchCalls += 1
        failure?.let { throw it }
        return rawLyrics
    }
}
