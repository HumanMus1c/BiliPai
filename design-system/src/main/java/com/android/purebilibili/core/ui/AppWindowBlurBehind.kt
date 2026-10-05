package com.android.purebilibili.core.ui

import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider

/**
 * 系统 blur-behind：对弹窗背后的 Activity 内容做跨窗口模糊（API 31+）。
 *
 * ModalBottomSheet / Dialog 都运行在独立窗口里，Haze 无法跨窗口采样，
 * 只能依赖 WindowManager 的 FLAG_BLUR_BEHIND。设备不支持跨窗口模糊
 * （低端机、省电模式、开发者选项关闭）时静默跳过，由遮罩层单独承担视觉降级。
 */
@Composable
fun ModalWindowBlurBehindEffect(
    enabled: Boolean,
    radius: Dp = 24.dp,
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val view = LocalView.current
    val density = LocalDensity.current
    DisposableEffect(view, enabled, radius) {
        val window = ((view.parent as? DialogWindowProvider) ?: (view as? DialogWindowProvider))?.window
        var applied = false
        if (enabled && window != null && window.windowManager.isCrossWindowBlurEnabled) {
            window.attributes = window.attributes.also { attrs ->
                attrs.flags = attrs.flags or WindowManager.LayoutParams.FLAG_BLUR_BEHIND
                attrs.blurBehindRadius = with(density) { radius.roundToPx() }
            }
            applied = true
        }
        onDispose {
            if (applied && window != null) {
                window.attributes = window.attributes.also { attrs ->
                    attrs.flags = attrs.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
                    attrs.blurBehindRadius = 0
                }
            }
        }
    }
}
