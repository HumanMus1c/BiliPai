package com.android.purebilibili.feature.dynamic

import com.android.purebilibili.core.store.SettingsManager
import kotlin.math.abs
import kotlin.math.sign

internal const val DynamicBottomBarTopRevealPx = 100
internal const val DynamicBottomBarScrollDirectionThresholdPx = 48f

internal data class DynamicBottomBarScrollState(
    val accumulatedY: Float = 0f,
)

internal enum class DynamicBottomBarScrollIntent {
    SHOW,
    HIDE,
}

internal data class DynamicBottomBarScrollUpdate(
    val state: DynamicBottomBarScrollState,
    val intent: DynamicBottomBarScrollIntent?,
)

/**
 * 向下浏览时隐藏：由子页面用滚动增量驱动，瀑布流/列表一并生效。
 */
internal fun shouldAutoCollapseDynamicBottomBar(
    visibilityMode: SettingsManager.BottomBarVisibilityMode,
): Boolean {
    return visibilityMode == SettingsManager.BottomBarVisibilityMode.SCROLL_HIDE
}

/**
 * 用 nested-scroll 增量推断底栏显隐。
 *
 * 瀑布流首个可见 item 会在 lane 间切换，不能用 index 判断方向；
 * available.y / consumed.y 与布局锚点无关，平板与折叠屏多列下同样稳定。
 * 正 y 表示向上滚回顶部，负 y 表示向下浏览。
 */
internal fun reduceDynamicBottomBarScrollDelta(
    previousState: DynamicBottomBarScrollState,
    deltaY: Float,
    isAtTop: Boolean,
    thresholdPx: Float = DynamicBottomBarScrollDirectionThresholdPx,
): DynamicBottomBarScrollUpdate {
    if (isAtTop) {
        return DynamicBottomBarScrollUpdate(
            state = DynamicBottomBarScrollState(accumulatedY = 0f),
            intent = DynamicBottomBarScrollIntent.SHOW,
        )
    }
    if (deltaY == 0f) {
        return DynamicBottomBarScrollUpdate(state = previousState, intent = null)
    }

    val previousAccumulated = previousState.accumulatedY
    val accumulated = if (
        previousAccumulated == 0f || sign(previousAccumulated) == sign(deltaY)
    ) {
        previousAccumulated + deltaY
    } else {
        // 方向反转后重新累计，避免一次反向抖动立刻翻转显隐。
        deltaY
    }

    val safeThreshold = abs(thresholdPx)
    return when {
        accumulated <= -safeThreshold -> DynamicBottomBarScrollUpdate(
            state = DynamicBottomBarScrollState(accumulatedY = 0f),
            intent = DynamicBottomBarScrollIntent.HIDE,
        )
        accumulated >= safeThreshold -> DynamicBottomBarScrollUpdate(
            state = DynamicBottomBarScrollState(accumulatedY = 0f),
            intent = DynamicBottomBarScrollIntent.SHOW,
        )
        else -> DynamicBottomBarScrollUpdate(
            state = DynamicBottomBarScrollState(accumulatedY = accumulated),
            intent = null,
        )
    }
}
