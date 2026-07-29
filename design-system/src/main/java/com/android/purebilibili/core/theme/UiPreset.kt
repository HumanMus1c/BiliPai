package com.android.purebilibili.core.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class UiPreset(val value: Int, val label: String) {
    IOS(0, "iOS"),
    MD3(1, "安卓原生");

    companion object {
        fun fromValue(value: Int): UiPreset = entries.find { it.value == value } ?: IOS
    }
}

enum class AndroidNativeVariant(val value: Int, val label: String) {
    MATERIAL3(0, "Material 3"),
    MIUIX(1, "Miuix");

    companion object {
        private const val LEGACY_REMOVED_VARIANT_VALUE = 2

        fun fromValue(value: Int): AndroidNativeVariant {
            if (value == LEGACY_REMOVED_VARIANT_VALUE) return MATERIAL3
            return entries.find { it.value == value } ?: MATERIAL3
        }
    }
}

enum class UiStyle {
    IOS,
    MATERIAL3,
    MIUIX;

    companion object {
        fun fromLegacyValues(
            rawUiPreset: Int?,
            rawAndroidNativeVariant: Int?
        ): UiStyle = resolveUiStyle(
            uiPreset = UiPreset.fromValue(rawUiPreset ?: UiPreset.MD3.value),
            androidNativeVariant = AndroidNativeVariant.fromValue(
                rawAndroidNativeVariant ?: AndroidNativeVariant.MATERIAL3.value
            )
        )
    }

    fun legacyWritePlan(): LegacyUiStyleWritePlan = when (this) {
        IOS -> LegacyUiStyleWritePlan(UiPreset.IOS, null)
        MATERIAL3 -> LegacyUiStyleWritePlan(UiPreset.MD3, AndroidNativeVariant.MATERIAL3)
        MIUIX -> LegacyUiStyleWritePlan(UiPreset.MD3, AndroidNativeVariant.MIUIX)
    }
}

fun resolveUiStyle(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): UiStyle = when (uiPreset) {
    UiPreset.IOS -> UiStyle.IOS
    UiPreset.MD3 -> when (androidNativeVariant) {
        AndroidNativeVariant.MATERIAL3 -> UiStyle.MATERIAL3
        AndroidNativeVariant.MIUIX -> UiStyle.MIUIX
    }
}

data class LegacyUiStyleWritePlan(
    val uiPreset: UiPreset,
    // null 表示保留旧键原值，包括旧键原本不存在的情况。
    val androidNativeVariant: AndroidNativeVariant?
)

data class UiRenderingProfile(
    val useMaterialChrome: Boolean,
    val useMaterialMotion: Boolean,
    val useMaterialIcons: Boolean
)

fun resolveUiRenderingProfile(preset: UiPreset): UiRenderingProfile {
    return when (preset) {
        UiPreset.IOS -> UiRenderingProfile(
            useMaterialChrome = false,
            useMaterialMotion = false,
            useMaterialIcons = false
        )

        UiPreset.MD3 -> UiRenderingProfile(
            useMaterialChrome = true,
            useMaterialMotion = true,
            useMaterialIcons = true
        )
    }
}

val LocalUiPreset = staticCompositionLocalOf { UiPreset.IOS }
val LocalAndroidNativeVariant = staticCompositionLocalOf { AndroidNativeVariant.MATERIAL3 }
val LocalDynamicColorActive = staticCompositionLocalOf { false }
val LocalSettingsLiquidGlassEnabled = staticCompositionLocalOf { false }
