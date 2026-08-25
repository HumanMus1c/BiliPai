package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import top.yukonga.miuix.kmp.blur.Backdrop

/**
 * App-wide category/page tab contract. The shared renderer keeps MD3's animated underline
 * while liquid glass is off, and switches every theme to the moving glass capsule when reuse
 * is enabled. Miuix keeps the capsule presentation in either state.
 */
@Composable
fun <T> AppThemeAdaptiveTabRow(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scrollable: Boolean = false,
    minTabWidth: Dp = 72.dp,
    miuixBackdrop: Backdrop? = null,
) {
    AppLiquidAwareTabRow(
        options = options,
        selectedValue = selectedValue,
        onSelectionChange = onSelectionChange,
        modifier = modifier,
        enabled = enabled,
        scrollable = scrollable,
        minTabWidth = minTabWidth,
        miuixBackdrop = miuixBackdrop,
    )
}

/**
 * App-level tab row that reuses the same liquid indicator and interaction contract as the
 * floating home dock. Scrollable rows keep horizontal scrolling and expose long-press drag
 * selection so drag-to-scroll and drag-to-select do not compete for the same gesture.
 */
@Composable
fun <T> AppLiquidAwareTabRow(
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
    val selectedIndex = options.indexOfFirst { it.value == selectedValue }.coerceAtLeast(0)
    if (scrollable) {
        val scrollState = rememberScrollState()
        val density = LocalDensity.current
        BoxWithConstraints(modifier = modifier) {
            val viewportWidthPx = with(density) { maxWidth.toPx() }
            val itemWidthPx = with(density) { minTabWidth.toPx() }
            LaunchedEffect(selectedIndex, scrollState.maxValue, viewportWidthPx, itemWidthPx) {
                val centeredItemOffset =
                    (selectedIndex + 0.5f) * itemWidthPx - viewportWidthPx / 2f
                scrollState.animateScrollTo(
                    centeredItemOffset.toInt().coerceIn(0, scrollState.maxValue),
                )
            }
            BottomBarLiquidSegmentedControl(
                items = options.map { it.label },
                selectedIndex = selectedIndex,
                onSelected = { index ->
                    options.getOrNull(index)?.let { onSelectionChange(it.value) }
                },
                modifier = Modifier.horizontalScroll(scrollState),
                enabled = enabled,
                itemWidth = minTabWidth,
                height = AppChromeSizeTokens.BottomBarMatchedSegmentedControlHeightDp.dp,
                indicatorHeight = AppChromeSizeTokens.BottomBarMatchedSegmentedIndicatorHeightDp.dp,
                liquidGlassEffectsEnabled = true,
                dragSelectionEnabled = false,
                longPressDragSelectionEnabled = options.size > 1,
                miuixBackdrop = miuixBackdrop,
            )
        }
    } else {
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
            dragSelectionEnabled = options.size > 1,
            miuixBackdrop = miuixBackdrop,
        )
    }
}
