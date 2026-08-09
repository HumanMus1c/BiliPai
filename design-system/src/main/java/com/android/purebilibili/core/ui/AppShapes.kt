package com.android.purebilibili.core.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.theme.resolveAndroidNativeChromeTokens
import com.android.purebilibili.core.theme.resolveCornerRadiusScale

/** Semantic container categories shared across the three UI presets. */
enum class ContainerLevel {
    /** Tiny tags / badges. iOS base = 4dp. */
    Tag,
    /** Small chips / micro-buttons. iOS base = 6dp. */
    Chip,
    /** Input fields, search bars, small chip-like containers. iOS base = 10dp. */
    Field,
    /** Standard surface cards. iOS base = 12dp. */
    Card,
    /** Alert / confirm dialog containers. iOS base = 14dp. */
    Dialog,
    /** Bottom sheet / modal sheet (top-rounded). iOS base = 20dp. */
    Sheet,
    /** Floating elements — FABs, floating bars. iOS base = 28dp. */
    Floating,
    /** Pill / segmented selectors — radius comes from chrome tokens directly. */
    Pill
}

/**
 * 两值主题的形状 tokens。使用 [AppShapes.container] 而非手写 `RoundedCornerShape(N.dp)`。
 * 基础值按主题风格缩放（MIUIX 更大、MATERIAL3 更紧凑）；Pill 级直接取 chrome tokens，
 * 使各风格保留原生胶囊曲率。
 */
object AppShapes {

    private fun baseDp(level: ContainerLevel): Float = when (level) {
        ContainerLevel.Tag -> 4f
        ContainerLevel.Chip -> 6f
        ContainerLevel.Field -> 10f
        ContainerLevel.Card -> 12f
        ContainerLevel.Dialog -> 14f
        ContainerLevel.Sheet -> 20f
        ContainerLevel.Floating -> 28f
        ContainerLevel.Pill -> 0f
    }

    fun resolveContainerCornerDp(
        level: ContainerLevel,
        uiStyle: AppUiStyle
    ): Dp {
        if (level == ContainerLevel.Pill) {
            return resolveAndroidNativeChromeTokens(uiStyle).pillCornerRadiusDp.dp
        }
        val scale = resolveCornerRadiusScale(uiStyle)
        return (baseDp(level) * scale).dp
    }

    fun resolveContainerShape(
        level: ContainerLevel,
        uiStyle: AppUiStyle
    ): Shape {
        val dp = resolveContainerCornerDp(level, uiStyle)
        return if (level == ContainerLevel.Sheet) {
            RoundedCornerShape(topStart = dp, topEnd = dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        } else {
            RoundedCornerShape(dp)
        }
    }

    /**
     * Shape for containers that draw a stroke via [androidx.compose.foundation.BorderStroke] or
     * [androidx.compose.foundation.border]. Material3 borders follow [RoundedCornerShape]
     * reliably; iOS continuous corners render as chamfered edges.
     */
    fun resolveBorderedContainerShape(
        level: ContainerLevel,
        uiStyle: AppUiStyle
    ): Shape {
        val dp = resolveContainerCornerDp(level, uiStyle)
        return if (level == ContainerLevel.Sheet) {
            RoundedCornerShape(topStart = dp, topEnd = dp, bottomStart = 0.dp, bottomEnd = 0.dp)
        } else {
            RoundedCornerShape(dp)
        }
    }

    @Composable
    fun container(level: ContainerLevel): Shape = resolveContainerShape(
        level = level,
        uiStyle = LocalAppUiStyle.current
    )

    @Composable
    fun borderedContainer(level: ContainerLevel): Shape = resolveBorderedContainerShape(
        level = level,
        uiStyle = LocalAppUiStyle.current
    )

    @Composable
    fun containerCornerDp(level: ContainerLevel): Dp = resolveContainerCornerDp(
        level = level,
        uiStyle = LocalAppUiStyle.current
    )
}
