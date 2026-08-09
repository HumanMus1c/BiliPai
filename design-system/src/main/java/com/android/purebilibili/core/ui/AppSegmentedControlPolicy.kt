package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle

data class AppSegmentedControlPolicy(
    val usesEmphasizedTitle: Boolean,
    val usesMaterialFallback: Boolean,
    val usesNativeTabRow: Boolean,
    val usesMaterialColorTokens: Boolean,
    val pillCornerRadius: Dp,
)

internal fun resolveAppSegmentedControlPolicy(
    uiStyle: AppUiStyle,
): AppSegmentedControlPolicy = when (uiStyle) {
    AppUiStyle.MIUIX -> AppSegmentedControlPolicy(
        usesEmphasizedTitle = true,
        usesMaterialFallback = true,
        usesNativeTabRow = true,
        usesMaterialColorTokens = false,
        pillCornerRadius = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Pill,
            uiStyle = AppUiStyle.MIUIX,
        ),
    )
    AppUiStyle.MATERIAL3 -> AppSegmentedControlPolicy(
        usesEmphasizedTitle = true,
        usesMaterialFallback = true,
        usesNativeTabRow = false,
        usesMaterialColorTokens = true,
        pillCornerRadius = AppShapes.resolveContainerCornerDp(
            level = ContainerLevel.Pill,
            uiStyle = AppUiStyle.MATERIAL3,
        ),
    )
}

@Composable
fun rememberAppSegmentedControlPolicy(): AppSegmentedControlPolicy {
    val uiStyle = LocalAppUiStyle.current
    return remember(uiStyle) {
        resolveAppSegmentedControlPolicy(uiStyle)
    }
}
