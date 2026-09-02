package com.android.purebilibili.feature.dynamic

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.store.SettingsManager

internal enum class DynamicVideoCardLayoutMode {
    VERTICAL,
    HORIZONTAL
}

internal fun resolveDynamicFeedMaxWidth(): Dp = 480.dp

internal fun resolveDynamicTimelineMaxWidth(): Dp = 1840.dp

internal fun resolveDynamicTimelineMinColumnWidth(): Dp = 360.dp

internal fun resolveDynamicTimelineHorizontalSpacing(): Dp = 18.dp

internal fun resolveDynamicTimelineVerticalSpacing(): Dp = 10.dp

internal fun shouldUseDynamicManualPrependAnchor(
    feedLayoutMode: SettingsManager.DynamicFeedLayoutMode,
): Boolean = feedLayoutMode == SettingsManager.DynamicFeedLayoutMode.LIST

internal fun resolveDynamicVideoCardLayoutMode(containerWidthDp: Int): DynamicVideoCardLayoutMode {
    return DynamicVideoCardLayoutMode.VERTICAL
}

internal fun resolveDynamicHorizontalUserListHorizontalPadding(): Dp = 10.dp

internal fun resolveDynamicHorizontalUserListSpacing(): Dp = 10.dp

internal fun resolveDynamicTopBarHorizontalPadding(): Dp = 14.dp

internal fun resolveDynamicTopBarTabEndPadding(): Dp = 20.dp

internal fun resolveDynamicTopBarHeightDp(): Int = 50

internal fun resolveDynamicSidebarReturnHeaderHeightDp(): Int = resolveDynamicTopBarHeightDp()

internal fun resolveDynamicSidebarDividerTopOffset(topPadding: Dp): Dp {
    return topPadding + resolveDynamicSidebarReturnHeaderHeightDp().dp
}

internal data class DynamicTopBarLiquidTabSpec(
    val topPaddingDp: Int,
    val bottomPaddingDp: Int,
    val heightDp: Int,
    val indicatorHeightDp: Int,
    val labelFontSizeSp: Int
)

internal fun resolveDynamicTopBarLiquidTabSpec(): DynamicTopBarLiquidTabSpec {
    val heightDp = resolveDynamicTopBarHeightDp()
    return DynamicTopBarLiquidTabSpec(
        topPaddingDp = 0,
        bottomPaddingDp = 0,
        heightDp = heightDp,
        indicatorHeightDp = com.android.purebilibili.core.ui.roundMatchedLiquidIndicatorHeightDp(
            heightDp.toFloat()
        ),
        labelFontSizeSp = 13
    )
}

internal fun resolveDynamicTabIndicatorPosition(
    selectedIndex: Int,
    externalPosition: Float?,
    itemCount: Int,
): Float {
    if (itemCount <= 0) return 0f
    val lastIndex = itemCount - 1
    return externalPosition
        ?.takeIf { it.isFinite() }
        ?.coerceIn(0f, lastIndex.toFloat())
        ?: selectedIndex.coerceIn(0, lastIndex).toFloat()
}

internal fun resolveDynamicSidebarWidth(isExpanded: Boolean): Dp {
    return if (isExpanded) 68.dp else 60.dp
}

internal fun shouldShowDynamicUserLiveBadge(isLive: Boolean): Boolean = isLive

internal fun resolveDynamicUserLiveBadgeLabel(): String = "直播"

internal fun resolveDynamicCardOuterPadding(): Dp = 0.dp

internal fun resolveDynamicCardContentPadding(): Dp = 12.dp

internal fun resolveDynamicActionButtonSlotWeight(): Float = 1f

internal fun resolveDynamicActionButtonSpacing(): Dp = 8.dp

internal fun resolveDynamicActionButtonText(
    label: String,
    count: Int,
    slotWidthDp: Int? = null
): String? {
    val countText = if (count > 0) formatDynamicActionCount(count) else null
    return when (label) {
        "评论" -> countText
        "转发" -> {
            listOfNotNull(label, countText).joinToString(separator = " ")
        }
        else -> countText
    }
}

private fun formatDynamicActionCount(count: Int): String {
    return when {
        count >= 10000 -> "${count / 10000}万"
        count >= 1000 -> String.format("%.1fk", count / 1000f)
        else -> count.toString()
    }
}
