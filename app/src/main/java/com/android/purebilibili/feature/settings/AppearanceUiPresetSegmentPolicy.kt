package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.ui.AppThemeSelection
import com.android.purebilibili.core.ui.components.AppSegmentOption

internal fun resolveThemeSelectionOptions(
    iosLabel: String,
    material3Label: String,
    miuixLabel: String,
): List<AppSegmentOption<AppThemeSelection>> {
    return listOf(
        AppSegmentOption(AppThemeSelection.IOS, iosLabel),
        AppSegmentOption(AppThemeSelection.MATERIAL3, material3Label),
        AppSegmentOption(AppThemeSelection.MIUIX, miuixLabel),
    )
}
