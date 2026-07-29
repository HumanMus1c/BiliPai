package com.android.purebilibili.core.store

import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.theme.UiStyle
import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.feature.settings.share.SettingsShareSection
import kotlin.test.Test
import kotlin.test.assertEquals

class UiPresetSettingsPolicyTest {

    @Test
    fun nullPreferenceValue_defaultsToMd3Preset() {
        assertEquals(
            UiPreset.MD3,
            resolveUiPresetPreferenceValue(null)
        )
    }

    @Test
    fun persistedPreferenceValue_restoresMatchingPreset() {
        assertEquals(
            UiPreset.IOS,
            resolveUiPresetPreferenceValue(UiPreset.IOS.value)
        )
        assertEquals(
            UiPreset.MD3,
            resolveUiPresetPreferenceValue(UiPreset.MD3.value)
        )
    }

    @Test
    fun missingLegacyKeys_defaultToMaterial3Style() {
        assertEquals(
            UiStyle.MATERIAL3,
            UiStyle.fromLegacyValues(
                rawUiPreset = null,
                rawAndroidNativeVariant = null
            )
        )
    }

    @Test
    fun persistedLegacyKeyCombinations_restoreUiStyle() {
        val cases = listOf(
            Triple(UiPreset.IOS, AndroidNativeVariant.MATERIAL3, UiStyle.IOS),
            Triple(UiPreset.IOS, AndroidNativeVariant.MIUIX, UiStyle.IOS),
            Triple(UiPreset.MD3, AndroidNativeVariant.MATERIAL3, UiStyle.MATERIAL3),
            Triple(UiPreset.MD3, AndroidNativeVariant.MIUIX, UiStyle.MIUIX)
        )

        cases.forEach { (preset, variant, expectedStyle) ->
            assertEquals(
                expectedStyle,
                UiStyle.fromLegacyValues(preset.value, variant.value)
            )
        }
    }

    @Test
    fun invalidLegacyValues_followExistingFallbackRules() {
        assertEquals(
            UiStyle.IOS,
            UiStyle.fromLegacyValues(
                rawUiPreset = 99,
                rawAndroidNativeVariant = 99
            )
        )
        assertEquals(
            UiStyle.MATERIAL3,
            UiStyle.fromLegacyValues(
                rawUiPreset = UiPreset.MD3.value,
                rawAndroidNativeVariant = 99
            )
        )
    }

    @Test
    fun settingsShare_keepsBothLegacyUiStyleKeys() {
        val appearanceKeys = SettingsManager.getShareableSettingsEntryDefinitions()
            .filter { it.section == SettingsShareSection.APPEARANCE }
            .mapTo(mutableSetOf()) { it.storageKey }

        assertEquals(
            setOf("ui_preset", "android_native_variant_v1"),
            appearanceKeys.intersect(setOf("ui_preset", "android_native_variant_v1"))
        )
    }
}
