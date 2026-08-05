package com.android.purebilibili.feature.settings.share

import kotlinx.serialization.json.JsonElement
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SETTINGS_SHARE_FILE_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss", Locale.US)

internal fun flattenSettingsShareSections(
    sections: SettingsShareSections
): Map<String, JsonElement> {
    return buildMap {
        putAll(sections.appearance)
        putAll(sections.playback)
        putAll(sections.gesture)
        putAll(sections.danmaku)
        putAll(sections.navigation)
    }
}

fun buildSettingsShareProfile(
    profileName: String,
    appVersion: String,
    exportedAtIso: String,
    rawSettings: Map<String, JsonElement>,
    definitions: List<SettingsShareEntryDefinition>,
    deviceDebug: SettingsShareDeviceDebugInfo? = null,
): SettingsShareProfile {
    val byKey = definitions.associateBy { it.storageKey }
    val appearance = linkedMapOf<String, JsonElement>()
    val playback = linkedMapOf<String, JsonElement>()
    val gesture = linkedMapOf<String, JsonElement>()
    val danmaku = linkedMapOf<String, JsonElement>()
    val navigation = linkedMapOf<String, JsonElement>()

    rawSettings.forEach { (key, value) ->
        when (byKey[key]?.section) {
            SettingsShareSection.APPEARANCE -> appearance[key] = value
            SettingsShareSection.PLAYBACK -> playback[key] = value
            SettingsShareSection.GESTURE -> gesture[key] = value
            SettingsShareSection.DANMAKU -> danmaku[key] = value
            SettingsShareSection.NAVIGATION -> navigation[key] = value
            null -> Unit
        }
    }

    return SettingsShareProfile(
        appVersion = appVersion,
        exportedAtIso = exportedAtIso,
        profileName = profileName,
        sections = SettingsShareSections(
            appearance = appearance,
            playback = playback,
            gesture = gesture,
            danmaku = danmaku,
            navigation = navigation
        ),
        deviceDebug = deviceDebug,
    )
}

/**
 * 从 raw settings 与显示指标组装设备调试块。
 * [uiPresetValue]/[androidNativeVariantValue] 优先用快照中的值，便于和导出外观设置对齐。
 */
fun buildSettingsShareDeviceDebugInfo(
    androidSdkInt: Int,
    androidRelease: String,
    securityPatch: String,
    manufacturer: String,
    brand: String,
    model: String,
    device: String,
    product: String,
    hardware: String,
    displayId: String,
    widthPixels: Int,
    heightPixels: Int,
    density: Float,
    densityDpi: Int,
    scaledDensity: Float,
    xdpi: Float,
    ydpi: Float,
    widthDp: Float,
    heightDp: Float,
    smallestWidthDp: Int,
    fontScale: Float,
    uiModeNight: Boolean,
    uiPresetValue: Int,
    uiPresetName: String,
    androidNativeVariantValue: Int,
    androidNativeVariantName: String,
    appVersionName: String,
    appVersionCode: Long,
): SettingsShareDeviceDebugInfo {
    return SettingsShareDeviceDebugInfo(
        androidSdkInt = androidSdkInt,
        androidRelease = androidRelease,
        securityPatch = securityPatch,
        manufacturer = manufacturer,
        brand = brand,
        model = model,
        device = device,
        product = product,
        hardware = hardware,
        displayId = displayId,
        widthPixels = widthPixels,
        heightPixels = heightPixels,
        density = density,
        densityDpi = densityDpi,
        scaledDensity = scaledDensity,
        xdpi = xdpi,
        ydpi = ydpi,
        widthDp = widthDp,
        heightDp = heightDp,
        smallestWidthDp = smallestWidthDp,
        fontScale = fontScale,
        uiModeNight = uiModeNight,
        uiPresetValue = uiPresetValue,
        uiPresetName = uiPresetName,
        androidNativeVariantValue = androidNativeVariantValue,
        androidNativeVariantName = androidNativeVariantName,
        appVersionName = appVersionName,
        appVersionCode = appVersionCode,
    )
}

internal fun resolveUiPresetNameFromValue(value: Int): String {
    return when (value) {
        0 -> "iOS"
        1 -> "安卓原生"
        else -> "unknown($value)"
    }
}

internal fun resolveAndroidNativeVariantNameFromValue(value: Int): String {
    return when (value) {
        0 -> "Material 3"
        1 -> "Miuix"
        2 -> "Material 3" // legacy removed
        else -> "unknown($value)"
    }
}

internal fun jsonElementAsInt(element: JsonElement?): Int? {
    val primitive = element as? kotlinx.serialization.json.JsonPrimitive ?: return null
    return primitive.content.toIntOrNull()
}

fun resolveSettingsShareImportPreview(
    profile: SettingsShareProfile,
    definitions: List<SettingsShareEntryDefinition>
): SettingsShareImportPreview {
    val importableKeys = definitions.mapTo(linkedSetOf()) { it.storageKey }
    val allKeys = flattenSettingsShareSections(profile.sections).keys
    val importableSections = definitions
        .filter { definition -> allKeys.contains(definition.storageKey) }
        .map { it.section }
        .distinct()
    val skippedKeys = allKeys
        .filterNot { importableKeys.contains(it) }
        .sorted()

    return SettingsShareImportPreview(
        profileName = profile.profileName,
        importableSections = importableSections,
        skippedKeys = skippedKeys
    )
}

fun buildSettingsShareFileName(
    appVersion: String,
    epochMs: Long
): String {
    val timestamp = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMs), ZoneOffset.UTC)
    return "bilipai-settings-$appVersion-${timestamp.format(SETTINGS_SHARE_FILE_TIME_FORMATTER)}.json"
}
