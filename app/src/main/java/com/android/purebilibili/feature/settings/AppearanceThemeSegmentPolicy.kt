package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.ui.components.AppSegmentOption
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec

internal fun resolveThemeModeSegmentOptions(
    followSystemLabel: String = AppThemeMode.FOLLOW_SYSTEM.label,
    lightLabel: String = AppThemeMode.LIGHT.label,
    darkLabel: String = AppThemeMode.DARK.label
): List<AppSegmentOption<AppThemeMode>> {
    return listOf(
        AppSegmentOption(AppThemeMode.FOLLOW_SYSTEM, followSystemLabel),
        AppSegmentOption(AppThemeMode.LIGHT, lightLabel),
        AppSegmentOption(AppThemeMode.DARK, darkLabel)
    )
}

internal fun resolveColorStyleOptions(): List<AppSegmentOption<PaletteStyle>> {
    return (listOf(PaletteStyle.TonalSpot) + PaletteStyle.entries.filterNot { it == PaletteStyle.TonalSpot })
        .map { style ->
            AppSegmentOption(style, style.name)
        }
}

internal fun resolveColorSpecOptions(): List<AppSegmentOption<ColorSpec.SpecVersion>> {
    return listOf(
        ColorSpec.SpecVersion.SPEC_2021,
        ColorSpec.SpecVersion.SPEC_2025
    ).map { spec ->
        AppSegmentOption(spec, spec.name)
    }
}

internal fun resolveMd3ColorSourceOptions(): List<AppSegmentOption<Md3ColorSource>> {
    return listOf(
        AppSegmentOption(Md3ColorSource.FOLLOW_WALLPAPER, Md3ColorSource.FOLLOW_WALLPAPER.label),
        AppSegmentOption(Md3ColorSource.CUSTOM, Md3ColorSource.CUSTOM.label)
    )
}

internal fun resolveDarkThemeStyleSegmentOptions(
    defaultLabel: String = DarkThemeStyle.DEFAULT.label,
    amoledLabel: String = DarkThemeStyle.AMOLED.label
): List<AppSegmentOption<DarkThemeStyle>> {
    return listOf(
        AppSegmentOption(DarkThemeStyle.DEFAULT, defaultLabel),
        AppSegmentOption(DarkThemeStyle.AMOLED, amoledLabel)
    )
}

internal fun resolveAppLanguageSegmentOptions(
    followSystemLabel: String = "跟随系统",
    simplifiedChineseLabel: String = "简体中文",
    traditionalChineseLabel: String = "繁體中文",
    englishLabel: String = "英语"
): List<AppSegmentOption<AppLanguage>> {
    return listOf(
        AppSegmentOption(AppLanguage.FOLLOW_SYSTEM, followSystemLabel),
        AppSegmentOption(AppLanguage.SIMPLIFIED_CHINESE, simplifiedChineseLabel),
        AppSegmentOption(AppLanguage.TRADITIONAL_CHINESE_TAIWAN, traditionalChineseLabel),
        AppSegmentOption(AppLanguage.ENGLISH, englishLabel)
    )
}
