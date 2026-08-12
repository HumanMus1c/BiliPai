package com.android.purebilibili.core.ui

import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppSegmentedControlPolicyTest {

    @Test
    fun `material3 exposes material segmented capabilities`() {
        val policy = resolveAppSegmentedControlPolicy(AppUiStyle.MATERIAL3)

        assertTrue(policy.usesEmphasizedTitle)
        assertTrue(policy.usesMaterialFallback)
        assertTrue(policy.usesMaterialColorTokens)
        assertFalse(policy.usesNativeTabRow)
    }

    @Test
    fun `miuix exposes native tab row capability`() {
        val policy = resolveAppSegmentedControlPolicy(AppUiStyle.MIUIX)

        assertTrue(policy.usesEmphasizedTitle)
        assertTrue(policy.usesMaterialFallback)
        assertTrue(policy.usesNativeTabRow)
        assertFalse(policy.usesMaterialColorTokens)
    }

    @Test
    fun `segmented corners are height-capped below half of tab height`() {
        // Card preferred then capped at 30% of 40dp tab height → max 12dp.
        val material = resolveAppSegmentedControlPolicy(AppUiStyle.MATERIAL3)
        val miuix = resolveAppSegmentedControlPolicy(AppUiStyle.MIUIX)
        assertEquals(40.dp, material.nativeTabRowHeight)
        assertEquals(40.dp, miuix.nativeTabRowHeight)
        // MD3 card 10.8 < 12 cap; Miuix card 13.8 → capped to 12.
        assertEquals(10.8.dp, material.pillCornerRadius)
        assertEquals(12.dp, miuix.pillCornerRadius)
        assertTrue(material.pillCornerRadius < material.nativeTabRowHeight / 2)
        assertTrue(miuix.pillCornerRadius < miuix.nativeTabRowHeight / 2)
    }

    @Test
    fun `height cap prevents full capsule on 48dp bars`() {
        val preferred = 28.dp
        val capped = resolveHeightCappedCornerRadius(48.dp, preferred)
        assertEquals(14.4.dp, capped)
        assertTrue(capped < 24.dp) // half of 48
    }
}
