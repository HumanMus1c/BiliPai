package com.android.purebilibili.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.theme.UiPreset

/** Shared content-card decisions for feed / search / dynamic list shells. */
data class ContentCardSurfaceSpec(
    val usesTonalContainerTreatment: Boolean,
    val cornerLevel: ContainerLevel,
    val borderWidthDp: Float,
    val borderAlpha: Float,
    val tonalElevationDp: Float,
    val shadowElevationDp: Float
)

fun resolveContentCardSurfaceSpec(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): ContentCardSurfaceSpec {
    val useMiuix = isNativeMiuixEnabled(uiPreset, androidNativeVariant)
    return if (useMiuix) {
        ContentCardSurfaceSpec(
            usesTonalContainerTreatment = true,
            cornerLevel = ContainerLevel.Card,
            borderWidthDp = 0.8f,
            borderAlpha = 0.22f,
            tonalElevationDp = 0f,
            shadowElevationDp = 0f
        )
    } else {
        ContentCardSurfaceSpec(
            usesTonalContainerTreatment = false,
            cornerLevel = ContainerLevel.Card,
            borderWidthDp = 0f,
            borderAlpha = 0f,
            tonalElevationDp = 0f,
            shadowElevationDp = 0f
        )
    }
}

@Composable
fun rememberContentCardSurfaceSpec(): ContentCardSurfaceSpec {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    return remember(uiPreset, androidNativeVariant) {
        resolveContentCardSurfaceSpec(uiPreset, androidNativeVariant)
    }
}

fun resolveContentCardCornerDp(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): Dp = AppShapes.resolveContainerCornerDp(
    level = ContainerLevel.Card,
    uiPreset = uiPreset,
    androidNativeVariant = androidNativeVariant
)
