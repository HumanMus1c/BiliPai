package com.android.purebilibili.feature.bangumi

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.purebilibili.core.store.HomeSettings
import com.android.purebilibili.core.store.SettingsManager
import com.android.purebilibili.core.ui.AppNativeTabRowHeightDp
import com.android.purebilibili.core.ui.components.AppNativeTabRow
import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.android.purebilibili.core.ui.roundMatchedLiquidIndicatorHeightDp
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl

@Composable
internal fun <T> BangumiLiquidAwareTabRow(
    options: List<AppSegmentOption<T>>,
    selectedValue: T,
    onSelectionChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    scrollable: Boolean = false,
    minTabWidth: Dp = 72.dp,
) {
    if (options.isEmpty()) return
    val context = LocalContext.current
    val homeSettings by SettingsManager
        .getHomeSettings(context)
        .collectAsStateWithLifecycle(initialValue = HomeSettings())
    if (!homeSettings.androidNativeLiquidGlassEnabled) {
        AppNativeTabRow(
            options = options,
            selectedValue = selectedValue,
            modifier = modifier,
            enabled = enabled,
            scrollable = scrollable,
            minTabWidth = minTabWidth,
            onSelectionChange = onSelectionChange,
        )
        return
    }

    val selectedIndex = options.indexOfFirst { it.value == selectedValue }.coerceAtLeast(0)
    val dockHeightDp = AppNativeTabRowHeightDp
    val indicatorHeightDp = roundMatchedLiquidIndicatorHeightDp(dockHeightDp.toFloat())
    val control: @Composable (Modifier) -> Unit = { controlModifier ->
        BottomBarLiquidSegmentedControl(
            items = options.map { it.label },
            selectedIndex = selectedIndex,
            onSelected = { index ->
                options.getOrNull(index)?.let { onSelectionChange(it.value) }
            },
            modifier = controlModifier,
            enabled = enabled,
            itemWidth = if (scrollable) minTabWidth else null,
            height = dockHeightDp.dp,
            indicatorHeight = indicatorHeightDp.dp,
            forceLiquidChrome = true,
            dragSelectionEnabled = !scrollable,
        )
    }
    if (scrollable) {
        Row(
            modifier = modifier.horizontalScroll(rememberScrollState())
        ) {
            control(Modifier)
        }
    } else {
        control(modifier)
    }
}
