package com.android.purebilibili.core.ui.components

import com.android.purebilibili.core.theme.AppUiStyle

internal fun shouldUseMiuixOutlinedTextField(
    uiStyle: AppUiStyle,
    hasPrefix: Boolean,
    hasSuffix: Boolean,
): Boolean = uiStyle == AppUiStyle.MIUIX && !hasPrefix && !hasSuffix
