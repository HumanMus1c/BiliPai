package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.LocalAppThemeConfig

/**
 * Non-glass rails keep their conventional rounded viewport. Liquid-glass rails must remain
 * unclipped so the moving indicator can bloom and disperse beyond both vertical dock edges.
 * The liquid shell and indicator already draw their own capsule shapes.
 */
@Composable
internal fun Modifier.liquidDockViewport(): Modifier {
    val uiStyle = LocalAppUiStyle.current
    val liquidGlassEnabled = LocalAppThemeConfig.current.liquidGlassEnabled
    if (liquidGlassEnabled) return this
    val shape = if (uiStyle == AppUiStyle.MIUIX) {
        AppShapes.container(ContainerLevel.Card)
    } else {
        CircleShape
    }
    return this.clip(shape)
}
