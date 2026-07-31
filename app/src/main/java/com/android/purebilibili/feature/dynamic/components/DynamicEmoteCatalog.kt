package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.data.model.response.EmotePackage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cached emote shortcode → image URL map for dynamic rich text.
 *
 * Loads `business=dynamic` first (see bilibili-API-collect emoji docs), then merges
 * `business=reply` so common yellow-face shortcodes still resolve when a feed only
 * returns plain text with `[表情]` tokens.
 */
internal object DynamicEmoteCatalog {
    private val mutex = Mutex()

    @Volatile
    private var cached: Map<String, String> = emptyMap()

    @Volatile
    private var loaded = false

    fun snapshot(): Map<String, String> = cached

    suspend fun ensureLoaded(): Map<String, String> {
        if (loaded && cached.isNotEmpty()) return cached
        return mutex.withLock {
            if (loaded && cached.isNotEmpty()) return@withLock cached
            val loadedMap = loadEmoteUrlMap()
            cached = loadedMap
            loaded = true
            loadedMap
        }
    }

    private suspend fun loadEmoteUrlMap(): Map<String, String> {
        val result = linkedMapOf<String, String>()
        // Seed a few common shortcodes so first paint is not empty if the panel request fails.
        result["[doge]"] =
            "https://i0.hdslb.com/bfs/emote/6f8743c3c13009f4705307b2750e32f5068225e3.png"
        listOf("dynamic", "reply").forEach { business ->
            runCatching {
                fetchPackages(business).forEach { pkg ->
                    pkg.emote.orEmpty().forEach { emote ->
                        val text = emote.text.trim()
                        val url = emote.url.trim()
                        if (text.isNotEmpty() && url.isNotEmpty()) {
                            result.putIfAbsent(text, normalizeEmoteUrl(url))
                        }
                    }
                }
            }
        }
        return result
    }

    private suspend fun fetchPackages(business: String): List<EmotePackage> {
        val response = NetworkModule.api.getEmotes(mapOf("business" to business))
        if (response.code != 0) return emptyList()
        return response.data?.packages ?: response.data?.all_packages.orEmpty()
    }

    private fun normalizeEmoteUrl(rawUrl: String): String {
        return when {
            rawUrl.startsWith("//") -> "https:$rawUrl"
            rawUrl.startsWith("http://") -> rawUrl.replaceFirst("http://", "https://")
            else -> rawUrl
        }
    }
}
