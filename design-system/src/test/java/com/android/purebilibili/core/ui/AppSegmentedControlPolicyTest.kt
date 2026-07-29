package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSegmentedControlPolicyTest {

    @Test
    fun `material3 exposes material segmented capabilities`() {
        val policy = resolveAppSegmentedControlPolicy(
            uiPreset = UiPreset.MD3,
            androidNativeVariant = AndroidNativeVariant.MATERIAL3,
        )

        assertTrue(policy.usesEmphasizedTitle)
        assertTrue(policy.usesMaterialFallback)
        assertTrue(policy.usesMaterialColorTokens)
        assertFalse(policy.usesNativeTabRow)
    }

    @Test
    fun `miuix exposes native tab row capability`() {
        val policy = resolveAppSegmentedControlPolicy(
            uiPreset = UiPreset.MD3,
            androidNativeVariant = AndroidNativeVariant.MIUIX,
        )

        assertTrue(policy.usesNativeTabRow)
        assertFalse(policy.usesMaterialColorTokens)
    }

    @Test
    fun `ios keeps liquid presentation without material fallback`() {
        val policy = resolveAppSegmentedControlPolicy(
            uiPreset = UiPreset.IOS,
            androidNativeVariant = AndroidNativeVariant.MIUIX,
        )

        assertFalse(policy.usesEmphasizedTitle)
        assertFalse(policy.usesMaterialFallback)
        assertFalse(policy.usesNativeTabRow)
    }
}
