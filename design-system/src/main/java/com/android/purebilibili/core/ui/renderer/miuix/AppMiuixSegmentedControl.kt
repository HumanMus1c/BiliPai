package com.android.purebilibili.core.ui.renderer.miuix

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
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
import com.android.purebilibili.core.ui.resolveMiuixNonGlassControlGeometry
import com.android.purebilibili.core.ui.isMiuixNonGlassEnabled
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TabRowDefaults
import top.yukonga.miuix.kmp.squircle.squircleClip
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    if (isMiuixNonGlassEnabled()) {
        AppMiuixNonGlassTabs(
            options = options,
            selectedValue = selectedValue,
            enabled = enabled,
            compact = true,
            minTabWidth = 0.dp,
            modifier = modifier,
            onSelectionChange = onSelectionChange,
        )
        return
    }
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
    if (isMiuixNonGlassEnabled()) {
        AppMiuixNonGlassTabs(
            options = options,
            selectedValue = selectedValue,
            enabled = enabled,
            compact = false,
            minTabWidth = minTabWidth,
            modifier = modifier,
            onSelectionChange = onSelectionChange,
        )
        return
    }
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

/** Native tabs own selection/press feedback; the wrapper only supplies measured geometry. */
@Composable
private fun <T> AppMiuixNonGlassTabs(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    enabled: Boolean,
    compact: Boolean,
    minTabWidth: Dp,
    modifier: Modifier,
    onSelectionChange: (T) -> Unit,
) {
    val labels = options.map { it.label }
    val selectedIndex = resolveAppSegmentedSelectionIndex(options, selectedValue)
    val density = LocalDensity.current
    val measurer = rememberTextMeasurer()
    // Match upstream TabItem: main text with body1 size, bold when selected.
    val style = MiuixTheme.textStyles.main.copy(
        fontSize = MiuixTheme.textStyles.body1.fontSize,
        fontWeight = FontWeight.Bold,
    )
    val labelSizes = remember(labels, style, measurer, density) {
        labels.map { measurer.measure(AnnotatedString(it), style, maxLines = 1).size }
    }
    val textHeight = with(density) { (labelSizes.maxOfOrNull { it.height } ?: 0).toDp() }
    val labelWidth = with(density) { (labelSizes.maxOfOrNull { it.width } ?: 0).toDp() }
    val geometry = resolveMiuixNonGlassControlGeometry(compact, textHeight)
    val interactiveHeight = maxOf(geometry.height, AppChromeSizeTokens.MinimumTouchTarget)
    val readableWidth = maxOf(
        AppChromeSizeTokens.MinimumTouchTarget,
        minTabWidth,
        labelWidth + 24.dp,
    )
    val scrollState = rememberLazyListState()
    BoxWithConstraints(
        modifier = modifier
            .heightIn(min = AppChromeSizeTokens.MinimumTouchTarget)
            .then(if (!enabled) Modifier.semantics { disabled() } else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        val needsScroll = readableWidth * options.size + 8.dp * (options.size - 1) > maxWidth
        TabRow(
            tabs = labels,
            selectedTabIndex = selectedIndex,
            onTabSelected = { index ->
                if (enabled) options.getOrNull(index)?.let { onSelectionChange(it.value) }
            },
            modifier = Modifier.squircleClip(geometry.cornerRadius),
            minWidth = readableWidth,
            maxWidth = Dp.Infinity,
            // Miuix 0.9.4 attaches selectable to the full TabRow height and does not expose a
            // separate hit slop API. Use the accessibility minimum as the actual native row
            // height; an outer 48dp wrapper alone leaves the selectable area at 36/42dp.
            height = interactiveHeight,
            cornerRadius = geometry.cornerRadius,
            itemSpacing = 8.dp,
            listState = scrollState,
        )
        LaunchedEffect(needsScroll, selectedIndex, options.size) {
            if (needsScroll && (selectedIndex == 0 || selectedIndex == options.lastIndex)) {
                withFrameNanos { }
                scrollState.scrollToItem(selectedIndex)
            }
        }
    }
}
