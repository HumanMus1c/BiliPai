package com.android.purebilibili.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.feature.home.components.miuix.DampedDragTrackingMode
import com.android.purebilibili.feature.home.components.liquid.rememberCombinedBackdrop
import top.yukonga.miuix.kmp.blur.Backdrop
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop

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
    longPressDragSelectionEnabled: Boolean,
    forceLiquidChrome: Boolean,
    miuixBackdrop: Backdrop?,
    containerColorOverride: Color? = null,
    selectedTextColorOverride: Color?,
    unselectedTextColorOverride: Color?,
    indicatorPositionProvider: (() -> Float)?,
    onIndicatorPositionChanged: ((Float) -> Unit)?,
    isScrollInProgressProvider: () -> Boolean = { false },
    liquidGlassTuningOverride: LiquidGlassTuning? = null,
) {
    if (items.isEmpty()) return

    val context = LocalContext.current
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    val nativeGlassEnabled = forceLiquidChrome || homeSettings.androidNativeLiquidGlassEnabled
    val liquidGlassEnabled = resolveSegmentedControlLiquidGlassEnabled(
        storedLiquidGlassEnabled = homeSettings.isBottomBarLiquidGlassEnabled,
        liquidGlassEffectsEnabled = liquidGlassEffectsEnabled,
        supportsIndependentLiquidGlass = false,
        androidNativeLiquidGlassEnabled = nativeGlassEnabled,
    )
    val storedLiquidGlassTuning = remember(
        homeSettings.liquidGlassProgress,
        homeSettings.liquidGlassAdvancedSettings,
        homeSettings.liquidGlassReadabilityMode,
    ) {
        resolveLiquidGlassTuning(
            homeSettings.liquidGlassProgress,
            homeSettings.liquidGlassAdvancedSettings,
            homeSettings.liquidGlassReadabilityMode,
        )
    }
    val liquidGlassTuning = liquidGlassTuningOverride ?: storedLiquidGlassTuning
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
        liquidGlassTuning = liquidGlassTuning,
    )
    // Reused docks cannot assume that a caller-provided backdrop covers the dock's
    // window coordinates. Keep a local, full-dock source behind the chrome just as
    // the home dock keeps its content source behind (and outside) the dock itself.
    val localBackdrop = rememberLayerBackdrop()
    val effectiveBackdrop = if (liquidGlassEnabled) {
        if (miuixBackdrop != null) {
            rememberCombinedBackdrop(localBackdrop, miuixBackdrop)
        } else {
            localBackdrop
        }
    } else {
        null
    }
    val floatingMode = if (effectiveBackdrop != null) {
        FloatingBottomBarMode.LiquidGlass
    } else {
        FloatingBottomBarMode.None
    }
    val rootModifier = if (itemWidth != null) {
        modifier.width(itemWidth * itemCount + containerHorizontalPadding * 2)
    } else {
        modifier
    }
    val selectedIndexState = rememberUpdatedState(safeSelectedIndex)
    val onSelectedState = rememberUpdatedState(onSelected)
    val enabledState = rememberUpdatedState(enabled)

    BoxWithConstraints(
        modifier = rootModifier.height(height)
    ) {
        val indicatorWidthDp = when {
            itemWidth != null -> itemWidth.value
            constraints.hasBoundedWidth ->
                ((maxWidth.value - 8f).coerceAtLeast(0f) / itemCount)
            else -> indicatorHeight.value * FLOATING_DOCK_MIN_INDICATOR_ASPECT
        }
        val fittedSegmentedIndicatorWidth = resolveSegmentedControlIndicatorWidthDp(
            slotWidthDp = indicatorWidthDp,
            indicatorHeightDp = indicatorHeight.value,
            itemCount = itemCount,
        ).dp
        val captureInsets = resolveFloatingDockCaptureInsets(
            shellHeightDp = height.value,
            requestedIndicatorHeightDp = indicatorHeight.value,
            indicatorWidthDp = fittedSegmentedIndicatorWidth.value,
        )
        if (effectiveBackdrop != null) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .bottomBarMatchedCaptureOverflow(
                        horizontalInset = captureInsets.horizontalDp.dp,
                        verticalInset = captureInsets.verticalDp.dp,
                    )
                    .alpha(0f)
                    .layerBackdrop(localBackdrop)
                    .background(AppSurfaceTokens.background())
            )
        }
        FloatingBottomBar(
            selectedIndex = { selectedIndexState.value },
            onSelected = { index ->
                if (enabledState.value && index in items.indices) onSelectedState.value(index)
            },
            onReselected = {
                if (enabledState.value) onSelectedState.value(selectedIndexState.value)
            },
            backdrop = effectiveBackdrop,
            tabsCount = itemCount,
            modifier = Modifier.matchParentSize(),
            mode = floatingMode,
            colors = FloatingBottomBarColors(
                containerColor = shellColor,
                indicatorColor = selectedTextColor,
                contentColor = unselectedTextColor,
                activeContentColor = selectedTextColor,
            ),
            shellHeight = height,
            indicatorHeight = indicatorHeight,
            indicatorWidth = fittedSegmentedIndicatorWidth,
            indicatorPositionProvider = indicatorPositionProvider,
            isScrollInProgressProvider = isScrollInProgressProvider,
            dragSelectionEnabled = dragSelectionEnabled && enabled && itemCount > 1,
            longPressDragSelectionEnabled =
                longPressDragSelectionEnabled && enabled && itemCount > 1,
            dragTrackingMode = DampedDragTrackingMode.SPRING,
            onIndicatorPositionChanged = onIndicatorPositionChanged,
            liquidGlassTuning = liquidGlassTuning,
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
}
