package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppDialogComponentsPolicyTest {

    @Test
    fun `md3 dialog actions stay content sized inside material alert dialogs`() {
        val policy = resolveDialogActionLayoutPolicy(UiPreset.MD3)

        assertFalse(policy.expandToContainer)
    }

    @Test
    fun `ios preset dialog actions expand to keep full width tap targets`() {
        val policy = resolveDialogActionLayoutPolicy(UiPreset.IOS)

        assertTrue(policy.expandToContainer)
    }

    @Test
    fun miuixAlertDialogUsesWindowDialogWhenOutsideScaffoldHost() {
        assertEquals(
            AppAlertDialogRenderer.LOCAL_DIALOG,
            resolveAppAlertDialogRenderer(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
    }

    @Test
    fun md3MaterialKeepsMaterialAlertDialogRenderer() {
        assertEquals(
            AppAlertDialogRenderer.MATERIAL_ALERT,
            resolveAppAlertDialogRenderer(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
    }
}
