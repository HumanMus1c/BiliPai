package com.android.purebilibili.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.TooltipAnchorPosition
import top.yukonga.miuix.kmp.basic.TooltipBox as MiuixTooltipBox

/**
 * Long-press / hover tooltip bridge.
 *
 * MIUIX uses the official [MiuixTooltipBox] plain-text convenience API.
 * iOS / Material paths pass through the [content] unchanged so callers can keep
 * visible copy (title/summary) without inventing a parallel Material tooltip.
 */
@Composable
fun AdaptivePlainTooltipBox(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    when (rememberPresetPrimitiveRenderer()) {
        PresetPrimitiveRenderer.MIUIX_BRIDGED -> {
            MiuixTooltipBox(
                text = text,
                modifier = modifier,
                enabled = enabled && text.isNotBlank(),
                positioning = TooltipAnchorPosition.Below,
                content = content
            )
        }
        PresetPrimitiveRenderer.IOS,
        PresetPrimitiveRenderer.MATERIAL3 -> {
            Box(modifier = modifier) {
                content()
            }
        }
    }
}
