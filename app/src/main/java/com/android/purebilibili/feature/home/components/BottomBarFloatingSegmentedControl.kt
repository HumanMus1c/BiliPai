package com.android.purebilibili.feature.home.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.rememberAppSemanticVisualPolicy
import top.yukonga.miuix.kmp.blur.Backdrop

/**
 * Reuse wrapper around [FloatingBottomBar]. No local drawBackdrop / lens / vibrancy.
 * Pager follow and drag flags are forwarded into the same dock implementation.
 */
@Composable
internal fun BottomBarFloatingSegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier,
    enabled: Boolean,
    itemWidth: Dp?,
    height: Dp,
    indicatorHeight: Dp,
    labelFontSize: TextUnit,
    containerHorizontalPadding: Dp,
    containerVerticalPadding: Dp,
    liquidGlassEffectsEnabled: Boolean,
    dragSelectionEnabled: Boolean,
    forceLiquidChrome: Boolean,
    miuixBackdrop: Backdrop?,
    containerColorOverride: Color? = null,
    selectedTextColorOverride: Color?,
    unselectedTextColorOverride: Color?,
    indicatorPositionProvider: (() -> Float)?,
    onIndicatorPositionChanged: ((Float) -> Unit)?,
    isScrollInProgressProvider: () -> Boolean = { false },
) {
    if (items.isEmpty()) return

    val context = LocalContext.current
    val visualPolicy = rememberAppSemanticVisualPolicy()
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val nativeGlassEnabled = forceLiquidChrome || homeSettings.androidNativeLiquidGlassEnabled
    val liquidGlassEnabled = resolveSegmentedControlLiquidGlassEnabled(
        storedLiquidGlassEnabled = homeSettings.isBottomBarLiquidGlassEnabled,
        liquidGlassEffectsEnabled = liquidGlassEffectsEnabled,
        supportsIndependentLiquidGlass = visualPolicy.supportsIndependentLiquidGlass,
        androidNativeLiquidGlassEnabled = nativeGlassEnabled,
    )
    val isDarkTheme = isSystemInDarkTheme()
    val itemCount = items.size
    val maxTabIndex = (itemCount - 1).coerceAtLeast(0)
    val safeSelectedIndex = selectedIndex.coerceIn(0, maxTabIndex)
    val selectedTextColor = selectedTextColorOverride ?: MaterialTheme.colorScheme.primary
    val unselectedTextColor = unselectedTextColorOverride
        ?: resolveLiquidSegmentedControlUnselectedTextColor(
            onSurface = MaterialTheme.colorScheme.onSurface,
            enabled = enabled,
        )
    val shellColor = containerColorOverride ?: resolveBiliPaiBottomBarShellColor(
        containerColor = AppSurfaceTokens.cardContainer(),
        liquidGlassEnabled = liquidGlassEnabled,
        darkTheme = isDarkTheme,
    )
    val floatingMode = if (liquidGlassEnabled && miuixBackdrop != null) {
        FloatingBottomBarMode.LiquidGlass
    } else {
        FloatingBottomBarMode.None
    }
    val dockShellHeight = if (liquidGlassEnabled) {
        FloatingBottomBarDefaultShellHeight
    } else {
        height
    }
    val dockIndicatorHeight = if (liquidGlassEnabled) {
        FloatingBottomBarIndicatorHeight
    } else {
        indicatorHeight
    }
    val dockModifier = if (liquidGlassEnabled) {
        modifier.wrapContentWidth()
    } else if (itemWidth != null) {
        modifier.width(itemWidth * itemCount + containerHorizontalPadding * 2)
    } else {
        modifier
    }
    val selectedIndexState = rememberUpdatedState(safeSelectedIndex)
    val onSelectedState = rememberUpdatedState(onSelected)
    val enabledState = rememberUpdatedState(enabled)

    FloatingBottomBar(
        selectedIndex = { selectedIndexState.value },
        onSelected = { index ->
            if (enabledState.value && index in items.indices) onSelectedState.value(index)
        },
        onReselected = {
            if (enabledState.value) onSelectedState.value(selectedIndexState.value)
        },
        backdrop = miuixBackdrop,
        tabsCount = itemCount,
        modifier = dockModifier,
        mode = floatingMode,
        colors = FloatingBottomBarColors(
            containerColor = shellColor,
            indicatorColor = selectedTextColor,
            contentColor = unselectedTextColor,
            activeContentColor = selectedTextColor,
        ),
        shellHeight = dockShellHeight,
        indicatorHeight = dockIndicatorHeight,
        indicatorPositionProvider = indicatorPositionProvider,
        isScrollInProgressProvider = isScrollInProgressProvider,
        dragSelectionEnabled = dragSelectionEnabled && enabled && itemCount > 1,
    ) {
        items.forEachIndexed { index, label ->
            FloatingBottomBarItem(
                onClick = {
                    if (enabled) onSelected(index)
                },
                selected = index == safeSelectedIndex,
            ) {
                val contentColor = LocalFloatingBottomBarContentColor.current
                AppText(
                    text = label,
                    color = contentColor,
                    fontSize = labelFontSize,
                    fontWeight = if (index == safeSelectedIndex) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
