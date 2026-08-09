package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import androidx.compose.ui.unit.dp

object AppChromeSizeTokens {
    val MinimumTouchTarget = 48.dp
    const val CompactControlHeightDp = 44
    const val CompactControlCornerRadiusDp = 20
    // Keeps bottom-bar matched segmented indicators clear after their visual scale-up.
    const val BottomBarMatchedSegmentedControlHeightDp = 40
    const val BottomBarMatchedSegmentedIndicatorHeightDp = 27
}

/**
 * 高频胶囊控件的尺寸基准。
 *
 * 这里不承载颜色和动画，只约束搜索栏、分段控件、筛选 chip、小操作按钮等
 * 常用 chrome 的尺寸、留白和圆角，避免各页面继续散落相近但不一致的硬编码。
 */
data class CompactCapsuleChromeSpec(
    val primaryHeightDp: Int,
    val secondaryButtonSizeDp: Int,
    val chipHeightDp: Int,
    val compactChipHeightDp: Int,
    val primaryCornerRadiusDp: Int,
    val secondaryButtonCornerRadiusDp: Int,
    val chipCornerRadiusDp: Int,
    val compactChipCornerRadiusDp: Int,
    val iconSizeDp: Int,
    val smallIconSizeDp: Int,
    val inputHorizontalPaddingDp: Int,
    val chipHorizontalPaddingDp: Int,
    val compactChipHorizontalPaddingDp: Int,
    val standardGapDp: Int
)

fun resolveCompactCapsuleChromeSpec(
    uiStyle: AppUiStyle
): CompactCapsuleChromeSpec {
    val chromeTokens = resolveAndroidNativeChromeTokens(uiStyle)
    return when (uiStyle) {
        AppUiStyle.MIUIX -> CompactCapsuleChromeSpec(
            primaryHeightDp = 48,
            secondaryButtonSizeDp = chromeTokens.rowMinTouchTargetDp,
            chipHeightDp = 32,
            compactChipHeightDp = 28,
            primaryCornerRadiusDp = chromeTokens.pillCornerRadiusDp,
            secondaryButtonCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
            chipCornerRadiusDp = 16,
            compactChipCornerRadiusDp = 14,
            iconSizeDp = 20,
            smallIconSizeDp = 16,
            inputHorizontalPaddingDp = 14,
            chipHorizontalPaddingDp = 12,
            compactChipHorizontalPaddingDp = 10,
            standardGapDp = 8
        )
        AppUiStyle.MATERIAL3 -> CompactCapsuleChromeSpec(
            primaryHeightDp = 56,
            secondaryButtonSizeDp = chromeTokens.rowMinTouchTargetDp,
            chipHeightDp = 32,
            compactChipHeightDp = 28,
            primaryCornerRadiusDp = chromeTokens.pillCornerRadiusDp,
            secondaryButtonCornerRadiusDp = chromeTokens.containerCornerRadiusDp,
            chipCornerRadiusDp = 8,
            compactChipCornerRadiusDp = 8,
            iconSizeDp = 24,
            smallIconSizeDp = 18,
            inputHorizontalPaddingDp = 16,
            chipHorizontalPaddingDp = 16,
            compactChipHorizontalPaddingDp = 12,
            standardGapDp = 12
        )
    }
}
