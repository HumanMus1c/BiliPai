package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.ui.AppThemeSelection
import kotlin.test.Test
import kotlin.test.assertEquals

class AppearanceAndroidNativeVariantSegmentPolicyTest {

    @Test
    fun uiStyleSegmentOptions_keepAndroidStylesInStableOrder() {
        val options = resolveThemeSelectionOptions(
            iosLabel = "iOS",
            material3Label = "Material 3",
            miuixLabel = "Miuix",
        )

        assertEquals(
            listOf(AppThemeSelection.MATERIAL3, AppThemeSelection.MIUIX),
            options.drop(1).map { it.value }
        )
        assertEquals(
            listOf("Material 3", "Miuix"),
            options.drop(1).map { it.label }
        )
    }
}
