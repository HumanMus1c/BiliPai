package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.UiStyle

/** Neutral settings value for choosing the app's visual system. */
enum class AppThemeSelection {
    IOS,
    MATERIAL3,
    MIUIX,
}

fun UiStyle.toAppThemeSelection(): AppThemeSelection = when (this) {
    UiStyle.IOS -> AppThemeSelection.IOS
    UiStyle.MATERIAL3 -> AppThemeSelection.MATERIAL3
    UiStyle.MIUIX -> AppThemeSelection.MIUIX
}

fun AppThemeSelection.toUiStyle(): UiStyle = when (this) {
    AppThemeSelection.IOS -> UiStyle.IOS
    AppThemeSelection.MATERIAL3 -> UiStyle.MATERIAL3
    AppThemeSelection.MIUIX -> UiStyle.MIUIX
}
