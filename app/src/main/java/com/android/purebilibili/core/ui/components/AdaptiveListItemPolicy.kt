package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset

enum class IosClickableItemRenderer {
    IOS_LEGACY,
    MD3_BASIC,
    MIUIX_ARROW,
    MIUIX_BASIC,
}

fun resolveIosClickableItemRenderer(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant,
    onClick: (() -> Unit)?,
    showChevron: Boolean,
    centered: Boolean
): IosClickableItemRenderer = when {
    uiPreset == UiPreset.IOS || centered -> IosClickableItemRenderer.IOS_LEGACY
    uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX -> {
        if (onClick != null && showChevron) {
            IosClickableItemRenderer.MIUIX_ARROW
        } else {
            IosClickableItemRenderer.MIUIX_BASIC
        }
    }
    uiPreset == UiPreset.MD3 -> IosClickableItemRenderer.MD3_BASIC
    else -> IosClickableItemRenderer.IOS_LEGACY
}

fun shouldRouteIosClickableItemToMiuixArrowPreference(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant,
    onClick: (() -> Unit)?,
    showChevron: Boolean,
    centered: Boolean
): Boolean = resolveIosClickableItemRenderer(
    uiPreset = uiPreset,
    androidNativeVariant = androidNativeVariant,
    onClick = onClick,
    showChevron = showChevron,
    centered = centered
) == IosClickableItemRenderer.MIUIX_ARROW

fun shouldRouteIosSwitchItemToMiuixSwitchPreference(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): Boolean = uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX

fun shouldRouteIosSliderPreferenceToMiuixSliderPreference(
    uiPreset: UiPreset,
    androidNativeVariant: AndroidNativeVariant
): Boolean = uiPreset == UiPreset.MD3 && androidNativeVariant == AndroidNativeVariant.MIUIX