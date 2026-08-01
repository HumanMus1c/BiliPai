package com.android.purebilibili.navigation3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class BiliPaiKeyEqualSceneStrategyTest {

    @Test
    fun keyEqualScenesWithSameKeyAreEqualEvenWhenNavEntriesDiffer() {
        val key = BiliPaiNavKey.MainHost
        val sceneA = KeyEqualScene(
            FakeScene(
                key = key,
                entries = listOf(navEntry(key, contentToken = "seek-target")),
            ),
        )
        val sceneB = KeyEqualScene(
            FakeScene(
                key = key,
                entries = listOf(navEntry(key, contentToken = "post-pop-rebuild")),
            ),
        )

        assertEquals(sceneA, sceneB)
        assertEquals(sceneA.hashCode(), sceneB.hashCode())
        assertTrue(shouldContinuePredictiveSeekOnCommit(sceneA, sceneB))
    }

    @Test
    fun keyEqualScenesWithDifferentKeysAreNotEqual() {
        val home = KeyEqualScene(
            FakeScene(
                key = BiliPaiNavKey.MainHost,
                entries = listOf(navEntry(BiliPaiNavKey.MainHost, contentToken = "a")),
            ),
        )
        val detail = KeyEqualScene(
            FakeScene(
                key = BiliPaiNavKey.VideoDetail(bvid = "BV1xx", sourceRoute = "home"),
                entries = listOf(
                    navEntry(
                        BiliPaiNavKey.VideoDetail(bvid = "BV1xx", sourceRoute = "home"),
                        contentToken = "b",
                    ),
                ),
            ),
        )

        assertNotEquals(home, detail)
        assertFalse(shouldContinuePredictiveSeekOnCommit(home, detail))
    }

    @Test
    fun rawScenesWithRebuiltEntriesWouldNotMatchWithoutKeyEqualWrapper() {
        val key = BiliPaiNavKey.MainHost
        val seekTarget = FakeScene(
            key = key,
            entries = listOf(navEntry(key, contentToken = "seek-target")),
        )
        val afterPop = FakeScene(
            key = key,
            entries = listOf(navEntry(key, contentToken = "post-pop-rebuild")),
        )

        // 模拟官方 SinglePaneScene / NavEntry 引用相等：content lambda 不同即不相等。
        assertNotEquals(seekTarget.entries.single(), afterPop.entries.single())
        // 未包装时 destination 语义仍相同，但 equals 失败 → animateTo 会重置 fraction。
        assertNotEquals(seekTarget, afterPop)
        // 包装后与 AnimatedSceneKey 一致，松手续播。
        assertTrue(
            shouldContinuePredictiveSeekOnCommit(
                KeyEqualScene(seekTarget),
                KeyEqualScene(afterPop),
            ),
        )
    }

    private fun navEntry(
        key: BiliPaiNavKey,
        contentToken: String,
    ): NavEntry<BiliPaiNavKey> {
        // 每次新的 content lambda 引用，触发 NavEntry.equals 失败。
        val content: @Composable (BiliPaiNavKey) -> Unit = {
            @Suppress("UNUSED_EXPRESSION")
            contentToken
        }
        return NavEntry(key = key, content = content)
    }

    private data class FakeScene(
        override val key: Any,
        override val entries: List<NavEntry<BiliPaiNavKey>>,
        override val previousEntries: List<NavEntry<BiliPaiNavKey>> = emptyList(),
    ) : Scene<BiliPaiNavKey> {
        override val content: @Composable () -> Unit = {}
    }
}
