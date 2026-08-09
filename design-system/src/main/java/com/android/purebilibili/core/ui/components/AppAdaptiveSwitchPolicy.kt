package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AppUiStyle

internal enum class AppAdaptiveSwitchTreatment {
    MATERIAL,
    MIUIX
}

internal fun resolveAppAdaptiveSwitchTreatment(
    uiStyle: AppUiStyle
): AppAdaptiveSwitchTreatment = when (uiStyle) {
    AppUiStyle.MIUIX -> AppAdaptiveSwitchTreatment.MIUIX
    AppUiStyle.MATERIAL3 -> AppAdaptiveSwitchTreatment.MATERIAL
}
