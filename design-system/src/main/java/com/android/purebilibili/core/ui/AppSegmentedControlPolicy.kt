package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.theme.UiPreset

data class AppSegmentedControlPolicy(
    val usesEmphasizedTitle: Boolean,
    val usesMaterialFallback: Boolean,
    val usesNativeTabRow: Boolean,
    val usesMaterialColorTokens: Boolean,
    val pillCornerRadius: Dp,
)

internal fun resolveAppSegmentedControlPolicy(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant,
): AppSegmentedControlPolicy = AppSegmentedControlPolicy(
    usesEmphasizedTitle = uiPreset == UiPreset.MD3,
    usesMaterialFallback = uiPreset == UiPreset.MD3,
    usesNativeTabRow = uiPreset == UiPreset.MD3 &&
        androidNativeVariant == AndroidNativeVariant.MIUIX,
    usesMaterialColorTokens = androidNativeVariant == AndroidNativeVariant.MATERIAL3,
    pillCornerRadius = AppShapes.resolveContainerCornerDp(
        level = ContainerLevel.Pill,
        uiPreset = uiPreset,
        androidNativeVariant = androidNativeVariant,
    ),
)

@Composable
fun rememberAppSegmentedControlPolicy(): AppSegmentedControlPolicy {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    return remember(uiPreset, androidNativeVariant) {
        resolveAppSegmentedControlPolicy(uiPreset, androidNativeVariant)
    }
}
