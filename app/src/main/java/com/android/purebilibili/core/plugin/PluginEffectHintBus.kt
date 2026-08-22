package com.android.purebilibili.core.plugin

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lightweight in-app hint bus for plugin effect visualization.
 *
 * Hints are coalesced per plugin id with a cooldown so high-frequency
 * filters (feed/danmaku) only surface a brief native chip.
 */
object PluginEffectHintBus {

    private val _current = MutableStateFlow<PluginEffectHint?>(null)
    val current: StateFlow<PluginEffectHint?> = _current.asStateFlow()

    private val lastAcceptedAtMs = mutableMapOf<String, Long>()

    fun tryEmit(
        hint: PluginEffectHint?,
        nowMs: Long = System.currentTimeMillis(),
        cooldownMs: Long = PLUGIN_EFFECT_HINT_VISIBLE_MS
    ) {
        if (hint == null) return
        synchronized(lastAcceptedAtMs) {
            val lastAccepted = lastAcceptedAtMs[hint.pluginId]
            if (!shouldAcceptPluginEffectHint(lastAccepted, nowMs, cooldownMs)) return
            lastAcceptedAtMs[hint.pluginId] = nowMs
            _current.value = hint.copy(issuedAtMs = nowMs)
        }
    }

    fun dismiss(issuedAtMs: Long) {
        val current = _current.value ?: return
        if (current.issuedAtMs == issuedAtMs) {
            _current.value = null
        }
    }

    fun clearForTests() {
        synchronized(lastAcceptedAtMs) {
            lastAcceptedAtMs.clear()
        }
        _current.value = null
    }
}
