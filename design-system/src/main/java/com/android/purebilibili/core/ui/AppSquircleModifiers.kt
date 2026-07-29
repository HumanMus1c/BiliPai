package com.android.purebilibili.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.LocalAndroidNativeVariant
import com.android.purebilibili.core.theme.LocalUiPreset
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.theme.shouldUseMiuixSmoothRounding
import top.yukonga.miuix.kmp.squircle.squircleBackground

@Composable
fun Modifier.adaptiveSquircleBackground(
    color: Color,
    cornerRadius: Dp
): Modifier {
    val uiPreset = LocalUiPreset.current
    val androidNativeVariant = LocalAndroidNativeVariant.current
    return when {
        shouldUseMiuixSmoothRounding(uiPreset, androidNativeVariant) -> {
            squircleBackground(color = color, cornerRadius = cornerRadius)
        }
        shouldUseIosContinuousRounding(uiPreset) -> {
            clip(IosContinuousRoundedCornerShape(cornerRadius))
                .background(color)
        }
        else -> {
            clip(RoundedCornerShape(cornerRadius))
                .background(color)
        }
    }
}

internal fun shouldApplyMiuixSquircleBackground(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): Boolean = shouldUseMiuixSmoothRounding(uiPreset, androidNativeVariant)