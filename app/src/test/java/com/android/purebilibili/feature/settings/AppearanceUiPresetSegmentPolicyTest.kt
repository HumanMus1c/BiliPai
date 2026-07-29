package com.android.purebilibili.feature.settings

import com.android.purebilibili.core.ui.AppThemeSelection
import kotlin.test.Test
import kotlin.test.assertEquals

class AppearanceUiPresetSegmentPolicyTest {

    @Test
    fun uiStyleSegmentOptions_exposeStableOrder_andUseProvidedLabels() {
        val options = resolveThemeSelectionOptions(
            iosLabel = "iOS",
            material3Label = "Material 3",
            miuixLabel = "Miuix",
        )

        assertEquals(
            listOf(AppThemeSelection.IOS, AppThemeSelection.MATERIAL3, AppThemeSelection.MIUIX),
            options.map { it.value }
        )
        assertEquals(
            listOf("iOS", "Material 3", "Miuix"),
            options.map { it.label }
        )
    }
}
