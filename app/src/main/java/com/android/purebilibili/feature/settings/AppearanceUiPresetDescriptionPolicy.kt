package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.ui.AppThemeSelection

data class AppearanceUiPresetDescription(
    val title: String,
    val summary: String
)

internal fun resolveAppearanceUiPresetDescription(
    selection: AppThemeSelection,
    iosTitle: String,
    iosSummary: String,
    materialTitle: String,
    materialSummary: String,
    miuixTitle: String,
    miuixSummary: String
): AppearanceUiPresetDescription {
    return when (selection) {
        AppThemeSelection.IOS -> AppearanceUiPresetDescription(
            title = iosTitle,
            summary = iosSummary
        )

        AppThemeSelection.MATERIAL3 -> AppearanceUiPresetDescription(
            title = materialTitle,
            summary = materialSummary
        )

        AppThemeSelection.MIUIX -> AppearanceUiPresetDescription(
            title = miuixTitle,
            summary = miuixSummary
        )
    }
}
