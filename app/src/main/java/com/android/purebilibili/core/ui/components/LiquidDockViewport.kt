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
 * Frame the viewport to a pill dock shape so the ends never expose sharp corners
 * while the inner rail scrolls horizontally.
 */
@Composable
internal fun Modifier.liquidDockViewport(): Modifier {
    val uiStyle = LocalAppUiStyle.current
    val liquidGlassEnabled = LocalAppThemeConfig.current.liquidGlassEnabled
    val shape = if (uiStyle == AppUiStyle.MIUIX && !liquidGlassEnabled) {
        AppShapes.container(ContainerLevel.Card)
    } else {
        CircleShape
    }
    return this.clip(shape)
}
