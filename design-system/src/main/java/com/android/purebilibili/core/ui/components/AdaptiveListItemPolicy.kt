package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset

enum class AppClickableItemRenderer {
    CUPERTINO,
    MD3_BASIC,
    MIUIX_ARROW,
    MIUIX_BASIC,
}

fun resolveAppClickableItemRenderer(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant,
    onClick: (() -> Unit)?,
    showChevron: Boolean,
    centered: Boolean,
): AppClickableItemRenderer = when {
    uiPreset == UiPreset.IOS || centered -> AppClickableItemRenderer.CUPERTINO
    uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX -> {
        if (onClick != null && showChevron) {
            AppClickableItemRenderer.MIUIX_ARROW
        } else {
            AppClickableItemRenderer.MIUIX_BASIC
        }
    }
    uiPreset == UiPreset.MD3 -> AppClickableItemRenderer.MD3_BASIC
    else -> AppClickableItemRenderer.CUPERTINO
}

fun shouldRouteIosClickableItemToMiuixArrowPreference(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant,
    onClick: (() -> Unit)?,
    showChevron: Boolean,
    centered: Boolean,
): Boolean = resolveAppClickableItemRenderer(
    uiPreset = uiPreset,
    androidNativeVariant = androidNativeVariant,
    onClick = onClick,
    showChevron = showChevron,
    centered = centered
) == AppClickableItemRenderer.MIUIX_ARROW

fun shouldRouteIosSwitchItemToMiuixSwitchPreference(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant,
): Boolean = uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX

fun shouldRouteIosSliderPreferenceToMiuixSliderPreference(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant,
): Boolean = uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX
