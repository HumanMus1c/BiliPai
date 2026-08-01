package com.android.purebilibili.navigation3

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.scene.SinglePaneSceneStrategy

/**
 * 预测返回松手时，NavDisplay 对 SeekableTransitionState.animateTo 用 Scene 的
 * `equals` 判断「是否仍在 seek 同一目标」：
 * - 相等 → 从当前 fraction 续播剩余时长
 * - 不相等 → **fraction 重置为 0**，整段 morph 从全屏再跑一遍
 *
 * 官方 SinglePaneScene 的 equals 绑了 [NavEntry] 引用（含 content lambda /
 * decorator 包装）。pop 后 MainHost entry 常被重建 → 与 seek 时的 previousScene
 * 不相等 → 卡片已缩回列表位却在松手后再从原始位置缩一次。
 *
 * 这里把 equals / hashCode 收敛到与 NavDisplay AnimatedSceneKey 一致的
 * destination [Scene.key]，保证预测 seek 目标与 pop 后 currentScene 在
 * 「同一 destination」时被视为同一过渡目标。
 */
internal class BiliPaiKeyEqualSceneStrategy<T : Any>(
    private val delegate: SceneStrategy<T> = SinglePaneSceneStrategy(),
) : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val scene = with(delegate) { calculateScene(entries) } ?: return null
        return KeyEqualScene(scene)
    }
}

/**
 * 仅按 class + [Scene.key] 判等的 Scene 包装。委托其余渲染契约给 [delegate]。
 */
internal class KeyEqualScene<T : Any>(
    private val delegate: Scene<T>,
) : Scene<T> {
    override val key: Any
        get() = delegate.key

    override val entries: List<NavEntry<T>>
        get() = delegate.entries

    override val previousEntries: List<NavEntry<T>>
        get() = delegate.previousEntries

    override val content: @Composable () -> Unit
        get() = delegate.content

    override val metadata: Map<String, Any>
        get() = delegate.metadata

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KeyEqualScene<*>) return false
        // 与 NavDisplay AnimatedSceneKey(scene::class, scene.key) 对齐：
        // 只认 destination key，忽略 NavEntry/content lambda 重建。
        return key == other.key
    }

    override fun hashCode(): Int = key.hashCode()

    override fun toString(): String = "KeyEqualScene(key=$key, delegate=$delegate)"
}

/**
 * 两个 Scene 是否表示同一 NavDisplay 过渡 destination（与 AnimatedSceneKey 对齐）。
 * 供单测锁定「预测松手不得因 entry 重建而换目标」契约。
 */
internal fun areScenesSameNavDestination(a: Scene<*>?, b: Scene<*>?): Boolean {
    if (a === b) return true
    if (a == null || b == null) return false
    return a.key == b.key
}

/**
 * 预测 seek 目标与 pop 后 currentScene 是否应被视作同一 animateTo 目标。
 * true 时 SeekableTransitionState 从当前 fraction 续播，不得 fraction=0 重开 morph。
 */
internal fun shouldContinuePredictiveSeekOnCommit(
    seekTargetScene: Scene<*>?,
    postPopCurrentScene: Scene<*>?,
): Boolean = areScenesSameNavDestination(seekTargetScene, postPopCurrentScene)
