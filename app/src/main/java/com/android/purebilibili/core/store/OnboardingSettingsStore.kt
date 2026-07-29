package com.android.purebilibili.core.store

import android.content.Context
import com.android.purebilibili.core.theme.UiStyle

suspend fun applyOnboardingRecommendedUiStyle(context: Context) {
    SettingsManager.setUiStyle(context, UiStyle.MATERIAL3)
}
