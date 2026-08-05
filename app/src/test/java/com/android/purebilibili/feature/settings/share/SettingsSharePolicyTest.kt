package com.android.purebilibili.feature.settings.share

import kotlinx.serialization.json.JsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsSharePolicyTest {

    @Test
    fun exportProfile_onlyIncludesAllowlistedKeysInGroupedSections() {
        val profile = buildSettingsShareProfile(
            profileName = "我的设置",
            appVersion = "6.8.2",
            exportedAtIso = "2026-03-07T13:00:00Z",
            rawSettings = mapOf(
                "ui_preset" to JsonPrimitive(1),
                "android_native_variant_v1" to JsonPrimitive(1),
                "app_language_v1" to JsonPrimitive(3),
                "theme_mode_v2" to JsonPrimitive(2),
                "dark_theme_style_v1" to JsonPrimitive(1),
                "auto_play" to JsonPrimitive(true),
                "download_path" to JsonPrimitive("/storage/emulated/0/Download/BiliPai")
            ),
            definitions = listOf(
                SettingsShareEntryDefinition(
                    storageKey = "ui_preset",
                    section = SettingsShareSection.APPEARANCE
                ),
                SettingsShareEntryDefinition(
                    storageKey = "android_native_variant_v1",
                    section = SettingsShareSection.APPEARANCE
                ),
                SettingsShareEntryDefinition(
                    storageKey = "app_language_v1",
                    section = SettingsShareSection.APPEARANCE
                ),
                SettingsShareEntryDefinition(
                    storageKey = "theme_mode_v2",
                    section = SettingsShareSection.APPEARANCE
                ),
                SettingsShareEntryDefinition(
                    storageKey = "dark_theme_style_v1",
                    section = SettingsShareSection.APPEARANCE
                ),
                SettingsShareEntryDefinition(
                    storageKey = "auto_play",
                    section = SettingsShareSection.PLAYBACK
                )
            )
        )

        assertEquals("我的设置", profile.profileName)
        assertEquals(JsonPrimitive(1), profile.sections.appearance["ui_preset"])
        assertEquals(
            JsonPrimitive(1),
            profile.sections.appearance["android_native_variant_v1"]
        )
        assertEquals(JsonPrimitive(3), profile.sections.appearance["app_language_v1"])
        assertEquals(JsonPrimitive(2), profile.sections.appearance["theme_mode_v2"])
        assertEquals(JsonPrimitive(1), profile.sections.appearance["dark_theme_style_v1"])
        assertEquals(JsonPrimitive(true), profile.sections.playback["auto_play"])
        assertFalse(profile.sections.appearance.containsKey("download_path"))
        assertTrue(profile.sections.gesture.isEmpty())
    }

    @Test
    fun importPreview_marksNonAllowlistedKeysAsSkipped() {
        val preview = resolveSettingsShareImportPreview(
            profile = SettingsShareProfile(
                profileName = "社群推荐配置",
                appVersion = "6.8.2",
                exportedAtIso = "2026-03-07T13:00:00Z",
                sections = SettingsShareSections(
                    appearance = mapOf(
                        "theme_mode_v2" to JsonPrimitive(1),
                        "ui_preset" to JsonPrimitive(1),
                        "android_native_variant_v1" to JsonPrimitive(1)
                    ),
                    playback = mapOf("download_path" to JsonPrimitive("/secret/path"))
                )
            ),
            definitions = listOf(
                SettingsShareEntryDefinition(
                    storageKey = "theme_mode_v2",
                    section = SettingsShareSection.APPEARANCE
                ),
                SettingsShareEntryDefinition(
                    storageKey = "ui_preset",
                    section = SettingsShareSection.APPEARANCE
                ),
                SettingsShareEntryDefinition(
                    storageKey = "android_native_variant_v1",
                    section = SettingsShareSection.APPEARANCE
                )
            )
        )

        assertEquals("社群推荐配置", preview.profileName)
        assertEquals(listOf(SettingsShareSection.APPEARANCE), preview.importableSections)
        assertTrue(preview.skippedKeys.contains("download_path"))
        assertFalse(preview.skippedKeys.contains("ui_preset"))
        assertFalse(preview.skippedKeys.contains("android_native_variant_v1"))
    }

    @Test
    fun shareFileName_containsVersionAndUtcTimestamp() {
        assertEquals(
            "bilipai-settings-6.8.2-20260307-130000.json",
            buildSettingsShareFileName(
                appVersion = "6.8.2",
                epochMs = 1_772_888_400_000L
            )
        )
    }

    @Test
    fun exportProfile_canAttachDeviceDebugSnapshot() {
        val debug = buildSettingsShareDeviceDebugInfo(
            androidSdkInt = 35,
            androidRelease = "15",
            securityPatch = "2025-01-01",
            manufacturer = "vivo",
            brand = "vivo",
            model = "V200",
            device = "device",
            product = "product",
            hardware = "qcom",
            displayId = "PQ3A",
            widthPixels = 1216,
            heightPixels = 2640,
            density = 3.0f,
            densityDpi = 480,
            scaledDensity = 3.0f,
            xdpi = 480f,
            ydpi = 480f,
            widthDp = 405f,
            heightDp = 880f,
            smallestWidthDp = 405,
            fontScale = 1.0f,
            uiModeNight = true,
            uiPresetValue = 1,
            uiPresetName = "安卓原生",
            androidNativeVariantValue = 0,
            androidNativeVariantName = "Material 3",
            appVersionName = "0.1.0",
            appVersionCode = 1L,
        )
        val profile = buildSettingsShareProfile(
            profileName = "调试包",
            appVersion = "0.1.0",
            exportedAtIso = "2026-08-05T00:00:00Z",
            rawSettings = mapOf("ui_preset" to JsonPrimitive(1)),
            definitions = listOf(
                SettingsShareEntryDefinition(
                    storageKey = "ui_preset",
                    section = SettingsShareSection.APPEARANCE,
                )
            ),
            deviceDebug = debug,
        )
        assertEquals("vivo", profile.deviceDebug?.manufacturer)
        assertEquals(1216, profile.deviceDebug?.widthPixels)
        assertEquals("安卓原生", profile.deviceDebug?.uiPresetName)
        assertEquals("Material 3", profile.deviceDebug?.androidNativeVariantName)
    }

    @Test
    fun uiPresetAndVariantNames_matchKnownValues() {
        assertEquals("iOS", resolveUiPresetNameFromValue(0))
        assertEquals("安卓原生", resolveUiPresetNameFromValue(1))
        assertEquals("Material 3", resolveAndroidNativeVariantNameFromValue(0))
        assertEquals("Miuix", resolveAndroidNativeVariantNameFromValue(1))
    }
}
