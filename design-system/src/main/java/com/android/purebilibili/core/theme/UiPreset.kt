package com.android.purebilibili.core.theme

import androidx.compose.runtime.staticCompositionLocalOf

enum class UiPreset(val value: Int, val label: String) {
    IOS(0, "iOS"),
    MD3(1, "安卓原生");

    companion object {
        fun fromValue(value: Int): UiPreset = fromValueOrNull(value) ?: MD3

        fun fromValueOrNull(value: Int): UiPreset? = entries.find { it.value == value }
    }
}

enum class AndroidNativeVariant(val value: Int, val label: String) {
    MATERIAL3(0, "Material 3"),
    MIUIX(1, "Miuix");

    companion object {
        private const val LEGACY_REMOVED_VARIANT_VALUE = 2

        fun fromValue(value: Int): AndroidNativeVariant = fromValueOrNull(value) ?: MIUIX

        /** 历史已移除的变体值 2 仍保留其原有映射为 MATERIAL3。 */
        fun fromValueOrNull(value: Int): AndroidNativeVariant? {
            if (value == LEGACY_REMOVED_VARIANT_VALUE) return MATERIAL3
            return entries.find { it.value == value }
        }
    }
}

/**
 * 运行时主题选择（两值模型）：仅保留 MIUIX / MATERIAL3。
 * 历史 iOS 值在迁移边界（fromLegacyValues / resolveUiStyle）解析为默认主题 MIUIX，
 * 不再进入运行时。
 */
enum class AppUiStyle {
    MATERIAL3,
    MIUIX;

    companion object {
        /**
         * 按迁移表解析历史旧键：iOS、缺失、非法值一律得到默认主题 MIUIX；
         * 仅 MD3 + 合法变体保留有效选择。
         */
        fun fromLegacyValues(
            rawUiPreset: Int?,
            rawAndroidNativeVariant: Int?
        ): AppUiStyle {
            val uiPreset = rawUiPreset?.let(UiPreset::fromValueOrNull)
            val androidNativeVariant = rawAndroidNativeVariant?.let(
                AndroidNativeVariant::fromValueOrNull
            )
            return when {
                // 缺失或非法（preset 或 variant 任一）→ 默认主题 MIUIX
                uiPreset == null || androidNativeVariant == null -> AppUiStyle.MIUIX
                else -> resolveUiStyle(uiPreset, androidNativeVariant)
            }
        }
    }
}

fun resolveUiStyle(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): AppUiStyle = when (uiPreset) {
    // 单向迁移：历史 iOS 值不再产生 iOS 运行时选择，统一迁移为默认主题 MIUIX。
    UiPreset.IOS -> AppUiStyle.MIUIX
    UiPreset.MD3 -> when (androidNativeVariant) {
        AndroidNativeVariant.MATERIAL3 -> AppUiStyle.MATERIAL3
        AndroidNativeVariant.MIUIX -> AppUiStyle.MIUIX
    }
}

/** 两值运行时主题 Local。 */
val LocalAppUiStyle = staticCompositionLocalOf { AppUiStyle.MIUIX }
val LocalDynamicColorActive = staticCompositionLocalOf { false }
val LocalSettingsLiquidGlassEnabled = staticCompositionLocalOf { false }
