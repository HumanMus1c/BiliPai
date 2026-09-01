package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import top.yukonga.miuix.kmp.blur.Backdrop

/**
 * App-wide category/page tab contract. The shared renderer keeps MD3's animated underline
 * while liquid glass is off, and switches every theme to the moving glass capsule when reuse
 * is enabled. The disabled path always delegates to the active theme's native tab row.
 */
@Composable
fun <T> AppThemeAdaptiveTabRow(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scrollable: Boolean = false,
    minTabWidth: Dp = AppChromeSizeTokens.MinimumTouchTarget,
    compactMiuixWhenTwoOptions: Boolean = true,
    height: Dp = AppChromeSizeTokens.BottomBarMatchedSegmentedControlHeightDp.dp,
    indicatorHeight: Dp = AppChromeSizeTokens.BottomBarMatchedSegmentedIndicatorHeightDp.dp,
    labelFontSize: TextUnit = TextUnit.Unspecified,
    dragSelectionEnabled: Boolean? = null,
    tapPressRefractionEnabled: Boolean = true,
    miuixBackdrop: Backdrop? = null,
    indicatorPositionProvider: (() -> Float)? = null,
    isScrollInProgressProvider: () -> Boolean = { false },
) {
    AppLiquidAwareTabRow(
        options = options,
        selectedValue = selectedValue,
        onSelectionChange = onSelectionChange,
        modifier = modifier,
        enabled = enabled,
        scrollable = scrollable,
        minTabWidth = minTabWidth,
        compactMiuixWhenTwoOptions = compactMiuixWhenTwoOptions,
        height = height,
        indicatorHeight = indicatorHeight,
        labelFontSize = labelFontSize,
        dragSelectionEnabled = dragSelectionEnabled,
        tapPressRefractionEnabled = tapPressRefractionEnabled,
        miuixBackdrop = miuixBackdrop,
        indicatorPositionProvider = indicatorPositionProvider,
        isScrollInProgressProvider = isScrollInProgressProvider,
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
    minTabWidth: Dp = AppChromeSizeTokens.MinimumTouchTarget,
    compactMiuixWhenTwoOptions: Boolean = true,
    height: Dp = AppChromeSizeTokens.BottomBarMatchedSegmentedControlHeightDp.dp,
    indicatorHeight: Dp = AppChromeSizeTokens.BottomBarMatchedSegmentedIndicatorHeightDp.dp,
    labelFontSize: TextUnit = TextUnit.Unspecified,
    dragSelectionEnabled: Boolean? = null,
    tapPressRefractionEnabled: Boolean = true,
    miuixBackdrop: Backdrop? = null,
    indicatorPositionProvider: (() -> Float)? = null,
    isScrollInProgressProvider: () -> Boolean = { false },
) {
    if (options.isEmpty()) return
    val liquidGlassEnabled = com.android.purebilibili.core.ui.LocalAppThemeConfig.current.liquidGlassEnabled
    if (!liquidGlassEnabled) {
        AppNativeTabRow(
            options = options,
            selectedValue = selectedValue,
            onSelectionChange = onSelectionChange,
            modifier = modifier,
            enabled = enabled,
            scrollable = scrollable,
            minTabWidth = minTabWidth,
            compactMiuixWhenTwoOptions = compactMiuixWhenTwoOptions,
            height = height,
            allowLabelOverflow = true,
            indicatorPositionProvider = indicatorPositionProvider,
        )
        return
    }
    val selectedIndex = options.indexOfFirst { it.value == selectedValue }.coerceAtLeast(0)
    val resolvedDragSelectionEnabled = dragSelectionEnabled ?: (options.size > 1)
    // Give every tab enough room for its longest label. The row itself remains
    // horizontally scrollable, so labels are never ellipsized or clipped on
    // narrow phones; this also applies to shared rows such as UP space tabs.
    val readableTabWidth = resolveReadableNativeTabMinWidth(
        requestedMinWidth = minTabWidth,
        labels = options.map { it.label },
        allowLabelOverflow = true,
    )
    val needsHorizontalScroll = scrollable || readableTabWidth > minTabWidth
    if (needsHorizontalScroll) {
        val scrollState = rememberScrollState()
        val density = LocalDensity.current
        val viewportMaxWidth = LocalConfiguration.current.screenWidthDp.dp
        BoxWithConstraints(
            modifier = modifier
                .widthIn(max = viewportMaxWidth)
                // The liquid shell intentionally draws beyond its content bounds for capture.
                // A rectangular scroll viewport would expose that overflow at either edge.
                .clip(CircleShape),
        ) {
            val viewportWidthPx = with(density) { maxWidth.toPx() }
            val itemWidthPx = with(density) { readableTabWidth.toPx() }
            KeepScrollableTabSelectionVisible(
                scrollState = scrollState,
                selectedIndex = selectedIndex,
                itemWidthPx = itemWidthPx,
                viewportWidthPx = viewportWidthPx,
                contentPaddingPx = with(density) { AppSpacingTokens.ExtraSmall.toPx() },
            )
            BottomBarLiquidSegmentedControl(
                items = options.map { it.label },
                selectedIndex = selectedIndex,
                onSelected = { index ->
                    options.getOrNull(index)?.let { onSelectionChange(it.value) }
                },
                modifier = Modifier.horizontalScroll(scrollState),
                enabled = enabled,
                itemWidth = readableTabWidth,
                height = height,
                indicatorHeight = indicatorHeight,
                labelFontSize = labelFontSize,
                liquidGlassEffectsEnabled = true,
                dragSelectionEnabled = false,
                longPressDragSelectionEnabled = resolvedDragSelectionEnabled,
                tapPressRefractionEnabled = tapPressRefractionEnabled,
                miuixBackdrop = miuixBackdrop,
                indicatorPositionProvider = indicatorPositionProvider,
                isScrollInProgressProvider = isScrollInProgressProvider,
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
            height = height,
            indicatorHeight = indicatorHeight,
            labelFontSize = labelFontSize,
            liquidGlassEffectsEnabled = true,
            dragSelectionEnabled = resolvedDragSelectionEnabled,
            tapPressRefractionEnabled = tapPressRefractionEnabled,
            miuixBackdrop = miuixBackdrop,
            indicatorPositionProvider = indicatorPositionProvider,
            isScrollInProgressProvider = isScrollInProgressProvider,
        )
    }
}
