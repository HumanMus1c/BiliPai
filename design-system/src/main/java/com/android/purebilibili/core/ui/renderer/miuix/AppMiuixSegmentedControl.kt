package com.android.purebilibili.core.ui.renderer.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.adaptiveSquircleBackground
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.core.ui.components.AppSegmentedControlColors
import com.android.purebilibili.core.ui.components.resolveAppMiuixSegmentedColors
import com.android.purebilibili.core.ui.components.resolveAppSegmentedSelectionIndex
import com.android.purebilibili.core.ui.resolveRoundedControlVisualGeometry
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.squircle.squircleClip

@Composable
internal fun <T> AppMiuixSegmentedControl(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    enabled: Boolean,
    colors: AppSegmentedControlColors,
    preferredCornerRadius: Dp,
    height: Dp? = null,
    modifier: Modifier,
    onSelectionChange: (T) -> Unit,
) {
    val selectedIndex = resolveAppSegmentedSelectionIndex(options, selectedValue)
    val tabColors = resolveAppMiuixSegmentedColors(colors)
    val itemGeometry = resolveRoundedControlVisualGeometry(
        preferredCornerRadius = preferredCornerRadius,
        nativeMinimumHeight = height ?: AppChromeSizeTokens.MinimumTouchTarget,
    )
    val outerGeometry = resolveRoundedControlVisualGeometry(
        preferredCornerRadius = preferredCornerRadius,
        nativeMinimumHeight = itemGeometry.height + 8.dp,
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .adaptiveSquircleBackground(
                color = colors.outerContainerColor,
                cornerRadius = outerGeometry.cornerRadius,
            )
            .padding(AppSpacingTokens.ExtraSmall),
    ) {
        TabRow(
            tabs = options.map { it.label },
            selectedTabIndex = selectedIndex,
            onTabSelected = { index ->
                if (enabled) options.getOrNull(index)?.let { onSelectionChange(it.value) }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = TabRowDefaults.tabRowColors(
                backgroundColor = tabColors.backgroundColor,
                contentColor = tabColors.contentColor,
                selectedBackgroundColor = tabColors.selectedBackgroundColor,
                selectedContentColor = tabColors.selectedContentColor,
            ),
            height = itemGeometry.height,
            cornerRadius = itemGeometry.cornerRadius,
            itemSpacing = AppSpacingTokens.ExtraSmall,
            // 与 AppMiuixTabRow 一致：交给 Miuix 按容器宽度均分，避免默认 minWidth=76dp
            // 在选项较多时压缩文字。
            minWidth = 0.dp,
            maxWidth = Dp.Infinity,
        )
    }
}

@Composable
internal fun <T> AppMiuixTabRow(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    enabled: Boolean,
    scrollable: Boolean,
    minTabWidth: Dp,
    colors: AppSegmentedControlColors,
    preferredCornerRadius: Dp,
    height: Dp? = null,
    modifier: Modifier,
    indicatorPositionProvider: (() -> Float)? = null,
    onSelectionChange: (T) -> Unit,
) {
    val selectedIndex = resolveAppSegmentedSelectionIndex(options, selectedValue)
    val scrollState = rememberLazyListState()
    val tabColors = resolveAppMiuixSegmentedColors(colors)
    val geometry = resolveRoundedControlVisualGeometry(
        preferredCornerRadius = preferredCornerRadius,
        nativeMinimumHeight = height ?: AppChromeSizeTokens.MinimumTouchTarget,
    )
    TabRow(
        tabs = options.map { it.label },
        selectedTabIndex = selectedIndex,
        onTabSelected = { index ->
            if (enabled) options.getOrNull(index)?.let { onSelectionChange(it.value) }
        },
        // Respect the caller's measured width so compact two-option controls do not
        // expand to the full parent and consume the adjacent action area.
        // Upstream paints a rectangular track and only rounds the selected item.
        // Clip the stationary viewport as well, including during horizontal scrolling.
        modifier = modifier.squircleClip(geometry.cornerRadius),
        colors = TabRowDefaults.tabRowColors(
            backgroundColor = tabColors.backgroundColor,
            contentColor = tabColors.contentColor,
            selectedBackgroundColor = tabColors.selectedBackgroundColor,
            selectedContentColor = tabColors.selectedContentColor,
        ),
        // 非 scrollable（如频道/状态切换）：交给 Miuix 按容器宽度均分，与 Material TabRow
        // 一致；scrollable（如时间表/分类）：minTabWidth 兜底保证可读。
        minWidth = if (scrollable) minTabWidth else 0.dp,
        maxWidth = Dp.Infinity,
        height = geometry.height,
        cornerRadius = geometry.cornerRadius,
        itemSpacing = AppSpacingTokens.Small,
        listState = if (scrollable) scrollState else null,
    )
    // Upstream centers every selected item, including the first and last. At a non-zero parent
    // x-position that places the boundary item outside LazyRow's viewport and desynchronizes the
    // selected squircle from its label. Let the upstream positioning settle, then pin boundaries.
    LaunchedEffect(scrollable, selectedIndex, options.size) {
        if (!scrollable || options.isEmpty()) return@LaunchedEffect
        if (selectedIndex == 0 || selectedIndex == options.lastIndex) {
            withFrameNanos { }
            scrollState.scrollToItem(selectedIndex)
        }
    }
}
