package com.android.purebilibili.feature.bangumi

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.components.AppNativeTabRow
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import top.yukonga.miuix.kmp.blur.Backdrop

@Composable
internal fun <T> BangumiLiquidAwareTabRow(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scrollable: Boolean = false,
    minTabWidth: Dp = 72.dp,
    miuixBackdrop: Backdrop? = null,
) {
    if (options.isEmpty()) return
    val useCompactLiquidDock = !scrollable || options.size <= 5
    if (useCompactLiquidDock) {
        val selectedIndex = options.indexOfFirst { it.value == selectedValue }.coerceAtLeast(0)
        BottomBarLiquidSegmentedControl(
            items = options.map { it.label },
            selectedIndex = selectedIndex,
            onSelected = { index ->
                options.getOrNull(index)?.let { onSelectionChange(it.value) }
            },
            modifier = modifier,
            enabled = enabled,
            height = AppChromeSizeTokens.BottomBarMatchedSegmentedControlHeightDp.dp,
            indicatorHeight = AppChromeSizeTokens.BottomBarMatchedSegmentedIndicatorHeightDp.dp,
            liquidGlassEffectsEnabled = true,
            dragSelectionEnabled = !scrollable && options.size > 1,
            miuixBackdrop = miuixBackdrop,
        )
        return
    }
    AppNativeTabRow(
        options = options,
        selectedValue = selectedValue,
        modifier = modifier,
        enabled = enabled,
        scrollable = scrollable,
        minTabWidth = minTabWidth,
        onSelectionChange = onSelectionChange,
    )
}
