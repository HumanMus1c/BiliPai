package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.core.ui.renderer.material3.AppMaterial3Switch
import com.android.purebilibili.core.ui.renderer.miuix.AppMiuixSwitch

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    thumbContent: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    showThumbIcon: Boolean = true,
) {
    if (LocalAppUiStyle.current == AppUiStyle.MIUIX &&
        com.android.purebilibili.core.ui.LocalAppThemeConfig.current.liquidGlassEnabled &&
        com.android.purebilibili.core.ui.LocalComponentMotionEnabled.current &&
        android.os.Build.VERSION.SDK_INT >= 33 && enabled && onCheckedChange != null && thumbContent == null) {
        AppLiquidSwitch(checked, onCheckedChange, modifier)
        return
    }
    when (LocalAppUiStyle.current) {
        AppUiStyle.MATERIAL3 -> AppMaterial3Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            thumbContent = thumbContent,
            enabled = enabled,
            interactionSource = interactionSource,
            showThumbIcon = showThumbIcon,
        )
        AppUiStyle.MIUIX -> AppMiuixSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = modifier,
            enabled = enabled,
        )
    }
}
